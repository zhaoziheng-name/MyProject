// 几个重要参数的解释:
// userId: 用户登陆时获取到的. (测试阶段可以先写死)
// roomId: 当前这局游戏的房间号. 通过匹配结果获取到
// isWhite: 当前这局游戏是否是白子. 通过匹配结果获取到
// 这三个属性包裹到一个 gameInfo 对象中

// 这个数字应该是登陆后从服务器获取的, 目前在页面写死

gameInfo = {
    userId: myUserId,
    roomId: null,
    isWhite: true,
}

//////////////////////////////////////////////////
// 设定界面显示相关操作
//////////////////////////////////////////////////
function onClick(userId) {
    startMatch(userId);
    // 将按钮设置为不可点击, 并修改文本
    $("#matchButton").attr('disabled', true);
    $("#matchButton").text("匹配中...");
}

function hideMatchButton() {
    $("#matchButton").hide();
}

function setScreenText(me) {
    if (me) {
        $("#screen").text("轮到你落子了!")
    } else {
        $("#screen").text("轮到对方落子了!")
    }
}

//////////////////////////////////////////////////
// 初始化 websocket
//////////////////////////////////////////////////

var websocket = new WebSocket("ws://127.0.0.1:8080/chess/game/" + gameInfo.userId);

// 给 websocket 对象实现对应的方法
websocket.onopen = function () {
    console.log("连接建立成功! " + gameInfo.userId);
}

websocket.onclose = function () {
    console.log("连接断开! " + gameInfo.userId);
}

websocket.onerror = function () {
    console.log("连接异常! " + gameInfo.userId);
}

websocket.onmessage = function () {
    // TODO 处理服务器返回的响应
}

window.onbeforeunload = function () {
    websocket.close();
}

//////////////////////////////////////////////////
// 实现匹配逻辑
//////////////////////////////////////////////////

function startMatch(userId) {
    // 设置处理响应的函数
    websocket.onmessage = handlerStartMatch;
    // 先构造要发送的数据内容
    var message = {
      type: "startMatch",
      userId: userId
    };
    // 把 JSON 对象转成一个字符串发送给服务器
    websocket.send(JSON.stringify(message));
}

// 发出匹配请求之后,就准备处理匹配响应了
function handlerStartMatch(event) {
    // 读取相应内容
    var response = JSON.parse(event.data) // 才能得到真实的服务器返回的数据
    console.log("handlerStartMatch: " + response);
    // 过滤非法的响应类型
    if (response.type != "startMatch") {
        // 服务器响应的数据不对,直接忽略错误数据
        return;
    }
    // 把响应的结果保存到 gameInfo 对象中
    gameInfo.roomId = response.roomId;
    gameInfo.isWhite = response.isWhite;
    gameInfo.otherUserId = response.otherUserId
    // 隐藏匹配按钮
    hideMatchButton();
    // 设置提示信息(轮到谁落子)
    // 参数为 true 表示自己落子, 参数为 false 表示对手落子
    setScreenText(gameInfo.isWhite);
    // 初始化一局游戏
    initGame();
}

