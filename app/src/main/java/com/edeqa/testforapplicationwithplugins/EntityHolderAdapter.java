package com.edeqa.testforapplicationwithplugins;

import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.edeqa.eventbus.EntityHolder;
import com.edeqa.waytous.WaytousPlugin;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;

/**
 * Created 8/16/2017.
 */
class EntityHolderAdapter implements EntityHolder {

    private static final String LOG_TAG = "EHA";
    
    private final WaytousPlugin plugin;

    public EntityHolderAdapter(WaytousPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void setContext(Object o) {
        Bundle m = new Bundle();
        m.putString("context", "context1");
        try {
            plugin.setContext(m);
        } catch (RemoteException ex) {
            Log.e(LOG_TAG, "setContext", ex);
        }
    }

    @Override
    public String getType() {
        String result = null;
        try {
            result = plugin.getType();
        } catch (RemoteException ex) {
            Log.e(LOG_TAG, "getType", ex);
        }
        Log.d(LOG_TAG, "getType result: " + result);
        return result;
    }

    @Override
    public void start() {
        try {
            plugin.start();
            Log.e(LOG_TAG, "start");
        } catch (RemoteException ex) {
            Log.e(LOG_TAG, "start", ex);
        }
    }

    @Override
    public void finish() {
        try {
            plugin.finish();
            Log.e(LOG_TAG, "finish");
        } catch (RemoteException ex) {
            Log.e(LOG_TAG, "finish", ex);
        }
    }

    @Override
    public List<String> events() {
        List<String> result = null;
        try {
            result = plugin.events();
        } catch (RemoteException ex) {
            Log.e(LOG_TAG, "events", ex);
        }
        Log.d(LOG_TAG, "events result: " + result);
        return result;
    }

    @Override
    public boolean onEvent(String s, Object o) {
        System.out.println("TYPE:" + getType());
        boolean result = true;
        if (plugin != null) {
            try {
                Bundle m = new Bundle();
                m.putSerializable("object", (Serializable) o);
                result = plugin.onEvent(s, m);
            } catch (RemoteException ex) {
                Log.e(LOG_TAG, "onEvent", ex);
            }
            Log.d(LOG_TAG, "getType result: " + result);
        }
        return result;
    }

    @Override
    public void setLoggingLevel(Level level) {
        try {
            Bundle m = new Bundle();
            m.putSerializable("object", (Serializable) level);
            plugin.setLoggingLevel(m);
            Log.e(LOG_TAG, "setLoggingLevel");
        } catch (RemoteException ex) {
            Log.e(LOG_TAG, "setLoggingLevel", ex);
        }
    }
}
