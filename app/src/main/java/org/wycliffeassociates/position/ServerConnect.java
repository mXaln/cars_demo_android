package org.wycliffeassociates.position;

import android.os.AsyncTask;
import android.util.Log;
import com.esotericsoftware.kryonet.Client;

import java.io.IOException;

public class ServerConnect extends AsyncTask<String, int[], Boolean> {
    public interface ConnectListener {
        void onComplete(boolean connected);
    }

    Client client;
    String host;
    ConnectListener callback;

    @Override
    protected Boolean doInBackground(String... arg0) {
        try {
            client.connect(5000, host, Network.tcpPort, Network.udpPort);
            return true;
        } catch (IOException e) {
            Log.w("TAG", e.toString());
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean connected) {
        super.onPostExecute(connected);

        if(callback != null) {
            callback.onComplete(connected);
        }
    }

    public ServerConnect(Client client, String host, ConnectListener callback) {
        this.client = client;
        this.host = host;
        this.callback = callback;
    }
}
