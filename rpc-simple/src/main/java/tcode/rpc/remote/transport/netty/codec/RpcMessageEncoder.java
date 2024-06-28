package tcode.rpc.remote.transport.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;
import tcode.rpc.compress.Compress;
import tcode.rpc.compress.gzip.GzipCompress;
import tcode.rpc.enums.CompressTypeEnum;
import tcode.rpc.enums.SerializationTypeEnum;
import tcode.rpc.remote.constants.RpcConstants;
import tcode.rpc.remote.dto.RpcMessage;
import tcode.rpc.serialize.Serializer;
import tcode.rpc.serialize.kyro.KyroSerializer;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 田成强
 * @time 2024.6.24
 * 自定义编码器将 RpcMessage 对象[包含信息的传输实体]编码为《符合协议格式》的《字节流》，
 * 确保数据在网络传输过程中保持《一致性》和《可解码性》。
 * */
/**
 * <p>
 * custom protocol decoder
 * <p>
 * <pre>
 *   0     1     2     3     4        5     6     7     8         9          10      11     12  13  14   15 16
 *   +-----+-----+-----+-----+--------+----+----+----+------+-----------+-------+----- --+-----+-----+-------+
 *   |   magic   code        |version | full length         | messageType| codec|compress|    RequestId       |
 *   +-----------------------+--------+---------------------+-----------+-----------+-----------+------------+
 *   |                                                                                                       |
 *   |                                         body                                                          |
 *   |                                                                                                       |
 *   |                                        ... ...                                                        |
 *   +-------------------------------------------------------------------------------------------------------+
 * 4B  magic code（魔法数）   1B version（版本）   4B full length（消息长度）    1B messageType（消息类型）
 * 1B compress（压缩类型） 1B codec（序列化类型）    4B  requestId（请求的Id）
 * body（object类型数据）
 * </pre>
 * @see <a href="https://zhuanlan.zhihu.com/p/95621344">LengthFieldBasedFrameDecoder解码器</a>
 */
@Slf4j
public class RpcMessageEncoder extends MessageToByteEncoder<RpcMessage> {
    //这个作用是？:用于生成自增的请求 ID，保证每个请求都有唯一的标识符
    private static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger(0);
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RpcMessage rpcMessage, ByteBuf out) throws Exception {
        try{
            out.writeBytes(RpcConstants.MAGIC_NUMBER);
            out.writeByte(RpcConstants.VERSION);
            //留一段距离4B来写入消息的长度
            out.writerIndex(out.writerIndex()+4);
            byte messageType=rpcMessage.getMessageType();
            out.writeByte(messageType);
            out.writeByte(rpcMessage.getCodec());
            out.writeByte(CompressTypeEnum.GZIP.getCode());
            out.writeInt(ATOMIC_INTEGER.getAndIncrement());
            //build full length
            byte[] bodyBytes = null;
            int fullLength=RpcConstants.HEAD_LENGTH;
            /**if messageType is not heartbeat message,fullLength = head length + body length
            *通过计算并写入完整消息的长度，确保消息在网络传输过程中能够被正确解析。
            *消息头中包含的长度字段对于消息的分片与重组、数据完整性以及解析效率都至关重要
            */
            if (messageType != RpcConstants.HEARTBEAT_REQUEST_TYPE
                    && messageType != RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
                // serialize the object
                String codecName = SerializationTypeEnum.getName(rpcMessage.getCodec());
                log.info("codec name: [{}] ", codecName);
                /*TODO:后续更换为SPI替换方式
                Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class)
                        .getExtension(codecName);
                */
                Serializer serializer =new KyroSerializer();
                bodyBytes = serializer.serialize(rpcMessage.getData());
                // compress the bytes
                String compressName = CompressTypeEnum.getName(rpcMessage.getCompress());
                /*同理
                Compress compress = ExtensionLoader.getExtensionLoader(Compress.class)
                        .getExtension(compressName);
                 */
                Compress compress =new GzipCompress();
                bodyBytes = compress.compress(bodyBytes);
                fullLength += bodyBytes.length;
            }

            if (bodyBytes != null) {
                out.writeBytes(bodyBytes);
            }
            int writeIndex = out.writerIndex();
            out.writerIndex(writeIndex - fullLength + RpcConstants.MAGIC_NUMBER.length + 1);
            out.writeInt(fullLength);
            out.writerIndex(writeIndex);
        }catch (Exception e){
            log.error("Encode request error!", e);
        }
    }




















}
