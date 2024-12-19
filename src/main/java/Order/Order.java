package Order;

import User.User;
import util.DBHelper;
import util.LogUtil;
import util.Transaction;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单类
 * Order Class
 * 管理移动电源租赁订单的基本信息和数据库操作
 */
public class Order {
    // 基本属性
    private int id;                     // 订单ID
    private int userId;                 // 用户ID
    private int powerbankId;           // 移动电源ID
    private String brand;              // 移动电源品牌
    private int rentalDurationHours;   // 租赁时长（小时）
    private double totalCost;          // 总费用
    private String orderCode;          // 订单编号
    private Timestamp rentalStartTime; // 租赁开始时间
    private Timestamp returnTime;      // 归还时间
    private double deposit;            // 押金

    /**
     * 默认构造函数
     */
    public Order() {}

    /**
     * 带参数的构造函数
     * @param id 订单ID
     * @param userId 用户ID
     * @param powerbankId 移动电源ID
     * @param brand 移动电源品牌
     * @param rentalDurationHours 租赁时长
     * @param totalCost 总费用
     * @param orderCode 订单编号
     * @param rentalStartTime 租赁开始时间
     * @param returnTime 归还时间
     * @param deposit 押金
     */
    public Order(int id, int userId, int powerbankId, String brand, int rentalDurationHours, 
                double totalCost, String orderCode, Timestamp rentalStartTime, Timestamp returnTime, double deposit) {
        this.id = id;
        this.userId = userId;
        this.powerbankId = powerbankId;
        this.brand = brand;
        this.rentalDurationHours = rentalDurationHours;
        this.totalCost = totalCost;
        this.orderCode = orderCode;
        this.rentalStartTime = rentalStartTime;
        this.returnTime = returnTime;
        this.deposit = deposit;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public int getPowerbankId() { return powerbankId; }
    public void setPowerbankId(int powerbankId) { this.powerbankId = powerbankId; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public int getRentalDurationHours() { return rentalDurationHours; }
    public void setRentalDurationHours(int rentalDurationHours) { this.rentalDurationHours = rentalDurationHours; }
    public double getTotalCost() { return totalCost; }
    public void setTotalCost(double totalCost) { this.totalCost = totalCost; }
    public String getOrderCode() { return orderCode; }
    public void setOrderCode(String orderCode) { this.orderCode = orderCode; }
    public Timestamp getRentalStartTime() { return rentalStartTime; }
    public void setRentalStartTime(Timestamp rentalStartTime) { this.rentalStartTime = rentalStartTime; }
    public Timestamp getReturnTime() { return returnTime; }
    public void setReturnTime(Timestamp returnTime) { this.returnTime = returnTime; }
    public double getDeposit() { return deposit; }
    public void setDeposit(double deposit) { this.deposit = deposit; }

    /**
     * 获取用户当前租借的订单
     * @param userId 用户ID
     * @return 租借订单列表
     * @throws SQLException SQL异常
     */
    public static List<Order> getCurrentRentals(int userId) throws SQLException {
        List<Order> rentals = new ArrayList<>();
        String sql = "SELECT o.* " +
                    "FROM `Order` o " +
                    "WHERE o.user_id = ? " +
                    "AND o.rental_duration_hours = 0 " +
                    "ORDER BY o.rental_start_time DESC";
        
        LogUtil.info("正在查询用户ID: " + userId + " 的当前租借记录");
        var rs = DBHelper.executeQuery(sql, userId);
        while (rs.next()) {
            Order order = new Order();
            order.setId(rs.getInt("id"));
            order.setUserId(rs.getInt("user_id"));
            order.setPowerbankId(rs.getInt("powerbank_id"));
            order.setBrand(rs.getString("brand"));
            order.setRentalStartTime(rs.getTimestamp("rental_start_time"));
            order.setRentalDurationHours(rs.getInt("rental_duration_hours"));
            order.setTotalCost(rs.getDouble("total_cost"));
            order.setOrderCode(rs.getString("order_code"));
            order.setDeposit(rs.getDouble("deposit"));
            rentals.add(order);
            LogUtil.debug("找到租借记录 - 订单ID: " + order.getId() + ", 充电宝ID: " + order.getPowerbankId());
        }
        DBHelper.closeResources(rs, null);
        return rentals;
    }

    /**
     * 创建租借订单
     * @param userId 用户ID
     * @param powerbankId 移动电源ID
     * @param brand 移动电源品牌
     * @return 是否创建成功
     * @throws SQLException SQL异常
     */
    public static boolean createRental(int userId, int powerbankId, String brand) throws SQLException {
        LogUtil.info("开始创建租借订单 - 用户ID: " + userId + ", 充电宝ID: " + powerbankId + ", 品牌: " + brand);

        // 开启事务
        Connection conn = DBHelper.getConnection();
        conn.setAutoCommit(false);
        try {
            // 检查用户会员状态和余额
            String checkUserSql = "SELECT balance, status FROM user WHERE id = ? FOR UPDATE";
            PreparedStatement checkUserStmt = conn.prepareStatement(checkUserSql);
            checkUserStmt.setInt(1, userId);
            ResultSet userRs = checkUserStmt.executeQuery();

            if (!userRs.next()) {
                LogUtil.error("租借创建失败 - 用户不存在");
                return false;
            }

            double balance = userRs.getDouble("balance");
            String status = userRs.getString("status");
            
            // 计算所需金额（押金）
            double requiredAmount = Transaction.calculateDeposit(status);
            LogUtil.debug("所需押金金额: " + requiredAmount);
            
            if (!Transaction.isBalanceSufficient(balance, requiredAmount)) {
                LogUtil.warning("租借创建失败 - 余额不足");
                return false;
            }

            // 检查电源是否可用
            String checkSql = "SELECT status FROM powerbank WHERE id = ? FOR UPDATE";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setInt(1, powerbankId);
            ResultSet rs = checkStmt.executeQuery();
            
            if (!rs.next() || !"Available".equals(rs.getString("status"))) {
                LogUtil.warning("租借创建失败 - 充电宝不可用");
                return false;
            }
            
            // 扣除押金
            if (!Transaction.isVipOrSvip(status)) {
                String updateBalanceSql = "UPDATE user SET balance = balance - ? WHERE id = ?";
                PreparedStatement updateBalanceStmt = conn.prepareStatement(updateBalanceSql);
                updateBalanceStmt.setDouble(1, requiredAmount);
                updateBalanceStmt.setInt(2, userId);
                int updateResult = updateBalanceStmt.executeUpdate();
                if (updateResult > 0) {
                    LogUtil.info("已扣除押金: " + requiredAmount + ", 新余额: " + (balance - requiredAmount));
                }
            } else {
                LogUtil.info("VIP/SVIP用户，无需押金");
            }

            // 创建订单
            String createOrderSql = "INSERT INTO `Order` (user_id, powerbank_id, brand, rental_start_time, deposit) VALUES (?, ?, ?, NOW(), ?)";
            PreparedStatement orderStmt = conn.prepareStatement(createOrderSql);
            orderStmt.setInt(1, userId);
            orderStmt.setInt(2, powerbankId);
            orderStmt.setString(3, brand);
            orderStmt.setDouble(4, requiredAmount);
            int orderResult = orderStmt.executeUpdate();

            if (orderResult > 0) {
                // 验证订单是否正确创建
                String verifyOrderSql = "SELECT * FROM `Order` WHERE id = LAST_INSERT_ID()";
                PreparedStatement verifyStmt = conn.prepareStatement(verifyOrderSql);
                ResultSet verifyRs = verifyStmt.executeQuery();
                if (verifyRs.next()) {
                    LogUtil.debug("订单创建已验证 - 押金: " + verifyRs.getDouble("deposit"));
                }
                // 更新电源状态
                String updatePowerBankSql = "UPDATE powerbank SET status = 'InUse' WHERE id = ?";
                PreparedStatement powerBankStmt = conn.prepareStatement(updatePowerBankSql);
                powerBankStmt.setInt(1, powerbankId);
                int powerBankResult = powerBankStmt.executeUpdate();

                if (powerBankResult > 0) {
                    conn.commit();
                    LogUtil.info("租借订单创建成功");
                    return true;
                } else {
                    LogUtil.error("租借创建失败 - 无法更新充电宝状态");
                }
            } else {
                LogUtil.error("租借创建失败 - 无法插入订单记录");
            }
            
            conn.rollback();
            return false;
        } catch (SQLException e) {
            LogUtil.error("租借创建过程中发生异常，正在回滚", e);
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
            LogUtil.info("租借创建流程结束");
        }
    }

    /**
     * 归还订单
     * @param orderId 订单ID
     * @param powerbankId 移动电源ID
     * @param hours 租赁时长（小时）
     * @param totalCost 总费用
     * @param orderCode 订单编号
     * @return 是否归还成功
     * @throws SQLException SQL异常
     */
    public static boolean returnRental(int orderId, int powerbankId, long hours, double totalCost, String orderCode) throws SQLException {
        LogUtil.info("开始归还流程 - 订单ID: " + orderId + ", 充电宝ID: " + powerbankId + ", 时长: " + hours + " 小时");

        // 开启事务
        Connection conn = DBHelper.getConnection();
        conn.setAutoCommit(false);
        try {
            // 获取管理员账户
            User admin = User.getAdminUser();
            if (admin == null) {
                throw new SQLException("无法找到管理员账户");
            }

            // 获取订单和用户信息
            String getOrderSql = "SELECT o.user_id, o.deposit, u.status, u.balance " +
                               "FROM `Order` o " +
                               "JOIN user u ON o.user_id = u.id " +
                               "WHERE o.id = ? FOR UPDATE";
            PreparedStatement getOrderStmt = conn.prepareStatement(getOrderSql);
            getOrderStmt.setInt(1, orderId);
            ResultSet orderRs = getOrderStmt.executeQuery();

            if (!orderRs.next()) {
                LogUtil.warning("归还失败 - 订单不存在");
                return false;
            }

            int userId = orderRs.getInt("user_id");
            double deposit = orderRs.getDouble("deposit");
            String userStatus = orderRs.getString("status");
            double currentBalance = orderRs.getDouble("balance");

            // 计算实际费用（应用会员折扣）
            double actualCost = Transaction.calculateActualCost(totalCost, userStatus);
            double newBalance = Transaction.calculateReturnBalance(currentBalance, deposit, actualCost);

            LogUtil.debug("用户当前余额: " + currentBalance + ", 押金: " + deposit + 
                         ", 原始费用: " + totalCost + ", 折扣后费用: " + actualCost + 
                         ", 归还后余额: " + newBalance);

            // 更新用户余额（退还押金并扣除折扣后的租金）
            String updateUserSql = "UPDATE user SET balance = balance + ? - ? WHERE id = ?";
            PreparedStatement userStmt = conn.prepareStatement(updateUserSql);
            userStmt.setDouble(1, deposit);
            userStmt.setDouble(2, actualCost);
            userStmt.setInt(3, userId);
            int updateResult = userStmt.executeUpdate();
            
            if (updateResult > 0) {
                LogUtil.info("已退还押金: " + deposit + ", 已扣除租金: " + actualCost + 
                            ", 新余额: " + (currentBalance + deposit - actualCost));
            }

            // 更新管理员余额（增加折扣后的租金收入）
            String updateAdminSql = "UPDATE user SET balance = balance + ? WHERE id = ?";
            PreparedStatement adminStmt = conn.prepareStatement(updateAdminSql);
            adminStmt.setDouble(1, actualCost);
            adminStmt.setInt(2, admin.getId());
            adminStmt.executeUpdate();

            // 更新订单（记录折扣后的实际费用）
            String updateOrderSql = "UPDATE `Order` SET " +
                                 "rental_duration_hours = ?, " +
                                 "total_cost = ?, " +
                                 "order_code = ?, " +
                                 "return_time = NOW() " +
                                 "WHERE id = ?";
            PreparedStatement orderStmt = conn.prepareStatement(updateOrderSql);
            orderStmt.setLong(1, hours);
            orderStmt.setDouble(2, actualCost);  // 保存折扣后的实际费用
            orderStmt.setString(3, orderCode);
            orderStmt.setInt(4, orderId);
            orderStmt.executeUpdate();

            // 更新电源状态
            String updatePowerBankSql = "UPDATE powerbank SET status = 'Available' WHERE id = ?";
            PreparedStatement powerBankStmt = conn.prepareStatement(updatePowerBankSql);
            powerBankStmt.setInt(1, powerbankId);
            powerBankStmt.executeUpdate();

            conn.commit();
            LogUtil.info("归还成功 - 订单ID: " + orderId + ", 充电宝ID: " + powerbankId);
            return true;
        } catch (SQLException e) {
            LogUtil.error("归还失败，正在回滚", e);
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    /**
     * 获取租借信息（通过电源ID）
     * @param powerbankId 移动电源ID
     * @return 租借订单
     * @throws SQLException SQL异常
     */
    public static Order getRentalByPowerbankId(int powerbankId) throws SQLException {
        String sql = "SELECT o.* " +
                    "FROM `Order` o " +
                    "WHERE o.powerbank_id = ? " +
                    "AND o.rental_duration_hours = 0 " +
                    "ORDER BY o.rental_start_time DESC LIMIT 1";
        
        System.out.println("查询租借信息 - SQL: " + sql.replace("?", String.valueOf(powerbankId)));
        var rs = DBHelper.executeQuery(sql, powerbankId);
        if (rs.next()) {
            Order order = new Order();
            order.setId(rs.getInt("id"));
            order.setUserId(rs.getInt("user_id"));
            order.setPowerbankId(rs.getInt("powerbank_id"));
            order.setBrand(rs.getString("brand"));
            order.setRentalStartTime(rs.getTimestamp("rental_start_time"));
            order.setRentalDurationHours(rs.getInt("rental_duration_hours"));
            order.setTotalCost(rs.getDouble("total_cost"));
            order.setOrderCode(rs.getString("order_code"));
            order.setDeposit(rs.getDouble("deposit"));
            System.out.println("找到租借订单 - 订单ID: " + order.getId() + ", 用户ID: " + order.getUserId());
            DBHelper.closeResources(rs, null);
            return order;
        }
        System.out.println("未找到租借信息");
        DBHelper.closeResources(rs, null);
        return null;
    }

    /**
     * 获取订单信息（通过订单ID）
     * @param orderId 订单ID
     * @return 订单信息
     * @throws SQLException SQL异常
     */
    public static Order getOrderById(int orderId) throws SQLException {
        String sql = "SELECT o.*, p.rental_price_per_hour, p.battery_level " +
                    "FROM `Order` o " +
                    "JOIN powerbank p ON o.powerbank_id = p.id " +
                    "WHERE o.id = ? AND o.rental_duration_hours = 0";
        
        System.out.println("执行查询订单信息 - 订单ID: " + orderId);
        var rs = DBHelper.executeQuery(sql, orderId);
        
        if (rs.next()) {
            Order order = new Order();
            order.setId(rs.getInt("id"));
            order.setUserId(rs.getInt("user_id"));
            order.setPowerbankId(rs.getInt("powerbank_id"));
            order.setBrand(rs.getString("brand"));
            order.setRentalStartTime(rs.getTimestamp("rental_start_time"));
            order.setRentalDurationHours(rs.getInt("rental_duration_hours"));
            order.setTotalCost(rs.getDouble("total_cost"));
            order.setOrderCode(rs.getString("order_code"));
            order.setDeposit(rs.getDouble("deposit"));
            System.out.println("找到订单信息 - 电源ID: " + order.getPowerbankId());
            DBHelper.closeResources(rs, null);
            return order;
        }
        System.out.println("未找到订单信息");
        DBHelper.closeResources(rs, null);
        return null;
    }

    /**
     * 获取用户的历史订单
     * @param userId 用户ID
     * @return 历史订单列表
     * @throws SQLException SQL异常
     */
    public static List<Order> getOrderHistory(int userId) throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM `Order` WHERE user_id = ? ORDER BY rental_start_time DESC";
        
        var rs = DBHelper.executeQuery(sql, userId);
        while (rs.next()) {
            Order order = new Order();
            order.setId(rs.getInt("id"));
            order.setUserId(rs.getInt("user_id"));
            order.setPowerbankId(rs.getInt("powerbank_id"));
            order.setBrand(rs.getString("brand"));
            order.setRentalStartTime(rs.getTimestamp("rental_start_time"));
            order.setRentalDurationHours(rs.getInt("rental_duration_hours"));
            order.setTotalCost(rs.getDouble("total_cost"));
            order.setOrderCode(rs.getString("order_code"));
            order.setReturnTime(rs.getTimestamp("return_time"));
            order.setDeposit(rs.getDouble("deposit"));
            orders.add(order);
        }
        DBHelper.closeResources(rs, null);
        return orders;
    }

    /**
     * 搜索订单
     * @param userId 用户ID
     * @param keyword 搜索关键词
     * @return 搜索结果订单列表
     * @throws SQLException SQL异常
     */
    public static List<Order> searchOrders(int userId, String keyword) throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM `Order` WHERE user_id = ? AND " +
                    "(order_code LIKE ? OR brand LIKE ? OR " +
                    "CAST(powerbank_id AS CHAR) LIKE ? OR " +
                    "CAST(total_cost AS CHAR) LIKE ?) " +
                    "ORDER BY rental_start_time DESC";
        
        String searchPattern = "%" + keyword + "%";
        var rs = DBHelper.executeQuery(sql, userId, searchPattern, searchPattern, searchPattern, searchPattern);
        while (rs.next()) {
            Order order = new Order();
            order.setId(rs.getInt("id"));
            order.setUserId(rs.getInt("user_id"));
            order.setPowerbankId(rs.getInt("powerbank_id"));
            order.setBrand(rs.getString("brand"));
            order.setRentalStartTime(rs.getTimestamp("rental_start_time"));
            order.setRentalDurationHours(rs.getInt("rental_duration_hours"));
            order.setTotalCost(rs.getDouble("total_cost"));
            order.setOrderCode(rs.getString("order_code"));
            order.setReturnTime(rs.getTimestamp("return_time"));
            order.setDeposit(rs.getDouble("deposit"));
            orders.add(order);
        }
        DBHelper.closeResources(rs, null);
        return orders;
    }

    /**
     * 删除订单
     * @param orderId 订单ID
     * @return 是否删除成功
     * @throws SQLException SQL异常
     */
    public static boolean deleteOrder(int orderId) throws SQLException {
        String sql = "DELETE FROM `Order` WHERE id = ?";
        return DBHelper.executeUpdate(sql, orderId) > 0;
    }

    /**
     * 通过订单号查找订单
     * @param orderCode 订单编号
     * @return 订单信息
     * @throws SQLException SQL异常
     */
    public static Order findByOrderCode(String orderCode) throws SQLException {
        String sql = "SELECT * FROM `Order` WHERE order_code = ?";
        var rs = DBHelper.executeQuery(sql, orderCode);
        if (rs.next()) {
            Order order = new Order();
            order.setId(rs.getInt("id"));
            order.setUserId(rs.getInt("user_id"));
            order.setPowerbankId(rs.getInt("powerbank_id"));
            order.setBrand(rs.getString("brand"));
            order.setRentalStartTime(rs.getTimestamp("rental_start_time"));
            order.setRentalDurationHours(rs.getInt("rental_duration_hours"));
            order.setTotalCost(rs.getDouble("total_cost"));
            order.setOrderCode(rs.getString("order_code"));
            order.setReturnTime(rs.getTimestamp("return_time"));
            order.setDeposit(rs.getDouble("deposit"));
            DBHelper.closeResources(rs, null);
            return order;
        }
        DBHelper.closeResources(rs, null);
        return null;
    }
}
