import gui.LoginLayout;
import util.BatteryMonitor;
import util.LogUtil;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // 设置程序使用系统默认的界面外观
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            LogUtil.error("设置系统外观失败", e);
        }

        LogUtil.info("程序启动，准备初始化电池监控服务...");
        // 启动电池监控服务，用于实时监控所有移动电源的状态
        BatteryMonitor.getInstance().start();
        LogUtil.info("电池监控服务初始化完成");

        // 在EDT（Event Dispatch Thread）线程中启动GUI，确保线程安全
        SwingUtilities.invokeLater(() -> {
            try {
                // 创建并显示登录界面
                LoginLayout loginLayout = new LoginLayout();
                loginLayout.setVisible(true);
                LogUtil.info("登录界面已启动");
            } catch (Exception e) {
                LogUtil.error("启动程序时发生错误", e);
                JOptionPane.showMessageDialog(null, 
                    "启动程序时发生错误：" + e.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
