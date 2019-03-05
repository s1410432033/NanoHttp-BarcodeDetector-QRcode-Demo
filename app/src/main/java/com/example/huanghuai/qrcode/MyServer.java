package com.example.huanghuai.qrcode;

import android.util.Log;

import java.util.ArrayList;

import fi.iki.elonen.NanoHTTPD;

/**
 *
 * Created by huanghuai on 2019/2/25.
 */

public class MyServer extends NanoHTTPD {
    MainActivity main = new MainActivity();
    public MyServer(int port) {
        super(port);
    }

    @Override
    public Response serve(IHTTPSession session) {
        Method method = session.getMethod();
        if (NanoHTTPD.Method.GET.equals(method)){
            Log.e("Size", "serve: "+main.scannList.size() );
            String list = "";
            for(String s : main.scannList){
                list =list+","+s;
            }
            return newFixedLengthResponse(""+list);
        }
        return super.serve(session);
    }
}
