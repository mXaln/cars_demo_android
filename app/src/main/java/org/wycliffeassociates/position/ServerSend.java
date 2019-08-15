package org.wycliffeassociates.position;

import android.os.AsyncTask;
import android.util.Log;
import com.esotericsoftware.kryonet.Client;

import java.io.IOException;

public class ServerSend extends AsyncTask<Object, int[], String> {
    Client client;

    @Override
    protected String doInBackground(Object... arg0) {
        if(arg0.length == 0) return null;
        Object obj = arg0[0];

        try {
            if(obj instanceof Network.MoveCharacter) {
                client.sendUDP(obj);
            } else {
                client.sendTCP(obj);
            }
            return "Sending " + obj.toString();
        } catch (Exception e) {
            Log.w("TAG", e.toString());
            return "Sending failed: " + obj.toString();
        }
    }

    public ServerSend(Client client) {
        this.client = client;
    }
}
