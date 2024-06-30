package tcode.rpc.loadbalance.loadBalanceImpl;

import tcode.rpc.loadbalance.LoadBalance;
import tcode.rpc.remote.dto.RpcRequest;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RandomLoadBalance implements LoadBalance {
    @Override
    public String selectServiceAddress(List<String> serviceAddresses, RpcRequest rpcRequest) {
        if (serviceAddresses.isEmpty()) {
            return null;
        }
        /**
         * ThreadLocalRandom 特别适用于高并发场景，例如服务器端的请求处理。
         * 在这些场景中，每个线程都会独立地生成随机数，彼此之间不会干扰，从而提高了效率。
         * */
        int randomIndex = ThreadLocalRandom.current().nextInt(serviceAddresses.size());
        return serviceAddresses.get(randomIndex);
    }
}
