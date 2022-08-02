package cn.edu.sgu.utils;

import cn.edu.sgu.main.Fee;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;


import java.math.BigDecimal;

public class queryFee {
    public static String buildingId;
    public static BigDecimal ElectricityFee;
    public static BigDecimal WaterFee;
    public static BigDecimal newElectricityFee;
    public static BigDecimal newWaterFee;
    public static volatile boolean waitFlag = true;//用于等待低额提醒后等待是否结束
    public static boolean cleanFlag = true;//用于记录周费是否清理
    public static boolean weekFlag = true;//用于周报提醒后等待是否结束
    public static BigDecimal weekElectricityFee = new BigDecimal("0");
    public static BigDecimal weekWaterFee = new BigDecimal("0");
    public static HttpRequest keepAliveRequest;
    public static HttpRequest queryFeeRequest;

    /*
      初始化请求头
     */
    static {
        keepAliveRequest = HttpRequest
                .post("http://210.38.192.117/wechat/basicQuery/querySubCardInfo.html")
                .cookie("JSESSIONID=" + Fee.sessionId)
                .keepAlive(true)
                .timeout(20000);//超时，毫秒
        queryFeeRequest = HttpRequest
                .post("http://210.38.192.117/wechat/basicQuery/queryElecRoomInfo.html")
                .cookie("JSESSIONID=" + Fee.sessionId)
                .keepAlive(true)
                .timeout(20000);//超时，毫秒
    }

    /**
     * sessionId保活进程
     */
    public static void keepAlive() {
        try {
            while (true) {
                String responseBody = keepAliveRequest.execute().body();
                if ("{\"errmsg\":\"会话已超时，请尝试重新访问业务应用。\",\"retcode\":\"91001\"}".equals(responseBody)) {
                    sendMess.sendError("sessionId已过期，程序立即退出");
                    System.exit(0);
                }
                System.out.println("保活进程运转中");
                Thread.sleep(900000);//每15分钟发送一次请求，确保sessionId不会过期
            }
        } catch (Exception e) {
            try {
                sendMess.sendError("保活进程请求失败，错误信息：" + e + "\n程序尝试重连");//出现函数处理错误
                Thread.sleep(10000);//保活进程的时间不应该由用户进行设置
                keepAlive();
            } catch (Exception ex) {
                sendMess.sendError("程序发生异常(代号:0x004)，程序立即退出，错误信息：" + ex);
                System.exit(0);
            }
        }
    }

    /**
     * 获取水电费
     */
    public static void queryFees() {
        try {
            for (int i = 5; i <= 6; i++) {
                queryFeeRequest.body("aid=003000000000250" + i + "&" +
                        "area={\"area\":\"1\",\"areaname\":\"韶关学院\"}&" +
                        "building={\"building\":\"\",\"buildingid\":\"" + buildingId + "\"}&" +
                        "floor={\"floorid\":\"\",\"floor\":\"\"}&" +
                        "room={\"room\":\"\",\"roomid\":\"" + Fee.room + "\"}");
                String responseBody = queryFeeRequest.execute().body();
                JSONObject responseJson = JSON.parseObject(responseBody);
                if (i == 5) {
                    if (newElectricityFee == null) {
                        newElectricityFee = new BigDecimal(responseJson.get("errmsg").toString().substring(7, 12));
                    }
                    ElectricityFee = newElectricityFee;
                    newElectricityFee = new BigDecimal(responseJson.get("errmsg").toString().substring(7, 12));
                } else {
                    if (newWaterFee == null) {
                        newWaterFee = new BigDecimal(responseJson.get("errmsg").toString().substring(7, 12));
                    }
                    WaterFee = newWaterFee;
                    newWaterFee = new BigDecimal(responseJson.get("errmsg").toString().substring(7, 12));
                }

            }
        } catch (Exception e) {
            try {
                sendMess.sendError("获取水电费信息失败，错误信息：" + e + "\n程序尝试重连");//出现函数处理错误
                Thread.sleep(Fee.errFrequency * 1000L);
                queryFees();
            } catch (Exception ex) {
                sendMess.sendError("程序发生异常(代号:0x003)，程序立即退出，错误信息：" + ex);
                System.exit(0);
            }
        }
    }

}
