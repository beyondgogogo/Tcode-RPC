package tcode.rpc.loadbalance.loadBalanceImpl;

import tcode.rpc.loadbalance.LoadBalance;
import tcode.rpc.remote.dto.RpcRequest;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class ConsistentHashingLoadBalance implements LoadBalance {
    private final TreeMap<Long, String> ring = new TreeMap<>();
    private static final int VIRTUAL_NODES = 100;

    @Override
    public String selectServiceAddress(List<String> serviceAddresses, RpcRequest rpcRequest) {
        if (serviceAddresses.isEmpty()) {
            return null;
        }
        // Rebuild the consistent hash ring
        ring.clear();
        for (String address : serviceAddresses) {
            for (int i = 0; i < VIRTUAL_NODES; i++) {
                long hash = hash(address + "#" + i);
                ring.put(hash, address);
            }
        }
        long hash = hash(rpcRequest.getRpcServiceName());
        SortedMap<Long, String> tailMap = ring.tailMap(hash);
        Long key = !tailMap.isEmpty() ? tailMap.firstKey() : ring.firstKey();
        return ring.get(key);
    }

    private long hash(String key) {
        return key.hashCode() & 0xffffffffL; // simple hash function for illustration
    }
}