package game;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.websocket.Session;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

// 这个类来实现匹配功能, 内部管理了一个匹配队列
// 也是一个单例类
public class Matcher {
    private Gson gson = new GsonBuilder().create();

    // 表示匹配成功后的响应数据
    public static class MatchResponse {
        public String type;
        public String roomId;
        public boolean isWhite;
        public int otherUserId;
    }

    // 实现一个匹配队列
    private BlockingQueue<GameAPI.Request> queue = new LinkedBlockingDeque<>();

    // 实现插入到阻塞队列的方法
    public void addToMatchQueue(GameAPI.Request request) throws InterruptedException {
        queue.put(request);
    }

    // 创建一个扫描线程, 尝试进行匹配功能
    // 在构造实例时创建该线程
    private Matcher() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    handlerMatch();
                }
            }
        }).start();
    }

    private void handlerMatch() {
        // 实现一次匹配的过程
        try {
            // 1. 从阻塞队列中取出两个玩家的信息
            GameAPI.Request player1 = queue.take();
            GameAPI.Request player2 = queue.take();
            System.out.println("匹配到两个玩家" + player1.userId + ", " + player2.userId);
            // 2. 检查两个玩家是否在线, 处理玩家下线的情况
            //    也要处理玩家匹配到自己的情况
            OnlineUserManager manager = OnlineUserManager.getInstance();
            Session session1 = manager.getSession(player1.userId);
            Session session2 = manager.getSession(player2.userId);
            // 如果玩家不在线, 对应的 session 就是 null
            if (session1 == null) {
                queue.put(player2);
                System.out.println("玩家 1 不在线");
                return;
            }
            if (session2 == null) {
                queue.put(player1);
                System.out.println("玩家 2 不在线");
                return;
            }
            if (session1 == session2) {
                // 自己匹配到了自己
                queue.put(player1);
                System.out.println("自己匹配到自己!!");
                return;
            }
            // 3. 把两个玩家加入到同一个游戏房间中
            // 此处需要引入房间这样的一个对象,把房间对象管理起来
            Room room = new Room();
            room.setPlayerId1(player1.userId);
            room.setPlayerId2(player2.userId);
            RoomManager.getInstance().addRoom(room);
            System.out.println("玩家进入房间成功! roomId: " + room.getRoomId());
            // 4. 给玩家 1 发送匹配响应, 告诉玩家匹配成功, 对手是谁, 房间号是多少
            // 按照前面所约定的响应格式, 把匹配成功的结果返回给客户端
            MatchResponse response1 = new MatchResponse();
            response1.type = "startMatch";
            response1.roomId = room.getRoomId();
            response1.isWhite = true;
            response1.otherUserId = player2.userId;
            String respJson1 = gson.toJson(response1);
            session1.getBasicRemote().sendText(respJson1);
            System.out.println("给玩家 1 响应" + respJson1);
            // 4. 给玩家 2 发送匹配响应, 告诉玩家匹配成功, 对手是谁, 房间号是多少
            // 按照前面所约定的响应格式, 把匹配成功的结果返回给客户端
            MatchResponse response2 = new MatchResponse();
            response2.type = "startMatch";
            response2.roomId = room.getRoomId();
            response2.isWhite = false;
            response2.otherUserId = player1.userId;
            String respJson2 = gson.toJson(response2);
            session2.getBasicRemote().sendText(respJson2);
            System.out.println("给玩家 2 响应" + respJson2);
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    private static volatile Matcher instance = null;

    public static Matcher getInstance() {
        if (instance == null) {
            synchronized (Matcher.class) {
                if (instance == null) {
                    instance = new Matcher();
                }
            }
        }
        return instance;
    }
}
