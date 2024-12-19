package gui;

import Commodity.Commodity;
import Commodity.Commodity.Status;
import User.User;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理员界面类
 * Administrator Interface Class
 * 提供商品管理和用户管理的主要功能界面
 */
public class AdminLayout extends JFrame {
    // UI组件声明
    private JPanel windows = new JPanel();                    // 主窗口面板
    private JPanel topPanel = new JPanel();                   // 顶部面板
    private JPanel buttonPanelContainer = new JPanel();       // 按钮容器面板
    private JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));  // 按钮面板
    
    // 功能按钮定义
    private JButton addButton = createStyledButton("添加", new Color(46, 204, 113));      // 添加按钮（绿色）
    private JButton deleteButton = createStyledButton("删除", new Color(231, 76, 60));    // 删除按钮（红色）
    private JButton commodity = createStyledButton("商品管理", new Color(52, 152, 219));   // 商品管理按钮（蓝色）
    private JButton user = createStyledButton("用户管理", new Color(52, 152, 219));        // 用户管理按钮（蓝色）
    private JButton logoutButton = createStyledButton("退出登录", new Color(231, 76, 60)); // 退出按钮（红色）
    
    // 筛选组件
    private JPanel filterPanel = new JPanel();
    private JComboBox<Status> statusFilter;          // 状态筛选下拉框
    private JComboBox<String> priceFilter;          // 价格筛选下拉框
    private JComboBox<String> brandFilter;          // 品牌筛选下拉框
    private JComboBox<String> batteryFilter;        // 电量筛选下拉框
    private JButton filterButton = createStyledButton("筛选", new Color(155, 89, 182));  // 筛选按钮（紫色）
    
    // 搜索组件
    private JTextField searchField;                 // 搜索输入框
    private JButton searchButton;                   // 搜索按钮
    private JComboBox<String> userStatusFilter;     // 用户状态筛选下拉框
    
    // 数据展示组件
    private JPanel otherPanel = new JPanel();       // 其他面板（包含表格等）
    private JTable dataTable;                       // 数据表格
    private DefaultTableModel tableModel;           // 表格数据模型
    private boolean showingCommodities = true;      // 当前显示状态（商品/用户）

    /**
     * 管理员界面构造函数
     * 初始化界面布局和所有组件
     */
    public AdminLayout() {
        setTitle("移动电源租赁系统 - 管理界面");
        setSize(1000, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);  // 居中显示

        // 设置主面板
        windows.setLayout(new BorderLayout(0, 10));
        windows.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(windows);

        // 设置顶部面板
        topPanel.setLayout(new BorderLayout(10, 0));
        topPanel.setBackground(new Color(236, 240, 241));
        topPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // 创建左侧菜单面板
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftPanel.setBackground(new Color(236, 240, 241));
        leftPanel.add(commodity);
        leftPanel.add(user);
        topPanel.add(leftPanel, BorderLayout.CENTER);

        // 添加顶部面板到主面板
        windows.add(topPanel, BorderLayout.NORTH);

        // 创建一个新的内容面板
        JPanel contentPanel = new JPanel(new BorderLayout(0, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));

        // 设置按钮面板
        buttonPanelContainer.setLayout(new BorderLayout(0, 10));
        buttonPanelContainer.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(logoutButton);
        buttonPanelContainer.add(buttonPanel, BorderLayout.CENTER);

        // 将按钮面板添加到内容面板的顶部
        contentPanel.add(buttonPanelContainer, BorderLayout.NORTH);

        // 设置其他面板（包含表格和筛选器）
        otherPanel.setLayout(new BorderLayout(0, 10));
        otherPanel.setBackground(new Color(236, 240, 241));
        otherPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // 初始化筛选和搜索组件
        initializeFilterComponents();
        initializeSearchComponents();
        showCommodityFilters();

        // 初始化表格并添加滚动面板
        initializeTable();
        JScrollPane scrollPane = new JScrollPane(dataTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199)));
        otherPanel.add(scrollPane, BorderLayout.CENTER);

        // 将其他面板添加到内容面板的中央
        contentPanel.add(otherPanel, BorderLayout.CENTER);

        // 将内容面板添加到主窗口
        windows.add(contentPanel, BorderLayout.CENTER);

        // 添加事件监听器
        commodity.addActionListener(e -> switchToCommodityView());    // 切换到商品视图
        user.addActionListener(e -> switchToUserView());             // 切换到用户视图
        filterButton.addActionListener(e -> applyFilters());         // 应用筛选
        searchButton.addActionListener(e -> searchUsers());          // 搜索用户
        addButton.addActionListener(e -> showAddDialog());          // 显示添加对话框
        deleteButton.addActionListener(e -> deleteSelected());      // 删除选中项
        logoutButton.addActionListener(e -> handleLogout());       // 处理登出

        // 初始显示商品
        showCommodities();
    }

    /**
     * 创建自定义样式按钮
     * @param text 按钮文本
     * @param backgroundColor 按钮背景色
     * @return 样式化的JButton
     */
    private JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        button.setPreferredSize(new Dimension(100, 35));
        
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

    /**
     * 处理用户登出
     * 清除自动登录设置并返回登录界面
     */
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

    /**
     * 创建标准样式按钮的快捷方法
     */
    private JButton createButton(String text) {
        return createStyledButton(text, new Color(52, 152, 219));
    }

    /**
     * 显示添加对话框
     * 根据当前视图显示添加商品或添加用户对话框
     */
    private void showAddDialog() {
        if (showingCommodities) {
            showAddCommodityDialog();
        } else {
            showAddUserDialog();
        }
    }

    /**
     * 显示添加商品对话框
     * 包含品牌、电量、状态和租金设置
     */
    private void showAddCommodityDialog() {
        JDialog dialog = new JDialog(this, "添加商品", true);
        dialog.setLayout(new GridLayout(0, 2, 5, 5));
        dialog.setSize(300, 250);
        dialog.setLocationRelativeTo(this);

        JTextField batteryField = new JTextField();
        JTextField priceField = new JTextField();
        JComboBox<Status> statusBox = new JComboBox<>(Status.values());
        JComboBox<String> brandBox = new JComboBox<>(new String[]{"美团", "怪兽", "街电", "饿了么"});

        dialog.add(new JLabel("品牌："));
        dialog.add(brandBox);
        dialog.add(new JLabel("电量："));
        dialog.add(batteryField);
        dialog.add(new JLabel("状态："));
        dialog.add(statusBox);
        dialog.add(new JLabel("租金(每小时)："));
        dialog.add(priceField);

        JButton confirmButton = new JButton("确认");
        confirmButton.addActionListener(e -> {
            try {
                int battery = Integer.parseInt(batteryField.getText());
                double price = Double.parseDouble(priceField.getText());
                Status status = (Status) statusBox.getSelectedItem();
                String brand = (String) brandBox.getSelectedItem();

                Commodity commodity = new Commodity(status, battery, price, brand);
                if (commodity.save()) {
                    JOptionPane.showMessageDialog(dialog, "添加成功！");
                    dialog.dispose();
                    showCommodities(); // 刷新表格
                } else {
                    JOptionPane.showMessageDialog(dialog, "添加失败！");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "请输入有效的数字！");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "数据库错误：" + ex.getMessage());
            }
        });

        dialog.add(confirmButton);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    /**
     * 显示添加用户对话框
     * 包含用户名、电话、状态和密码设置
     */
    private void showAddUserDialog() {
        JDialog dialog = new JDialog(this, "添加用户", true);
        dialog.setLayout(new GridLayout(7, 2, 5, 5));

        JTextField usernameField = new JTextField();
        JTextField phoneField = new JTextField();
        JComboBox<String> statusBox = new JComboBox<>(new String[]{"Common", "VIP", "SVIP", "Admin"});
        JPasswordField passwordField = new JPasswordField();
        JTextField balanceField = new JTextField("0.0");

        dialog.add(new JLabel("用户名:"));
        dialog.add(usernameField);
        dialog.add(new JLabel("电话:"));
        dialog.add(phoneField);
        dialog.add(new JLabel("状态:"));
        dialog.add(statusBox);
        dialog.add(new JLabel("密码:"));
        dialog.add(passwordField);
        dialog.add(new JLabel("余额:"));
        dialog.add(balanceField);

        JButton confirmButton = new JButton("确认");
        confirmButton.addActionListener(e -> {
            try {
                String username = usernameField.getText();
                String phone = phoneField.getText();
                String status = (String) statusBox.getSelectedItem();
                String password = new String(passwordField.getPassword());
                double balance = Double.parseDouble(balanceField.getText());

                User user = new User(0, username, phone, status, password, balance, null);
                if (user.save()) {
                    JOptionPane.showMessageDialog(dialog, "添加成功！");
                    dialog.dispose();
                    showUsers(); // 刷新表格
                } else {
                    JOptionPane.showMessageDialog(dialog, "添加失败！");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "请输入有效的数字！");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "数据库错误：" + ex.getMessage());
            }
        });

        dialog.add(confirmButton);
        dialog.pack();
        dialog.setSize(400, dialog.getHeight());
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    /**
     * 删除选中项
     * 根据当前视图删除商品或用户
     */
    private void deleteSelected() {
        int[] selectedRows = dataTable.getSelectedRows();
        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(this, "请先选择要删除的项目！");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "确定要删除这" + selectedRows.length + "条记录吗？", "确认删除",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean hasError = false;
                StringBuilder errorMessage = new StringBuilder("以下记录删除失败：\n");

                if (showingCommodities) {
                    for (int selectedRow : selectedRows) {
                        int id = (int) tableModel.getValueAt(selectedRow, 4);
                        Commodity commodity = new Commodity();
                        commodity.setId(id);
                        if (!commodity.delete()) {
                            hasError = true;
                            errorMessage.append("ID: ").append(id).append("\n");
                        }
                    }
                } else {
                    for (int selectedRow : selectedRows) {
                        String username = (String) tableModel.getValueAt(selectedRow, 0);
                        String phone = (String) tableModel.getValueAt(selectedRow, 1);
                        User user = User.findByUsernameAndPhone(username, phone);
                        if (user == null || !user.delete()) {
                            hasError = true;
                            errorMessage.append("用户名: ").append(username)
                                    .append(", 电话: ").append(phone).append("\n");
                        }
                    }
                }

                // 刷新表格显示
                if (showingCommodities) {
                    showCommodities();
                } else {
                    showUsers();
                }

                // 如果有删除失败的记录，显示错误信息
                if (hasError) {
                    JOptionPane.showMessageDialog(this, errorMessage.toString(),
                            "部分删除失败", JOptionPane.WARNING_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "删除成功！");
                }

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "删除失败: " + ex.getMessage());
            }
        }
    }

    /**
     * 初始化筛选组件
     * 包含状态、价格、品牌和电量筛选
     */
    private void initializeFilterComponents() {
        // 商品状态筛选
        statusFilter = new JComboBox<>(Status.values());
        
        // 价格筛选
        String[] prices = {"全部", "1.0", "1.5", "2.0", "2.5"};
        priceFilter = new JComboBox<>(prices);
        
        // 供应商筛选
        String[] brands = {"全部", "美团", "怪兽", "街电", "饿了么"};
        brandFilter = new JComboBox<>(brands);
        
        // 电量筛选
        String[] batteryLevels = {"全部", "50", "100"};
        batteryFilter = new JComboBox<>(batteryLevels);

        // 用户状态筛选
        String[] userStatuses = {"全部", "Common", "VIP", "SVIP", "Admin"};
        userStatusFilter = new JComboBox<>(userStatuses);
    }

    /**
     * 初始化搜索组件
     * 包含搜索输入框和搜索按钮
     */
    private void initializeSearchComponents() {
        searchField = new JTextField(20);
        searchButton = createButton("搜索");
    }

    /**
     * 显示商品筛选器
     * 包含状态、价格、品牌和电量筛选
     */
    private void showCommodityFilters() {
        filterPanel.removeAll();
        filterPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        filterPanel.setBackground(new Color(236, 240, 241));

        filterPanel.add(new JLabel("状态:"));
        filterPanel.add(statusFilter);
        filterPanel.add(new JLabel("价格(≤):"));
        filterPanel.add(priceFilter);
        filterPanel.add(new JLabel("供应商:"));
        filterPanel.add(brandFilter);
        filterPanel.add(new JLabel("电量(≤):"));
        filterPanel.add(batteryFilter);
        filterPanel.add(filterButton);

        topPanel.add(filterPanel, BorderLayout.EAST);
        topPanel.revalidate();
        topPanel.repaint();
    }

    /**
     * 显示用户筛选器
     * 包含状态筛选和搜索输入框
     */
    private void showUserFilters() {
        filterPanel.removeAll();
        filterPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        filterPanel.setBackground(new Color(236, 240, 241));

        filterPanel.add(new JLabel("状态:"));
        filterPanel.add(userStatusFilter);
        filterPanel.add(filterButton);
        filterPanel.add(searchField);
        filterPanel.add(searchButton);

        topPanel.add(filterPanel, BorderLayout.EAST);
        topPanel.revalidate();
        topPanel.repaint();
    }

    /**
     * 初始化表格
     * 包含商品或用户数据
     */
    private void initializeTable() {
        JScrollPane scrollPane = new JScrollPane();
        otherPanel.setLayout(new BorderLayout());
        otherPanel.add(scrollPane, BorderLayout.CENTER);
        windows.add(otherPanel, BorderLayout.CENTER);
        updateTableForCurrentView();
    }

    /**
     * 更新表格数据
     * 根据当前视图显示商品或用户数据
     */
    private void updateTableForCurrentView() {
        String[] columnNames;
        if (showingCommodities) {
            columnNames = new String[]{"品牌", "状态", "电量", "价格(元/小时)", "ID"};
        } else {
            columnNames = new String[]{"用户名", "电话", "状态", "密码", "余额", "会员到期时间"};
        }
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        if (dataTable != null) {
            otherPanel.remove(dataTable);
        }
        dataTable = new JTable(tableModel);

        // 设置多选模式
        dataTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        // 如果是商品视图，隐藏ID列
        if (showingCommodities) {
            dataTable.getColumnModel().getColumn(4).setMinWidth(0);
            dataTable.getColumnModel().getColumn(4).setMaxWidth(0);
            dataTable.getColumnModel().getColumn(4).setWidth(0);
        }
        
        dataTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int row = dataTable.rowAtPoint(evt.getPoint());
                    int col = dataTable.columnAtPoint(evt.getPoint());
                    if (row >= 0 && col >= 0) {
                        handleDoubleClick(row, col);
                    }
                }
            }
        });
        JScrollPane scrollPane = new JScrollPane(dataTable);
        otherPanel.removeAll();
        otherPanel.add(scrollPane, BorderLayout.CENTER);
        otherPanel.revalidate();
        otherPanel.repaint();
    }

    /**
     * 切换到商品视图
     * 显示商品筛选器和商品数据
     */
    private void switchToCommodityView() {
        showingCommodities = true;
        showCommodityFilters();
        updateTableForCurrentView();
        showCommodities();
    }

    /**
     * 切换到用户视图
     * 显示用户筛选器和用户数据
     */
    private void switchToUserView() {
        showingCommodities = false;
        showUserFilters();
        updateTableForCurrentView();
        showUsers();
    }

    /**
     * 显示商品数据
     * 包含所有商品信息
     */
    private void showCommodities() {
        try {
            List<Commodity> commodities = Commodity.getAllCommodities();
            tableModel.setRowCount(0);
            for (Commodity commodity : commodities) {
                Object[] row = {
                    commodity.getBrand(),
                    commodity.getStatus().toString(),
                    commodity.getBatteryLevel() + "%",
                    String.format("%.2f", commodity.getRentalPricePerHour()),
                    commodity.getId()
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "加载商品数据失败：" + e.getMessage());
        }
    }

    /**
     * 显示用户数据
     * 包含所有用户信息
     */
    private void showUsers() {
        try {
            List<User> users = User.getAllUsers();
            updateUserTableData(users);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "获取用户信息失败: " + ex.getMessage());
        }
    }

    /**
     * 搜索用户
     * 根据输入框内容搜索用户
     */
    private void searchUsers() {
        try {
            String keyword = searchField.getText().trim();
            if (keyword.isEmpty()) {
                showUsers();
                return;
            }
            List<User> users = User.searchUsers(keyword);
            updateUserTableData(users);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "搜索用户失败: " + ex.getMessage());
        }
    }

    /**
     * 应用筛选
     * 根据当前视图应用筛选
     */
    private void applyFilters() {
        if (showingCommodities) {
            applyCommodityFilters();
        } else {
            applyUserFilters();
        }
    }

    /**
     * 应用商品筛选
     * 根据状态、价格、品牌和电量筛选商品
     */
    private void applyCommodityFilters() {
        try {
            Status selectedStatus = (Status) statusFilter.getSelectedItem();
            String selectedBrand = (String) brandFilter.getSelectedItem();
            Double maxPrice = null;
            Integer maxBattery = null;

            String selectedPrice = (String) priceFilter.getSelectedItem();
            if (!selectedPrice.equals("全部")) {
                maxPrice = Double.parseDouble(selectedPrice);
            }

            String selectedBattery = (String) batteryFilter.getSelectedItem();
            if (!selectedBattery.equals("全部")) {
                maxBattery = Integer.parseInt(selectedBattery);
            }

            List<Commodity> filteredCommodities = Commodity.filterCommodities(
                selectedStatus.equals("全部") ? null : selectedStatus,
                null, maxPrice, null, maxBattery
            );

            if (!selectedBrand.equals("全部")) {
                filteredCommodities = filteredCommodities.stream()
                        .filter(c -> c.getBrand().equals(selectedBrand))
                        .collect(Collectors.toList());
            }

            updateCommodityTableData(filteredCommodities);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "筛选失败: " + e.getMessage());
        }
    }

    /**
     * 应用用户筛选
     * 根据状态筛选用户
     */
    private void applyUserFilters() {
        try {
            String selectedStatus = (String) userStatusFilter.getSelectedItem();
            if (selectedStatus.equals("全部")) {
                showUsers();
                return;
            }
            List<User> users = User.filterUsersByStatus(selectedStatus);
            updateUserTableData(users);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "筛选失败: " + ex.getMessage());
        }
    }

    /**
     * 更新商品表格数据
     * 包含筛选后的商品信息
     */
    private void updateCommodityTableData(List<Commodity> commodities) {
        tableModel.setRowCount(0);
        for (Commodity commodity : commodities) {
            Object[] row = {
                commodity.getBrand(),
                commodity.getStatus().toString(),
                commodity.getBatteryLevel() + "%",
                String.format("%.2f", commodity.getRentalPricePerHour()),
                commodity.getId()
            };
            tableModel.addRow(row);
        }
    }

    /**
     * 更新用户表格数据
     * 包含筛选后的用户信息
     */
    private void updateUserTableData(List<User> users) {
        tableModel.setRowCount(0);
        for (User user : users) {
            Object[] row = {
                    user.getUsername(),
                    user.getPhone(),
                    user.getStatus(),
                    user.getPassword(),
                    String.format("%.2f", user.getBalance()),
                    user.getExpiresTime() != null ? user.getExpiresTime().toString() : "无"
            };
            tableModel.addRow(row);
        }
    }

    /**
     * 处理双击事件
     * 根据当前视图显示商品或用户详情
     */
    private void handleDoubleClick(int row, int col) {
        if (showingCommodities) {
            // 获取商品信息
            try {
                int id = (int) tableModel.getValueAt(row, 4); // 隐藏的ID列
                Commodity commodity = Commodity.findById(id);
                if (commodity != null) {
                    CommodityDetailDialog dialog = new CommodityDetailDialog(this, commodity);
                    dialog.setVisible(true);
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "获取商品信息失败：" + e.getMessage());
            }
        } else {
            try {
                // 获取用户信息
                String username = (String) tableModel.getValueAt(row, 0);
                String phone = (String) tableModel.getValueAt(row, 1);
                User user = User.findByUsernameAndPhone(username, phone);
                
                if (user != null) {
                    // 显示用户详情对话框
                    UserDetailDialog dialog = new UserDetailDialog(this, user);
                    dialog.setVisible(true);
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "获取用户信息失败：" + e.getMessage());
            }
        }
    }

    /**
     * 刷新商品表格
     * 包含所有商品信息
     */
    public void refreshCommodityTable() {
        try {
            // 清空表格数据
            tableModel.setRowCount(0);

            // 重新加载所有商品数据
            List<Commodity> commodities = Commodity.getAllCommodities();
            for (Commodity commodity : commodities) {
                tableModel.addRow(new Object[]{
                    commodity.getBrand(),
                    commodity.getStatus(),
                    commodity.getBatteryLevel() + "%",
                    String.format("￥%.2f", commodity.getRentalPricePerHour()),
                    commodity.getId()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "刷新商品列表失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 刷新用户表格
     * 包含所有用户信息
     */
    public void refreshUserTable() {
        if (!showingCommodities) {
            showUsers();
        }
    }
}