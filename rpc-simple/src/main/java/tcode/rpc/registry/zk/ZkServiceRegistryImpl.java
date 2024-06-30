package tcode.rpc.registry.zk;

import tcode.rpc.registry.ServiceRegistry;
import tcode.rpc.registry.zk.util.CuratorUtils;

import java.net.InetSocketAddress;

public class ZkServiceRegistryImpl implements ServiceRegistry {

    @Override
    public void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress) {
        //我们需要把inetSocketAddress,转换为String类型的serviceAddress
        String ip= inetSocketAddress.getAddress().getHostAddress();
        int port= inetSocketAddress.getPort();
        String serviceAddress=ip+":"+port;
        CuratorUtils.registerService(rpcServiceName,serviceAddress);
    }
}
