package tcode.rpc;

import tcode.rpc.ServiceImpl.ServiceImpl2;
import tcode.rpc.config.RpcServiceConfig;
import tcode.rpc.factory.SingletonFactory;
import tcode.rpc.remote.transport.netty.server.NettyRpcServer;

public class NettyServerMain {
    public static void main(String[] args) {
        //Netty服务端
        NettyRpcServer nettyRpcServer= SingletonFactory.getInstance(NettyRpcServer.class);
        // Register service manually
        tianService service2 = new ServiceImpl2();
        RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                .group("test2").version("version2").service(service2).build();
        nettyRpcServer.registerService(rpcServiceConfig);
        //启动服务端
        nettyRpcServer.start();
    }
}