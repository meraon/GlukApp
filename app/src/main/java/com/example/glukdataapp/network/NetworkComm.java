package com.example.glukdataapp.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.glukdataapp.R;
import com.example.glukdataapp.SettingsActivity;
import com.example.glukdataapp.realm.models.GlucoseEntry;
import com.example.glukdataapp.realm.models.InsulinEntry;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
import java.util.logging.Logger;

import main.java.gluklibrary.Glucose;
import main.java.gluklibrary.Insulin;
import main.java.gluklibrary.Network;

public class NetworkComm implements INetworkComm {
    private static final Logger LOG = Logger.getLogger(NetworkComm.class.getSimpleName());

    private Context context;
    private INetworkOperationsListener operationsListener;
    private boolean isAlive;
    private SharedPreferences prefs;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private int portTcp = 0;

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
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        setScheduledTasks();
    }

    public NetworkComm(Context context, INetworkOperationsListener operationsListener) {
        this.context = context;
        this.operationsListener = operationsListener;
        isAlive = false;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        setScheduledTasks();
    }

    private void setScheduledTasks(){
        Runnable udpRunnable = new Runnable() {
            @Override
            public void run() {
                findServer();
            }
        };
        scheduler.scheduleAtFixedRate(udpRunnable, 0, 4000, TimeUnit.MILLISECONDS);
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
            DatagramSocket socket = new DatagramSocket();
            InetAddress group = InetAddress.getByName(prefs.getString(
                    context.getString(R.string.pref_multicast_ip_key),
                    context.getString(R.string.pref_multicast_ip_default_value)));
            byte[] buf = Network.EXPECTED_MESSAGE.getBytes();
            int port = Integer.parseInt(prefs.getString(
                    context.getString(R.string.pref_multicast_port_key),
                    context.getString(R.string.pref_multicast_port_default_value)));
            DatagramPacket sendPacket
                    = new DatagramPacket(buf,
                    buf.length,
                    group,
                    port);

            socket.send(sendPacket);
            for (int i = 0; i < buf.length; i++) {
                buf[i] = 0;
            }

            DatagramPacket receivePacket = new DatagramPacket(buf, buf.length);
            socket.setSoTimeout(3000);
            socket.receive(receivePacket);
            String received = new String(buf, 0, buf.length).trim();

            if(SettingsActivity.SettingsFragment.checkPortValidity(received)){
                if(!isAlive) {
                    setAlive(true);
                    portTcp = Integer.parseInt(received);
                }
            } else {
                if(isAlive) {
                    setAlive(false);
                }
            }

            serverAddress = receivePacket.getAddress();
            socket.close();

        } catch (SocketException ex) {
            LOG.severe(ex.toString());
        } catch (UnknownHostException ex) {
            LOG.severe(ex.toString());
        } catch (IOException ex) {
            LOG.severe(ex.toString());
            if(ex instanceof SocketTimeoutException){
                if(isAlive) setAlive(false);
            }
        }
        catch (NumberFormatException ex){
            LOG.severe(ex.toString());
        }

        return null;
    }

    @Override
    public void findAndSetServerAddress() {
        setServerAddress(findServer());
    }

    @Override
    public boolean isAlive() {
        return isAlive;
    }

    @Override
    public boolean sendGlucose(List<GlucoseEntry> items) {
        if(!isAlive()){
            return false;
        }

        List<Glucose> glucoseList = new ArrayList<>();
        for (GlucoseEntry item : items) {
            glucoseList.add(getGlucose(item));
        }

        try {
            Socket socket = new Socket(serverAddress, portTcp);
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            BufferedReader inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            Gson gson = new Gson();
            PrintWriter pw = new PrintWriter(outputStream);
            pw.println(gson.toJson(glucoseList));
            pw.flush();
            socket.setSoTimeout(10000);

            String response = inputStream.readLine();
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
            Socket socket = new Socket(serverAddress, portTcp);
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            BufferedReader inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            Gson gson = new Gson();
            PrintWriter pw = new PrintWriter(outputStream);
            pw.println(gson.toJson(insulinList));
            pw.flush();
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
