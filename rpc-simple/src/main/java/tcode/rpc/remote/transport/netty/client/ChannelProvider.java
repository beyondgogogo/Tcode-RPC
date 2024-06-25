package tcode.rpc.remote.transport.netty.client;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用来缓存Channel通道，避免频繁创建连接，消耗资源性能
 *
 */
@Slf4j
public class ChannelProvider {

    private final Map<String, Channel> channelMap;

    /**
     * 多线程情况下采用ConcurrentHashMap实现线程安全
     * JDK8之前：分段锁特点：通过降低锁的粒度来控制并发
     * JDK8及之后：移除了分段锁，通过synchronized+CAS来进行控制，提升性能
     * */
    public ChannelProvider() {
        channelMap = new ConcurrentHashMap<>();
    }

    public Channel get(InetSocketAddress inetSocketAddress) {
        String key = inetSocketAddress.toString();
        // determine if there is a connection for the corresponding address
        if (channelMap.containsKey(key)) {
            Channel channel = channelMap.get(key);
            // if so, determine if the connection is available, and if so, get it directly
            if (channel != null && channel.isActive()) {
                return channel;
            } else {
                channelMap.remove(key);
            }
        }
        return null;
    }

    public void set(InetSocketAddress inetSocketAddress, Channel channel) {
        String key = inetSocketAddress.toString();
        channelMap.put(key, channel);
    }

}
