package com.erise.habaak.driverapp;

import android.content.Context;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
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
    private String url = "http://192.168.0.24/first/admin/";
    private TextView tempView;
    private TextView humiView;

    private TextView mTextMessage;
    String lon, lat;

    private TimerTask timerTempHumiTask;
    private TimerTask speedTask;
    private TimerTask locationTask;
    private TimerTask engineLoadValueTask;
    private TimerTask engineCoolantTemperatureTask;
    private TimerTask enginRPMTask;
    private TimerTask vehicleSpeedTask;
    private TimerTask MAFTask;
    private TimerTask throttlePositionTask;

    private Timer timer;

    private LatLonSender latLonSender;
    private LocationManager manager;
    private GPSListener gpsListener;

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
        startLocationService();
        try {
            server = new Server();
            server.start();
//Timer
            timerTempHumiTask = new TimerTask() {
                @Override
                public void run() {
                    server.sendTempHumi();
                }
            };

            speedTask = new TimerTask() {
                @Override
                public void run() {
                    server.sendSpeed();
                }
            };

            engineLoadValueTask = new TimerTask() {
                @Override
                public void run() {
                    server.sendCanData("engineLoadValue");
                }
            };
            engineCoolantTemperatureTask = new TimerTask() {
                @Override
                public void run() {
                    server.sendCanData("engineCoolantTemperature");
                }
            };
            enginRPMTask = new TimerTask() {
                @Override
                public void run() {
                    server.sendCanData("enginRPM");
                }
            };
            vehicleSpeedTask = new TimerTask() {
                @Override
                public void run() {
                    server.sendCanData("vehicleSpeed");
                }
            };
            MAFTask = new TimerTask() {
                @Override
                public void run() {
                    server.sendCanData("MAF");
                }
            };
            throttlePositionTask = new TimerTask() {
                @Override
                public void run() {
                    server.sendCanData("throttlePosition");
                }
            };
            locationTask = new TimerTask() {
                @Override
                public void run() {
                    latLonSender = (LatLonSender) new LatLonSender().execute(url+"relocation.do",lat,lon);
                }
            };



            timer = new Timer();
            //timer.schedule(timerTempHumiTask,0,5000);
            //timer.schedule(speedTask,0,3000);
            timer.schedule(engineLoadValueTask,0,500);
            timer.schedule(engineCoolantTemperatureTask,0,10000);
            /*timer.schedule(enginRPMTask,0,10000);
            timer.schedule(vehicleSpeedTask,0,11000);
            timer.schedule(MAFTask,0,12000);
            timer.schedule(throttlePositionTask,0,13000);
            timer.schedule(locationTask,0,3000);*/

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
        tempView = v.findViewById(R.id.temp);
        humiView = v.findViewById(R.id.humi);

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
    온도
*/
    public void setTempView(final String tempNum, final String humiNum){
        Runnable r = new Runnable() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("[setTempView]",tempNum + " : "+ humiNum);

                        tempView.setText(tempNum);
                        humiView.setText(humiNum);
                    }
                });
            }
        };
        new Thread(r).start();
    }
    /*public void setHumi(final String msg){
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
    }*/

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
                    //new Sender();
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
                            Log.i("[Receive MSG]",str);
                            //조건문으로 통신 값 분류

                            StringTokenizer st = new StringTokenizer(str, " ");
                            String[] tokenBox = new String[10];
                            for( int x = 0; st.hasMoreElements(); x++ ){
                                //Log.i("[Token]" , x + " : " + st.nextToken() );
                                tokenBox[x]=st.nextToken();
                                Log.i("[Receive MSG array]",tokenBox[x]);
                            }

                            Log.i("[Token]",tokenBox[1] +":"+tokenBox[3]);

                            setTempView(tokenBox[1],tokenBox[3]); //화면 온도 변화
                            latLonSender = (LatLonSender) new LatLonSender().execute(url+"relocation.do",lat,lon);
                            //HTTP
                            SendTempHumiHttp sendTempHumiHttp = (SendTempHumiHttp) new  SendTempHumiHttp().execute(tokenBox[1],tokenBox[3]);


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
        public void sendTempHumi() {
            Log.i("[TEMPELATURE]","send");
            TempHumiSender sender = new TempHumiSender();
            //sender.setMsg(msg);
            sender.start();
        }

        public void sendSpeed(){
            Log.i("[SPEED]","send");
            SpeedSender sender = new SpeedSender();
            sender.start();
        }
        public void sendCanData(String msg) {
            Log.d("[CAN DATA]",msg);
            CanDataSender sender = new CanDataSender();
            sender.setCanMsg(msg);
            sender.start();
        }
        //Send Message All Clients

        //온습도 sender
        class TempHumiSender extends Thread{
            /*String msg;
            public void setMsg(String msg) {
                this.msg = msg;
            }*/

            @Override
            public void run() {
                try {
                    if(!list.isEmpty() && list.size()>=0) {
                        for(DataOutputStream dos : list) {
                            dos.writeUTF("H&T");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        //속도 sender
        class SpeedSender extends Thread{
            /*String msg;
            public void setMsg(String msg) {
                this.msg = msg;
            }*/

            @Override
            public void run() {
                try {
                    if(!list.isEmpty() && list.size()>=0) {
                        for(DataOutputStream dos : list) {
                            dos.writeUTF("v");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        //CAN DATA
        class CanDataSender extends Thread{
            String msg;
            public void setCanMsg(String msg) {
                this.msg = msg;
            }

            @Override
            public void run() {
                try {
                    if(!list.isEmpty() && list.size()>=0) {
                        for(DataOutputStream dos : list) {
                            dos.writeUTF(msg);
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


    //LAT, LON
    private void startLocationService() {

        manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        gpsListener = new GPSListener();
        long minTime = 1000;
        float minDistance = 0;
        try {
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, gpsListener);
            manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistance, gpsListener);
            Location lastLocation = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastLocation != null) {
                Double latitude = lastLocation.getLatitude();
                Double longitude = lastLocation.getLongitude();
                //textView.setText("내 위치 : " + latitude + ", " + longitude);
            }

        } catch (SecurityException ex) {
            ex.printStackTrace();
        }
    }
    private class GPSListener implements LocationListener {
        public void onLocationChanged(Location location) {
            lat = String.valueOf(location.getLatitude());
            lon = String.valueOf(location.getLongitude());
            String msg = "Latitude : "+ lat + "\nLongitude:"+ lon; Log.i("GPSListener", msg);
            //textView.setText("내 위치는 : " + latitude + ", " + longitude);
            Toast.makeText(getActivity().getApplicationContext(), msg, Toast.LENGTH_SHORT).show();

            manager.removeUpdates(gpsListener);

        }
        public void onProviderDisabled(String provider) {

        } public void onProviderEnabled(String provider) {

        } public void onStatusChanged(String provider, int status, Bundle extras) {

        }
    }



    //==================latlon Networking============================
    public class LatLonSender extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... strings) {
            getMainJson(strings[0],strings[1],strings[2]);
            return null;
        }

        private void getMainJson(String url,String lat, String lon){

            try{
                HttpClient client = new DefaultHttpClient();
                HttpPost post = new HttpPost(url);
                Log.i("[LatLonURL]",url);
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
                nameValuePairs.add(new BasicNameValuePair("lat", lat));
                nameValuePairs.add(new BasicNameValuePair("lon", lon));
                nameValuePairs.add(new BasicNameValuePair("busidx", "5"));
                nameValuePairs.add(new BasicNameValuePair(HTTP.CONTENT_TYPE,"application/json"));

                post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                HttpResponse response = client.execute(post);
                BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                String line = "";
                while ((line = rd.readLine()) != null) {
                    Log.i("[Location JSON]",line);

                    JSONArray jsonArray = new JSONArray(line);
                    System.out.println("JSONArray -- "+jsonArray);

                    for(int i = 0 ; i < jsonArray.length(); i++){
                        /*JSONObject jsonObject = jsonArray.getJSONObject(i);
                        //String isSuccess = jsonObject.getString("MainSucess");
                        *//**//*cmt = jsonObject.getString("cmt");
                        uidx = jsonObject.getString("uidx");
                        pidx = jsonObject.getString("pidx");
                        date = jsonObject.getString("dt");
                        like = jsonObject.getString("heart");*//**//*
                        cmtArr[i] = jsonObject.getString("cmt");
                        uidxArr[i] = jsonObject.getString("uidx");
                        pidxArr[i] = jsonObject.getString("pidx");
                        dateArr[i] = jsonObject.getString("dt");
                        likeArr[i] = jsonObject.getString("heart");
                        //imgurlArr[i] = jsonObject.getString("img");*/
                    }
                }
            }catch (Exception e) {
                e.printStackTrace();
            }finally {
            }

        }
    }
}





//ServerSocket Stop....

//  HTTP Client Post
    /*class SendHttp extends AsyncTask<String,Void,Void> {

        //String surl = "http://192.168.0.24/first/admin/retemp.do?temp=27&humid=30&busidx=5";
        String url = "http://192.168.0.24/first/admin/";

        String controller;
        String str;

        public SendHttp(String controller, String deviceid){
            controller = this.controller;
            deviceid = this.str;
        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                String controller = params[0];
                String str = params[1];
                URL url = new URL("http://192.168.0.24/first/admin/"+controller+"?");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("content-type","application/x-www-form-urlencoded");
//Making url
                StringBuffer buffer = new StringBuffer();
                buffer.append("temp").append("=").append(str).append("&");
                buffer.append("humid").append("=").append(str).append("&");
                buffer.append("busidx").append("=").append("5");
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                PrintWriter wr = new PrintWriter(osw);
                wr.write(buffer.toString());
                wr.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }*/
class SendTempHumiHttp extends AsyncTask<String,Void,Void> {
    String surl = "http://192.168.0.24/first/admin/retemp.do?";

    String tempNum;
    String HumidNum;
    HttpURLConnection urlConn;
    URL url;
    public SendTempHumiHttp(){}
    public SendTempHumiHttp(String tempNum, String HumidNum){
        HumidNum = this.HumidNum;
        tempNum = this.tempNum;

    }

    @Override
    protected Void doInBackground(String... params) {
        try {
            String surl = "http://192.168.0.24/first/admin/retemp.do?";
            StringBuffer buffer = new StringBuffer();
            buffer.append(surl);
            buffer.append("temp").append("=").append(params[0]).append("&");
            buffer.append("humid").append("=").append(params[1]).append("&");
            buffer.append("busidx").append("=").append("5");
            surl = buffer.toString();
            Log.i("[URL-retemp]",surl);
            try {
                url = new URL(surl);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            urlConn = (HttpURLConnection) url.openConnection();
            urlConn.getResponseCode();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
   /* class SendTempHumiHttp extends AsyncTask<String,Void,Void>{
        String surl = "http://192.168.0.24/first/admin/retemp.do?humid=&busidx=5&temp=";
        URL url;
        HttpURLConnection urlConn;
        String speed;
        public SendTempHumiHttp(){}
        public SendTempHumiHttp(String speed){
            this.speed = speed;
            surl +=speed;
            try {
                url = new URL(surl);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        @Override
        protected Void doInBackground(String... params) {
            try {
                urlConn = (HttpURLConnection) url.openConnection();
                urlConn.getResponseCode();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }*/

