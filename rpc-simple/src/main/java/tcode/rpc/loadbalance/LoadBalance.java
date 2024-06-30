package tcode.rpc.loadbalance;

import tcode.rpc.remote.dto.RpcRequest;

import java.util.List;

public interface LoadBalance {
    String selectServiceAddress(List<String> serviceAddresses, RpcRequest rpcRequest);
}
