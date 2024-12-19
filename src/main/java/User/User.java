package User;

import util.DBHelper;
import util.LogUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户类
 * User Class
 * 管理用户的基本信息、会员状态和数据库操作
 */
public class User {

    // 基本属性
    private int id;                 // 用户ID
    private String username;        // 用户名
    private String phone;          // 手机号
    private String status;         // 用户状态（普通用户/VIP/SVIP/管理员）
    private String password;       // 密码
    private String confirmPassword; // 确认密码
    private double balance;        // 账户余额
    private Timestamp expiresTime; // 会员过期时间
    private String avatar;         // 用户头像路径

    /**
     * 默认构造函数
     */
    public User() {}

    /**
     * 带基本参数的构造函数
     * @param id 用户ID
     * @param username 用户名
     * @param phone 手机号
     * @param status 用户状态
     * @param password 密码
     * @param balance 账户余额
     * @param expiresTime 会员过期时间
     */
    public User(int id, String username, String phone, String status, String password, double balance, Timestamp expiresTime) {
        this.id = id;
        this.username = username;
        this.phone = phone;
        this.status = status;
        this.password = password;
        this.balance = balance;
        this.expiresTime = expiresTime;
        this.avatar = "src/source/defualt.png"; // 默认头像
    }

    /**
     * 带头像的构造函数
     * @param id 用户ID
     * @param username 用户名
     * @param phone 手机号
     * @param status 用户状态
     * @param password 密码
     * @param balance 账户余额
     * @param expiresTime 会员过期时间
     * @param avatar 头像路径
     */
    public User(int id, String username, String phone, String status, String password, double balance, Timestamp expiresTime, String avatar) {
        this(id, username, phone, status, password, balance, expiresTime);
        if (avatar != null && !avatar.isEmpty()) {
            this.avatar = avatar;
        }
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }
    public Timestamp getExpiresTime() { return expiresTime; }
    public void setExpiresTime(Timestamp expiresTime) { this.expiresTime = expiresTime; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    /**
     * 用户信息字符串表示
     * @return 用户信息字符串
     */
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", balance=" + balance +
                '}';
    }

    /**
     * 检查用户是否为管理员
     * @param username 用户名
     * @param password 密码
     * @return 是否为管理员
     */
    public boolean checkUser_IsAdmin(String username, String password) {
        try {
            String query = "SELECT status FROM User WHERE username = ? AND password = ?";
            ResultSet rs = DBHelper.executeQuery(query, username, password);

            boolean isAdmin = rs.next() && rs.getString("status").equals("Admin");
            LogUtil.info("检查用户是否为管理员: " + username + " - 结果: " + isAdmin);

            DBHelper.closeResources(rs, null);
            return isAdmin;
        } catch (SQLException e) {
            LogUtil.error("检查用户管理员权限时出错: " + username, e);
            return false;
        }
    }

    /**
     * 检查用户是否存在于数据库
     * @param username 用户名
     * @param password 密码
     * @return 是否存在
     */
    public boolean checkUser_IsDataBase(String username, String password) {
        try {
            String query = "SELECT * FROM User WHERE username = ? AND password = ?";
            ResultSet rs = DBHelper.executeQuery(query, username, password);
            
            boolean exists = rs.next();
            LogUtil.info("用户认证尝试: " + username + " - 成功: " + exists);
            
            DBHelper.closeResources(rs, null);
            return exists;
        } catch (SQLException e) {
            LogUtil.error("用户认证过程中出错: " + username, e);
            return false;
        }
    }

