package com.leesugyun.connection2;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * Created by yylwd on 2017-12-03.
 */

public class ClientWork extends Thread {
    private boolean flag = true;

    private Socket socket;

    private BufferedReader reader;
    private BufferedWriter writer;

    private String readData = "";

    public String getReadData() {
        return readData;
    }

    public ClientWork(String serverIP, int gamePort) {
        try {
            this.socket = new Socket(serverIP, gamePort);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
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
                Log.d("Client", readData);
            }
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void write(String writeData) {
        try {
            writer.write(writeData + "\n");
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
