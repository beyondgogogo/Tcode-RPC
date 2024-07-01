package tcode.rpc.proxy;

import lombok.extern.slf4j.Slf4j;
import tcode.rpc.config.RpcServiceConfig;
import tcode.rpc.enums.RpcResponseCodeEnum;
import tcode.rpc.remote.dto.RpcRequest;
import tcode.rpc.remote.dto.RpcResponse;
import tcode.rpc.tolerance.FaultTolerance;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

/**
 * @author 田成强
 * 代理层，为客户端进行代理
 * 基本逻辑：代理对象中完成实际的网络传输和调用
 * */
@Slf4j
public class RpcClientProxy implements InvocationHandler {

    private final RpcServiceConfig rpcServiceConfig;
    private final FaultTolerance faultTolerance;
    public RpcClientProxy(RpcServiceConfig rpcServiceConfig,FaultTolerance faultTolerance){
        this.rpcServiceConfig=rpcServiceConfig;
        this.faultTolerance=faultTolerance;
    }

    public <T> T getProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        log.info("invoked method: [{}]", method.getName());
        // 1. 构建请求
        RpcRequest rpcRequest = RpcRequest.builder()
                .requestId(UUID.randomUUID().toString())
                .interfaceName(method.getDeclaringClass().getName())
                .parameters(args)
                .paramTypes(method.getParameterTypes())
                .group(rpcServiceConfig.getGroup())
                .version(rpcServiceConfig.getVersion())
                .build();
        // 2. 发送请求并获取响应
        RpcResponse<Object> response = faultTolerance.invoke(rpcRequest);

        // 3. 返回响应结果
        if (response == null || !response.getCode().equals(RpcResponseCodeEnum.SUCCESS.getCode())) {
            throw new RuntimeException("RPC request failed: " + (response != null ? response.getMessage() : "Unknown error"));
        }
        return response.getData();
    }
}
