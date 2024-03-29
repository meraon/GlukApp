package com.example.glukdataapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.glukdataapp.fragments.GlucoseEntryFragment;
import com.example.glukdataapp.fragments.GlucoseListFragment;
import com.example.glukdataapp.fragments.InsulinEntryFragment;
import com.example.glukdataapp.fragments.InsulinListFragment;
import com.example.glukdataapp.network.INetworkComm;
import com.example.glukdataapp.network.NetworkComm;
import com.example.glukdataapp.realm.IRealmControl;
import com.example.glukdataapp.realm.RealmControl;

import java.util.logging.Logger;

import io.realm.Realm;
import main.java.gluklibrary.HelperMethods;


public class MainActivity extends AppCompatActivity implements GlucoseEntryFragment.OnFragmentInteractionListener, NavigationView.OnNavigationItemSelectedListener {

    Logger LOG = Logger.getLogger(MainActivity.class.getSimpleName());
    private DrawerLayout drawer;

    IRealmControl realmController;
    INetworkComm networkComm;
    Handler handler;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initMyResources();
        Realm.init(this);

        //load content view and toolbar
        setContentView(R.layout.activity_main);
        Toolbar toolbar = this.findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //load navigation view
        drawer = findViewById(R.id.drawer_layout);
        NavigationView nav_view = findViewById(R.id.nav_view);
        final ImageView serverStatus = nav_view.getHeaderView(0).findViewById(R.id.server_status_icon);
        nav_view.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,
                drawer,
                toolbar,
                R.string.nav_drawer_open, R.string.nav_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //init handler and realm
        handler = new Handler();
        realmController = new RealmControl();

        //init shared preferences
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        //init network
        NetworkComm.INetworkOperationsListener operationsListener = new NetworkComm.INetworkOperationsListener() {
            @Override
            public void notifyServerReachable() {
                Runnable setImageRunnable = new Runnable() {
                    @Override
                    public void run() {
                        serverStatus.setImageDrawable(getDrawable(R.drawable.ic_lens_green_24dp));
                    }
                };
                handler.post(setImageRunnable);
            }

            @Override
            public void notifyServerUnreachable() {
                Runnable setImageRunnable = new Runnable() {
                    @Override
                    public void run() {
                        serverStatus.setImageDrawable(getDrawable(R.drawable.ic_lens_red_24dp));
                    }
                };
                handler.post(setImageRunnable);
            }

            @Override
            public void sendGlucoseSuccess() {
                boolean deleteAfterUpload = prefs.getBoolean(getString(R.string.pref_delete_after_upload_key), false);
                if (deleteAfterUpload){
                    realmController.clearGlucoses();
                }
            }

            @Override
            public void sendGlucoseFailure() {
                makeToast("Server was unable to receive glucose data");
            }

            @Override
            public void sendInsulinSuccess() {
                boolean deleteAfterUpload = prefs.getBoolean(getString(R.string.pref_delete_after_upload_key), false);
                if (deleteAfterUpload){
                    realmController.clearInsulins();
                }
            }

            @Override
            public void sendInsulinFailure() {
                makeToast("Server was unable to receive insulin data");
            }
        };
        networkComm = new NetworkComm(this, operationsListener);

        //load default fragment
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                GlucoseEntryFragment.newInstance())
                .commit();

        //reset preferences to default values DEBUG MODE
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();

    }

    private void initMyResources() {
        MyResources resources = MyResources.getInstance();
        resources.setDateFormat(getString(R.string.date_format));
        resources.setTimeFormat(getString(R.string.time_format));
        resources.setGlucoseValueFormat(getString(R.string.glucose_value_format));
        resources.setInsulinValueFormat(getString(R.string.insulin_value_format));
    }

    public void makeToast(final String message) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }

    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        drawer.closeDrawer(GravityCompat.START);
        switch (menuItem.getItemId()) {
            case R.id.value_input:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        GlucoseEntryFragment.newInstance())
                        .commit();

                break;

            case R.id.insulin_input:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        InsulinEntryFragment.newInstance())
                        .commit();

                break;

            case R.id.values_list:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        GlucoseListFragment.newInstance())
                        .commit();

                break;
            case R.id.insulin_applied_list:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        InsulinListFragment.newInstance())
                        .commit();
                break;

            case R.id.upload:
                if (networkComm.isAlive()) {
                    Runnable sendRunnable = new Runnable() {
                        @Override
                        public void run() {
                            networkComm.sendGlucose(realmController.getGlucoseList());
                            networkComm.sendInsulin(realmController.getInsulinList());
                            makeToast("Upload success!");
                        }
                    };
                    Thread sendThread = new Thread(sendRunnable, "sendData");
                    sendThread.start();
                } else {
                    makeToast("Server unreachable");
                }
                break;
            case R.id.settings:
//                realmController.clearRealm();

                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                break;

        }

        return true;
    }


}
