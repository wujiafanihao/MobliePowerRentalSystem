package Commodity;

import util.DBHelper;
import util.LogUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Commodity {
    private int id;                         // 移动电源ID
    private Status status;                  // 当前状态
    private int batteryLevel;              // 电池电量
    private double rentalPricePerHour;     // 每小时租金
    private String brand;                   // 品牌

    /**
     * 移动电源状态枚举
     * Available: 可用
     * InUse: 使用中
     * Unavailable: 不可用
     */
    public enum Status {
        Available, InUse, Unavailable
    }

    public Commodity() {}

    public Commodity(Status status, int batteryLevel, double rentalPricePerHour, String brand) {
        this.status = status;
        this.batteryLevel = batteryLevel;
        this.rentalPricePerHour = rentalPricePerHour;
        this.brand = brand;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public int getBatteryLevel() { return batteryLevel; }
    public void setBatteryLevel(int batteryLevel) { this.batteryLevel = batteryLevel; }
    public double getRentalPricePerHour() { return rentalPricePerHour; }
    public void setRentalPricePerHour(double rentalPricePerHour) { this.rentalPricePerHour = rentalPricePerHour; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    /**
     * 获取所有可用的移动电源
     * @return 可用移动电源列表
     * @throws SQLException 数据库操作异常
     */
    public static List<Commodity> getAvailablePowerBanks() throws SQLException {
        List<Commodity> powerBanks = new ArrayList<>();
        String sql = "SELECT * FROM powerbank WHERE status = 'Available'";
        LogUtil.info("查询所有可用的电源");
        var rs = DBHelper.executeQuery(sql);
        while (rs.next()) {
            Commodity powerBank = new Commodity();
            powerBank.setId(rs.getInt("id"));
            powerBank.setBrand(rs.getString("brand"));
            powerBank.setBatteryLevel(rs.getInt("battery_level"));
            powerBank.setStatus(Status.valueOf(rs.getString("status")));
            powerBank.setRentalPricePerHour(rs.getDouble("rental_price_per_hour"));
            powerBanks.add(powerBank);
            LogUtil.debug("找到可用电源 - ID: " + powerBank.getId() + ", 品牌: " + powerBank.getBrand());
        }
        DBHelper.closeResources(rs, null);
        return powerBanks;
    }

    /**
     * 更新移动电源状态
     * @param id 移动电源ID
     * @param status 新状态
     * @return 是否更新成功
     * @throws SQLException 数据库操作异常
     */
    public static boolean updateStatus(int id, Status status) throws SQLException {
        String sql = "UPDATE powerbank SET status = ? WHERE id = ?";
        boolean updated = DBHelper.executeUpdate(sql, status.toString(), id) > 0;
        LogUtil.info("更新电源状态 - ID: " + id + ", 新状态: " + status);
        return updated;
    }

    /**
     * 获取移动电源的小时租金
     * @param id 移动电源ID
     * @return 移动电源的小时租金
     * @throws SQLException 数据库操作异常
     */
    public static double getRentalPricePerHour(int id) throws SQLException {
        String sql = "SELECT rental_price_per_hour FROM powerbank WHERE id = ?";
        var rs = DBHelper.executeQuery(sql, id);
        if (rs.next()) {
            double price = rs.getDouble("rental_price_per_hour");
            LogUtil.info("获取电源小时租金 - ID: " + id + ", 租金: " + price);
            DBHelper.closeResources(rs, null);
            return price;
        }
        DBHelper.closeResources(rs, null);
        LogUtil.warning("未找到指定的电源 - ID: " + id);
        throw new SQLException("未找到指定的电源");
    }

    /**
     * 保存移动电源信息
     * @return 是否保存成功
     * @throws SQLException 数据库操作异常
     */
    public boolean save() throws SQLException {
        if (this.id == 0) {
            // 新增
            String sql = "INSERT INTO powerbank (status, battery_level, rental_price_per_hour, brand) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = DBHelper.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, status.toString());
                stmt.setInt(2, batteryLevel);
                stmt.setDouble(3, rentalPricePerHour);
                stmt.setString(4, brand);
                
                int result = stmt.executeUpdate();
                if (result > 0) {
                    ResultSet generatedKeys = stmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        this.id = generatedKeys.getInt(1);
                    }
                    LogUtil.info("新增商品成功 - ID: " + this.id + ", 品牌: " + this.brand);
                    return true;
                }
                LogUtil.warning("新增商品失败 - 品牌: " + this.brand);
                return false;
            }
        } else {
            // 更新（不修改品牌）
            String sql = "UPDATE powerbank SET status = ?, battery_level = ?, rental_price_per_hour = ? WHERE id = ?";
            try (PreparedStatement stmt = DBHelper.getConnection().prepareStatement(sql)) {
                stmt.setString(1, status.toString());
                stmt.setInt(2, batteryLevel);
                stmt.setDouble(3, rentalPricePerHour);
                stmt.setInt(4, id);
                boolean updated = stmt.executeUpdate() > 0;
                LogUtil.info("更新商品信息 - ID: " + this.id + ", 状态: " + this.status);
                return updated;
            }
        }
    }

    /**
     * 删除移动电源信息
     * @return 是否删除成功
     * @throws SQLException 数据库操作异常
     */
    public boolean delete() throws SQLException {
        if (this.id <= 0) {
            LogUtil.warning("删除商品失败 - 无效的ID: " + this.id);
            return false;
        }
        String sql = "DELETE FROM powerbank WHERE id = ?";
        try (PreparedStatement stmt = DBHelper.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, this.id);
            boolean deleted = stmt.executeUpdate() > 0;
            LogUtil.info("删除商品成功 - ID: " + this.id);
            return deleted;
        }
    }

    /**
     * 获取所有移动电源信息
     * @return 所有移动电源信息列表
     * @throws SQLException 数据库操作异常
     */
    public static List<Commodity> getAllCommodities() throws SQLException {
        List<Commodity> commodities = new ArrayList<>();
        String sql = "SELECT * FROM powerbank";
        var rs = DBHelper.executeQuery(sql);
        while (rs.next()) {
            Commodity commodity = new Commodity();
            commodity.setId(rs.getInt("id"));
            commodity.setBrand(rs.getString("brand"));
            commodity.setBatteryLevel(rs.getInt("battery_level"));
            commodity.setStatus(Status.valueOf(rs.getString("status")));
            commodity.setRentalPricePerHour(rs.getDouble("rental_price_per_hour"));
            commodities.add(commodity);
        }
        DBHelper.closeResources(rs, null);
        return commodities;
    }

    /**
     * 根据条件筛选移动电源信息
     * @param status 状态
     * @param minPrice 最低租金
     * @param maxPrice 最高租金
     * @param minBattery 最低电量
     * @param maxBattery 最高电量
     * @return 筛选后的移动电源信息列表
     * @throws SQLException 数据库操作异常
     */
    public static List<Commodity> filterCommodities(Status status, Double minPrice, Double maxPrice, Integer minBattery, Integer maxBattery) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM powerbank WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (status != null) {
            sql.append(" AND status = ?");
            params.add(status.toString());
        }

        if (minPrice != null) {
            sql.append(" AND rental_price_per_hour >= ?");
            params.add(minPrice);
        }

        if (maxPrice != null) {
            sql.append(" AND rental_price_per_hour <= ?");
            params.add(maxPrice);
        }

        if (minBattery != null) {
            sql.append(" AND battery_level >= ?");
            params.add(minBattery);
        }

        if (maxBattery != null) {
            sql.append(" AND battery_level <= ?");
            params.add(maxBattery);
        }

        try (PreparedStatement stmt = DBHelper.getConnection().prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            ResultSet rs = stmt.executeQuery();
            List<Commodity> commodities = new ArrayList<>();
            while (rs.next()) {
                Commodity commodity = new Commodity();
                commodity.setId(rs.getInt("id"));
                commodity.setBrand(rs.getString("brand"));
                commodity.setBatteryLevel(rs.getInt("battery_level"));
                commodity.setStatus(Status.valueOf(rs.getString("status")));
                commodity.setRentalPricePerHour(rs.getDouble("rental_price_per_hour"));
                commodities.add(commodity);
            }
            return commodities;
        }
    }

    /**
     * 根据品牌和状态查找移动电源信息
     * @param brand 品牌
     * @param status 状态
     * @return 移动电源信息
     * @throws SQLException 数据库操作异常
     */
    public static Commodity findByBrandAndStatus(String brand, Status status) throws SQLException {
        String sql = "SELECT * FROM powerbank WHERE brand = ? AND status = ?";
        try (PreparedStatement stmt = DBHelper.getConnection().prepareStatement(sql)) {
            stmt.setString(1, brand);
            stmt.setString(2, status.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Commodity commodity = new Commodity();
                commodity.setId(rs.getInt("id"));
                commodity.setBrand(rs.getString("brand"));
                commodity.setBatteryLevel(rs.getInt("battery_level"));
                commodity.setStatus(Status.valueOf(rs.getString("status")));
                commodity.setRentalPricePerHour(rs.getDouble("rental_price_per_hour"));
                return commodity;
            }
            return null;
        }
    }

    /**
     * 更新移动电源状态
     * @return 是否更新成功
     * @throws SQLException 数据库操作异常
     */
    public boolean updateStatus() throws SQLException {
        return updateStatus(this.id, this.status);
    }

    /**
     * 根据ID查找移动电源信息
     * @param id 移动电源ID
     * @return 移动电源信息
     * @throws SQLException 数据库操作异常
     */
    public static Commodity findById(int id) throws SQLException {
        String sql = "SELECT * FROM powerbank WHERE id = ?";
        var rs = DBHelper.executeQuery(sql, id);
        if (rs.next()) {
            Commodity powerBank = new Commodity();
            powerBank.setId(rs.getInt("id"));
            powerBank.setBrand(rs.getString("brand"));
            powerBank.setBatteryLevel(rs.getInt("battery_level"));
            powerBank.setStatus(Status.valueOf(rs.getString("status")));
            powerBank.setRentalPricePerHour(rs.getDouble("rental_price_per_hour"));
            DBHelper.closeResources(rs, null);
            return powerBank;
        }
        DBHelper.closeResources(rs, null);
        return null;
    }

    /**
     * 更新移动电源状态
     * @param newStatus 新状态
     * @return 是否更新成功
     * @throws SQLException 数据库操作异常
     */
    public boolean updateStatus(Status newStatus) throws SQLException {
        String sql = "UPDATE powerbank SET status = ? WHERE id = ?";
        return DBHelper.executeUpdate(sql, newStatus.toString(), this.id) > 0;
    }
}
