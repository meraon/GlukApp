package com.example.glukdataapp.network;

import com.example.glukdataapp.realm.models.GlucoseEntry;
import com.example.glukdataapp.realm.models.InsulinEntry;

import java.net.InetAddress;
import java.util.List;

public interface INetworkComm {
    InetAddress findServer();
    void findAndSetServerAddress();
    boolean isAlive();
    boolean sendGlucose(List<GlucoseEntry> items);
    boolean sendInsulin(List<InsulinEntry> items);

}
