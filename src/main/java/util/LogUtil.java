package util;

import java.util.logging.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogUtil {
    private static final Logger LOGGER = Logger.getLogger("MobilePowerRental");
    private static boolean isInitialized = false;

    static {
        init();
    }


    private static void init() {
        if (isInitialized) {
            return;
        }

        try {
            // 移除所有现有的处理器
            Logger rootLogger = Logger.getLogger("");
            Handler[] handlers = rootLogger.getHandlers();
            for (Handler handler : handlers) {
                rootLogger.removeHandler(handler);
            }

            // 创建控制台处理器
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.ALL);
            
            // 自定义格式
            SimpleFormatter formatter = new SimpleFormatter() {
                private static final String format = "[%1$tF %1$tT] [%2$s] %3$s %n";

                @Override
                public synchronized String format(LogRecord lr) {
                    return String.format(format,
                        LocalDateTime.now(),
                        lr.getLevel().getLocalizedName(),
                        lr.getMessage()
                    );
                }
            };
            consoleHandler.setFormatter(formatter);

            // 设置日志级别
            LOGGER.setLevel(Level.ALL);
            LOGGER.addHandler(consoleHandler);

            // 创建logs目录（如果不存在）
            File logsDir = new File("logs");
            if (!logsDir.exists()) {
                logsDir.mkdir();
            }

            // 创建文件处理器
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            FileHandler fileHandler = new FileHandler("logs/powerbank_" + timestamp + ".log", true);
            fileHandler.setLevel(Level.ALL);
            fileHandler.setFormatter(formatter);
            LOGGER.addHandler(fileHandler);

            isInitialized = true;
        } catch (IOException e) {
            System.err.println("初始化日志系统失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void info(String message) {
        LOGGER.info(message);
    }

    public static void warning(String message) {
        LOGGER.warning(message);
    }

    public static void error(String message) {
        LOGGER.severe(message);
    }

    public static void error(String message, Throwable throwable) {
        LOGGER.log(Level.SEVERE, message, throwable);
    }

    public static void debug(String message) {
        LOGGER.fine(message);
    }
} 