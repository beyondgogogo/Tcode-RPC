package tcode.rpc.registry;

import org.apache.dubbo.common.extension.SPI;
import tcode.rpc.remote.dto.RpcRequest;

import java.net.InetSocketAddress;
@SPI
public interface ServiceDiscovery {
    InetSocketAddress lookupService(RpcRequest rpcRequest);
}
