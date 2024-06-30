package tcode.rpc.registry.zk.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 田成强
 * Zookeeper的工具类，需要完成连接Zookeeper服务+服务的注册与发现
 * 服务注册：也比较容易，把IP+端口号+服务名注册到Node即可
 * 服务发现：根据RPC的requset的服务名进行节点信息查找
 * */
@Slf4j
public class CuratorUtils {
    private static final String ZK_ADDRESS="8.134.98.125:2181";
    private static final String ZK_REGISTRY_PATH="/rpc";

    private static final Map<String, List<String>> SERVICE_ADDRESS_MAP = new ConcurrentHashMap<>();
    private static final Map<String, PathChildrenCache> SERVICE_CACHE_MAP = new ConcurrentHashMap<>();

    //设置成静态的，这样只用在第一次进行连接，其他时候直接使用即可
    private  static CuratorFramework zkClient;

    static {
        zkClient = CuratorFrameworkFactory.builder()
                .connectString(ZK_ADDRESS)
                .retryPolicy(new ExponentialBackoffRetry(1000,3))
                .sessionTimeoutMs(5000)
                .connectionTimeoutMs(5000)
                .build();
        zkClient.start();
    }
    // 获取 Zookeeper 客户端实例
    public static CuratorFramework getZkClient() {
        return zkClient;
    }



    // 服务注册
    public static void registerService(String serviceName, String serviceAddress) {
        String servicePath = ZK_REGISTRY_PATH + "/" + serviceName + "/" + serviceAddress;
        try {
            // 创建临时节点，确保父节点存在
            if (zkClient.checkExists().forPath(servicePath) == null) {
                zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(servicePath);
                log.info("Service [{}] registered at path [{}]", serviceName, servicePath);
            }
        } catch (Exception e) {
            log.error("Failed to register service [{}]", serviceName, e);
        }
    }



    //服务发现
    public static List<String> discoverService(String serviceName){
        //1.先从缓存里面查询服务
        if(SERVICE_ADDRESS_MAP.containsKey(serviceName)){
            return SERVICE_ADDRESS_MAP.get(serviceName);
        }
        //2.本地缓存没有再到注册中心中查找
        String servicePath=ZK_REGISTRY_PATH+"/"+serviceName;
        try{
            //先检查有没有节点
            if(zkClient.checkExists().forPath(servicePath)!=null){
                //获取子节点列表
                List<String> serviceAddresses=zkClient.getChildren().forPath(servicePath);
                if(serviceAddresses==null || serviceAddresses.isEmpty()){
                    log.error("No available service found for[{}]",serviceName);
                    return null;
                }
                return serviceAddresses;
            }

        }catch (Exception e){
            log.error("failed to discover service[{}]",serviceName,e);
        }
        return null;
    }

    // 注册监听器
    private static void registerWatcher(String serviceName) {
        if (SERVICE_CACHE_MAP.containsKey(serviceName)) {
            return; // 如果已存在监听器，则无需再次注册
        }
        String servicePath = ZK_REGISTRY_PATH + "/" + serviceName;
        PathChildrenCache pathChildrenCache = new PathChildrenCache(zkClient, servicePath, true);
        try {
            pathChildrenCache.start();
            PathChildrenCacheListener pathChildrenCacheListener = (curatorFramework, event) -> {
                List<String> serviceAddresses = curatorFramework.getChildren().forPath(servicePath);
                SERVICE_ADDRESS_MAP.put(serviceName, serviceAddresses);
                log.info("Service [{}] addresses updated: {}", serviceName, serviceAddresses);
            };
            pathChildrenCache.getListenable().addListener(pathChildrenCacheListener);
            SERVICE_CACHE_MAP.put(serviceName, pathChildrenCache);
        } catch (Exception e) {
            log.error("Failed to register watcher for service [{}]", serviceName, e);
        }
    }

}
