package tcode.rpc.remote.transport.netty.server;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import tcode.rpc.enums.CompressTypeEnum;
import tcode.rpc.enums.RpcResponseCodeEnum;
import tcode.rpc.enums.SerializationTypeEnum;
import tcode.rpc.factory.SingletonFactory;
import tcode.rpc.remote.constants.RpcConstants;
import tcode.rpc.remote.dto.RpcMessage;
import tcode.rpc.remote.dto.RpcRequest;
import tcode.rpc.remote.dto.RpcResponse;
import tcode.rpc.remote.invoke.RpcServiceInvoke;

@Slf4j
public class NettyRpcServerHandler extends ChannelInboundHandlerAdapter {

    private final RpcServiceInvoke rpcServiceInvoke;
    public NettyRpcServerHandler(){
        rpcServiceInvoke= SingletonFactory.getInstance(RpcServiceInvoke.class);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg){
        try{
            if(msg instanceof RpcMessage){
                log.info("server receive msg:[{}]",msg);
                byte messageType=((RpcMessage) msg).getMessageType();
                RpcMessage rpcMessage=new RpcMessage();
                rpcMessage.setCodec(SerializationTypeEnum.KYRO.getCode());
                rpcMessage.setCompress(CompressTypeEnum.GZIP.getCode());
                //如果是心跳请求，则返回心跳响应
                if(messageType== RpcConstants.HEARTBEAT_REQUEST_TYPE){
                    rpcMessage.setMessageType(RpcConstants.HEARTBEAT_RESPONSE_TYPE);
                    rpcMessage.setData(RpcConstants.PONG);
                }else{
                    //如果不是心跳消息，则需要根据参数进行服务方法调用然后返回Data
                    RpcRequest rpcRequest=(RpcRequest) ((RpcMessage) msg).getData();
                    //利用反射进行方法调用
                    Object result =rpcServiceInvoke.handle(rpcRequest);
                    log.info(String.format("server get result: %s"),result.toString());
                    rpcMessage.setMessageType(RpcConstants.REQUEST_TYPE);
                    if(ctx.channel().isActive() && ctx.channel().isWritable()){
                        RpcResponse<Object> rpcResponse = RpcResponse.success(result, rpcRequest.getRequestId());
                        rpcMessage.setData(rpcResponse);
                    }else{
                        RpcResponse<Object> rpcResponse = RpcResponse.fail(RpcResponseCodeEnum.FAIL);
                        rpcMessage.setData(rpcResponse);
                        log.error("not writable now, message dropped");
                    }
                }
                ctx.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        }finally {
            //Ensure that ByteBuf is released, otherwise there may be memory leaks
            ReferenceCountUtil.release(msg);
        }
    }
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            //注意这边的一个空闲处理逻辑,客户端是写空闲[不再向服务端发送消息]
            //服务端则是读空闲[没有接到客服端发送的相关请求或者心跳]，直接关必通道
            if (state == IdleState.READER_IDLE) {
                log.info("idle check happen, so close the connection");
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("server catch exception");
        cause.printStackTrace();
        ctx.close();
    }
}
