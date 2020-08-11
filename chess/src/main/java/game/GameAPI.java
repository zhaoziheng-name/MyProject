package game;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

// 通过这个类处理 websocket 的相关通信逻辑
@ServerEndpoint(value="/game/{userId}")
public class GameAPI {
    // 这个类就表示服务器收到的 websocket 请求
    // 匹配请求
    static class MatchRequest {
        public String type;
        public int userId;
    }

    // 落子请求
    static class PutChessRequest {
        public String type;
        public int userId;
        public int roomId;
        public int row;
        public int col;
    }

    private int userId;

    @OnOpen
    public void onOpen(@PathParam("userId") String userIdStr, Session session) {
        userId = Integer.parseInt(userIdStr);
        System.out.println("玩家建立连接: " + userId);

        // 把玩家加入到在线玩家列表中
        OnlineUserManager.getInstance().online(userId, session);
    }

    @OnClose
    public void onClose() {
        System.out.println("玩家断开连接: " + userId);

        // 把玩家从在线列表中剔除
        OnlineUserManager.getInstance().offline(userId);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        System.out.println("玩家断开连接: " + userId);
        // 把玩家从在线列表中剔除
        OnlineUserManager.getInstance().offline(userId);

        error.printStackTrace();
    }

    // onMessage 收到的数据可能是匹配请求, 也可能是落子请求
    // 就需要根据请求的 type 类型来做出区分
    // 如果 type 是 startMatch 处理匹配请求
    // 如果 type 是 putChess 处理落子请求
    // message 请求内容是一个 JSON 结构的字符串, 于是就需要针对 JSON 进行解析
    // 不需要自己实现, 用 GSON 即可
    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.printf("收到玩家 %d 的消息: %s\n", userId, message);

        // 后续的处理请求的逻辑
        // 实例化 Gson 对象
        Gson gson = new GsonBuilder().create();
        // 这一句代码, 就把 message 这个 JSON 格式的字符串转成了 需要的 Request 对象
        MatchRequest matchRequest = gson.fromJson(message, MatchRequest.class);
        PutChessRequest putChessRequest = gson.fromJson(message, PutChessRequest.class);
    }
}
