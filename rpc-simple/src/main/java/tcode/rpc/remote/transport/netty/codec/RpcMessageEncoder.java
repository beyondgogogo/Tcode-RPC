package tcode.rpc.remote.transport.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import tcode.rpc.remote.dto.RpcMessage;

/**
 * @author 田成强
 * @time 2024.6.24
 * 自定义编码器将 RpcMessage 对象[包含信息的传输实体]编码为《符合协议格式》的《字节流》，
 * 确保数据在网络传输过程中保持《一致性》和《可解码性》。
 * */
public class RpcMessageEncoder extends MessageToByteEncoder<RpcMessage> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RpcMessage rpcMessage, ByteBuf byteBuf) throws Exception {

    }
}
