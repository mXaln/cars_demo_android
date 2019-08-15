package org.wycliffeassociates.position;

import android.os.AsyncTask;
import android.util.Log;
import com.esotericsoftware.kryonet.Client;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;

public class ServerGetHosts extends AsyncTask<Object, int[], List<Map.Entry<String, InetAddress>>> {

    public interface GetHostListener {
        void onComplete(List<Map.Entry<String, InetAddress>> servers);
    }

    Client client;
    GetHostListener callback;

    @Override
    protected List<Map.Entry<String, InetAddress>> doInBackground(Object... arg0) {
        List<Map.Entry<String, InetAddress>> servers = client.discoverHosts(Network.udpPort, 5000);
        Log.w("TAG", "---------- DISCOVERING HOSTS -----------");
        for(Map.Entry<String, InetAddress> server: servers) {
            Log.w("TAG", server.getKey() + "(" + server.getValue().getHostAddress() + ")");
        }

        return servers;
    }

    @Override
    protected void onPostExecute(List<Map.Entry<String, InetAddress>> servers) {
        super.onPostExecute(servers);

        if(callback != null) {
            callback.onComplete(servers);
        }
    }

    public ServerGetHosts(Client client, GetHostListener callback) {
        this.client = client;
        this.callback = callback;
    }
}
