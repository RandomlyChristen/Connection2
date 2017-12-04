package com.leesugyun.connection2;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Main2Activity extends Activity {

    private String myName;
    private String myIP;
    private String opponentName;
    private String opponentIP;
    private String isHost;

    private ServerWork serverWork;
    private ClientWork clientWork;

    private Button readyBtn;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        int gamePort = 1451;

        myName = getIntent().getStringExtra("myName");
        myIP = getIntent().getStringExtra("myIP");
        opponentName = getIntent().getStringExtra("opponentName");
        opponentIP = getIntent().getStringExtra("opponentIP");
        isHost = getIntent().getStringExtra("isHost");


        if (isHost.equals("yes")) {
            serverWork = new ServerWork(myName, myIP, opponentName, opponentIP, gamePort);
            clientWork = new ClientWork(myIP, gamePort);
        } else {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            clientWork = new ClientWork(opponentIP, gamePort);
        }

        textView = (TextView) findViewById(R.id.textView);
        readyBtn = (Button) findViewById(R.id.readyBtn);
        readyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clientWork.write("ready");
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("Data", clientWork.getReadData());
                            textView.setText(clientWork.getReadData());
                        }
                    });
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();


    }
}
