package Client.rpcClient.impl;

import Client.netty.nettyInitializer.NettyClientInitializer;
import Client.rpcClient.RpcClient;
import Client.serviceCenter.ServiceCenter;
import Client.serviceCenter.impl.ZKServiceCenter;
import common.message.RpcRequest;
import common.message.RpcResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import lombok.AllArgsConstructor;

import java.net.InetSocketAddress;

@AllArgsConstructor
public class NettyRpcClient implements RpcClient {
    private static final Bootstrap bootstrap;

    private static final EventLoopGroup eventLoopGroup;

    private ServiceCenter serviceCenter;

    public NettyRpcClient(){
        this.serviceCenter=new ZKServiceCenter();
    }

    //netty客户端初始化
    static {
        bootstrap = new Bootstrap();
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                //NettyClientInitializer这里 配置netty对消息的处理机制
                .handler(new NettyClientInitializer());
    }
    @Override
    public RpcResponse sendRequest(RpcRequest request) {
        //从注册中心获取host，post
        InetSocketAddress address=serviceCenter.serviceDiscovery(request.getInterfaceName());
        String host=address.getHostName();
        int port=address.getPort();

        //创建channelFuture对象，代表着一个操作事件，sync方法堵塞事件
        try {
            ChannelFuture channelFuture =bootstrap.connect(host,port).sync();
            Channel channel = channelFuture.channel();
            //发送数据
            channel.writeAndFlush(request);
            //sync堵塞获取返回结果
            channel.closeFuture().sync();
            // 阻塞的获得结果，通过给channel设计别名，获取特定名字下的channel中的内容（这个在hanlder中设置）
            // AttributeKey是，线程隔离的，不会由线程安全问题。
            // 当前场景下选择堵塞获取结果
            // 其它场景也可以选择添加监听器的方式来异步获取结果 channelFuture.addListener...
            AttributeKey<RpcResponse> key=AttributeKey.valueOf("RPCResponse");
            RpcResponse response = channel.attr(key).get();

            System.out.println(response);
            return response;

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
