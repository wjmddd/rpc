package Client.rpcClient;

import common.message.RpcRequest;
import common.message.RpcResponse;

public interface RpcClient {
    
    //定义底层通信的方法
    RpcResponse sendRequest(RpcRequest request);
}