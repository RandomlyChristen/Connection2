package com.leesugyun.connection2;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

/**
 * Created by LeeSugyun on 2017-12-03.
 */

public class ServerWork extends Thread {
    private boolean flag = true;

    private String myName;
    private String myIP;
    private String opponentName;
    private String opponentIP;
    private int gamePort;

    private ServerSocket serverSocket;

    private HashMap<String, ServerThread> serverMap;

    public ServerWork(String myName, String myIP, String opponentName, String opponentIP, int gamePort) {
        this.myName = myName;
        this.myIP = myIP;
        this.opponentName = opponentName;
        this.opponentIP = opponentIP;
        this.gamePort = gamePort;

        serverMap = new HashMap<>();

        try {
            serverSocket = new ServerSocket(gamePort);
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.start();
    }

    @Override
    public void run() {
        try {
            while (!serverMap.containsKey(myName) || !serverMap.containsKey(opponentName)) {
                Socket socket = serverSocket.accept();
                if (socket.getInetAddress().getHostAddress().equals(myIP)) {
                    serverMap.put(myName, new ServerThread(socket));
                }
                if (socket.getInetAddress().getHostAddress().equals(opponentIP)) {
                    serverMap.put(opponentName, new ServerThread(socket));
                }
            }

            serverSocket.close();

            // TODO 서버작업
            while (flag) {
                String data1 = serverMap.get(myName).readData;
                String data2 = serverMap.get(opponentName).readData;

                String sendData = myName + ":" + data1 + " " + opponentName + ":" + data2;

                if (data1.equals("ready") && data2.equals("ready")) {
                    sendData = sendData + " ALL PLAYER READY";
                } else {
                    sendData = sendData + " SOMEONE NEED TO PREPARE";
                }

                serverMap.get(myName).write(sendData + "\n");
                serverMap.get(opponentName).write(sendData + "\n");

                this.sleep(1000);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    final class ServerThread extends Thread {
        private boolean flag = true;

        private Socket socket;

        private String readData = "PREPARE";

        private BufferedWriter writer;
        private BufferedReader reader;

        public ServerThread(Socket socket) {
            this.socket = socket;

            try {
                writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (Exception e) {
                e.printStackTrace();
            }

            this.start();
        }

        @Override
        public void run() {
            try {
                while (flag) {
                    readData = reader.readLine();
                }
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void write(String writeData) {
            try {
                writer.write(writeData);
                writer.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
