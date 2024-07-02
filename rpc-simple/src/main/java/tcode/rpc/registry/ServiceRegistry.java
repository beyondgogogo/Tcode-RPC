package tcode.rpc.registry;

import org.apache.dubbo.common.extension.SPI;

import java.net.InetSocketAddress;
@SPI
public interface ServiceRegistry {
    void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress);
}
