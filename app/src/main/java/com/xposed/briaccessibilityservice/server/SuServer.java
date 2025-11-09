package com.xposed.briaccessibilityservice.server;

import com.xposed.briaccessibilityservice.utils.Logs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

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
    private Thread outputThread;
    private Thread errorThread;
    private volatile boolean isRunning = false;

    public SuServer() {
        new Thread(this).start(); // 启动主线程
    }

    @Override
    public void run() {
        if (startSession()) {
            isRunning = true;
            // 启动输出流读取线程
            outputThread = new Thread(() -> {
                try {
                    String line;
                    while (isRunning && (line = outputReader.readLine()) != null) {
                        Logs.d(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            outputThread.start();

            // 启动错误流读取线程
            errorThread = new Thread(() -> {
                try {
                    String line;
                    while (isRunning && (line = errorReader.readLine()) != null) {
                        Logs.d("[ERROR]" + line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            errorThread.start();
        }
    }

    // 启动 su 会话
    private synchronized boolean startSession() {
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

    // 发送命令（非阻塞）
    public synchronized void executeCommand(String command) {
        if (suProcess == null || !isRunning) {
            return;
        }
        try {
            commandOutputStream.write((command + "\n").getBytes());
            commandOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 关闭会话
    public synchronized void closeSession() {
        isRunning = false;
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
}
