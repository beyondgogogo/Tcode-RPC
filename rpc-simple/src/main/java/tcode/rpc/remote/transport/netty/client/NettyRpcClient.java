package tcode.rpc.remote.transport.netty.client;


import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import tcode.rpc.registry.ServiceDiscovery;
import tcode.rpc.registry.zk.ZkServiceDiscoveryImpl;
import tcode.rpc.remote.dto.RpcRequest;
import tcode.rpc.remote.dto.RpcResponse;
import tcode.rpc.remote.transport.RpcRequestTransport;

import java.net.InetSocketAddress;

import java.util.concurrent.CompletableFuture;

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
                        //TODO:待施工,添加相应处理器
                        //p.addLast(new IdleStateHandler());
                    }
                });
        //TODO:服务发现的初始化
        channelProvider=new ChannelProvider();
        serviceDiscovery=new ZkServiceDiscoveryImpl();
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
