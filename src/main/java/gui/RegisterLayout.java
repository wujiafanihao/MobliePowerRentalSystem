package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import User.User;
import gui.util.setDocumentFilter;

public class RegisterLayout extends JFrame {

    private static final int MAX_USERNAME_LENGTH = 20;
    private static final int MAX_PASSWORD_LENGTH = 16;
    private static final int MAX_PHONE_LENGTH = 11;

    JPanel windows = new JPanel();
    JPanel registerContainer = new JPanel();
    JPanel panel1 = new JPanel();
    JPanel panel2 = new JPanel();
    JPanel panel3 = new JPanel();
    JPanel panel4 = new JPanel();
    JPanel panel5 = new JPanel();
    JPanel panel6 = new JPanel();
    JLabel username = new JLabel("用 户 名:");
    JLabel phone = new JLabel("手 机 号:");
    JLabel password = new JLabel("密    码:");
    JLabel confirmPassword = new JLabel("确认密码:");
    JLabel alert = new JLabel();
    JTextField usernameText = new JTextField(15);
    JPasswordField passwordText = new JPasswordField(15);
    JTextField phoneText = new JTextField(15);
    JPasswordField confirmPasswordText = new JPasswordField(15);

    JButton register = new JButton("注 册");
    JButton returnButton = new JButton("返 回");
    Font font = new Font("微软雅黑", Font.BOLD, 10);

    public RegisterLayout(){
        setTitle("注册");
        setSize(350, 250);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 设置整体布局
        windows.setLayout(new BorderLayout());
        registerContainer.setLayout(new BoxLayout(registerContainer, BoxLayout.Y_AXIS));

        // 设置各个面板的布局
        panel1.setLayout(new FlowLayout(FlowLayout.CENTER));
        panel6.setLayout(new FlowLayout(FlowLayout.CENTER));
        panel2.setLayout(new FlowLayout(FlowLayout.CENTER));
        panel3.setLayout(new FlowLayout(FlowLayout.CENTER));
        panel4.setLayout(new FlowLayout(FlowLayout.CENTER));
        panel5.setLayout(new FlowLayout(FlowLayout.CENTER));

        // 限制用户名和密码长度
        setDocumentFilter.setFilter(usernameText, MAX_USERNAME_LENGTH);
        setDocumentFilter.setFilter(passwordText, MAX_PASSWORD_LENGTH);
        setDocumentFilter.setFilter(confirmPasswordText, MAX_PASSWORD_LENGTH);
        setDocumentFilter.setFilter(phoneText, MAX_PHONE_LENGTH);

        // 添加组件到相应的面板
        panel1.add(username);
        panel1.add(usernameText);
        panel6.add(phone);
        panel6.add(phoneText);
        panel2.add(password);
        panel2.add(passwordText);
        panel3.add(confirmPassword);
        panel3.add(confirmPasswordText);
        panel4.add(register);
        panel4.add(returnButton);
        panel5.add(alert);

        // 添加组件到相应的面板
        registerContainer.add(Box.createVerticalStrut(20)); // 顶部间距
        registerContainer.add(panel1);
        registerContainer.add(panel6);
        registerContainer.add(panel2);
        registerContainer.add(panel3);
        registerContainer.add(panel4);
        registerContainer.add(panel5);
        registerContainer.add(Box.createVerticalStrut(20)); // 底部间距

        // 设置registerContainer的边框，使其居中
        registerContainer.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        // 将registerContainer添加到windows
        windows.add(registerContainer, BorderLayout.CENTER);

        // 将windows添加到框架
        add(windows);

        // 美化界面
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.updateComponentTreeUI(this);

        // 监听注册按钮
        register.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                register();
            }
        });

        // 监听返回按钮
        returnButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                returnfun();
            }
        });
    }

    public void returnfun() {
        LoginLayout loginLayout = new LoginLayout();
        loginLayout.setVisible(true);
    }

    public void register(){
        User user = new User();
        user.setUsername(usernameText.getText());
        user.setPhone(phoneText.getText());
        user.setPassword(passwordText.getText());
        user.setConfirmPassword(confirmPasswordText.getText());

        try {
            boolean isValidUser = user.checkUser_register(user.getUsername(), user.getPhone(), user.getPassword(), user.getConfirmPassword());
            if (isValidUser) {
                alert.setText("欢迎注册");
                alert.setForeground(Color.GREEN);
                System.out.println("注册成功");
            } else {
                alert.setText("注册失败");
                System.out.println("注册失败");
            }
        }catch (Exception e){
            alert.setText("注册失败");
            e.printStackTrace();
            System.out.println("注册过程中发生错误");
        }
    }

//    public static void main(String[] args) {
//        SwingUtilities.invokeLater(() -> {
//            RegisterLayout registerLayout = new RegisterLayout();
//            registerLayout.setVisible(true);
//        });
//    }
}
