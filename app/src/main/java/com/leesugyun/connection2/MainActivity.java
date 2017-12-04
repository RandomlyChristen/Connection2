package com.leesugyun.connection2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;

public class MainActivity extends Activity {

    private Button hostBtn;
    private Button joinBtn;
    private ListView hostList;

    protected Intent intent;

    protected Handler handler;

    HostStart hostStart;
    ChallengeStart challengeStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        intent = new Intent(MainActivity.this, Main2Activity.class);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 100) startActivity(intent);
            }
        };

        final String myIP = getLocalServerIp();
        final int bPort = 4516;
        final int cPort = 1335;




        hostList = (ListView) findViewById(R.id.hostList);


        hostBtn = (Button) findViewById(R.id.hostBtn);
        joinBtn = (Button) findViewById(R.id.joinBtn);

        hostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hostBtn.setEnabled(false); joinBtn.setEnabled(false);
                hostStart = new HostStart("host", myIP, bPort, cPort, intent, handler);
            }
        });

        joinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hostBtn.setEnabled(false); joinBtn.setEnabled(false);
                challengeStart = new ChallengeStart("challenger", myIP, bPort, cPort, intent, handler, hostList, MainActivity.this);
            }
        });
    }





    private String getLocalServerIp()
    {
        try
        {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();)
            {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();)
                {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress() && inetAddress.isSiteLocalAddress())
                    {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        }
        catch (SocketException ex) {}
        return null;
    }
}
