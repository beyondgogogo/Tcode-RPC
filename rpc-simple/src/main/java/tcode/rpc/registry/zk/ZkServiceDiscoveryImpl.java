package tcode.rpc.registry.zk;

import tcode.rpc.registry.ServiceDiscovery;
import tcode.rpc.remote.dto.RpcRequest;

import java.net.InetSocketAddress;

public class ZkServiceDiscoveryImpl implements ServiceDiscovery {
    @Override
    public InetSocketAddress lookupService(RpcRequest rpcRequest) {
        return null;
    }
}
