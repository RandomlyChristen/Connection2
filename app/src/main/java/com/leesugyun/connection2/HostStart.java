package com.leesugyun.connection2;

import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by LeeSugyun on 2017-12-03.
 */

public class HostStart {

    private String hostName;
    private String hostIP;
    private Intent intent;
    private int broadcastPort;
    private int connectionPort;

    private String broadcastIP;

    private BroadcastingThread broadcastingThread;
    private ConnectionWaitingThread connectionThread;

    private Handler handler;

    public HostStart(String hostName, String hostIP, int broadcastPort, int connectionPort, Intent intent, Handler handler) {
        this.hostName = hostName; // 호스트의 정보
        this.hostIP = hostIP;
        this.broadcastPort = broadcastPort; // 데이터그램 방송에 사용될 포트
        this.connectionPort = connectionPort; // 연결확인에 사용될 포트
        this.intent = intent; // TODO 연결결과를 담는 인텐트
        this.handler = handler; // TODO 방송을 마치고 연결을 확인후, 다음으로의 진행을 구현하는 핸들러

        broadcastIP = hostIP.substring(0, hostIP.length() - 1) + "255"; // 방송에 사용 될 로컬 주소 (255 : 광역)

        broadcastingThread = new BroadcastingThread();
        broadcastingThread.start();
        connectionThread = new ConnectionWaitingThread();
        connectionThread.start();
    }


    // 호스팅 정보를 방송해주는 스레드
    final class BroadcastingThread extends Thread {
        private boolean flag = true; // 데이터그램 방송을 제어할 플래그변수

        @Override
        public void run() {
            try {
                DatagramSocket socket = new DatagramSocket(); // 방송에 사용 될 데이터그램 소켓
                InetAddress ip = InetAddress.getByName(broadcastIP); // 방송할 로컬 주소
                byte[] broadcastData = hostName.getBytes(); // 방송할 데이터 (호스트 이름)

                while (flag) {
                    // 방송할 네트워크 포트로 전송할 패킷
                    DatagramPacket packet = new DatagramPacket(broadcastData, broadcastData.length, ip, broadcastPort);
                    socket.send(packet); // 방송
                    this.sleep(1000);
                }

                socket.close(); // 스레드가 종료 되기전, 데이터그램 소켓을 닫음

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    // 하나의 연결을 대기하는 스레드
    final class ConnectionWaitingThread extends Thread {
        private String opponentName; // 접속하는 클라이언트가 제공하는 데이터
        private String opponentIP; // 접속하는 클라이언트의 IP

        @Override
        public void run() {
            try {
                ServerSocket serverSocket = new ServerSocket(connectionPort); // 한번의 접속을 대기할 서버소켓
                Socket socket = serverSocket.accept(); // 한번의 접속을 받음
                serverSocket.close();

                // 접속을 받은 소켓에서 데이터를 읽을 리더
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // 리더로부터 클라이언트가 제공하는 이름과 IP를 받음
                opponentName = reader.readLine();
                opponentIP = socket.getInetAddress().getHostAddress();

                socket.close(); // 수신이 되고나면, 수신을 대기하던 소켓을 닫음

                // TODO 다음 엑티비티로 보낼 인텐트에 내용을 담음
                intent.putExtra("myName", hostName);
                intent.putExtra("myIP", hostIP);
                intent.putExtra("opponentName", opponentName);
                intent.putExtra("opponentIP", opponentIP);
                intent.putExtra("isHost", "yes"); // 이 클라이언트는 이후에 서버를 엶

                broadcastingThread.flag = false; // 데이터그램 방송을 중지

                handler.sendEmptyMessage(100); // TODO 핸들러에게 엑티비티를 시작하기위한 메세지를 보냄 : 100

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
