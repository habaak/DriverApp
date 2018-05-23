package com.erise.habaak.driverapp;

import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.lang.Runnable;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HomeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private Server server;
    private TextView temp;
    private TextView humi;

    private TextView mTextMessage;
    private TimerTask timerTask;
    private Timer timer;

    private OnFragmentInteractionListener mListener;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        try {
            server = new Server();
            server.start();
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    server.sendAll();
                }
            };
            timer = new Timer();
            timer.schedule(timerTask,0,3000);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        //  TCP IP
        temp = v.findViewById(R.id.temp);
        humi = v.findViewById(R.id.humi);

        //  RecyclerView
        RecyclerView recyclerView = v.findViewById(R.id.recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        List<Recycler_item> items = new ArrayList<>();
        Recycler_item[] item = new Recycler_item[8];
        item[0]=new Recycler_item(R.drawable.ic_warning,"경고");
        item[1]=new Recycler_item(R.drawable.ic_speed,"과속");
        item[2]=new Recycler_item(R.drawable.ic_temperature_hot,"온도 하강 요청");
        item[3]=new Recycler_item(R.drawable.ic_police,"범죄발생");
        item[4]=new Recycler_item(R.drawable.ic_temperature_hot,"온도 하강 요청");
        item[5]=new Recycler_item(R.drawable.ic_temperature_cold,"온도 상승 요청");
        item[6]=new Recycler_item(R.drawable.ic_speed,"과속");
        item[7]=new Recycler_item(R.drawable.ic_speed,"과속");

        for (int i=0; i<8; i++) {
            items.add(item[i]);
            recyclerView.setAdapter(new RecyclerAdapter(getActivity().getApplicationContext(), items, R.layout.activity_main));
        }

        return v;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    /*@Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }*/

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

//Recycle item
    public class Recycler_item {
        int image;
        String title;

        int getImage(){
            return  this.image;
        }
        String getTitle(){
            return this.title;
        }
        Recycler_item(int image, String title){
            this.image=image;
            this.title=title;
        }
    }


//  TCP IP Server
/*
    온도, 습도, 차량정보
*/
    public void setTemp(final String msg){
        Runnable r = new Runnable() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        temp.setText(msg);
                    }
                });
            }
        };
        new Thread(r).start();
    }
    public void setHumi(final String msg){
        Runnable r = new Runnable() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        humi.setText(msg);
                    }
                });
            }
        };
        new Thread(r).start();
    }

    public class Server extends Thread{

        ServerSocket serverSocket;
        boolean flag = true;
        boolean rflag = true;
        ArrayList<DataOutputStream> list = new ArrayList<>();

        public Server() throws IOException {
            serverSocket = new ServerSocket(8888);
            Log.i("[Server]","Server Ready...");
        }

        //start server
        @Override
        public void run() {
            //Accept Client Connection...
            try {
                while(flag) {
                    Log.i("[Server]","Ready Accept");
                    Socket socket = serverSocket.accept();
                    String client = socket.getInetAddress().getHostAddress();
                    new Receiver(socket).start();
                    new Sender();
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

        class Receiver extends Thread{
            InputStream is;
            DataInputStream dis;
            OutputStream os;
            DataOutputStream dos;
            Socket socket;// Thread가 끝날때 socket을 죽이기 위해

            public Receiver (Socket socket) {
                try {
                    this.socket = socket;
                    is = socket.getInputStream();
                    dis = new DataInputStream(is);
                    os = socket.getOutputStream();
                    dos = new DataOutputStream(os);
                    list.add(dos);
                    Log.d("[Server]","Connected Count : "+list.size());
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }



            @Override
            public void run() {
                try {
                    //client가 보내는 메시지를 받는다.
                    while(rflag) {
                        if(socket.isConnected() && dis != null & dis.available() > 0) {
                            String str = dis.readUTF();
                            setTemp(str);
                            /*SendHttp sendHttp = new SendHttp(str);
                            sendHttp.execute();*/
                        }
                    }

                } catch(Exception e) {
                    e.printStackTrace();
                } finally {
                    list.remove(dos);
                    //Log.d("[Server App]",list.size());
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                    if(dis != null) {
                        try {
                            dis.close();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    if(dos != null) {
                        try {
                            dos.close();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    if(socket != null) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        public void sendAll() {
            Log.i("[Server App]","TEMPELATURE");
            Sender sender = new Sender();
            //sender.setMsg(msg);
            sender.start();
        }

        //Send Message All Clients
        class Sender extends Thread{
            /*String msg;
            public void setMsg(String msg) {
                this.msg = msg;
            }*/

            @Override
            public void run() {
                try {
                    if(!list.isEmpty() && list.size()>=0) {
                        for(DataOutputStream dos : list) {
                            dos.writeUTF("t");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public void stopServer() {
            rflag = false;

        }

    }

//ServerSocket Stop....

//  HTTP Client
    /*class SendHttp extends AsyncTask<Void,Void,Void>{

        String surl = "http://70.12.114.132:8070/webserver/main.do?";
        URL url;
        HttpURLConnection urlConn;
        String speed;

        public SendHttp(){}
        public SendHttp(String speed){
            this.speed = speed;
            surl +=speed;
            try {
                url = new URL(surl);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                urlConn = (HttpURLConnection) url.openConnection();
                urlConn.getResponseCode();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }*/

}
