package com.example.glukdataapp;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;

public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new SettingsFragment()).commit();
    }

    public static class SettingsFragment extends PreferenceFragment{

        private EditTextPreference multicastIp;
        private EditTextPreference multicastPortEditText;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings_screen);
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            multicastIp = (EditTextPreference) findPreference(getString(R.string.pref_multicast_ip_key));
            multicastPortEditText = (EditTextPreference) findPreference(getString(R.string.pref_multicast_port_key));

            multicastIp.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    Boolean isValid = true;
                    if (!checkIpValidity(o.toString())) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("Invalid Input");
                        builder.setMessage("Please enter valid IP address...");
                        builder.setPositiveButton(android.R.string.ok, null);
                        builder.show();
                        isValid = false;
                    }
                    return isValid;
                }
            });
            multicastPortEditText.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    Boolean isValid = true;
                    if (!checkPortValidity(o.toString())) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("Invalid Input");
                        builder.setMessage("Please enter valid port...");
                        builder.setPositiveButton(android.R.string.ok, null);
                        builder.show();
                        isValid = false;
                    }
                    return isValid;
                }
            });

        }

        public static boolean checkIpValidity(String ip){
            try {

                if (ip == null || ip.isEmpty()) {
                    return false;
                }

                if (ip.equals("localhost")) {
                    return true;
                }

                String[] parts = ip.split("\\.");
                if (parts.length != 4) {
                    return false;
                }

                for (String s : parts) {
                    int i = Integer.parseInt(s);
                    if ((i < 0) || (i > 255)) {
                        return false;
                    }
                }
                if (ip.endsWith(".")) {
                    return false;
                }

                return true;
            } catch (NumberFormatException ex) {
                return false;
            }
        }
        public static boolean checkPortValidity(String port){
            try {
                int p = Integer.parseInt(port);
                if (p < 1 || p > 65535) {
                    return false;
                }
                return true;
            } catch (NumberFormatException ex) {
                return false;
            }
        }
    }
}
