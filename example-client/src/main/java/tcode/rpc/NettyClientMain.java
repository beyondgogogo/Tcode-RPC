package tcode.rpc;


import tcode.rpc.config.RpcServiceConfig;
import tcode.rpc.proxy.RpcClientProxy;


/**
 * @author 田成强
 * 通过代理层进行服务调用：构建rpcserviceconfig:服务的组以及版本号
 * 因为组一确定，版本一确定，服务就确定了*/
public class NettyClientMain {
    public static void main(String[] args) {
        RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                .group("test2").version("version2").build();
        RpcClientProxy rpcClientProxy=new RpcClientProxy(rpcServiceConfig);
        tianService s=rpcClientProxy.getProxy(tianService.class);
        s.method("test");

    }
}