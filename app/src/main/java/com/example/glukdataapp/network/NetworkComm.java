package com.example.glukdataapp.network;

import android.content.Context;
import android.os.Looper;
import android.widget.Toast;

import com.example.glukdataapp.realm.RealmControl;
import com.example.glukdataapp.realm.models.GlucoseEntry;
import com.example.glukdataapp.realm.models.InsulinEntry;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import gluklibrary.Glucose;
import gluklibrary.Insulin;
import gluklibrary.Network;

public class NetworkComm implements INetworkComm {

    private Context context;
    private INetworkOperationsListener operationsListener;
    private boolean isAlive;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public INetworkOperationsListener getOperationsListener() {
        return operationsListener;
    }

    public void setOperationsListener(INetworkOperationsListener operationsListener) {
        this.operationsListener = operationsListener;
    }

    public void setAlive(boolean alive) {
        if(operationsListener != null){
            if(alive){
                operationsListener.notifyServerReachable();
            } else {
                operationsListener.notifyServerUnreachable();
            }
        }
        isAlive = alive;
    }

    InetAddress serverAddress;

    public InetAddress getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(InetAddress serverAddress) {
        this.serverAddress = serverAddress;
    }

    public NetworkComm(Context context) {
        this.context = context;
        isAlive = false;
        //TODO create periodic action to check for server reachable
        setScheduledTasks();
    }

    public NetworkComm(Context context, INetworkOperationsListener operationsListener) {
        this.context = context;
        this.operationsListener = operationsListener;
        isAlive = false;
        //TODO create periodic action to check for server reachable
        setScheduledTasks();


    }

    private void setScheduledTasks(){
        Runnable udpRunnable = new Runnable() {
            @Override
            public void run() {
                findServer();
            }
        };
        scheduler.scheduleAtFixedRate(udpRunnable, 0, 1000, TimeUnit.MILLISECONDS);
    }

    private boolean pingServer(){
        try {
            return serverAddress.isReachable(1000);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Glucose getGlucose(GlucoseEntry item){
        Glucose glucose = new Glucose();
        glucose.setTimestamp(item.getTimestamp());
        glucose.setValue(item.getValue());
        return glucose;
    }

    private Insulin getInsulin(InsulinEntry item){
        Insulin insulin = new Insulin();
        insulin.setTimestamp(item.getTimestamp());
        insulin.setValue(item.getValue());
        insulin.setIsDayDosage(item.isDayDosage());
        return insulin;
    }

    @Override
    public InetAddress findServer() {

        try {
            byte[] buf = new byte[100];
            DatagramSocket socket = new DatagramSocket();
            InetAddress group = InetAddress.getByName(Network.IP_MULTICAST);
            buf = Network.EXPECTED_MESSAGE.getBytes();
            DatagramPacket sendPacket
                    = new DatagramPacket(buf, buf.length, group, Network.DEFAULT_PORT_UDP);

            socket.send(sendPacket);
            for (int i = 0; i < buf.length; i++) {
                buf[i] = 0;
            }

            DatagramPacket receivePacket = new DatagramPacket(buf, buf.length);
            socket.setSoTimeout(1000);
            socket.receive(receivePacket);
            String received = new String(buf, 0, buf.length);

            if(received.trim().equals(Network.EXPECTED_RESPONSE)){
                if(!isAlive) setAlive(true);
            } else {
                if(isAlive) setAlive(false);
            }

            serverAddress = receivePacket.getAddress();
            isAlive();
            socket.close();

        } catch (SocketException ex) {
//            Logger.getLogger(NetworkComm.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnknownHostException ex) {
//            Logger.getLogger(NetworkComm.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
//            Logger.getLogger(NetworkComm.class.getName()).log(Level.SEVERE, null, ex);
            if(ex instanceof SocketTimeoutException){
                if(isAlive) setAlive(false);
                isAlive();
            }
        }

        return null;
    }

    @Override
    public void findAndSetServerAddress() {
        setServerAddress(findServer());
    }

    @Override
    public boolean isAlive() {
        if(serverAddress == null) return false;
        if(pingServer()) {
            return true;
        }
        else {
            findServer();
        }
        return pingServer();
    }

    @Override
    public boolean sendGlucose(List<GlucoseEntry> items) {
        byte[] buffer = new byte[10];
        if(!isAlive()){
            return false;
        }

        List<Glucose> glucoseList = new ArrayList<>();
        for (GlucoseEntry item : items) {
            glucoseList.add(getGlucose(item));
        }

        try {
            Socket socket = new Socket(serverAddress, Network.DEFAULT_PORT);
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            BufferedReader inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            outputStream.writeObject(glucoseList);
            outputStream.flush();
            socket.setSoTimeout(10000);

            String response = inputStream.readLine();
            //TODO handle response
            if(response.trim().equals(Network.OBJECT_ACCEPTED_MESSAGE)) {
                if(operationsListener != null){
                    operationsListener.sendGlucoseSuccess();
                }
            } else if(response.trim().equals(Network.OBJECT_REJECTED_MESSAGE)) {
                if(operationsListener != null){
                    operationsListener.sendGlucoseFailure();
                }
            } else {
                System.out.println("unknown");
            }

            socket.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean sendInsulin(List<InsulinEntry> items) {

        if(!isAlive()){
            return false;
        }

        List<Insulin> insulinList = new ArrayList<>();
        for (InsulinEntry item : items) {
            insulinList.add(getInsulin(item));
        }

        try {
            Socket socket = new Socket(serverAddress, Network.DEFAULT_PORT);
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            BufferedReader inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            outputStream.writeObject(insulinList);
            outputStream.flush();
            socket.setSoTimeout(10000);

            String response = inputStream.readLine();
            //TODO handle response
            if(response.trim().equals(Network.OBJECT_ACCEPTED_MESSAGE)) {
                if(operationsListener != null){
                    operationsListener.sendInsulinSuccess();
                }
            } else if(response.trim().equals(Network.OBJECT_REJECTED_MESSAGE)) {
                if(operationsListener != null){
                    operationsListener.sendInsulinFailure();
                }
            } else {
                System.out.println("unknown");
            }

            socket.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

    }

    public interface INetworkOperationsListener{
        void notifyServerReachable();
        void notifyServerUnreachable();
        void sendGlucoseSuccess();
        void sendGlucoseFailure();
        void sendInsulinSuccess();
        void sendInsulinFailure();
    }
}
