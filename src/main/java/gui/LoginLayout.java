package gui;

import User.User;
import gui.util.setDocumentFilter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class LoginLayout extends JFrame {

    // 常量定义
    private static final int MAX_USERNAME_LENGTH = 20;      // 用户名最大长度
    private static final int MAX_PASSWORD_LENGTH = 16;      // 密码最大长度
    private static final String USER_INFO_FILE = "user.info";  // 用户信息存储文件

    // UI组件声明
    JPanel windows = new JPanel();              // 主窗口面板
    JPanel loginContainer = new JPanel();       // 登录容器面板
    JPanel panel1 = new JPanel();              // 提示信息面板
    JPanel panel2 = new JPanel();              // 用户名输入面板
    JPanel panel3 = new JPanel();              // 密码输入面板
    JPanel panel4 = new JPanel();              // 选项面板（记住密码、自动登录）
    JPanel panel5 = new JPanel();              // 按钮面板（登录、注册）
    
    // 界面元素
    JLabel alert = new JLabel();                           // 提示信息标签
    JLabel username = new JLabel("用 户 名:");              // 用户名标签
    JLabel password = new JLabel("密    码:");             // 密码标签
    JTextField usernameText = new JTextField(15);          // 用户名输入框
    JPasswordField passwordText = new JPasswordField(15);  // 密码输入框
    JCheckBox remember = new JCheckBox("记住密码");         // 记住密码复选框
    JCheckBox autoLogin = new JCheckBox("自动登录");        // 自动登录复选框
    JButton login = new JButton("登 录");                   // 登录按钮
    JButton register = new JButton("注 册");                // 注册按钮
    Font font = new Font("微软雅黑", Font.BOLD, 10);        // 统一字体

    /**
     * 登录界面构造函数
     * 初始化界面布局和所有组件
     */
    public LoginLayout() {
        setTitle("登录");
        setSize(350, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);  // 窗口居中显示

        // 设置整体布局
        windows.setLayout(new BorderLayout());
        loginContainer.setLayout(new BoxLayout(loginContainer, BoxLayout.Y_AXIS));

        // 设置各个面板的布局
        panel1.setLayout(new FlowLayout(FlowLayout.CENTER));
        panel2.setLayout(new FlowLayout(FlowLayout.CENTER));
        panel3.setLayout(new FlowLayout(FlowLayout.CENTER));
        panel4.setLayout(new FlowLayout(FlowLayout.CENTER));
        panel5.setLayout(new FlowLayout(FlowLayout.CENTER));

        // 限制用户名和密码长度
        setDocumentFilter.setFilter(usernameText, MAX_USERNAME_LENGTH);
        setDocumentFilter.setFilter(passwordText, MAX_PASSWORD_LENGTH);

        // 添加组件到相应的面板
        panel1.add(username);
        panel1.add(usernameText);

        panel2.add(password);
        panel2.add(passwordText);

        panel3.add(remember);
        panel3.add(autoLogin);

        panel4.add(register);
        panel4.add(login);
        
        alert.setFont(font);
        panel5.add(alert);

        // 将面板添加到loginContainer
        loginContainer.add(Box.createVerticalStrut(20)); // 顶部间距
        loginContainer.add(panel1);
        loginContainer.add(panel2);
        loginContainer.add(panel3);
        loginContainer.add(panel5);
        loginContainer.add(panel4);
        loginContainer.add(Box.createVerticalStrut(20)); // 底部间距

        // 设置loginContainer的边框，使其居中
        loginContainer.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        // 将loginContainer添加到windows
        windows.add(loginContainer, BorderLayout.CENTER);

        // 将windows添加到框架
        add(windows);

        // 美化界面
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.updateComponentTreeUI(this);

        // 监听登录按钮
        login.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                login();
            }
        });

        // 监听记住密码复选框
        remember.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (remember.isSelected()) {
                    System.out.println("记住密码已选中");
                } else {
                    System.out.println("记住密码未选中");
                }
            }
        });

        // 监听自动登录复选框
        autoLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (autoLogin.isSelected()) {
                    // 自动勾选"记住密码"选项
                    remember.setSelected(true);
                    System.out.println("自动登录已选中");
                } else {
                    System.out.println("自动登录未选中");
                }
            }
        });

        // 监听注册按钮
        register.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                register();
            }
        });
        // 读取用户信息
        loadUserInfo();
    }

    /**
     * 注册界面
     * Register Interface
     */
    public void register() {
        RegisterLayout registerLayout = new RegisterLayout();
        registerLayout.setVisible(true);
    }

    /**
     * 加载用户信息
     * Load User Information
     */
    private void loadUserInfo() {
        try {
            File file = new File(USER_INFO_FILE);
            if (!file.exists()) {
                return;
            }
            
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String username = reader.readLine();
            String password = reader.readLine();
            boolean rememberChecked = Boolean.parseBoolean(reader.readLine());
            boolean autoLoginChecked = Boolean.parseBoolean(reader.readLine());
            reader.close();

            usernameText.setText(username != null ? username : "");
            remember.setSelected(rememberChecked);
            autoLogin.setSelected(autoLoginChecked);
            
            if (rememberChecked && password != null) {
                passwordText.setText(password);
                if (autoLoginChecked) {
                    // 自动登录
                    SwingUtilities.invokeLater(() -> login());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取组件的方法
     * Get Component Methods
     */
    public JTextField getUsernameText() { return usernameText; }
    public JPasswordField getPasswordText() { return passwordText; }
    public JButton getLoginButton() { return login; }
    public JButton getRegisterButton() { return register; }
    public JCheckBox getRememberCheckBox() { return remember; }
    public JCheckBox getAutoLoginCheckBox() { return autoLogin; }

    /**
     * 主函数
     * Main Function
     */
//    public static void main(String[] args) {
//        SwingUtilities.invokeLater(() -> {
//            LoginLayout loginLayout = new LoginLayout();
//            loginLayout.setVisible(true);
//        });
//    }

    /**
     * 登录功能
     * Login Function
     */
    public boolean login() {
        User user = new User();
        user.setUsername(usernameText.getText());
        user.setPassword(passwordText.getText());

        try {
            boolean isValidUser = user.checkUser_IsDataBase(user.getUsername(), user.getPassword());
            boolean isAdmin = user.checkUser_IsAdmin(user.getUsername(), user.getPassword());

            if (isValidUser) {
                if (isAdmin) {
                    alert.setText("欢迎管理员");
                    alert.setForeground(Color.GREEN);
                    // 跳转到管理员界面
                    SwingUtilities.invokeLater(() -> {
                        AdminLayout adminLayout = new AdminLayout();
                        adminLayout.setVisible(true);
                        dispose(); // 关闭登录窗口
                    });
                } else {
                    alert.setText("登入成功");
                    alert.setForeground(Color.GREEN);
                    // 跳转到用户界面
                    SwingUtilities.invokeLater(() -> {
                        UserLayout userLayout = new UserLayout(user.getUsername());
                        userLayout.setVisible(true);
                        dispose(); // 关闭登录窗口
                    });
                }
                // 保存用户信息
                saveUserInfo();
                return true;
            } else {
                alert.setText("用户名或密码错误");
                alert.setForeground(Color.RED);
                return false;
            }
        } catch (Exception e) {
            alert.setText("登录失败");
            alert.setForeground(Color.RED);
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 保存用户信息
     * Save User Information
     */
    private void saveUserInfo() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(USER_INFO_FILE))) {
            writer.println(usernameText.getText());
            // Only save password if "remember password" is checked
            writer.println(remember.isSelected() ? new String(passwordText.getPassword()) : "");
            writer.println(remember.isSelected());
            writer.println(autoLogin.isSelected());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
