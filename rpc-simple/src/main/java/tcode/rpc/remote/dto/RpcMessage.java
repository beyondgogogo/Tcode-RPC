package tcode.rpc.remote.dto;

import lombok.*;

/**
 * @author 田成强
 * 为了统一请求/响应消息与心跳请求&响应消息，再拿个数据实体来进行封装
 * */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RpcMessage {

    /**
     * 1.rpc message type
     */
    private byte messageType;
    /**
     * serialization type
     */
    private byte codec;
    /**
     * compress type
     */
    private byte compress;
    /**
     * request id
     */
    private int requestId;
    /**
     * 2.request data
     */
    private Object data;

}