package ui;

import javafx.application.Application;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 纯 Java 启动器，避免把 {@link MainWindow} 直接作为 JAR/jpackage 入口。
 */
public final class Launcher {
    private Launcher() {
    }

    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            throwable.printStackTrace();
            StringWriter buffer = new StringWriter();
            throwable.printStackTrace(new PrintWriter(buffer));
            System.err.println(buffer);
        });
        Application.launch(MainWindow.class, args);
    }
}