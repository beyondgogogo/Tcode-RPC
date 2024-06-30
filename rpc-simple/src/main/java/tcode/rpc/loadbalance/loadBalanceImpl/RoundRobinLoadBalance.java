package tcode.rpc.loadbalance.loadBalanceImpl;

import tcode.rpc.loadbalance.LoadBalance;
import tcode.rpc.remote.dto.RpcRequest;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinLoadBalance implements LoadBalance {
    /**
     * AtomicInteger 是 Java 并发包 (java.util.concurrent.atomic) 中的一个类，
     * 提供了一种通过原子操作更新整数值的机制。
     * 它的核心是利用了硬件提供的原子指令（如 CAS，Compare-And-Swap）来确保操作的原子性，
     * 而不是依赖锁来实现线程安全。
     * 保证线程安全的前提下，性能更加优越*/
    private final AtomicInteger index = new AtomicInteger(0);

    @Override
    public String selectServiceAddress(List<String> serviceAddresses, RpcRequest rpcRequest) {
        if (serviceAddresses.isEmpty()) {
            return null;
        }
        int currentIndex = index.getAndIncrement();
        return serviceAddresses.get(currentIndex % serviceAddresses.size());
    }
}
