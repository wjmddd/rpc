package Server;


import Server.provider.ServiceProvider;
import Server.server.RpcServer;
import Server.server.impl.SimpleRPCRPCServer;
import common.service.Impl.UserServiceImpl;
import common.service.UserService;

/**
 * @author wxx
 * @version 1.0
 * @create 2024/2/11 19:39
 */
public class TestServer {
    public static void main(String[] args) {
        UserService userService=new UserServiceImpl();

        ServiceProvider serviceProvider=new ServiceProvider();
        serviceProvider.provideServiceInterface(userService);

        RpcServer rpcServer=new SimpleRPCRPCServer(serviceProvider);
        rpcServer.start(9999);
    }
}
