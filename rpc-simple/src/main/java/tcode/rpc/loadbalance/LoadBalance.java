package tcode.rpc.loadbalance;

import org.apache.dubbo.common.extension.SPI;
import tcode.rpc.remote.dto.RpcRequest;

import java.util.List;
@SPI
public interface LoadBalance {
    String selectServiceAddress(List<String> serviceAddresses, RpcRequest rpcRequest);
}
