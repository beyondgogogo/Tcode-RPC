package tcode.rpc.tolerance;

import tcode.rpc.remote.dto.RpcRequest;
import tcode.rpc.remote.dto.RpcResponse;

public interface FaultTolerance {
    RpcResponse<Object> invoke(RpcRequest rpcRequest);
}
