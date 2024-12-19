package util;

import Commodity.Commodity;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBHelper implements DBConfig {
    private static Connection connection = null;

    // 获取数据库连接
    public static Connection getConnection() throws SQLException {
        try {
            if (connection == null || connection.isClosed()) {
                LogUtil.info("正在建立数据库连接...");
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                LogUtil.info("数据库连接建立成功");
            }
            return connection;
        } catch (SQLException e) {
            LogUtil.error("建立数据库连接失败", e);
            throw e;
        }
    }

    // 关闭数据库连接
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                LogUtil.info("数据库连接已关闭");
            }
        } catch (SQLException e) {
            LogUtil.error("关闭数据库连接失败", e);
        }
    }

    // 执行查询操作
    public static ResultSet executeQuery(String sql, Object... params) throws SQLException {
        Connection conn = getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql);
        
        try {
            // 设置参数
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            LogUtil.debug("执行SQL查询: " + sql);
            return pstmt.executeQuery();
        } catch (SQLException e) {
            LogUtil.error("执行SQL查询失败: " + sql, e);
            throw e;
        }
    }

    // 执行更新操作（插入、更新、删除）
    public static int executeUpdate(String sql, Object... params) throws SQLException {
        Connection conn = getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql);
        
        try {
            // 设置参数
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            LogUtil.debug("执行SQL更新: " + sql);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            LogUtil.error("执行SQL更新失败: " + sql, e);
            throw e;
        }
    }

    // 关闭ResultSet和Statement
    public static void closeResources(ResultSet rs, Statement stmt) {
        try {
            if (rs != null) {
                rs.close();
            }
            if (stmt != null) {
                stmt.close();
            }
        } catch (SQLException e) {
            LogUtil.error("关闭数据库资源失败", e);
        }
    }
}
