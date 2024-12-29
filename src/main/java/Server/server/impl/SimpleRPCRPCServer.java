package Server.server.impl;

import Server.server.RpcServer;
import Server.provider.ServiceProvider;
import Server.work.WorkThread;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
@AllArgsConstructor
public class SimpleRPCRPCServer implements RpcServer {
    private ServiceProvider serviceProvider;
    @Override
    public void start(int port) {
        try {
            ServerSocket serverSocket=new ServerSocket(port);
            System.out.println("服务器启动了");
            while(true){
                //如果没有连接，会堵塞在这里
                Socket socket=serverSocket.accept();
                //有链接，则创建一个新的线程执行处理
                new Thread(new WorkThread(socket,serviceProvider)).start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stop() {

    }
}
