package tcode.rpc.tolerance.toleranceImpl;

import lombok.SneakyThrows;
import tcode.rpc.enums.RpcResponseCodeEnum;
import tcode.rpc.remote.dto.RpcRequest;
import tcode.rpc.remote.dto.RpcResponse;
import tcode.rpc.remote.transport.netty.client.NettyRpcClient;
import tcode.rpc.tolerance.FaultTolerance;

import java.util.concurrent.CompletableFuture;
/**
 * @author 田成强
 * 容错层
 * 基本逻辑：
 * */
public class FailfastFaultTolerance implements FaultTolerance {
    private  final NettyRpcClient nettyRpcClient;
    public  FailfastFaultTolerance(NettyRpcClient nettyRpcClient){
        this.nettyRpcClient=nettyRpcClient;
    }

    @SneakyThrows
    @Override
    public RpcResponse<Object> invoke(RpcRequest rpcRequest) {
        RpcResponse<Object> response = null;
        // 假设有一个方法 sendRequest 发送请求并获取响应
        CompletableFuture<RpcResponse<Object>> resultFuture = (CompletableFuture<RpcResponse<Object>>) nettyRpcClient.sendRpcRequest(rpcRequest);
        response = resultFuture.get(); // 等待并获取结果
        // 3. 返回响应结果
        if (response != null && response.getCode().equals(RpcResponseCodeEnum.SUCCESS)) {
            return response;
        }
        throw new RuntimeException("Failfast failed on the first attempt");
    }


}
