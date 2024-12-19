package gui;

import User.User;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.util.Date;
import javax.imageio.ImageIO;
import javax.swing.filechooser.FileNameExtensionFilter;

public class UserDetailDialog extends JDialog {
    private static final int AVATAR_SIZE = 120;
    private final AdminLayout adminLayout;
    private User currentUser;
    private JLabel avatarLabel;
    private JTextField usernameField;
    private JTextField phoneField;
    private JPasswordField passwordField;
    private JComboBox<String> statusComboBox;
    private JSpinner expirySpinner;

    public UserDetailDialog(AdminLayout parent, User user) {
        super(parent, "用户详情", true);
        this.adminLayout = parent;
        this.currentUser = user;
        initializeUI(user);
    }

    private void initializeUI(User user) {
        setSize(500, 600);
        setLocationRelativeTo(getParent());
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);

        // 添加头像显示
        avatarLabel = new JLabel();
        avatarLabel.setPreferredSize(new Dimension(AVATAR_SIZE, AVATAR_SIZE));
        avatarLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        
        // 加载头像
        loadAvatar(user.getAvatar());
        
        JPanel avatarPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        avatarPanel.add(avatarLabel);
        
        // 添加更换头像按钮
        JButton changeAvatarButton = new JButton("更换头像");
        changeAvatarButton.addActionListener(e -> changeAvatar());
        avatarPanel.add(changeAvatarButton);

        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(avatarPanel, gbc);
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;

        // 添加可编辑的用户信息字段
        usernameField = new JTextField(user.getUsername(), 20);
        phoneField = new JTextField(user.getPhone(), 20);
        passwordField = new JPasswordField(20);
        
        addEditableField(panel, gbc, "用户名:", usernameField);
        addEditableField(panel, gbc, "电话:", phoneField);
        addEditableField(panel, gbc, "新密码:", passwordField);
        addField(panel, gbc, "余额:", "￥" + String.format("%.2f", user.getBalance()));

        // 添加状态选择下拉框
        statusComboBox = new JComboBox<>(new String[]{"Common", "VIP", "SVIP"});
        statusComboBox.setSelectedItem(user.getStatus());
        addField(panel, gbc, "会员状态：", statusComboBox);

        // 添加到期时间选择器
        expirySpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(expirySpinner, "yyyy-MM-dd HH:mm:ss");
        expirySpinner.setEditor(dateEditor);
        if (user.getExpiresTime() != null) {
            expirySpinner.setValue(user.getExpiresTime());
        }
        addField(panel, gbc, "到期时间：", expirySpinner);

        // 添加按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        
        // 保存按钮
        JButton saveButton = new JButton("保存修改");
        saveButton.addActionListener(e -> saveChanges());
        
        // 登录按钮
        JButton loginButton = new JButton("登录此账户");
        loginButton.addActionListener(e -> {
            try {
                UserLayout userLayout = new UserLayout(user.getUsername(), true);
                userLayout.setVisible(true);
                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "登录失败：" + ex.getMessage());
            }
        });
        
        // 关闭按钮
        JButton closeButton = new JButton("关闭");
        closeButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(loginButton);
        buttonPanel.add(closeButton);

        // 添加按钮面板
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(buttonPanel, gbc);

        add(panel);
    }

    private void loadAvatar(String avatarPath) {
        try {
            if (avatarPath != null && !avatarPath.isEmpty()) {
                File imageFile = new File(avatarPath);
                if (imageFile.exists()) {
                    BufferedImage originalImage = ImageIO.read(imageFile);
                    if (originalImage != null) {
                        // 创建一个圆形遮罩的图像
                        BufferedImage circularImage = new BufferedImage(AVATAR_SIZE, AVATAR_SIZE, BufferedImage.TYPE_INT_ARGB);
                        Graphics2D g2d = circularImage.createGraphics();
                        
                        // 启用抗锯齿
                        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                        
                        // 创建圆形遮罩
                        Ellipse2D.Double circle = new Ellipse2D.Double(0, 0, AVATAR_SIZE - 1, AVATAR_SIZE - 1);
                        g2d.setClip(circle);
                        
                        // 计算缩放比例以填充圆形区域
                        double scale = Math.max(
                            (double) AVATAR_SIZE / originalImage.getWidth(),
                            (double) AVATAR_SIZE / originalImage.getHeight()
                        );
                        
                        int scaledWidth = (int) (originalImage.getWidth() * scale);
                        int scaledHeight = (int) (originalImage.getHeight() * scale);
                        
                        // 计算居中位置
                        int x = (AVATAR_SIZE - scaledWidth) / 2;
                        int y = (AVATAR_SIZE - scaledHeight) / 2;
                        
                        // 绘制缩放后的图像
                        g2d.drawImage(originalImage, x, y, scaledWidth, scaledHeight, null);
                        
                        // 绘制边框
                        g2d.setClip(null);
                        g2d.setColor(new Color(200, 200, 200));
                        g2d.setStroke(new BasicStroke(2));
                        g2d.draw(circle);
                        
                        g2d.dispose();
                        
                        // 设置头像
                        avatarLabel.setIcon(new ImageIcon(circularImage));
                        avatarLabel.setText(null);
                        return;
                    }
                }
            }
            // 如果无法加载头像，显示默认图标
            avatarLabel.setIcon(null);
            avatarLabel.setText("无头像");
            avatarLabel.setHorizontalAlignment(SwingConstants.CENTER);
            avatarLabel.setOpaque(true);
            avatarLabel.setBackground(new Color(240, 240, 240));
        } catch (IOException e) {
            System.err.println("加载头像失败：" + e.getMessage());
            avatarLabel.setIcon(null);
            avatarLabel.setText("加载失败");
            avatarLabel.setHorizontalAlignment(SwingConstants.CENTER);
            avatarLabel.setOpaque(true);
            avatarLabel.setBackground(new Color(240, 240, 240));
        }
    }

    private void changeAvatar() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "图片文件", "jpg", "jpeg", "png", "gif"));
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
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
                
                // 更新用户头像路径
                currentUser.setAvatar(targetPath);
                loadAvatar(targetPath);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "更新头像失败：" + ex.getMessage());
            }
        }
    }

    private void saveChanges() {
        try {
            // 更新用户信息
            currentUser.setUsername(usernameField.getText());
            currentUser.setPhone(phoneField.getText());
            String newPassword = new String(passwordField.getPassword());
            if (!newPassword.isEmpty()) {
                currentUser.setPassword(newPassword);
            }
            currentUser.setStatus((String) statusComboBox.getSelectedItem());
            currentUser.setExpiresTime(new Timestamp(((Date) expirySpinner.getValue()).getTime()));
            
            if (currentUser.save()) {
                JOptionPane.showMessageDialog(this, "保存成功！");
                adminLayout.refreshUserTable(); // 刷新管理界面的用户列表
            } else {
                JOptionPane.showMessageDialog(this, "保存失败！");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "保存失败：" + ex.getMessage());
        }
    }

    private void addEditableField(JPanel panel, GridBagConstraints gbc, String label, JTextField field) {
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        panel.add(field, gbc);
        gbc.gridy++;
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