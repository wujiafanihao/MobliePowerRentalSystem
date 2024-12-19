package util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BatteryMonitor {
    private static final int CHECK_INTERVAL = 5; // 检查间隔（分钟）
    private static final int BATTERY_DECREASE_RATE = 1; // 每次减少的电量
    private static final int BATTERY_INCREASE_RATE = 1; // 每次增加的电量
    private static final int BATTERY_FULL_THRESHOLD = 30; // 电量达到此值时视为充满
    private static BatteryMonitor instance; // 用于避免多次实例化
    private final ScheduledExecutorService scheduler; // 用于定时检查电池状态，执行定时任务
    private boolean isRunning; // 是否正在运行

    // 创建单例构造函数
    private BatteryMonitor() {
        scheduler = Executors.newSingleThreadScheduledExecutor(); // 创建定时任务执行器线程
        isRunning = false;
        LogUtil.info("BatteryMonitor 实例已创建");
    }

    // 获取对象实例
    public static BatteryMonitor getInstance() {
        if (instance == null) {
            synchronized (BatteryMonitor.class) {
                if (instance == null) {
                    instance = new BatteryMonitor();
                }
            }
        }
        return instance;
    }

    // 启动电池监控服务
    public void start() {
        if (!isRunning) {
            LogUtil.info("正在启动电池监控服务...");
            // 初始化定时任务，参数为任务、开始延迟、间隔、时间单位
            scheduler.scheduleAtFixedRate(this::checkAndUpdateBatteries, 0, CHECK_INTERVAL, TimeUnit.MINUTES);
            isRunning = true;
            LogUtil.info("电池监控服务已启动，将每" + CHECK_INTERVAL + "分钟检查一次电池状态");
        } else {
            LogUtil.info("电池监控服务已经在运行中");
        }
    }

    public void stop() {
        if (isRunning) {
            scheduler.shutdown();
            isRunning = false;
            LogUtil.info("电池监控服务已停止");
        }
    }

    private void checkAndUpdateBatteries() {
        LogUtil.info("开始检查电池状态...");
        try {
            // 处理正在使用的电池（电量减少）
            String inUseQuery = "SELECT * FROM powerbank WHERE status = 'InUse'";
            ResultSet inUseRs = DBHelper.executeQuery(inUseQuery);
            int inUseCount = 0;
            while (inUseRs.next()) {
                inUseCount++;
                int id = inUseRs.getInt("id");
                int currentBattery = inUseRs.getInt("battery_level");
                int newBattery = Math.max(0, currentBattery - BATTERY_DECREASE_RATE);
                
                String updateQuery;
                if (newBattery == 0) {
                    updateQuery = "UPDATE powerbank SET battery_level = ?, status = 'Unavailable' WHERE id = ?";
                } else {
                    updateQuery = "UPDATE powerbank SET battery_level = ? WHERE id = ?";
                }
                DBHelper.executeUpdate(updateQuery, newBattery, id);
                
                LogUtil.info(String.format("已更新电池ID %d 的电量：%d -> %d", id, currentBattery, newBattery));
            }
            LogUtil.info("已处理 " + inUseCount + " 个正在使用的电池");
            DBHelper.closeResources(inUseRs, null);

            // 处理不可用的电池（充电）
            String unavailableQuery = "SELECT * FROM powerbank WHERE status = 'Unavailable'";
            ResultSet unavailableRs = DBHelper.executeQuery(unavailableQuery);
            int unavailableCount = 0;
            while (unavailableRs.next()) {
                unavailableCount++;
                int id = unavailableRs.getInt("id");
                int currentBattery = unavailableRs.getInt("battery_level");
                int newBattery = Math.min(100, currentBattery + BATTERY_INCREASE_RATE);
                
                String updateQuery;
                if (newBattery >= BATTERY_FULL_THRESHOLD) {
                    updateQuery = "UPDATE powerbank SET battery_level = ?, status = 'Available' WHERE id = ?";
                } else {
                    updateQuery = "UPDATE powerbank SET battery_level = ? WHERE id = ?";
                }
                DBHelper.executeUpdate(updateQuery, newBattery, id);
                
                LogUtil.info(String.format("已更新电池ID %d 的电量：%d -> %d", id, currentBattery, newBattery));
            }
            LogUtil.info("已处理 " + unavailableCount + " 个正在充电的电池");
            DBHelper.closeResources(unavailableRs, null);

            LogUtil.info("电池状态检查完成");
        } catch (SQLException e) {
            LogUtil.error("更新电池状态时发生错误", e);
        }
    }
}
