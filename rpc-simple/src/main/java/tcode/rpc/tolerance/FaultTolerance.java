package tcode.rpc.tolerance;

import org.apache.dubbo.common.extension.SPI;
import tcode.rpc.remote.dto.RpcRequest;
import tcode.rpc.remote.dto.RpcResponse;
@SPI
public interface FaultTolerance {
    RpcResponse<Object> invoke(RpcRequest rpcRequest);
}
