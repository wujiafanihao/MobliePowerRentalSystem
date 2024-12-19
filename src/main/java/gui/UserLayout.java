package gui;

import Commodity.Commodity;
import Order.Order;
import User.User;
import util.AddTestdata;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class UserLayout extends JFrame {
    private static final int AVATAR_SIZE = 120;
    private static final String DEFAULT_AVATAR_PATH = "src/source/defualt.png";

    private String username;
    private User currentUser;
    private JPanel mainPanel;
    private JPanel leftPanel;
    private JPanel rightPanel;
    private JPanel contentPanel;
    private JLabel avatarLabel;
    private JLabel welcomeLabel;
    private JLabel balanceLabel;
    private JLabel statusLabel;
    private JTable dataTable;
    private DefaultTableModel tableModel;
    private JButton rentButton;
    private JButton rechargeButton;
    private JButton orderHistoryButton;
    private JButton editProfileButton;
    private JButton upgradeButton;
    private JButton svipMonthButton;
    private JButton svipYearButton;
    private JButton vipMonthButton;
    private JButton vipYearButton;
    private JButton logoutButton;
    private boolean isFromAdmin;

    public UserLayout(String username, boolean isFromAdmin) {
        this.username = username;
        this.isFromAdmin = isFromAdmin;
        try {
            // 使用User类的方法加载用户信息
            this.currentUser = User.loadUserByUsername(username);
            if (this.currentUser == null) {
                throw new SQLException("无法加载用户信息");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "加载用户信息失败：" + e.getMessage());
        }
        initializeUI();

        // 加载用户头像
        if (currentUser != null && currentUser.getAvatar() != null) {
            loadAvatar();
        }
    }

    public UserLayout(String username) {
        this(username, false);
    }

    private void initializeUI() {
        setTitle("移动电源租赁系统 - 用户界面");
        setSize(1000, 600);
        setDefaultCloseOperation(isFromAdmin ? JFrame.DISPOSE_ON_CLOSE : JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 创建主面板
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 创建左侧面板
        leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setPreferredSize(new Dimension(250, getHeight()));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 创建右侧面板
        rightPanel = new JPanel(new BorderLayout(10, 10));

        // 初始化左侧面板
        initializeLeftPanel();

        // 初始化右侧面板
        initializeRightPanel();

        // 添加面板到主面板
        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(rightPanel, BorderLayout.CENTER);

        // 添加主面板到窗口
        add(mainPanel);
    }

    private void initializeLeftPanel() {
        // 添加头像
        avatarLabel = new JLabel();
        avatarLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        updateAvatar(DEFAULT_AVATAR_PATH);

        // 添加用户信息
        JPanel userInfoPanel = new JPanel();
        userInfoPanel.setLayout(new BoxLayout(userInfoPanel, BoxLayout.Y_AXIS));
        userInfoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        userInfoPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        welcomeLabel = new JLabel("用户名: " + username);
        balanceLabel = new JLabel("余额: ￥" + String.format("%.2f", currentUser.getBalance()));
        statusLabel = new JLabel("会员状态: " + currentUser.getStatus());

        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        balanceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        userInfoPanel.add(welcomeLabel);
        userInfoPanel.add(Box.createVerticalStrut(5));
        userInfoPanel.add(balanceLabel);
        userInfoPanel.add(Box.createVerticalStrut(5));
        userInfoPanel.add(statusLabel);

        // 添加修改信息按钮
        editProfileButton = createStyledButton("修改信息", new Color(52, 152, 219));
        editProfileButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        editProfileButton.addActionListener(e -> showEditProfileDialog());

        // 添加充值会员按钮
        upgradeButton = createStyledButton("充值会员", new Color(155, 89, 182));
        upgradeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        upgradeButton.addActionListener(e -> showUpgradeMembershipDialog());

        // 添加退出按钮
        logoutButton = createStyledButton("退出登录", new Color(231, 76, 60));
        logoutButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoutButton.addActionListener(e -> handleLogout());

        // 添加组件到左侧面板
        leftPanel.add(avatarLabel);
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(userInfoPanel);
        leftPanel.add(Box.createVerticalStrut(20));
        leftPanel.add(editProfileButton);
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(upgradeButton);
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(logoutButton);
    }

    private void initializeRightPanel() {
        // 创建顶部按钮面板
        JPanel topButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));

        rentButton = createStyledButton("租聘", new Color(52, 152, 219));
        JButton currentRentalsButton = createStyledButton("正在租借", new Color(230, 126, 34));
        rechargeButton = createStyledButton("充值金额", new Color(46, 204, 113));
        orderHistoryButton = createStyledButton("订单历史", new Color(155, 89, 182));

        topButtonPanel.add(rentButton);
        topButtonPanel.add(currentRentalsButton);
        topButtonPanel.add(rechargeButton);
        topButtonPanel.add(orderHistoryButton);

        // 创建内容面板
        contentPanel = new JPanel(new CardLayout());

        // 创建商品表格面板
        JPanel commodityPanel = createCommodityPanel();

        // 创建充值面板
        JPanel rechargePanel = createRechargePanel();

        // 创建当前租借面板
        JPanel currentRentalsPanel = createCurrentRentalsPanel();

        // 添加面板到内容面板
        contentPanel.add(commodityPanel, "commodity");
        contentPanel.add(rechargePanel, "recharge");
        contentPanel.add(currentRentalsPanel, "currentRentals");

        // 添加按钮事件监听器
        rentButton.addActionListener(e -> {
            CardLayout cl = (CardLayout) contentPanel.getLayout();
            cl.show(contentPanel, "commodity");
        });

        rechargeButton.addActionListener(e -> {
            CardLayout cl = (CardLayout) contentPanel.getLayout();
            cl.show(contentPanel, "recharge");
        });

        currentRentalsButton.addActionListener(e -> {
            CardLayout cl = (CardLayout) contentPanel.getLayout();
            loadCurrentRentals(); // 刷新当前租借数据
            cl.show(contentPanel, "currentRentals");
        });

        // 添加订单历史按钮事件监听器
        orderHistoryButton.addActionListener(e -> {
            OrderLayout orderLayout = new OrderLayout(currentUser);
            orderLayout.setVisible(true);
        });

        // 添加组件到右侧面板
        rightPanel.add(topButtonPanel, BorderLayout.NORTH);
        rightPanel.add(contentPanel, BorderLayout.CENTER);
    }

    private JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(120, 40));
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        // 添加鼠标悬停效果
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(backgroundColor.darker());
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(backgroundColor);
            }
        });

        return button;
    }

    private void updateAvatar(String imagePath) {
        try {
            // 更新界面显示
            BufferedImage originalImage = ImageIO.read(new File(imagePath));
            BufferedImage circularImage = new BufferedImage(AVATAR_SIZE, AVATAR_SIZE, BufferedImage.TYPE_INT_ARGB);

            Graphics2D g2d = circularImage.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Ellipse2D.Double circle = new Ellipse2D.Double(0, 0, AVATAR_SIZE, AVATAR_SIZE);
            g2d.setClip(circle);

            g2d.drawImage(originalImage, 0, 0, AVATAR_SIZE, AVATAR_SIZE, null);
            g2d.dispose();

            avatarLabel.setIcon(new ImageIcon(circularImage));

            // 更新数据库中的头像路径
            if (currentUser.updateAvatar()) {
                // 更新成功
                avatarLabel.setIcon(new ImageIcon(circularImage));
            } else {
                throw new SQLException("更新头像失败");
            }

        } catch (IOException | SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "更新头像失败：" + e.getMessage());
        }
    }

    private JPanel createCommodityPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // 创建表格模型
        String[] columnNames = {"ID", "品牌", "电量", "状态", "租金(每小时)", "操作"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // 只有操作列可编辑
            }
        };

        dataTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(dataTable);

        // 加载商品数据
        loadCommodities();

        // 添加租借和归还按钮列
        dataTable.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer());
        dataTable.getColumnModel().getColumn(5).setCellEditor(new ButtonEditor(new JCheckBox()));

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createRechargePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        JTextField amountField = new JTextField(10);
        JButton rechargeButton = createStyledButton("充值", new Color(46, 204, 113));

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("充值金额："), gbc);

        gbc.gridx = 1;
        panel.add(amountField, gbc);

        gbc.gridx = 2;
        panel.add(rechargeButton, gbc);

        rechargeButton.addActionListener(e -> {
            try {
                double amount = Double.parseDouble(amountField.getText());
                if (amount <= 0) {
                    JOptionPane.showMessageDialog(this, "请输入正确的充值金额");
                    return;
                }

                // 使用User类的方法进行充值
                if (currentUser.rechargeBalance(amount)) {
                    // 更新显示
                    balanceLabel.setText("余额: ￥" + String.format("%.2f", currentUser.getBalance()));
                    JOptionPane.showMessageDialog(this, "充值成功！");
                    amountField.setText("");
                } else {
                    JOptionPane.showMessageDialog(this, "充值失败");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "请输入有效的金额");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "充值失败：" + ex.getMessage());
            }
        });

        return panel;
    }

    private JPanel createCurrentRentalsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        String[] columnNames = {"电源ID", "品牌", "电量", "租借时间", "当前费用", "操作"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // 只有操作列可编辑
            }
        };

        JTable table = new JTable(model);
        table.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer());
        table.getColumnModel().getColumn(5).setCellEditor(new ButtonEditor(new JCheckBox()));

        // 设置列宽
        table.getColumnModel().getColumn(0).setPreferredWidth(50);  // 电源ID
        table.getColumnModel().getColumn(1).setPreferredWidth(100); // 品牌
        table.getColumnModel().getColumn(2).setPreferredWidth(50);  // 电量
        table.getColumnModel().getColumn(3).setPreferredWidth(150); // 租借时间
        table.getColumnModel().getColumn(4).setPreferredWidth(100); // 当前费用
        table.getColumnModel().getColumn(5).setPreferredWidth(80);  // 操作

        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void loadCommodities() {
        tableModel.setRowCount(0);
        try {
            List<Commodity> powerBanks = Commodity.getAvailablePowerBanks();
            for (Commodity powerBank : powerBanks) {
                Object[] row = {
                    powerBank.getId(),
                    powerBank.getBrand(),
                    powerBank.getBatteryLevel() + "%",
                    powerBank.getStatus().toString(),
                    String.format("%.2f", powerBank.getRentalPricePerHour()),
                    "租借"
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "加载商品数据失败：" + e.getMessage());
        }
    }

    private void loadCurrentRentals() {
        System.out.println("开始加载当前租借数据 - 用户ID: " + currentUser.getId());
        try {
            List<Order> rentals = Order.getCurrentRentals(currentUser.getId());
            System.out.println("查询到 " + rentals.size() + " 条租借记录");

            DefaultTableModel model = (DefaultTableModel) ((JTable)((JScrollPane)((JPanel)contentPanel.getComponent(2)).getComponent(0)).getViewport().getView()).getModel();
            model.setRowCount(0);

            for (Order rental : rentals) {
                System.out.println("处理租借记录 - 单ID: " + rental.getId() + ", 电源ID: " + rental.getPowerbankId());
                long hours = ChronoUnit.HOURS.between(rental.getRentalStartTime().toLocalDateTime(), LocalDateTime.now());
                if (hours == 0) hours = 1;
                double currentCost = hours * Commodity.getRentalPricePerHour(rental.getPowerbankId());
                double deposit = rental.getDeposit(); // 获取订单的押金金额

                // 获取电源信息
                Commodity powerBank = Commodity.findById(rental.getPowerbankId());
                String batteryLevel = powerBank != null ? powerBank.getBatteryLevel() + "%" : "N/A";

                Object[] row = {
                    rental.getPowerbankId(), // 显示电源ID而不是订单ID
                    rental.getBrand(),
                    batteryLevel,
                    rental.getRentalStartTime().toString(),
                    String.format("￥%.2f", currentCost),
                    "归还"
                };
                model.addRow(row);
            }
        } catch (SQLException e) {
            System.err.println("加载当前租借数据失败 - 错误详情：");
            e.printStackTrace(System.err);
            JOptionPane.showMessageDialog(this, "加载当前租借数据失败：" + e.getMessage());
        }
    }

    private void handleRental(int powerbankId, String brand) {
        System.out.println("开始处理租借 - 用户: " + currentUser.getUsername() + ", 电源ID: " + powerbankId);
        try {
            // 检查电源是否存在
            Commodity powerBank = Commodity.findById(powerbankId);
            if (powerBank == null) {
                System.err.println("租借失败 - 未找到电源ID: " + powerbankId);
                JOptionPane.showMessageDialog(this, "未找到指定的电源。");
                return;
            }

            // 开始租借流程
            System.out.println("开始创建订单...");
            if (Order.createRental(currentUser.getId(), powerbankId, brand)) {
                System.out.println("订单创建成功，更新电源状态...");
                if (powerBank.updateStatus(Commodity.Status.InUse)) {
                    // 更新用户余额显示
                    currentUser.refreshBalance();
                    balanceLabel.setText("余额: ￥" + String.format("%.2f", currentUser.getBalance()));

                    // 显示租借成功信息，包含押金信息
                    double deposit = currentUser.isVipOrSvip() ? 0 : 99.0;
                    String message = String.format("租借成功！\n%s\n请在正在租借中查看详情",
                        deposit > 0 ? "已扣除押金：￥" + deposit : "VIP用户无需支付押金");
                    JOptionPane.showMessageDialog(this, message);

                    loadCurrentRentals();
                    loadCommodities();
                } else {
                    System.err.println("租借失败 - 无法更新电源状态");
                    throw new SQLException("更新电源状态失败");
                }
            } else {
                System.err.println("租借失败 - 无法创建订单");
                throw new SQLException("创建订单失败");
            }
        } catch (SQLException e) {
            System.err.println("租借处理异常:");
            e.printStackTrace(System.err);
            JOptionPane.showMessageDialog(this, "租借失败：" + e.getMessage());
        }
    }

    private void handleReturn(int powerBankId) {
        System.out.println("开始处理归还 - 电源ID: " + powerBankId);
        try {
            Order rental = Order.getRentalByPowerbankId(powerBankId);
            if (rental != null && rental.getUserId() == currentUser.getId()) {
                // 计算租借时长和费用
                long hours = ChronoUnit.HOURS.between(rental.getRentalStartTime().toLocalDateTime(), LocalDateTime.now());
                if (hours == 0) hours = 1; // 最少收取一小时的费用
                double totalCost = hours * Commodity.getRentalPricePerHour(powerBankId);
                double deposit = rental.getDeposit(); // 获取订单的押金金额

                // 生成订单号
                String orderCode = AddTestdata.generateOrderCode(currentUser.getId());

                System.out.println("归还信息 - 订单ID: " + rental.getId() + ", 时长: " + hours + "小时, 费用: " + totalCost);

                // 更新订单和电源状态
                if (Order.returnRental(rental.getId(), powerBankId, hours, totalCost, orderCode)) {
                    // 更新用户余额显示
                    currentUser.refreshBalance();
                    balanceLabel.setText("余额: ￥" + String.format("%.2f", currentUser.getBalance()));

                    // 刷新显示
                    loadCurrentRentals();
                    loadCommodities();

                    // 计算实际费用考虑会员折扣）
                    double actualCost = totalCost;
                    if (currentUser.isSvip()) {
                        actualCost *= 0.5; // SVIP 5折
                    } else if (currentUser.isVip()) {
                        actualCost *= 0.8; // VIP 8折
                    }

                    // 显示详细的费用信息
                    String message = String.format("归还成功！\n" +
                            "租借时长：%d小时\n" +
                            "租金费用：￥%.2f\n" +
                            "%s\n" +  // 会员折扣信息
                            "实际费用：￥%.2f\n" +
                            "%s\n" +  // 押金信息
                            "订单号：%s",
                            hours,
                            totalCost,
                            currentUser.isSvip() ? "SVIP享受5折优惠" : (currentUser.isVip() ? "VIP享受8折优惠" : "普通用户无折扣"),
                            actualCost,
                            deposit > 0 ? String.format("已退还押金：￥%.2f", deposit) : "无押金退还",
                            orderCode);

                    JOptionPane.showMessageDialog(this, message);
                }
            } else {
                System.err.println("未找到租借信息或用户ID不匹配 - 电源ID: " + powerBankId);
                JOptionPane.showMessageDialog(this, "未找到租借信息，请刷新后重试");
            }
        } catch (SQLException e) {
            System.err.println("归还处理异常:");
            e.printStackTrace(System.err);
            JOptionPane.showMessageDialog(this, "归还失败：" + e.getMessage());
        }
    }

    private void showEditProfileDialog() {
        JDialog dialog = new JDialog(this, "修改个人信息", true);
        dialog.setLayout(new GridLayout(6, 2, 5, 5));
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        JTextField usernameField = new JTextField(currentUser.getUsername());
        JTextField phoneField = new JTextField(currentUser.getPhone());
        JPasswordField passwordField = new JPasswordField();
        JButton avatarButton = new JButton("更换头像");

        dialog.add(new JLabel("用户名："));
        dialog.add(usernameField);
        dialog.add(new JLabel("手机号："));
        dialog.add(phoneField);
        dialog.add(new JLabel("新密码："));
        dialog.add(passwordField);
        dialog.add(new JLabel("头像："));
        dialog.add(avatarButton);

        JButton saveButton = new JButton("保存");
        saveButton.addActionListener(e -> {
            try {
                String newUsername = usernameField.getText();
                String newPhone = phoneField.getText();
                String newPassword = new String(passwordField.getPassword());

                if (!newPassword.isEmpty()) {
                    currentUser.setPassword(newPassword);
                }
                currentUser.setUsername(newUsername);
                currentUser.setPhone(newPhone);

                if (currentUser.save()) {
                    JOptionPane.showMessageDialog(dialog, "修改成功！");
                    welcomeLabel.setText("用户名: " + newUsername);
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "修改失败！");
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "保存失败：" + ex.getMessage());
            }
        });

        avatarButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "图片文件", "jpg", "jpeg", "png", "gif"));
            if (fileChooser.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                String path = fileChooser.getSelectedFile().getAbsolutePath();
                try {
                    // 复制头像到项目目录
                    File sourceFile = new File(path);
                    String fileName = "avatar_" + currentUser.getId() + "_" + System.currentTimeMillis() +
                                   path.substring(path.lastIndexOf('.'));
                    String targetPath = "src/source/" + fileName;
                    File targetFile = new File(targetPath);

                    // 确保目标目录存在
                    targetFile.getParentFile().mkdirs();

                    Files.copy(sourceFile.toPath(), targetFile.toPath(),
                             StandardCopyOption.REPLACE_EXISTING);

                    // 更新数据库中的头像路径
                    currentUser.setAvatar(targetPath);
                    if (currentUser.updateAvatar()) {
                        // 更新界面显示
                        updateAvatar(targetPath);
                        JOptionPane.showMessageDialog(dialog, "头像更新成功！");
                    } else {
                        JOptionPane.showMessageDialog(dialog, "头像更新失败！");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "更新头像失败：" + ex.getMessage());
                }
            }
        });

        dialog.add(saveButton);
        dialog.setVisible(true);
    }

    private void showUpgradeMembershipDialog() {
        JDialog dialog = new JDialog(this, "充值会员", true);
        dialog.setLayout(new GridLayout(0, 1, 5, 5));
        dialog.setSize(300, 400);
        dialog.setLocationRelativeTo(this);

        JPanel svipPanel = new JPanel(new GridLayout(0, 1));
        svipPanel.setBorder(BorderFactory.createTitledBorder("SVIP会员"));
        svipMonthButton = new JButton("月费 - ￥30");
        svipYearButton = new JButton("年费 - ￥180");
        svipPanel.add(svipMonthButton);
        svipPanel.add(svipYearButton);

        JPanel vipPanel = new JPanel(new GridLayout(0, 1));
        vipPanel.setBorder(BorderFactory.createTitledBorder("VIP会员"));
        vipMonthButton = new JButton("月费 - ￥25");
        vipYearButton = new JButton("年费 - ￥150");
        vipPanel.add(vipMonthButton);
        vipPanel.add(vipYearButton);

        ActionListener upgradeListener = e -> {
            JButton source = (JButton) e.getSource();
            String type = "";
            int months = 0;
            double cost = 0;

            if (source == svipMonthButton) {
                type = "SVIP";
                months = 1;
                cost = 30;
            } else if (source == svipYearButton) {
                type = "SVIP";
                months = 12;
                cost = 180;
            } else if (source == vipMonthButton) {
                type = "VIP";
                months = 1;
                cost = 25;
            } else if (source == vipYearButton) {
                type = "VIP";
                months = 12;
                cost = 150;
            }

            try {
                if (currentUser.getBalance() < cost) {
                    JOptionPane.showMessageDialog(dialog, "余额不足，请先充值！");
                    return;
                }

                if (currentUser.upgradeMembership(type, months, cost)) {
                    statusLabel.setText("会员状态: " + type);
                    balanceLabel.setText("余额: ￥" + String.format("%.2f", currentUser.getBalance()));
                    JOptionPane.showMessageDialog(dialog, "升级成功！");
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "升级失败！");
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "操作失败：" + ex.getMessage());
            }
        };

        svipMonthButton.addActionListener(upgradeListener);
        svipYearButton.addActionListener(upgradeListener);
        vipMonthButton.addActionListener(upgradeListener);
        vipYearButton.addActionListener(upgradeListener);

        dialog.add(svipPanel);
        dialog.add(vipPanel);
        dialog.setVisible(true);
    }

    private void handleLogout() {
        // 清除自动登录设置
        try {
            File userInfoFile = new File("user.info");
            if (userInfoFile.exists()) {
                // 先读取用户名
                String username = null;
                try (BufferedReader reader = new BufferedReader(new FileReader(userInfoFile))) {
                    username = reader.readLine();
                }

                // 然后再写入文件
                try (PrintWriter writer = new PrintWriter(new FileWriter(userInfoFile))) {
                    writer.println(username != null ? username : ""); // 保留用户名
                    writer.println(""); // 清空密码
                    writer.println("false"); // 取消记住密码
                    writer.println("false"); // 取消自动登录
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 返回登录界面
        this.dispose();
        LoginLayout loginLayout = new LoginLayout();
        loginLayout.setVisible(true);
    }

    // 按钮渲染器
    private class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    // 按钮编辑器
    private class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean isPushed;
        private int currentRow;
        private JTable currentTable;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> {
                if (currentTable != null && currentRow >= 0) {
                    try {
                        int powerBankId = (int) currentTable.getValueAt(currentRow, 0);
                        String buttonText = button.getText();
                        if ("租借".equals(buttonText)) {
                            String brand = (String) currentTable.getValueAt(currentRow, 1);
                            handleRental(powerBankId, brand);
                        } else if ("归还".equals(buttonText)) {
                            handleReturn(powerBankId);
                        }
                    } catch (Exception ex) {
                        System.err.println("处理按钮点击时出错:");
                        ex.printStackTrace(System.err);
                        JOptionPane.showMessageDialog(null, "操作失败：" + ex.getMessage());
                    }
                }
                fireEditingStopped();
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            currentTable = table;
            currentRow = row;
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return label;
        }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
        welcomeLabel.setText("用户名: " + username);
    }

    public void updateUserStatus(String newStatus) {
        statusLabel.setText("会员状态: " + newStatus);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UserLayout userLayout = new UserLayout("username");
            userLayout.setVisible(true);
        });
    }

    // 添加头像加载辅助方法
    private void loadAvatar() {
        try {
            File imageFile = new File(currentUser.getAvatar());
            if (imageFile.exists()) {
                // 读取图像文件
                BufferedImage originalImage = ImageIO.read(imageFile);
                if (originalImage != null) {
                    // 创建一个圆形的 BufferedImage
                    BufferedImage circularImage = new BufferedImage(AVATAR_SIZE, AVATAR_SIZE, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g2d = circularImage.createGraphics();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    // 设置圆形剪裁区域
                    Ellipse2D.Double circle = new Ellipse2D.Double(0, 0, AVATAR_SIZE, AVATAR_SIZE);
                    g2d.setClip(circle);

                    // 绘制原始图像到圆形 BufferedImage
                    g2d.drawImage(originalImage, 0, 0, AVATAR_SIZE, AVATAR_SIZE, null);
                    g2d.dispose();

                    // 将圆形图像设置为头像标签的图标
                    avatarLabel.setIcon(new ImageIcon(circularImage));
                }
            }
        } catch (IOException e) {
            System.err.println("加载头像失败：" + e.getMessage());
        }
    }
}
