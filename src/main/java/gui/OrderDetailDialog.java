package gui;

import Order.Order;
import javax.swing.*;
import java.awt.*;

public class OrderDetailDialog extends JDialog {
    public OrderDetailDialog(Frame parent, Order order) {
        super(parent, "订单详情", true);
        initializeUI(order);
    }

    private void initializeUI(Order order) {
        setSize(400, 400);
        setLocationRelativeTo(getParent());
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);

        // 添加订单信息
        addField(panel, gbc, "订单编号:", order.getOrderCode());
        addField(panel, gbc, "电源ID:", String.valueOf(order.getPowerbankId()));
        addField(panel, gbc, "品牌:", order.getBrand());
        addField(panel, gbc, "租借时间:", order.getRentalStartTime().toString());
        addField(panel, gbc, "归还时间:", order.getReturnTime() != null ? order.getReturnTime().toString() : "未归还");
        addField(panel, gbc, "租借时长:", order.getRentalDurationHours() + "小时");
        addField(panel, gbc, "押金:", "￥" + String.format("%.2f", order.getDeposit()));
        addField(panel, gbc, "总费用:", "￥" + String.format("%.2f", order.getTotalCost()));

        // 添加关闭按钮
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton closeButton = new JButton("关闭");
        closeButton.addActionListener(e -> dispose());
        panel.add(closeButton, gbc);

        add(panel);
    }

    private void addField(JPanel panel, GridBagConstraints gbc, String label, String value) {
        gbc.gridx = 0;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        panel.add(new JLabel(value), gbc);
        gbc.gridy++;
    }
} 