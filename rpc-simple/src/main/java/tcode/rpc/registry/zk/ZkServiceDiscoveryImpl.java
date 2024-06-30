package tcode.rpc.registry.zk;

import tcode.rpc.loadbalance.LoadBalance;
import tcode.rpc.registry.ServiceDiscovery;
import tcode.rpc.registry.zk.util.CuratorUtils;
import tcode.rpc.remote.dto.RpcRequest;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;

public class ZkServiceDiscoveryImpl implements ServiceDiscovery {
    private final LoadBalance loadBalance;
    public ZkServiceDiscoveryImpl(LoadBalance loadBalance){
        this.loadBalance=loadBalance;
    }
    @Override
    public InetSocketAddress lookupService(RpcRequest rpcRequest) {
        //1.先查到服务列表
        List<String> serviceList=CuratorUtils.discoverService(rpcRequest.getRpcServiceName());
        //2.再经过负载均衡策略选出合适的服务来
        String path=loadBalance.selectServiceAddress(serviceList,rpcRequest);
        String[] str=path.split(":");
        return new InetSocketAddress(str[0], Integer.parseInt(str[1]));
    }
}
