package tcode.rpc.remote.transport.netty.client;

import io.netty.channel.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import tcode.rpc.enums.CompressTypeEnum;
import tcode.rpc.enums.SerializationTypeEnum;
import tcode.rpc.factory.SingletonFactory;
import tcode.rpc.remote.constants.RpcConstants;
import tcode.rpc.remote.dto.RpcMessage;
import tcode.rpc.remote.dto.RpcResponse;

import java.lang.ref.Reference;
import java.net.InetSocketAddress;

/**
 * @author 田成强
 * Customize the client ChannelHandler to process the data sent by the server
 * 继承 ChannelInboundHandlerAdapter 的主要原因是简化 ChannelInboundHandler 接口的实现，
 * 只重写需要的方法，从而提高代码的可读性和可维护性
 **/
@Slf4j
public class NettyRpcClientHandler extends ChannelInboundHandlerAdapter {
    private final UnprocessedRequests unprocessedRequests;
    private final NettyRpcClient nettyRpcClient;

    public NettyRpcClientHandler() {
        //这里最好也使用单例工厂获取
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
        this.nettyRpcClient = SingletonFactory.getInstance(NettyRpcClient.class);
    }

    /**
     * 处理来自服务器发送的消息
     * */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg){
        try{
            log.info("client receive msg:[{}]",msg);
            if(msg instanceof RpcMessage){
                RpcMessage tmp=(RpcMessage) msg;
                byte messageType=tmp.getMessageType();
                if(messageType==RpcConstants.RESPONSE_TYPE){
                    //强制类型转换，感觉引用数据类型一般都有强制类型转换
                    RpcResponse<Object> rpcResponse=(RpcResponse<Object>) tmp.getData();
                }else if(messageType==RpcConstants.HEARTBEAT_RESPONSE_TYPE){
                    log.info("heart [{}]",tmp.getData());
                }
            }
        }finally {
            /**
             * 这一行代码在 Netty 中的作用是释放消息对象的引用计数，以防止内存泄漏。
             * */
            ReferenceCountUtil.release(msg);
        }
    }

    /**
     * 用户事件触发器：空闲检测
     * */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        //检测事件
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            //如果是写空闲，则向服务器发送心跳
            if (state == IdleState.WRITER_IDLE) {
                log.info("write idle happen [{}]", ctx.channel().remoteAddress());
                Channel channel = nettyRpcClient.getChannel((InetSocketAddress) ctx.channel().remoteAddress());
                RpcMessage rpcMessage = new RpcMessage();
                rpcMessage.setCodec(SerializationTypeEnum.PROTOSTUFF.getCode());
                rpcMessage.setCompress(CompressTypeEnum.GZIP.getCode());
                rpcMessage.setMessageType(RpcConstants.HEARTBEAT_REQUEST_TYPE);
                rpcMessage.setData(RpcConstants.PING);
                //添加监听器，如果请求发送失败，则关闭相应通道
                channel.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    /**
     * Called when an exception occurs in processing a client message
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("NettyRpcClientHandler catch exception：", cause);
        cause.printStackTrace();
        ctx.close();
    }

}
