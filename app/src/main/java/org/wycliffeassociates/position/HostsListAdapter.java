package org.wycliffeassociates.position;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;

public class HostsListAdapter extends BaseAdapter {
    List<Map.Entry<String, InetAddress>> servers;
    LayoutInflater layoutInflater;
    HostClickListener callback;

    public HostsListAdapter(Activity activity, List<Map.Entry<String, InetAddress>> servers, HostClickListener callback) {
        this.servers = servers;
        this.callback = callback;
        layoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public interface HostClickListener {
        void onClick(Map.Entry<String, InetAddress> server);
    }

    @Override
    public int getCount() {
        return servers.size();
    }

    @Override
    public Object getItem(int position) {
        return servers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public boolean isEnabled(int position) {
        return super.isEnabled(position);
    }

    private static class ViewHolder {
        TextView serverName;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        final Map.Entry<String, InetAddress> item = servers.get(position);

        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = layoutInflater.inflate(R.layout.host_name, null);
            viewHolder.serverName = convertView.findViewById(R.id.serverName);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.serverName.setText(item.getKey());

        viewHolder.serverName.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                if(callback != null) {
                    callback.onClick(item);
                }
                Log.w("TAG", item.getKey());
            }

        });

        return convertView;
    }
}