    /**
     * 用户注册
     * @param username 用户名
     * @param phone 手机号
     * @param password 密码
     * @param confirmPassword 确认密码
     * @return 是否注册成功
     */
    public boolean checkUser_register(String username, String phone, String password, String confirmPassword) {
        // 检查用户名长度
        if (username == null || username.length() < 3 || username.length() > 20) {
            System.out.println("用户名长度必须在3-20个字符之间");
            return false;
        }
        if (phone == null || phone.length() < 11 || phone.length() > 11) {
            System.out.println("手机号长度必须在11-11个字符之间");
            return false;
        }
        // 检查密码长度
        if (password == null || password.length() < 6 || password.length() > 20) {
            System.out.println("密码长度必须在6-20个字符之间");
            return false;
        }

        // 检查两次密码是否一致
        if (!password.equals(confirmPassword)) {
            System.out.println("两次输入的密码不一致");
            return false;
        }

        try {
            // 检查手机号是否已存在
            String checkQuery = "SELECT username FROM User WHERE phone = ?";
            ResultSet rs = DBHelper.executeQuery(checkQuery, phone);
            if (rs.next()) {
                LogUtil.warning("注册失败: 手机号已存在 - " + phone);
                DBHelper.closeResources(rs, null);
                return false;
            }
            DBHelper.closeResources(rs, null);

            // 插入新用户
            String insertQuery = "INSERT INTO User (username, phone, password, balance) VALUES (?, ?, ?, 0.0)";
            int result = DBHelper.executeUpdate(insertQuery, username, phone, password);
            
            if (result > 0) {
                LogUtil.info("用户注册成功: " + username);
                return true;
            } else {
                LogUtil.warning("用户注册失败: " + username);
                return false;
            }
        } catch (SQLException e) {
            LogUtil.error("用户注册过程中出错: " + username, e);
            return false;
        }
    }

    /**
     * 获取所有用户
     * @return 用户列表
     * @throws SQLException SQL异常
     */
    public static List<User> getAllUsers() throws SQLException {
        String sql = "SELECT * FROM user";
        ResultSet rs = DBHelper.executeQuery(sql);
        return resultSetToUsers(rs);
    }

    /**
     * 搜索用户
     * @param keyword 搜索关键词
     * @return 用户列表
     * @throws SQLException SQL异常
     */
    public static List<User> searchUsers(String keyword) throws SQLException {
        String sql = "SELECT * FROM user WHERE username LIKE ? OR phone LIKE ?";
        String searchPattern = "%" + keyword + "%";
        ResultSet rs = DBHelper.executeQuery(sql, searchPattern, searchPattern);
        return resultSetToUsers(rs);
    }

    /**
     * 根据状态筛选用户
     * @param status 用户状态
     * @return 用户列表
     * @throws SQLException SQL异常
     */
    public static List<User> filterUsersByStatus(String status) throws SQLException {
        String sql = "SELECT * FROM user WHERE status = ?";
        ResultSet rs = DBHelper.executeQuery(sql, status);
        return resultSetToUsers(rs);
    }

