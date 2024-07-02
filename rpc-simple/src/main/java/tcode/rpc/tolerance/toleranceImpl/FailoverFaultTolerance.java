package tcode.rpc.tolerance.toleranceImpl;

import tcode.rpc.enums.RpcResponseCodeEnum;;
import tcode.rpc.registry.zk.util.CuratorUtils;
import tcode.rpc.remote.dto.RpcRequest;
import tcode.rpc.remote.dto.RpcResponse;
import tcode.rpc.remote.transport.netty.client.NettyRpcClient;
import tcode.rpc.tolerance.FaultTolerance;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
/**
 * @author 田成强
 * 容错层
 * 基本逻辑：
 * */
public class FailoverFaultTolerance implements FaultTolerance {
    private final NettyRpcClient nettyRpcClient;
    private final int maxRetries;

    public FailoverFaultTolerance(NettyRpcClient nettyRpcClient, int maxRetries) {
        this.nettyRpcClient=nettyRpcClient;
        this.maxRetries = maxRetries;
    }

    @Override
    public RpcResponse<Object> invoke(RpcRequest rpcRequest) {
        int attempts = 0;
        RpcResponse<Object> response = null;
        Set<InetSocketAddress> triedAddresses = new HashSet<>();

        while (attempts < maxRetries) {
            try {
                // 发现服务并选择一个地址
                InetSocketAddress inetSocketAddress = getServiceAddress(rpcRequest, triedAddresses);
                if (inetSocketAddress == null) {
                    throw new RuntimeException("No available service found after " + attempts + " attempts");
                }

                CompletableFuture<RpcResponse<Object>> resultFuture = (CompletableFuture<RpcResponse<Object>>) nettyRpcClient.sendRpcRequest(rpcRequest);
                response = resultFuture.get(); // 等待并获取结果
                if (response != null && response.getCode().equals(RpcResponseCodeEnum.SUCCESS)) {
                    return response;
                }

                // 将当前地址加入不可用地址列表
                triedAddresses.add(inetSocketAddress);
            } catch (Exception e) {
                attempts++;
                if (attempts >= maxRetries) {
                    throw new RuntimeException("Failover failed after " + maxRetries + " attempts", e);
                }
            }
        }
        return response;
    }

    private InetSocketAddress getServiceAddress(RpcRequest rpcRequest, Set<InetSocketAddress> triedAddresses) {
        List<String> serviceList = CuratorUtils.discoverService(rpcRequest.getRpcServiceName());
        for (String address : serviceList) {
            String[] str = address.split(":");
            InetSocketAddress inetSocketAddress = new InetSocketAddress(str[0], Integer.parseInt(str[1]));
            if (!triedAddresses.contains(inetSocketAddress)) {
                return inetSocketAddress;
            }
        }
        return null;
    }

}
