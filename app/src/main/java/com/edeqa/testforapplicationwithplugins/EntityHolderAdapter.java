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

    private String type;

    public EntityHolderAdapter(WaytousPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void setContext(Object o) {
        try {
            Bundle m = new Bundle();
            m.putSerializable("context", (Serializable) o);
            plugin.setContext(m);
        } catch (Exception e) {
            Log.e(LOG_TAG, "setContext", e);
        }
    }

    @Override
    public String getType() {
        if(type != null) return type;
        try {
            type = plugin.getType();

        } catch (RemoteException e) {
            Log.e(LOG_TAG, "getType", e);
        }
        return type;
    }

    @Override
    public void start() {
        try {
            plugin.start();
        } catch (RemoteException e) {
            Log.e(LOG_TAG, "start", e);
        }
    }

    @Override
    public void finish() {
        try {
            plugin.finish();
        } catch (RemoteException e) {
            Log.e(LOG_TAG, "finish", e);
        }
    }

    @Override
    public List<String> events() {
        List<String> result = null;
        try {
            result = plugin.events();
        } catch (RemoteException e) {
            Log.e(LOG_TAG, "events", e);
        }
        return result;
    }

    @Override
    public boolean onEvent(String s, Object o) {
        boolean result = true;
        if (plugin != null) {
            try {
                Bundle m = new Bundle();
                m.putSerializable("object", (Serializable) o);
                result = plugin.onEvent(s, m);
            } catch (RemoteException e) {
                Log.e(LOG_TAG, "onEvent", e);
            }
        }
        return result;
    }

    @Override
    public void setLoggingLevel(Level level) {
        try {
            Bundle m = new Bundle();
            m.putSerializable("level", level);
            plugin.setLoggingLevel(m);
        } catch (RemoteException e) {
            Log.e(LOG_TAG, "setLoggingLevel", e);
        }
    }

}
