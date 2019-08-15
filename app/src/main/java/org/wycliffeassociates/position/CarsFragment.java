package org.wycliffeassociates.position;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.ClientDiscoveryHandler;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.*;

public class CarsFragment extends Fragment implements ClientDiscoveryHandler {

    Client client;
    String name;
    String host;
    String serverName;
    Character me;
    boolean isDead;

    HostsListAdapter mAdapter;

    CanvasView canvas;
    TextView serverLabel;
    EditText serverInput;
    TextView connectLabel;
    EditText nameInput;
    TextView message;
    ListView hostsList;
    Button hostsRefresh;

    HashMap<Integer, Character> characters = new HashMap();

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.cars_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        canvas = view.findViewById(R.id.canvas);
        hostsList = view.findViewById(R.id.hostsList);
        nameInput = view.findViewById(R.id.nameInput);
        serverInput = view.findViewById(R.id.serverInput);
        message = view.findViewById(R.id.message);
        hostsRefresh = view.findViewById(R.id.hostsRefresh);
        serverLabel = view.findViewById(R.id.serverLabel);
        connectLabel = view.findViewById(R.id.connectLabel);

        nameInput.setVisibility(View.INVISIBLE);
        canvas.setVisibility(View.INVISIBLE);

        init();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    protected void init() {
        client = new Client();
        client.setDiscoveryHandler(this);
        client.addListener(new Listener() {
            @Override
            public void idle(Connection connection) {}
            public void connected (Connection connection) {
                Log.w("TAG", "***** Android Client Connected *****");
            }
            public void received(Connection c, Object object) {
                if (object instanceof Network.RegistrationRequired) {
                    Network.Register register = new Network.Register();
                    register.name = name;
                    new ServerSend(client).execute(register);
                }

                if (object instanceof Network.AddCharacter) {
                    Network.AddCharacter msg = (Network.AddCharacter)object;
                    addCharacter(msg);
                    return;
                }

                if (object instanceof Network.UpdateCharacter) {
                    Network.UpdateCharacter msg = (Network.UpdateCharacter)object;
                    updateCharacter(msg);

                    Date now = new Date();
                    int diff = (int)(now.getTime() - me.started) / 1000;

                    // Check if me intersects others in 10 seconds after respawn
                    if(diff > 10) {
                        for(Map.Entry<Integer, Character> car: characters.entrySet()) {
                            diff = (int)(now.getTime() - car.getValue().started) / 1000;
                            if(diff > 10 && isIntersected(me, car.getValue())) {
                                long restartedTime = new Date().getTime();
                                me.started = restartedTime;
                                isDead = true;
                                gameOver();
                            }
                        }
                    }
                    return;
                }

                if (object instanceof Network.RemoveCharacter) {
                    Network.RemoveCharacter msg = (Network.RemoveCharacter)object;
                    removeCharacter(msg);
                    return;
                }
            }

            public void disconnected (Connection connection) {
                System.exit(0);
            }
        });

        client.start();
        Network.register(client);

        mAdapter = new HostsListAdapter(getActivity(), new ArrayList(), new HostsListAdapter.HostClickListener() {
            @Override
            public void onClick(Map.Entry<String, InetAddress> server) {
                host = server.getValue().getHostAddress();
                message.setText("Connecting to " + server.getKey() + "(" + host + ")");
                message.setVisibility(View.VISIBLE);
                new ServerConnect(client, host, new ServerConnect.ConnectListener() {
                    @Override
                    public void onComplete(boolean connected) {
                        if(connected) {
                            nameInput.setVisibility(View.VISIBLE);
                            serverLabel.setVisibility(View.INVISIBLE);
                            serverInput.setVisibility(View.INVISIBLE);
                            connectLabel.setVisibility(View.INVISIBLE);
                            hostsList.setVisibility(View.INVISIBLE);
                            hostsRefresh.setVisibility(View.INVISIBLE);
                        } else {
                            Toast.makeText(getView().getContext(), "Connection failed", Toast.LENGTH_SHORT).show();
                        }
                        message.setVisibility(View.INVISIBLE);
                    }
                }).execute();
            }
        });
        hostsList.setAdapter(mAdapter);

        message.setText("Looking for servers...");

        new ServerGetHosts(client, new ServerGetHosts.GetHostListener() {
            @Override
            public void onComplete(List<Map.Entry<String, InetAddress>> servers) {
                message.setVisibility(View.INVISIBLE);
                mAdapter.servers = servers;
                mAdapter.notifyDataSetChanged();

                System.out.println(servers);
            }
        }).execute();

        serverInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    serverName = serverInput.getText().toString();

                    if(!serverName.isEmpty()) {
                        try {
                            new PositionServer(getContext(), serverName);
                            serverInput.setFocusable(false);
                            hostsRefresh.performClick();
                        } catch(IOException e) {
                            Log.w("TAG", e.toString());
                        }
                    }

                    InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });

        hostsRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                message.setText("Looking for servers...");
                message.setVisibility(View.VISIBLE);
                mAdapter.servers.clear();
                mAdapter.notifyDataSetChanged();
                new ServerGetHosts(client, new ServerGetHosts.GetHostListener() {
                    @Override
                    public void onComplete(List<Map.Entry<String, InetAddress>> servers) {
                        message.setVisibility(View.INVISIBLE);
                        mAdapter.servers = servers;
                        mAdapter.notifyDataSetChanged();

                        System.out.println(servers);
                    }
                }).execute();
            }
        });

        nameInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    name = nameInput.getText().toString();

                    if(!name.isEmpty() && !host.isEmpty()) {
                        nameInput.setVisibility(View.INVISIBLE);
                        canvas.setVisibility(View.VISIBLE);

                        Network.Login login = new Network.Login();
                        login.name = name;

                        new ServerSend(client).execute(login);
                    }

                    InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });

        canvas.setCallback(new CanvasView.DrawListener() {
            @Override
            public void onDraw(int x, int y) {
                if(isDead) return;

                Network.MoveCharacter msg = new Network.MoveCharacter();
                msg.x = x;
                msg.y = y;

                if (msg != null) new ServerSend(client).execute(msg);
            }

            @Override
            public void onDrawFinished(int x, int y) {
                if(isDead) return;

                Network.MoveFinishedCharacter msg = new Network.MoveFinishedCharacter();
                msg.x = x;
                msg.y = y;

                if (msg != null) new ServerSend(client).execute(msg);
            }
        });
    }

    private void refreshCanvas() {
        canvas.isBlocked = true;
        canvas.setCharacters(characters);
        canvas.clearCanvas();
    }

    @Override
    public DatagramPacket onRequestNewDatagramPacket() {
        byte[] recvBuf = new byte[15000];
        DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
        return packet;
    }

    @Override
    public void onDiscoveredHost(DatagramPacket datagramPacket) {

    }

    @Override
    public void onFinally() {

    }

    public void addCharacter(Network.AddCharacter msg) {
        characters.put(msg.character.id, msg.character);

        if(name.equals(msg.character.name)) {
            me = msg.character;
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                refreshCanvas();
            }
        });
    }

    public void updateCharacter(Network.UpdateCharacter msg) {
        Character character = characters.get(msg.id);
        if (character == null) return;
        character.x = msg.x;
        character.y = msg.y;

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                refreshCanvas();
            }
        });
    }

    public void removeCharacter(Network.RemoveCharacter msg) {
        characters.remove(msg.id);

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                refreshCanvas();
            }
        });
    }

    public boolean isIntersected(Character car1, Character car2) {
        if(car1.id == car2.id) return false;

        if((car1.x + 60) >= car2.x && (car2.x + 60) >= car1.x
                && (car1.y + 60) >= car2.y && (car2.y + 60) >= car1.y) {
            return true;
        }

        return false;
    }

    public void gameOver() {
        characters.get(me.id).color = 0;
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                alertDialogBuilder.setMessage("Oooops!!!");
                alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        System.exit(0);
                    }
                });
                alertDialogBuilder.setCancelable(false);

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        });
    }
}
