import util.DBConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class CreateDataBase implements DBConfig {

    public static void main(String[] args) {
        // 用户表SQL：存储用户信息，包括基本信息、会员状态和余额
        String createUserTable = "CREATE TABLE IF NOT EXISTS User (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +           // 用户唯一标识
                "username VARCHAR(50) NOT NULL UNIQUE, " +        // 用户名，不可重复
                "phone VARCHAR(50) NOT NULL, " +                  // 手机号
                "status ENUM('Common', 'VIP', 'Admin', 'SVIP') NOT NULL DEFAULT 'Common', " +  // 用户身份状态
                "password VARCHAR(50) NOT NULL, " +               // 密码
                "balance DECIMAL(10, 2) DEFAULT 0.00, " +        // 账户余额
                "is_vip BOOLEAN DEFAULT FALSE, " +               // VIP标志
                "is_svip BOOLEAN DEFAULT FALSE, " +              // SVIP标志
                "expiresTime DATETIME DEFAULT NULL, " +          // 会员过期时间
                "avatar VARCHAR(255) DEFAULT NULL" +             // 用户头像路径
                ");";

        // 移动电源表SQL：管理所有移动电源设备的状态和信息
        String createPowerBankTable = "CREATE TABLE IF NOT EXISTS PowerBank (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +          // 设备唯一标识
                "status ENUM('Available', 'InUse', 'Unavailable') NOT NULL DEFAULT 'Available', " +  // 设备当前状态
                "battery_level INT NOT NULL, " +                 // 电池电量
                "rental_price_per_hour DECIMAL(5, 2) NOT NULL DEFAULT 1.50, " +  // 每小时租金
                "brand VARCHAR(500) NOT NULL, " +                // 设备品牌
                "INDEX idx_brand (brand)" +                      // 品牌索引，提高查询效率
                ");";

        // 订单表SQL：记录租赁交易信息
        String createOrderTable = "CREATE TABLE IF NOT EXISTS `Order` (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +          // 订单唯一标识
                "user_id INT NOT NULL, " +                       // 关联用户ID
                "powerbank_id INT NOT NULL, " +                  // 关联移动电源ID
                "brand VARCHAR(500) NOT NULL, " +                // 移动电源品牌
                "rental_duration_hours INT DEFAULT 0, " +        // 租赁时长（小时）
                "total_cost DECIMAL(10, 2) DEFAULT 0.00, " +     // 总费用
                "order_code VARCHAR(50) DEFAULT NULL, " +        // 订单编号
                "rental_start_time DATETIME NOT NULL, " +        // 租赁开始时间
                "return_time DATETIME DEFAULT NULL, " +          // 归还时间
                "deposit DECIMAL(10, 2) DEFAULT 0.00, " +        // 押金金额
                "FOREIGN KEY (user_id) REFERENCES User(id) ON DELETE CASCADE, " +           // 用户外键关联
                "FOREIGN KEY (powerbank_id) REFERENCES PowerBank(id) ON DELETE CASCADE, " + // 设备外键关联
                "FOREIGN KEY (brand) REFERENCES PowerBank(brand) ON DELETE CASCADE" +       // 品牌外键关联
                ");";

        // 尝试创建数据表
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement statement = connection.createStatement()) {

            // 依次执行建表SQL语句
            statement.execute(createUserTable);
            System.out.println("User 表创建成功或已存在。");

            statement.execute(createPowerBankTable);
            System.out.println("PowerBank 表创建成功或已存在。");

            statement.execute(createOrderTable);
            System.out.println("Order 表创建成功或已存在。");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("创建表时出现错误！");
        }
    }
}
