package cn.edu.sgu.main;

import cn.edu.sgu.test.propertiesCheck;
import cn.edu.sgu.utils.queryFee;
import cn.edu.sgu.utils.sendMess;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.dialect.Props;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * @author: MoNo
 * @time: 2022/8/2 12:15
 */
public class Fee {
    public static String room;
    public static String building;
    public static String adminUrl;
    public static String userUrl;
    public static int frequency;
    public static int errFrequency;
    public static int waitTime;
    static boolean feeUpEnable;
    static boolean weekEnable;
    public static int weekDay;
    public static String sessionId;

    static {
        //初始化配置文件
        try {
            Props props = new Props(System.getProperty("user.dir") + System.getProperty("file.separator") + "fee.properties", CharsetUtil.UTF_8);
            room = props.getProperty("room");
            building = props.getProperty("building");
            adminUrl = props.getProperty("adminUrl");
            userUrl = props.getProperty("userUrl");
            frequency = props.getInt("frequency");
            errFrequency = props.getInt("errFrequency");
            waitTime = props.getInt("waitTime");
            feeUpEnable = props.getBool("feeUpEnable");
            weekEnable = props.getBool("weekEnable");
            weekDay = props.getInt("weekDay");
            sessionId = props.getProperty("sessionId");
            if (StrUtil.hasBlank(room,building,adminUrl,userUrl,sessionId)){
                System.out.println("请完善配置文件！");
                System.exit(0);
            }
        } catch (Exception e) {
            System.out.println("请检查配置文件！");
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        try {
            //启动参数测试程序
            propertiesCheck.propertiesTest();
            //脚本主函数
            System.out.println("脚本启动");
            //启动sessionId保活进程
            new Thread(queryFee::keepAlive).start();
            while (true) {
                //查询水电费信息
                queryFee.queryFees();

                //低额提醒部分
                if (queryFee.waitFlag) {
                    String message = null;
                    if (queryFee.newElectricityFee.compareTo(new BigDecimal("20")) < 0) {
                        message = "电费剩余：" + queryFee.newElectricityFee + "元，电费小于20元\n";
                    }
                    if (queryFee.newWaterFee.compareTo(new BigDecimal("20")) < 0) {
                        message = message + "水费剩余：" + queryFee.newWaterFee + "元，水费小于20元\n";
                    }
                    if (message != null) {
                        message = message + "提示将在约" + new DecimalFormat("#.##").format((float) waitTime / 3600) + "小时后再次提醒您，请及时充值\n" +
                                "若您已充值，请忽略此提示，企业微信更新余额存在延迟";
                        sendMess.sendMessage(message);
                        queryFee.waitFlag = false;
                        new Thread(() -> {
                            try {
                                Thread.sleep(waitTime * 1000L);
                            } catch (InterruptedException e) {
                                sendMess.sendError("程序发生异常(代号:0x002)，程序立即退出，错误信息：" + e);
                                System.exit(0);
                            }
                            queryFee.waitFlag = true;
                        });
                    }
                }

                //充值提醒部分
                if (feeUpEnable) {
                    if (queryFee.ElectricityFee.compareTo(queryFee.newElectricityFee) < 0) {
                        sendMess.sendMessage("电费充值成功，充值金额" + queryFee.newElectricityFee.subtract(queryFee.ElectricityFee) + "元，信息可能有延迟");
                    }
                    if (queryFee.WaterFee.compareTo(queryFee.newWaterFee) < 0) {
                        sendMess.sendMessage("水费充值成功，充值金额" + queryFee.newWaterFee.subtract(queryFee.WaterFee) + "元，信息可能有延迟");
                    }
                }

                //周报提醒部分
                if (weekEnable) {
                    //每个星期一重新计算周费
                    if (new DateTime().dayOfWeek() == 2 && queryFee.cleanFlag) {
                        queryFee.weekElectricityFee = BigDecimal.valueOf(0);//清空周费
                        queryFee.weekWaterFee = BigDecimal.valueOf(0);
                        queryFee.cleanFlag = false;
                    } else if (!queryFee.cleanFlag && new DateTime().dayOfWeek() != 2) {
                        queryFee.cleanFlag = true;
                    }
                    //周费计算部分
                    if (queryFee.ElectricityFee.compareTo(queryFee.newElectricityFee) > 0) {
                        queryFee.weekElectricityFee = queryFee.ElectricityFee.subtract(queryFee.newElectricityFee).add(queryFee.weekElectricityFee);
                    }
                    if (queryFee.WaterFee.compareTo(queryFee.newWaterFee) > 0) {
                        queryFee.weekWaterFee = queryFee.weekWaterFee.subtract(queryFee.newWaterFee).add(queryFee.weekWaterFee);
                    }
                    //周报提醒
                    if (new DateTime().dayOfWeek() == weekDay && DateUtil.date().hour(true) > 7 && queryFee.weekFlag) {
                        sendMess.sendMessage("本周电费花费" + queryFee.weekElectricityFee + "元\n水费花费" + queryFee.weekWaterFee + "元");
                        queryFee.weekFlag = false;
                    } else if (!queryFee.weekFlag && new DateTime().dayOfWeek() != weekDay) {
                        queryFee.weekFlag = true;
                    }
                }

                //程序延迟
                System.out.println("脚本运行中");
                Thread.sleep(frequency * 1000L);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendMess.sendError("程序发生异常(代号:0x001)，程序立即退出，错误信息：" + e);
            System.exit(0);
        }
    }
}
