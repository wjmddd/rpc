package Client.serviceCenter.impl;

import Client.cache.serviceCache;
import Client.serviceCenter.ServiceCenter;
import Client.serviceCenter.ZKWacther.WatchZK;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.net.InetSocketAddress;
import java.util.List;

public class ZKServiceCenter implements ServiceCenter {
    // curator 提供的zookeeper客户端
    private CuratorFramework client;

    //zookeeper根路径节点
    private static final String ROOT_PATH="MyRPC";
    private serviceCache cache;

    // 负责zookeeper客户端的初始化，并于zookeeper服务端建立连接
    public ZKServiceCenter(){
        //指数时间重试
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        this.client = CuratorFrameworkFactory.builder().connectString("127.0.0.1:2181")
                .sessionTimeoutMs(40000).retryPolicy(retryPolicy).namespace(ROOT_PATH).build();
        this.client.start();
        System.out.println("zookeeper连接成功");
        //初始化本地缓存
        cache=new serviceCache();
        WatchZK watcher=new WatchZK(client,cache);
        watcher.watchToUpdate(ROOT_PATH);
    }
    //根据服务名返回地址
    @Override
    public InetSocketAddress serviceDiscovery(String serviceName) {
        try {
            //先从本地缓存中寻找
            List<String> serviceList=cache.getServcieFromCache(serviceName);
            if(serviceList==null){
                serviceList=client.getChildren().forPath("/"+serviceName);
            }
            //先默认第一个，后面加负载均衡
            String string=serviceList.get(0);
            return parseAddress(string);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //字符串解析为地址
    private InetSocketAddress parseAddress(String address) {
        String[] result = address.split(":");
        return new InetSocketAddress(result[0], Integer.parseInt(result[1]));
    }

    // 地址 -> XXX.XXX.XXX.XXX:port 字符串
    private String getServiceAddress(InetSocketAddress serverAddress) {
        return serverAddress.getHostName() +
                ":" +
                serverAddress.getPort();
    }
}