    /**
     * 根据用户名和手机号查找用户
     * @param username 用户名
     * @param phone 手机号
     * @return 用户对象
     * @throws SQLException SQL异常
     */
    public static User findByUsernameAndPhone(String username, String phone) throws SQLException {
        String sql = "SELECT * FROM user WHERE username = ? AND phone = ?";
        try (PreparedStatement stmt = DBHelper.getConnection().prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, phone);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("phone"),
                    rs.getString("status"),
                    rs.getString("password"),
                    rs.getDouble("balance"),
                    rs.getTimestamp("expiresTime"),
                    rs.getString("avatar")  // 添加头像路径
                );
            }
            return null;
        }
    }

    /**
     * 删除用户
     * @return 是否删除成功
     * @throws SQLException SQL异常
     */
    public boolean delete() throws SQLException {
        String sql = "DELETE FROM user WHERE id = ?";
        try (PreparedStatement stmt = DBHelper.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, this.id);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * 保存用户信息
     * @return 是否保存成功
     * @throws SQLException SQL异常
     */
    public boolean save() throws SQLException {
        if (this.id == 0) {
            // Insert new user
            String sql = "INSERT INTO user (username, phone, status, password, balance, expiresTime, avatar) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = DBHelper.getConnection().prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, this.username);
                stmt.setString(2, this.phone);
                stmt.setString(3, this.status);
                stmt.setString(4, this.password);
                stmt.setDouble(5, this.balance);
                stmt.setTimestamp(6, this.expiresTime);
                stmt.setString(7, this.avatar);
                
                int affectedRows = stmt.executeUpdate();
                if (affectedRows == 0) {
                    return false;
                }

                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        this.id = generatedKeys.getInt(1);
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        } else {
            // Update existing user
            String sql = "UPDATE user SET username = ?, phone = ?, status = ?, password = ?, balance = ?, expiresTime = ?, avatar = ? WHERE id = ?";
            try (PreparedStatement stmt = DBHelper.getConnection().prepareStatement(sql)) {
                stmt.setString(1, this.username);
                stmt.setString(2, this.phone);
                stmt.setString(3, this.status);
                stmt.setString(4, this.password);
                stmt.setDouble(5, this.balance);
                stmt.setTimestamp(6, this.expiresTime);
                stmt.setString(7, this.avatar);
                stmt.setInt(8, this.id);
                
                return stmt.executeUpdate() > 0;
            }
        }
    }

    /**
     * 更新用户状态
     * @return 是否更新成功
     * @throws SQLException SQL异常
     */
    public boolean updateStatus() throws SQLException {
        String sql = "UPDATE user SET status = ? WHERE id = ?";
        try (PreparedStatement stmt = DBHelper.getConnection().prepareStatement(sql)) {
            stmt.setString(1, this.status);
            stmt.setInt(2, this.id);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * 更新用户过期时间
     * @return 是否更新成功
     * @throws SQLException SQL异常
     */
    public boolean updateExpiresTime() throws SQLException {
        String sql = "UPDATE user SET expiresTime = ? WHERE id = ?";
        try (PreparedStatement stmt = DBHelper.getConnection().prepareStatement(sql)) {
            stmt.setTimestamp(1, this.expiresTime);
            stmt.setInt(2, this.id);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * 更新用户头像
     * @return 是否更新成功
     * @throws SQLException SQL异常
     */
    public boolean updateAvatar() throws SQLException {
        String sql = "UPDATE user SET avatar = ? WHERE id = ?";
        try (PreparedStatement stmt = DBHelper.getConnection().prepareStatement(sql)) {
            stmt.setString(1, this.avatar);
            stmt.setInt(2, this.id);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * ResultSet转换为User列表
     * @param rs ResultSet对象
     * @return 用户列表
     * @throws SQLException SQL异常
     */
    private static List<User> resultSetToUsers(ResultSet rs) throws SQLException {
        List<User> users = new ArrayList<>();
        while (rs.next()) {
            users.add(new User(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("phone"),
                rs.getString("status"),
                rs.getString("password"),
                rs.getDouble("balance"),
                rs.getTimestamp("expiresTime"),
                rs.getString("avatar")  // 添加头像路径
            ));
        }
        return users;
    }

    /**
     * 检查用户是否为VIP
     * @return 是否为VIP
     */
    public boolean isVip() {
        return "VIP".equals(status);
    }

    /**
     * 检查用户是否为SVIP
     * @return 是否为SVIP
     */
    public boolean isSvip() {
        return "SVIP".equals(status);
    }

    /**
     * 检查用户是否为VIP或SVIP
     * @return 是否为VIP或SVIP
     */
    public boolean isVipOrSvip() {
        return isVip() || isSvip();
    }

    /**
     * 刷新用户余额
     * @throws SQLException SQL异常
     */
    public void refreshBalance() throws SQLException {
        String sql = "SELECT balance FROM user WHERE id = ?";
        var rs = DBHelper.executeQuery(sql, this.id);
        if (rs.next()) {
            this.balance = rs.getDouble("balance");
        }
        DBHelper.closeResources(rs, null);
    }

    /**
     * 获取管理员账户
     * @return 管理员账户
     * @throws SQLException SQL异常
     */
    public static User getAdminUser() throws SQLException {
        String sql = "SELECT * FROM User WHERE status = 'Admin' LIMIT 1";
        var rs = DBHelper.executeQuery(sql);
        if (rs.next()) {
            User admin = new User(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("phone"),
                rs.getString("status"),
                rs.getString("password"),
                rs.getDouble("balance"),
                rs.getTimestamp("expiresTime")
            );
            DBHelper.closeResources(rs, null);
            return admin;
        }
        DBHelper.closeResources(rs, null);
        return null;
    }

    /**
     * 更新用户余额
     * @param newBalance 新余额
     * @return 是否更新成功
     * @throws SQLException SQL异常
     */
    public boolean updateBalance(double newBalance) throws SQLException {
        this.balance = newBalance;
        String sql = "UPDATE User SET balance = ? WHERE id = ?";
        return DBHelper.executeUpdate(sql, newBalance, this.id) > 0;
    }

    /**
     * 充值会员
     * @param type 会员类型
     * @param months 会员月数
     * @param cost 充值金额
     * @return 是否充值成功
     * @throws SQLException SQL异常
     */
    public boolean upgradeMembership(String type, int months, double cost) throws SQLException {
        // 开启事务
        Connection conn = DBHelper.getConnection();
        conn.setAutoCommit(false);
        
        try {
            // 检查余额
            if (this.balance < cost) {
                return false;
            }

            // 获取管理员账户
            User admin = getAdminUser();
            if (admin == null) {
                throw new SQLException("无法找到管理员账户");
            }

            // 扣除用户余额
            this.balance -= cost;
            if (!updateBalance(this.balance)) {
                throw new SQLException("更新用户余额失败");
            }

            // 增加管理员余额
            admin.setBalance(admin.getBalance() + cost);
            if (!admin.updateBalance(admin.getBalance())) {
                throw new SQLException("更新管理员余额失败");
            }

            // 更新用户状态和到期时间
            this.status = type;
            this.expiresTime = Timestamp.valueOf(LocalDateTime.now().plusMonths(months));

            if (save()) {
                conn.commit();
                return true;
            } else {
                conn.rollback();
                return false;
            }
        } catch (SQLException ex) {
            conn.rollback();
            throw ex;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    /**
     * 更新用户状态和过期时间
     * @param newStatus 新状态
     * @param newExpiryTime 新过期时间
     * @return 是否更新成功
     * @throws SQLException SQL异常
     */
    public boolean updateStatusAndExpiry(String newStatus, Timestamp newExpiryTime) throws SQLException {
        String sql = "UPDATE User SET status = ?, expiresTime = ? WHERE id = ?";
        try {
            boolean updated = DBHelper.executeUpdate(sql, newStatus, newExpiryTime, this.id) > 0;
            LogUtil.info("已更新用户状态和到期时间 - 用户ID: " + this.id + " - 状态: " + newStatus + ", 到期时间: " + newExpiryTime);
            return updated;
        } catch (SQLException e) {
            LogUtil.error("更新用户状态和到期时间失败 - 用户ID: " + this.id, e);
            throw new SQLException("更新用户状态和到期时间失败: " + e.getMessage());
        }
    }

    /**
     * 从数据库加载用户信息
     * @param username 用户名
     * @return 用户对象
     * @throws SQLException SQL异常
     */
    public static User loadUserByUsername(String username) throws SQLException {
        String query = "SELECT * FROM User WHERE username = ?";
        var rs = util.DBHelper.executeQuery(query, username);
        User user = null;
        if (rs.next()) {
            user = new User(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("phone"),
                rs.getString("status"),
                rs.getString("password"),
                rs.getDouble("balance"),
                rs.getTimestamp("expiresTime")
            );
            user.setAvatar(rs.getString("avatar"));
        }
        util.DBHelper.closeResources(rs, null);
        return user;
    }

    /**
     * 充值余额
     * @param amount 充值金额
     * @return 是否充值成功
     * @throws SQLException SQL异常
     */
    public boolean rechargeBalance(double amount) throws SQLException {
        this.balance += amount;
        String updateSql = "UPDATE User SET balance = ? WHERE id = ?";
        return util.DBHelper.executeUpdate(updateSql, this.balance, this.id) > 0;
    }
}
