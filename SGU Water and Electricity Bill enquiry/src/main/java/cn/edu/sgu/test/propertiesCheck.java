package cn.edu.sgu.test;

import cn.edu.sgu.main.Fee;
import cn.edu.sgu.utils.queryFee;
import cn.edu.sgu.utils.sendMess;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class propertiesCheck {

    /**
     * 测试sessionId参数
     */
    public static void testSessionId() {
        String responseBody = queryFee.keepAliveRequest.execute().body();//超时，毫秒
        if ("{\"errmsg\":\"会话已超时，请尝试重新访问业务应用。\",\"retcode\":\"91001\"}".equals(responseBody)) {
            System.out.println("sessionId已过期");
            System.exit(0);
        }
    }

    /**
     * 测试building参数
     */
    public static void testBuilding() {
        String responseBody = HttpRequest
                .post("http://210.38.192.117/wechat/basicQuery/queryElecBuilding.html")
                .cookie("JSESSIONID=" + Fee.sessionId)
                .keepAlive(false)
                .body("aid=0030000000002505&" +
                        "area={\"area\":\"1\",\"areaname\":\"韶关学院\"}")
                .timeout(20000)
                .execute().body();
        JSONArray buildingList = (JSONArray) JSON.parseObject(responseBody).get("buildingtab");
        for (Object building : buildingList) {
            JSONObject buildingInFo = (JSONObject) building;
            if (Fee.building.equals(buildingInFo.get("building"))) {
                queryFee.buildingId = (String) buildingInFo.get("buildingid");
                return;
            }
        }
        System.out.println("请检查建筑名称是否正确，详细建筑名称格式在readme.md");
        System.exit(0);
    }

    /**
     * 测试room参数
     */
    public static void testRoom() {
        for (int i = 5; i <= 6; i++) {
            queryFee.queryFeeRequest.body("aid=003000000000250" + i + "&" +
                    "area={\"area\":\"1\",\"areaname\":\"韶关学院\"}&" +
                    "building={\"building\":\"\",\"buildingid\":\"" + queryFee.buildingId + "\"}&" +
                    "floor={\"floorid\":\"\",\"floor\":\"\"}&" +
                    "room={\"room\":\"\",\"roomid\":\"" + Fee.room + "\"}");
            String responseBody = queryFee.queryFeeRequest.execute().body();
            JSONObject responseJson = JSON.parseObject(responseBody);
            if ("无法获取房间信息".equals(responseJson.get("errmsg"))) {
                System.out.println("无法获取房间信息，请检查房间号是否正确");
                System.exit(0);
            }
            if (i == 5) {
                System.out.println("查询到电费："+ responseJson.get("errmsg").toString().substring(7, 12) + "元，请检查是否正确");
            } else {
                System.out.println("查询到水费："+ responseJson.get("errmsg").toString().substring(7, 12) + "元，请检查是否正确");
            }
        }
    }

    /**
     * 测试weekDay参数
     */
    private static void testWeekDay() {
        if (Fee.weekDay < 0 || Fee.weekDay > 7){
            System.out.println("weekDay参数错误，请于1-7取值，星期日为1，星期一为2，星期二为3，以此类推");
            System.exit(0);
        }
    }

    /**
     * 测试frequency参数
     */
    private static void testFrequency() {
        if (Fee.frequency < 300){
            System.out.println("检测频率不宜过快，以免被封杀");
            System.exit(0);
        }
    }

    /**
     * 测试errFrequency参数
     */
    private static void testErrFrequency() {
        if (Fee.errFrequency < 5){
            System.out.println("错误重试时间不宜过快，以免被封杀");
            System.exit(0);
        }
    }

    /**
     * 测试Url参数
     */
    private static void testUrl() {
        sendMess.sendTest();
        System.out.println("已往息知发送测试信息，请自行查看是否收到");
    }

    /**
     * 测试waitTime参数
     */
    private static void testWaitTime() {
        if (Fee.waitTime < 60){
            System.out.println("waitTime不宜低于一分钟，由于充值电费后，学校数据库起码要3-4小时才会更新数据，因此waitTime时间太短的话会导致疯狂重复提示");
        }
    }

    public static void propertiesTest() {
        System.out.println("测试程序启动");
        testSessionId();
        testBuilding();
        testRoom();
        testFrequency();
        testWeekDay();
        testErrFrequency();
        testUrl();
        testWaitTime();
        System.out.println("所有参数检测完成，即将启动脚本\n");
        System.out.println("                 ,---.                      .=-.-.       _,---.   \n" +
                "  .-.,.---.    .--.'  \\         _..---.    /==/_ /   _.='.'-,  \\  \n" +
                " /==/  `   \\   \\==\\-/\\ \\      .' .'.-. \\  |==|, |   /==.'-     /  \n" +
                "|==|-, .=., |  /==/-|_\\ |    /==/- '=' /  |==|  |  /==/ -   .-'   \n" +
                "|==|   '='  /  \\==\\,   - \\   |==|-,   '   |==|- |  |==|_   /_,-.  \n" +
                "|==|- ,   .'   /==/ -   ,|   |==|  .=. \\  |==| ,|  |==|  , \\_.' ) \n" +
                "|==|_  . ,'.  /==/-  /\\ - \\  /==/- '=' ,| |==|- |  \\==\\-  ,    (  \n" +
                "/==/  /\\ ,  ) \\==\\ _.\\=\\.-' |==|   -   /  /==/. /   /==/ _  ,  /  \n" +
                "`--`-`--`--'   `--`         `-._`.___,'   `--`-`    `--`------'   \n");
    }

}
