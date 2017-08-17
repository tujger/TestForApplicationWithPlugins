package com.edeqa.testforapplicationwithplugins;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.edeqa.eventbus.EventBus;
import com.edeqa.waytous.WaytousPlugin;

/**
 * Created 8/16/2017.
 */
class PluginConnection implements ServiceConnection {

    private final static String LOG = "viewClicked";

    private final EventBus eventBus;
    private final String serviceName;
    private final String packageName;
    private final Runnable1<PluginConnection> onConnected;
    private final Runnable1<PluginConnection> onDisconnected;

    private WaytousPlugin plugin;
    private MainActivity context;
    private EntityHolderAdapter holder;

    private View view;
    private String type;
    private Integer viewResId;

    public PluginConnection(MainActivity context, EventBus eventBus, String serviceName, String packageName, Runnable1<PluginConnection> onConnected, Runnable1<PluginConnection> onDisconnected) {
        this.context = context;
        this.eventBus = eventBus;
        this.serviceName = serviceName;
        this.packageName = packageName;
        this.onConnected = onConnected;
        this.onDisconnected = onDisconnected;
    }

    public void onServiceConnected(ComponentName className, IBinder boundService) {
        plugin = WaytousPlugin.Stub.asInterface(boundService);
        holder = new EntityHolderAdapter(plugin);
        type = holder.getType();
        eventBus.register(holder);

//        System.out.println("VIEWID:"+holder.getViewResId());
        if(onConnected != null) {
            onConnected.call(this);
        }
    }

    public void onServiceDisconnected(ComponentName className) {
        if(onDisconnected != null) {
            onDisconnected.call(this);
        }
        eventBus.unregister(type);
    }

    public PluginConnection bind() {
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

    public EntityHolderAdapter getHolder() {
        return holder;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getPackageName() {
        return packageName;
    }

    @Override
    public String toString() {
        return "PluginConnection{" +
                "serviceName='" + serviceName + '\'' +
                ", packageName='" + packageName + '\'' +
                ", type='" + type + '\'' +
                '}';
    }

    public int getViewResId() {
        if(viewResId != null) return viewResId;
        try {
            viewResId = plugin.getViewResId();
        } catch (RemoteException e) {
            Log.e("PluginConnection", "getViewResId", e);
            viewResId = 0;
        }
        return viewResId;
    }

    public void addViewTo(ViewGroup pluginsView) {
        if(getViewResId() > 0) {
            String id = getServiceName() + ":" + getViewResId();
            View already = pluginsView.findViewWithTag(id);
            if (already == null) {
                try {
                    ApplicationInfo info = context.getPackageManager().getApplicationInfo(getPackageName(), 0);
                    Resources res = context.getPackageManager().getResourcesForApplication(info);
                    XmlResourceParser xres = res.getLayout(getViewResId());
                    LayoutInflater.from(context).inflate(xres, pluginsView);
                    view = pluginsView.getChildAt(pluginsView.getChildCount() - 1);
                    view.setTag(id);

                    setListener(view);


                }catch (Exception e) {
                    Log.e("PluginConnection", "addViewTo", e);
                }
            }
        }
    }

    private void setListener(View view) {
        System.out.println(serviceName +":"+ (view instanceof ViewGroup));
        if(view instanceof ViewGroup) {
            for(int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                setListener(((ViewGroup) view).getChildAt(i));
            }
        } else if (view instanceof Button) {
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    viewClicked(v.getId());
                }
            });
        } else {
            System.out.println(serviceName +":::"+ view.getClass());
        }
    }

    private void viewClicked(int resId) {
        try {
            plugin.viewClicked(resId);
        } catch (RemoteException e) {
            Log.e(LOG, "viewClicked", e);
        }
    }


    public void removeView() {
        if(view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            parent.removeViewAt(parent.indexOfChild(view));

            view = null;
        }
    }
}
