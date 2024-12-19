package gui;

import Commodity.Commodity;
import Commodity.Commodity.Status;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class CommodityDetailDialog extends JDialog {
    private final AdminLayout adminLayout;
    private final Commodity commodity;
    private JComboBox<Status> statusComboBox;
    private JSpinner batterySpinner;
    private JTextField priceField;

    public CommodityDetailDialog(AdminLayout parent, Commodity commodity) {
        super(parent, "商品详情", true);
        this.adminLayout = parent;
        this.commodity = commodity;
        initializeUI(commodity);
    }

    private void initializeUI(Commodity commodity) {
        setSize(400, 350);
        setLocationRelativeTo(getParent());
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);

        // 添加商品ID（不可编辑）
        addField(panel, gbc, "商品ID:", String.valueOf(commodity.getId()));

        // 添加品牌（不可编辑）
        addField(panel, gbc, "品牌:", commodity.getBrand());

        // 添加状态选择
        statusComboBox = new JComboBox<>(Status.values());
        statusComboBox.setSelectedItem(commodity.getStatus());
        addField(panel, gbc, "状态:", statusComboBox);

        // 添加电量设置
        batterySpinner = new JSpinner(new SpinnerNumberModel(commodity.getBatteryLevel(), 0, 100, 1));
        addField(panel, gbc, "电量:", batterySpinner);

        // 添加租金设置
        priceField = new JTextField(String.format("%.2f", commodity.getRentalPricePerHour()), 10);
        addField(panel, gbc, "租金(每小时):", priceField);

        // 添加按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        
        // 保存按钮
        JButton saveButton = new JButton("保存");
        saveButton.addActionListener(e -> saveChanges());
        
        // 关闭按钮
        JButton closeButton = new JButton("关闭");
        closeButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(closeButton);

        // 添加按钮面板
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(buttonPanel, gbc);

        add(panel);
    }

    private void saveChanges() {
        try {
            // 更新商品信息（不包括品牌）
            commodity.setStatus((Status) statusComboBox.getSelectedItem());
            commodity.setBatteryLevel((Integer) batterySpinner.getValue());
            
            try {
                double price = Double.parseDouble(priceField.getText());
                if (price < 0) {
                    throw new NumberFormatException("租金不能为负数");
                }
                commodity.setRentalPricePerHour(price);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "请输入有效的租金金额！", "输入错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 保存到数据库
            if (commodity.save()) {
                JOptionPane.showMessageDialog(this, "保存成功！");
                adminLayout.refreshCommodityTable(); // 刷新商品列表
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "保存失败！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "保存失败：" + ex.getMessage(), "数据库错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addField(JPanel panel, GridBagConstraints gbc, String label, String value) {
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        panel.add(new JLabel(value), gbc);
        gbc.gridy++;
    }

    private void addField(JPanel panel, GridBagConstraints gbc, String label, JComponent component) {
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        panel.add(component, gbc);
        gbc.gridy++;
    }
}