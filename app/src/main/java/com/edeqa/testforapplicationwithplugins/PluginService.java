package com.edeqa.testforapplicationwithplugins;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.edeqa.eventbus.EventBus;
import com.edeqa.waytous.WaytousPlugin;

/**
 * Created 8/16/2017.
 */
class PluginService implements ServiceConnection {
    private MainActivity context;
    private final String serviceName;
    private final String packageName;
    private final EventBus eventBus;
    private WaytousPlugin plugin;
    private String type;

    public PluginService(MainActivity context, EventBus eventBus, String serviceName, String packageName) {
        this.context = context;
        this.eventBus = eventBus;
        this.serviceName = serviceName;
        this.packageName = packageName;
    }

    private EntityHolderAdapter holder;

    public void onServiceConnected(ComponentName className, IBinder boundService) {
        plugin = WaytousPlugin.Stub.asInterface(boundService);
        holder = new EntityHolderAdapter(plugin);
        type = holder.getType();
        eventBus.register(holder);
    }

    public void onServiceDisconnected(ComponentName className) {
        eventBus.unregister(type);
    }

    public PluginService bind() {
        System.out.println("BIND:"+serviceName);
        Intent intent = new Intent();
        intent.setClassName(packageName, serviceName);
        context.bindService(intent, this, Context.BIND_AUTO_CREATE);
        return this;
    }

    public void unbind() {
        System.out.println("UNBIND:"+serviceName);
        context.unbindService(this);
    }
}
