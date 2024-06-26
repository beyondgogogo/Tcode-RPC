package tcode.rpc.remote.transport.netty.client;


import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import tcode.rpc.enums.CompressTypeEnum;
import tcode.rpc.enums.SerializationTypeEnum;
import tcode.rpc.factory.SingletonFactory;
import tcode.rpc.registry.ServiceDiscovery;
import tcode.rpc.registry.zk.ZkServiceDiscoveryImpl;
import tcode.rpc.remote.constants.RpcConstants;
import tcode.rpc.remote.dto.RpcMessage;
import tcode.rpc.remote.dto.RpcRequest;
import tcode.rpc.remote.dto.RpcResponse;
import tcode.rpc.remote.transport.RpcRequestTransport;
import tcode.rpc.remote.transport.netty.codec.RpcMessageDecoder;
import tcode.rpc.remote.transport.netty.codec.RpcMessageEncoder;

import java.net.InetSocketAddress;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author 田成强
 * 我们需要完成几件事情呢，启动Netty客户端*/
@Slf4j
public class NettyRpcClient implements RpcRequestTransport {
    //Netty启动器
    private final Bootstrap bootstrap;

    //事件循环组
    private final EventLoopGroup eventLoopGroup;

    //连接缓存器
    private final ChannelProvider channelProvider;

    //服务发现
    private final ServiceDiscovery serviceDiscovery;

    //未完成请求存储
    private final UnprocessedRequests unprocessedRequests;
    /**
     * 构造函数完成对Netty客户端的基本设置
     * */
    public NettyRpcClient(){
        eventLoopGroup=new NioEventLoopGroup();
        bootstrap=new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                //.handler(new LoggHandler(LogLevel.INFO))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,5000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline p = socketChannel.pipeline();
                        p.addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS));
                        p.addLast(new RpcMessageEncoder());
                        p.addLast(new RpcMessageDecoder());
                        p.addLast(new NettyRpcClientHandler());
                    }
                });
        channelProvider= SingletonFactory.getInstance(ChannelProvider.class);
        //TODO:服务发现有多个实现，可通过Spi来进行替换
        serviceDiscovery=SingletonFactory.getInstance(ServiceDiscovery.class);
        unprocessedRequests=SingletonFactory.getInstance(UnprocessedRequests.class);
    }
    /**
     * 此方法用于连接Netty服务端，并返回可用通道
     * @SneakyThrows 是 Lombok 提供的一个注解，
     * 用于在编译时生成代码，以在运行时抛出【受检异常】而无需显式地在方法签名中声明这些异常
     * */
    @SneakyThrows
    public Channel doConnect(InetSocketAddress inetSocketAddress){
        CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
        bootstrap.connect(inetSocketAddress).addListener((ChannelFutureListener) future -> {
            if(future.isSuccess()){
                log.info("The client has connected [{}] successful",inetSocketAddress.toString());
                completableFuture.complete(future.channel());
            }else{
                throw new IllegalStateException();
            }
        });
        return completableFuture.get();
    }

    /**
     * 此方法用于发送消息,将经过编码器处理的符合自定义协议的字节流通过Netty进行网络传输
     * 先通过服务发现找到IP[负载均衡],再利用doConnect进行连接返回channel，通过channel发送数据
     * TODO:容错机制可以在这里做吧
     * */
    public Object sendRpcRequest(RpcRequest rpcRequest){
        // build return value
        CompletableFuture<RpcResponse<Object>> resultFuture = new CompletableFuture<>();
        //获取服务地址
        InetSocketAddress inetSocketAddress=serviceDiscovery.lookupService(rpcRequest);
        //获取连接通道
        Channel channel=getChannel(inetSocketAddress);
        if(channel.isActive()){
            //先将请求放入未完成队列
            unprocessedRequests.put(rpcRequest.getRequestId(),resultFuture);
            //利用Stream流式构建RpcMessage
            RpcMessage rpcMessage=RpcMessage.builder().data(rpcRequest)
                    .codec(SerializationTypeEnum.KYRO.getCode())//序列化方式
                    .compress(CompressTypeEnum.GZIP.getCode())//压缩方式
                    .messageType(RpcConstants.REQUEST_TYPE).build();//消息类型
            channel.writeAndFlush(rpcMessage).addListener((ChannelFutureListener) future->{
                if(future.isSuccess()){
                    log.info("client send message:[{}]",rpcMessage);
                }else{
                    //请求失败，关闭通道:报异常
                    future.channel().close();
                    resultFuture.completeExceptionally(future.cause());
                    log.error("Send failed:",future.cause());
                }
            });
        }
        return resultFuture;
    }

    /**
     * 获取可用通道,先从缓存中获取，没有再重新通过doConnect建立连接
     * */
    public  Channel getChannel(InetSocketAddress inetSocketAddress){
        Channel channel = channelProvider.get(inetSocketAddress);
        if (channel == null) {
            channel = doConnect(inetSocketAddress);
            channelProvider.set(inetSocketAddress, channel);
        }
        return channel;
    }

    /**
     * 优雅 的关闭连接
     * */
    public void close(){
        eventLoopGroup.shutdownGracefully();
    }
}
