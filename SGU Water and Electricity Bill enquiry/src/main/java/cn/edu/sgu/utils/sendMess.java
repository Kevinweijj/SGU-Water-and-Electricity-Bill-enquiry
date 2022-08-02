package cn.edu.sgu.utils;

import cn.edu.sgu.main.Fee;
import cn.hutool.http.HttpException;
import cn.hutool.http.HttpRequest;

public class sendMess {

    private static final String upInfo =
            "||更新内容(v3.7)|\n" +
                    "|-|-|\n" +
                    "|1|完善周报功能|\n" +
                    "|2|改进循环优化|\n" +
                    "|3|增加异常捕获，完善掉线重连机制|\n" +
                    "|4|改进建筑id的获取|\n" +
                    "|5|新增充值提醒，该功能会有延迟|\n" +
                    "|6|增加sessionId保活进程|\n" +
                    "|7|优化复用对象的创建，优化性能|\n" +
                    "|8|全部改用hutool的HttpUtil发送请求|\n" +
                    "|9|改用properties作为配置文件|\n" +
                    "|10|增加参数检测功能|\n" +
                    "> [拉比阁](http://rabig.cn/) - 愿你在韶院发光发亮";

    private static final String adminInfo =
            "|代码|错误提示|\n" +
                    "|-|-|\n" +
                    "|0x001|主函数出现错误|\n" +
                    "|0x002|重置等待时间出现错误|\n" +
                    "|0x003|获取水电费用出现错误|\n" +
                    "|0x004|保活进程出现错误|\n" +
                    "> [拉比阁](http://rabig.cn/) - 管理员提示";

    static HttpRequest sendError;
    static HttpRequest sendMessage;

    static {
        sendError = HttpRequest.post(Fee.adminUrl).timeout(20000);
        sendMessage = HttpRequest.post(Fee.userUrl).timeout(20000);
    }

    public static void sendError(String error) {
        try {
            System.out.println(error);
            String title = "\n" + error + "\n详细错误提示请点击查看";
            //请求息知
            sendError.body("title=" + title + "&" +
                            "content=" + adminInfo)
                    .execute().body();
        } catch (Exception e) {
            System.out.println(error + "\n");
            System.out.println("息知发送失败(代号:1x001)，错误信息：" + e);
        }

    }

    public static void sendMessage(String message) {
        try {
            System.out.println(message);
            String title = "\n" + message + "\n\nversion: 3.7，详细更新内容请点击查看";
            //请求息知
            sendMessage.body("title=" + title + "&" +
                            "content=" + upInfo)
                    .execute().body();
        } catch (HttpException e) {
            System.out.println(message + "\n");
            System.out.println("息知发送失败(代号:1x002)，错误信息：" + e);
        }
    }

    public static void sendTest() {
        try {
            //请求息知
            sendError.body("title=韶关学院水电费提醒测试信息&" +
                           "content=" + adminInfo).execute().body();
            sendMessage.body("title=韶关学院水电费提醒测试信息&" +
                             "content=" + upInfo).execute().body();
        } catch (HttpException e) {
            System.out.println("息知发送失败，请检查用户以及管理员的推送Url");
            System.exit(0);
        }
    }
}