//////////////////////////////////////////////////
// 匹配成功, 则初始化一局游戏
//////////////////////////////////////////////////
function initGame() {
    // 是我下还是对方下. 根据服务器分配的先后手情况决定
    var me = gameInfo.isWhite;
    // 游戏是否结束
    var over = false;
    var chessBoard = [];
    //初始化chessBord数组(表示棋盘的数组)
    for (var i = 0; i < 15; i++) {
        chessBoard[i] = [];
        for (var j = 0; j < 15; j++) {
            chessBoard[i][j] = 0;
        }
    }
    var chess = document.getElementById('chess');
    var context = chess.getContext('2d');
    context.strokeStyle = "#BFBFBF";
    // 背景图片
    var logo = new Image();
    logo.src = "images/sky.jpg";
    logo.onload = function () {
        context.drawImage(logo, 0, 0, 450, 450);
        initChessBoard();
    }

    // 绘制棋盘网格
    function initChessBoard() {
        for (var i = 0; i < 15; i++) {
            context.moveTo(15 + i * 30, 15);
            context.lineTo(15 + i * 30, 435);
            context.stroke();
            context.moveTo(15, 15 + i * 30);
            context.lineTo(435, 15 + i * 30);
            context.stroke();
        }
    }

    // 绘制一个棋子, me 为 true
    function oneStep(i, j, isWhite) {
        context.beginPath();
        context.arc(15 + i * 30, 15 + j * 30, 13, 0, 2 * Math.PI);
        context.closePath();
        var gradient = context.createRadialGradient(15 + i * 30 + 2, 15 + j * 30 - 2, 13, 15 + i * 30 + 2, 15 + j * 30 - 2, 0);
        if (!isWhite) {
            gradient.addColorStop(0, "#0A0A0A");
            gradient.addColorStop(1, "#636766");
        } else {
            gradient.addColorStop(0, "#D1D1D1");
            gradient.addColorStop(1, "#F9F9F9");
        }
        context.fillStyle = gradient;
        context.fill();
    }

    chess.onclick = function (e) {
        if (over) {
            return;
        }
        if (!me) {
            return;
        }
        var x = e.offsetX;
        var y = e.offsetY;
        // 注意, 横坐标是列, 纵坐标是行
        var col = Math.floor(x / 30);
        var row = Math.floor(y / 30);
        if (chessBoard[row][col] == 0) {
            // 新增发送数据给服务器的逻辑
            send(row, col);
            // oneStep(col, row, gameInfo.isWhite);
            // chessBoard[row][col] = 1;
            // 通过这个语句控制落子轮次
            // me = !me;
        }
    }

    function send(row, col) {
        console.log("send: " + row + ", " + col);
        var request = {
            type: "putChess",
            userId: gameInfo.userId,
            roomId: gameInfo.roomId,
            row: row,
            col: col
        }
        websocket.send(JSON.stringify(request));
    }

    // 新增处理服务器返回数据的请求
    // 并绘制棋子, 以及判定胜负
    function handlerPutChess(event) {
        console.log("handlerPutChess: " + event.data);
        // event.data 是一个字符串, 需要转换成 JSON 对象
        // 1. 读取响应内容
        var response = JSON.parse(event.data);
        if (response.type != "putChess") {
            console.log("handlerPutChess: 响应类型不匹配")
            return;
        }
        // 2. 根据响应内容, 决定绘制哪种颜色的棋子
        if (response.userId == gameInfo.userId) {
            // 当前棋子是自己落的, 就根据自己的 isWhite 来绘制棋子
            oneStep(response.col, response.row, gameInfo.isWhite);
        } else {
            // 当前棋子是对方落的, 就根据对方的 isWhite 来绘制棋子
            // 对方的 isWhite 和自己的一定是相反的
            oneStep(response.col, response.row, !gameInfo.isWhite)
        }
        // 3. 给棋盘数组记录位置(防止同一个位置被多次落子)
        chessBoard[response.row][response.col] = 1;
        // 每次落子完毕就需要更新 me, 表示由对方来落子
        me = !me;
        // 4. 处理胜负
        if (response.winner != 0) {
            // 胜负已分
            if (response.winner == gameInfo.userId) {
                // 自己赢了
                alert("您赢了!");
            } else {
                // 对方赢了
                alert("您输了!");
            }
            // 自动刷新页面, 以备进行下一局游戏
            // 一旦页面刷新, 当前这里的 JS 代码的变量, 状态就没有了
            window.location.reload();
        }
        // 5. 更新界面显示, "轮到 xx 落子"
        setScreenText(me);
    }

    websocket.onmessage = handlerPutChess;
}

// 应该在匹配成功后再绘制棋盘
// initGame();