package util;

import util.DBConfig;
import util.DBHelper;
import java.sql.SQLException;
import java.util.Random;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.sql.Timestamp;
import java.util.stream.IntStream;
import java.util.ArrayList;
import java.util.List;

public class AddTestdata {
    private static final String[] BRANDS = {"美团", "饿了么", "怪兽", "街电"};
    private static final double[] PRICES = {1.5, 2.5, 2.0, 1.0};
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    public static void main(String[] args) {
        try {
            addPowerBankTestData(1000);
            addAdminUser();
            addUserAndOrderTestData(1000);
            System.out.println("Test data added successfully.");
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void addPowerBankTestData(int count) throws SQLException {
        Random random = new Random();
        String sql = "INSERT INTO PowerBank (status, battery_level, rental_price_per_hour, brand) VALUES (?, ?, ?, ?)";

        for (int i = 0; i < count; i++) {
            int batteryLevel = random.nextInt(51) + 50;
            int brandIndex = random.nextInt(BRANDS.length);
            String brand = BRANDS[brandIndex];
            double rentalPrice = PRICES[brandIndex];

            DBHelper.executeUpdate(sql, "Available", batteryLevel, rentalPrice, brand);
        }
    }

    private static void addAdminUser() throws SQLException {
        String sql = "INSERT INTO User (username, phone, status, password, balance, " +
                    "expiresTime, avatar, is_vip, is_svip) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        DBHelper.executeUpdate(sql, 
            "root",                    // username 
            "13798234029",            // phone
            "Admin",                   // status
            "251605",                 // password
            0.0,                      // balance
            null,                     // expiresTime
            "src\\source\\admin.png", // avatar
            false,                    // is_vip
            false                     // is_svip
        );
        System.out.println("Admin user added: root.");
    }

    private static void addUserAndOrderTestData(int count) throws SQLException {
        Random random = new Random();

        String getPowerBankSql = "SELECT id, brand, rental_price_per_hour FROM PowerBank";
        var rs = DBHelper.executeQuery(getPowerBankSql);
        List<PowerBankInfo> powerBanks = new ArrayList<>();
        while (rs.next()) {
            powerBanks.add(new PowerBankInfo(
                rs.getInt("id"),
                rs.getString("brand"),
                rs.getDouble("rental_price_per_hour")
            ));
        }
        DBHelper.closeResources(rs, null);

        if (powerBanks.isEmpty()) {
            System.err.println("No power banks available");
            return;
        }

        IntStream.range(0, count).forEach(i -> {
            try {
                String username = generateRandomUsername(random);
                String phone = generateRandomPhoneNumber(random);
                String status = random.nextInt(3) == 0 ? "Common" : random.nextInt(2) == 0 ? "VIP" : "SVIP";
                String password = String.valueOf(random.nextInt(10000));
                double balance = random.nextDouble() * 100;

                LocalDateTime expiresTime = null;
                boolean isVip = false;
                boolean isSvip = false;

                if ("VIP".equals(status)) {
                    isVip = true;
                    expiresTime = LocalDateTime.now().plusMonths(1);
                } else if ("SVIP".equals(status)) {
                    isSvip = true;
                    expiresTime = LocalDateTime.now().plusMonths(3);
                }

                String insertUserSql = "INSERT INTO User (username, phone, status, password, balance, " +
                                     "expiresTime, avatar, is_vip, is_svip) " +
                                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                int userId = DBHelper.executeUpdate(insertUserSql, 
                    username, 
                    phone, 
                    status, 
                    password, 
                    balance,
                    expiresTime != null ? Timestamp.valueOf(expiresTime) : null, 
                    "src\\source\\default.png",
                    isVip,
                    isSvip
                );

                int numberOfOrders = random.nextInt(4) + 2;
                for (int j = 0; j < numberOfOrders; j++) {
                    PowerBankInfo powerBank = powerBanks.get(random.nextInt(powerBanks.size()));
                    int rentalDurationHours = random.nextInt(23) + 1;
                    double totalCost = rentalDurationHours * powerBank.rentalPrice;
                    String orderCode = generateOrderCode(userId);
                    String rentalStartTime = generateRandomRentalStartTime(random);
                    
                    double deposit = ("VIP".equals(status) || "SVIP".equals(status)) ? 0 : 99.0;

                    String insertOrderSql = "INSERT INTO `Order` (user_id, powerbank_id, brand, " +
                                         "rental_duration_hours, total_cost, order_code, " +
                                         "rental_start_time, deposit) " +
                                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                    DBHelper.executeUpdate(insertOrderSql, 
                        userId, 
                        powerBank.id,
                        powerBank.brand,
                        rentalDurationHours,
                        totalCost,
                        orderCode,
                        rentalStartTime,
                        deposit
                    );
                }

                System.out.println("Added user: " + username + " with status: " + status);
            } catch (SQLException e) {
                System.err.println("Error adding user: " + e.getMessage());
            }
        });
    }

    private static String generateRandomUsername(Random random) {
        StringBuilder sb = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    private static String generateRandomPhoneNumber(Random random) {
        StringBuilder sb = new StringBuilder(11);
        for (int i = 0; i < 11; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    public static String generateOrderCode(int userId) {
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        String uniqueId = String.format("%05d", new Random().nextInt(100000));
        return String.format("ORD%s%04d%s", timestamp, userId % 10000, uniqueId);
    }

    private static String generateRandomRentalStartTime(Random random) {
        long startMillis = System.currentTimeMillis() - 365L * 24 * 60 * 60 * 1000;
        long endMillis = System.currentTimeMillis();
        long randomMillisSinceEpoch = startMillis + (long) (random.nextDouble() * (endMillis - startMillis));

        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(randomMillisSinceEpoch));
    }

    private static class PowerBankInfo {
        final int id;
        final String brand;
        final double rentalPrice;

        PowerBankInfo(int id, String brand, double rentalPrice) {
            this.id = id;
            this.brand = brand;
            this.rentalPrice = rentalPrice;
        }
    }
}
