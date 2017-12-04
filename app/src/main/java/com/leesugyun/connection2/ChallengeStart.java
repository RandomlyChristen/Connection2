package com.leesugyun.connection2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by LeeSugyun on 2017-12-03.
 */

public class ChallengeStart {

    private String challengerName;
    private String challengerIP;

    private int broadcastPort;
    private int connectionPort;

    private Intent intent;
    private Handler handler;

    private SearchingThread searchingThread;

    private ArrayList<HashMap<String, String>> hostArray;
    private ListView hostList;
    private SimpleAdapter adapter;
    private Context context;
    private Activity activity;

    public ChallengeStart(String challengerName, String challengerIP, int broadcastPort, int connectionPort, Intent intent, Handler handler, ListView hostList, Context context) {
        this.challengerName = challengerName; // 호스트를 검색하여 접속할 도전자의 정보
        this.challengerIP = challengerIP;
        this.broadcastPort = broadcastPort; // 호스트의 방송을 청취할 포트
        this.connectionPort = connectionPort; // 호스트의 서버에 접속할 포트
        this.intent = intent; // TODO 연결결과를 담는 인텐트
        this.handler = handler; // TODO 방송을 마치고 연결을 확인후, 다음으로의 진행을 구현하는 핸들러

        this.hostArray = new ArrayList<>(); // 호스트의 방송 청취 결과를 담는 어레이리스트
        this.hostList = hostList; // 호스트의 방송 청취 결과를 보여주는 리스트뷰
        this.context = context; // 리스트 뷰가 선언되어 존재하는 콘택스트 (엑티비티 = (엑티비티) 콘텍스트)
        this.activity = (Activity) context;
        // 방송 청취 결과를 담은 어레이리스트를 리스트뷰에 적용시킬 심플어뎁터
        adapter = new SimpleAdapter(context, hostArray, android.R.layout.simple_list_item_2, new String[]{"foundName","foundIP"}, new int[]{android.R.id.text1, android.R.id.text2});
        hostList.setAdapter(adapter); // 리스트뷰에 어뎁터 세팅

        hostList.setOnItemClickListener(new HostListOnClick()); // 리스트뷰에 온아이템클릭리스너 세팅, TODO 해당 클래스 아래 구현

        searchingThread = new SearchingThread(); // 호스트를 검색하는 스레드, TODO 해당 클래스 아래 구현
        searchingThread.start();
    }



    // TODO 호스트를 검색하는 스레드
    final class SearchingThread extends Thread {
        private boolean flag = true; // 스레드를 제어 할 플래그변수

        @Override
        public void run() {
            try {
                DatagramSocket socket = new DatagramSocket(broadcastPort); // 방송 청취에 사용될 데이터그램 소켓
                byte[] broadcastData = new byte[20]; // 청취 데이터를 담을 버퍼

                while (flag) {
                    // 방송을 청취하여 버퍼에 담는 패킷
                    DatagramPacket packet = new DatagramPacket(broadcastData, broadcastData.length);
                    socket.receive(packet); // 소켓으로부터 한번 씩 패킷을 청취

                    String foundName = new String(packet.getData()); // 청취한 데이터 (방송을 한 호스트의 이름)
                    String foundIP = packet.getAddress().getHostAddress(); // 청취한 패킷의 호스트 주소

                    // 청취 내용(호스트 이름, 호스트 주소)을 해쉬맵에 담고, 그 해쉬맵이 어레이리스트에 없으면, 새로 넣어줌
                    HashMap<String, String> resultMap = new HashMap<>();
                    resultMap.put("foundName", foundName);
                    resultMap.put("foundIP", foundIP);
                    if (!hostArray.contains(resultMap)) hostArray.add(resultMap);

                    // TODO 지정한 콘텍스트의 엑티비티의 메인스레드에서 리스트뷰의 내용을 초기화 시켜줌
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                        }
                    });

                    this.sleep(1000); // 1초마다 청취
                }

                socket.close(); // 스레드가 종료 되기전, 데이터그램 소켓을 닫음

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    // TODO 리스트뷰의 항목을 클릭하면, 해당 항목의 호스트 내용을 인텐트에 정리하고 다음 액티비티로 진행할 리스너
    final class HostListOnClick implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            HashMap<String, String> hashMap = hostArray.get(i); // 클릭한 아이템의 인덱스와 같은 어레이리스트의 엘리먼트를 가져옴

            String opponentName = hashMap.get("foundName"); // 가져온 엘리먼트에서 검색한 호스트 이름과 IP를 추출
            String opponentIP = hashMap.get("foundIP");

            try {
                Socket socket = new Socket(opponentIP, connectionPort); // 호스트의 서버소켓에 연결 할 소켓

                // 접속한 소켓으로 데이터를 전송 할 버퍼라이터
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                writer.write(challengerName + "\n"); // 도전자의 이름을 전송
                writer.flush();

                socket.close(); // 전송이 끝나면, 전송을 위해 연 소켓을 닫음

                intent.putExtra("myName", challengerName);
                intent.putExtra("myIP", challengerIP);
                intent.putExtra("opponentName", opponentName);
                intent.putExtra("opponentIP", opponentIP);
                intent.putExtra("isHost", "no"); // 이 클라이언트는 이후에 서버를 열지 않고, 접속만 함

                searchingThread.flag = false; // 호스트를 검색하고, 리스트뷰에 올리는 작업을 중지

                handler.sendEmptyMessage(100); // TODO 핸들러에게 엑티비티를 시작하기위한 메세지를 보냄 : 100

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
