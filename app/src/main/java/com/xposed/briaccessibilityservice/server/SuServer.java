package com.xposed.briaccessibilityservice.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class SuServer implements Runnable {
    private Process suProcess;
    private OutputStream commandOutputStream;
    private BufferedReader outputReader;
    private BufferedReader errorReader;

    public SuServer() {
        new Thread(this).start();
    }

    // 启动 su 会话
    public boolean startSession() {
        try {
            suProcess = Runtime.getRuntime().exec("su");
            commandOutputStream = suProcess.getOutputStream();
            outputReader = new BufferedReader(new InputStreamReader(suProcess.getInputStream()));
            errorReader = new BufferedReader(new InputStreamReader(suProcess.getErrorStream()));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 执行单条命令
    public String executeCommand(String command) {
        if (suProcess == null) {
            return "Error: Session not started";
        }
        try {
            // 发送命令
            commandOutputStream.write((command + "\n").getBytes());
            commandOutputStream.flush();
            // 读取输出和错误流
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = outputReader.readLine()) != null) {
                result.append(line).append("\n");
            }
            while ((line = errorReader.readLine()) != null) {
                result.append("[ERROR] ").append(line).append("\n");
            }
            return result.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    // 关闭会话
    public void closeSession() {
        try {
            if (commandOutputStream != null) {
                commandOutputStream.write("exit\n".getBytes());
                commandOutputStream.flush();
                commandOutputStream.close();
            }
            if (outputReader != null) outputReader.close();
            if (errorReader != null) errorReader.close();
            if (suProcess != null) suProcess.destroy();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        startSession();
    }
}
