package Server.work;

import Server.provider.ServiceProvider;
import common.message.RpcRequest;
import common.message.RpcResponse;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;

@AllArgsConstructor
//WorkThread类负责启动线程和客户端进行数据传输
//WorkThread类中的getResponse方法负责解析收到的request信息，寻找服务进行调用并返回结果
public class WorkThread implements  Runnable{
    private Socket socket;
    private ServiceProvider serviceProvider;
    @Override
    public void run() {
        try{
            ObjectOutputStream oss=new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois=new ObjectInputStream(socket.getInputStream());

            //读取客户端传来的request
            RpcRequest rpcRequest=(RpcRequest) ois.readObject();
            //反射调用方法并获取返回值
            RpcResponse rpcResponse=getResponse(rpcRequest);
            //向客户端返回response
            oss.writeObject(rpcResponse);
            oss.flush();
        } catch (IOException |ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private RpcResponse getResponse(RpcRequest rpcRequest) {
        //得到服务名
        String interfaceName = rpcRequest.getInterfaceName();
        //得到服务端相应的服务实现类
        Object service=serviceProvider.getService(interfaceName);
        //反射调用方法
        Method method=null;
        try {
            method=service.getClass().getMethod(rpcRequest.getMethodName(),rpcRequest.getParamsTypes());
            Object invoke=method.invoke(service,rpcRequest.getParams());
            return RpcResponse.success(invoke);
        } catch (NoSuchMethodException |InvocationTargetException |IllegalAccessException e) {
            e.printStackTrace();
            System.out.println("方法执行错误");
            return RpcResponse.fail();
        }
    }


}