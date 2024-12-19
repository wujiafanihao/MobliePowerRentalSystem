package gui;

import Order.Order;
import User.User;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class OrderLayout extends JFrame {
    private User currentUser;
    private JPanel mainPanel;
    private JPanel topPanel;
    private JTable orderTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JButton searchButton;
    private JButton deleteButton;

    public OrderLayout(User user) {
        this.currentUser = user;
        initializeUI();
        loadOrders();
    }

    private void initializeUI() {
        setTitle("订单历史");
        setSize(800, 600);
        setLocationRelativeTo(null);

        // 创建主面板
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 创建顶部面板
        topPanel = new JPanel(new BorderLayout(10, 0));
        
        // 创建搜索面板
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(20);
        searchButton = createStyledButton("搜索", new Color(52, 152, 219));
        searchPanel.add(new JLabel("搜索: "));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        
        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        deleteButton = createStyledButton("删除", new Color(231, 76, 60));
        buttonPanel.add(deleteButton);

        // 添加到顶部面板
        topPanel.add(searchPanel, BorderLayout.WEST);
        topPanel.add(buttonPanel, BorderLayout.EAST);

        // 创建表格
        String[] columnNames = {"订单号", "电源ID", "品牌", "租借时间", "归还时间", "租借时长(小时)", "总费用"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        orderTable = new JTable(tableModel);
        
        // 允许多选
        orderTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        // 添加鼠标监听器
        orderTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = orderTable.rowAtPoint(evt.getPoint());
                int col = orderTable.columnAtPoint(evt.getPoint());
                
                if (row >= 0 && col >= 0) {
                    String orderCode = (String) tableModel.getValueAt(row, 0);
                    
                    // 右键点击订单号列
                    if (evt.getButton() == java.awt.event.MouseEvent.BUTTON3 && col == 0) {
                        copyOrderCode(orderCode);
                    }
                    // 左键双击任意列
                    else if (evt.getButton() == java.awt.event.MouseEvent.BUTTON1 && 
                             evt.getClickCount() == 2) {
                        showOrderDetail(orderCode);
                    }
                }
            }
        });

        // 添加右键菜单
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem copyItem = new JMenuItem("复制订单号");
        copyItem.addActionListener(e -> {
            int row = orderTable.getSelectedRow();
            if (row >= 0) {
                String orderCode = (String) tableModel.getValueAt(row, 0);
                copyOrderCode(orderCode);
            }
        });
        popupMenu.add(copyItem);
        orderTable.setComponentPopupMenu(popupMenu);

        JScrollPane scrollPane = new JScrollPane(orderTable);

        // 添加组件到主面板
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // 添加到窗口
        add(mainPanel);

        // 添加事件监听器
        searchButton.addActionListener(e -> searchOrders());
        deleteButton.addActionListener(e -> deleteSelectedOrders());
        
        // 添加搜索框回车事件
        searchField.addActionListener(e -> searchOrders());
    }

    private void loadOrders() {
        try {
            List<Order> orders = Order.getOrderHistory(currentUser.getId());
            updateTableData(orders);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "加载订单失败：" + e.getMessage());
        }
    }

    private void searchOrders() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadOrders();
            return;
        }

        try {
            List<Order> orders = Order.searchOrders(currentUser.getId(), keyword);
            updateTableData(orders);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "搜索订单失败：" + e.getMessage());
        }
    }

    private void deleteSelectedOrders() {
        int[] selectedRows = orderTable.getSelectedRows();
        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(this, "请选择要删除的订单");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "确定要删除选中的" + selectedRows.length + "个订单吗？",
                "确认删除",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean allSuccess = true;
                for (int row : selectedRows) {
                    String orderCode = (String) tableModel.getValueAt(row, 0);
                    // 获取订单信息
                    Order order = Order.findByOrderCode(orderCode);
                    if (order != null && order.getUserId() == currentUser.getId()) {
                        if (!Order.deleteOrder(order.getId())) {
                            allSuccess = false;
                            System.err.println("删除订单失败：" + orderCode);
                        } else {
                            System.out.println("成功删除订单：" + orderCode);
                        }
                    } else {
                        allSuccess = false;
                        System.err.println("无权删除订单或订单不存在：" + orderCode);
                    }
                }
                
                // 重新加载订单列表
                loadOrders();
                
                // 显示操作结果
                if (allSuccess) {
                    JOptionPane.showMessageDialog(this, "删除成功");
                } else {
                    JOptionPane.showMessageDialog(this, "部分订单删除失败，请重试");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "删除订单失败：" + e.getMessage());
            }
        }
    }

    private void updateTableData(List<Order> orders) {
        tableModel.setRowCount(0);
        for (Order order : orders) {
            Object[] row = {
                order.getOrderCode(),
                order.getPowerbankId(),
                order.getBrand(),
                order.getRentalStartTime(),
                order.getReturnTime(),
                order.getRentalDurationHours(),
                String.format("￥%.2f", order.getTotalCost())
            };
            tableModel.addRow(row);
        }
    }

    private JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        button.setPreferredSize(new Dimension(80, 30));

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

    private void copyOrderCode(String orderCode) {
        java.awt.Toolkit.getDefaultToolkit()
            .getSystemClipboard()
            .setContents(new java.awt.datatransfer.StringSelection(orderCode), null);
        JOptionPane.showMessageDialog(this, "订单号已复制到剪贴板");
    }

    private void showOrderDetail(String orderCode) {
        try {
            Order order = Order.findByOrderCode(orderCode);
            if (order != null) {
                OrderDetailDialog dialog = new OrderDetailDialog(this, order);
                dialog.setVisible(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "获取订单详情失败：" + e.getMessage());
        }
    }
}
