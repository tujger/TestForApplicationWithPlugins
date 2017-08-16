package com.edeqa.testforapplicationwithplugins;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.edeqa.eventbus.AbstractEntityHolder;
import com.edeqa.eventbus.EntityHolder;
import com.edeqa.eventbus.EventBus;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TooManyListenersException;
import java.util.logging.Level;

public class MainActivity extends Activity {

    private EventBus<EntityHolder> eventBus;
    private int count = 0;
    private Map<String, PluginService> pluginsMap = new LinkedHashMap<>();
    private Map<String, String> servicesMap = new LinkedHashMap<>();
    private IntentFilter packageFilter;
    private PackageBroadcastReceiver packageBroadcastReceiver;
//    private ArrayList<HashMap<String, String>> services;
    private static final String LOG = "RESPLUGINAPP";
    private static final String ACTION_COLLECT_PLUGINS = "com.edeqa.waytous.intent.action.PLUGIN";

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            EventBus.LOGGING_LEVEL = Level.ALL;
            EventBus.setMainRunner(EventBus.RUNNER_DEFAULT);
            eventBus = new EventBus<>("app");
            eventBus.register(new AbstractEntityHolder<Object>() {
                @Override
                public void setContext(Object context) {
                    super.setContext(context);
                }
            });
        } catch (TooManyListenersException e) {
            e.printStackTrace();
        }

        populatePluginList();
        packageBroadcastReceiver = new PackageBroadcastReceiver();
        packageFilter = new IntentFilter();
        packageFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        packageFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        packageFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        packageFilter.addCategory(Intent.CATEGORY_DEFAULT);
        packageFilter.addDataScheme("package");

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eventBus.post("test_event", "test_object");
            }
        });

    }

    protected void onStart() {
        super.onStart();
        Log.d(LOG, "onStart");
        registerReceiver(packageBroadcastReceiver, packageFilter);
        bindPlugins();
    }

    protected void onStop() {
        super.onStop();
        Log.d(LOG, "onStop");
        unregisterReceiver(packageBroadcastReceiver);
        releasePlugins();
    }

    private void bindPlugins() {
        for(Map.Entry<String,String> entry: servicesMap.entrySet()) {
            pluginsMap.put(entry.getKey(), new PluginService(this, eventBus, entry.getKey(), entry.getValue()).bind());
        }
    }

    private void releasePlugins() {
        for(Map.Entry<String,PluginService> entry: pluginsMap.entrySet()) {
            entry.getValue().unbind();
        }
    }

    private void populatePluginList() {
        PackageManager packageManager = getPackageManager();
        Intent baseIntent = new Intent(ACTION_COLLECT_PLUGINS);
        baseIntent.setFlags(Intent.FLAG_DEBUG_LOG_RESOLUTION);
        List<ResolveInfo> list = packageManager.queryIntentServices(baseIntent, PackageManager.GET_RESOLVED_FILTER);
        Log.d(LOG, "populatePluginList: " + list);
        int i;
        for (i = 0; i < list.size(); ++i) {
            ResolveInfo info = list.get(i);
            ServiceInfo sinfo = info.serviceInfo;
            Log.d(LOG, "populatePluginList: i: " + i + "; sinfo: " + sinfo);
            if (sinfo != null) {
                servicesMap.put(sinfo.name, sinfo.packageName);
//                inflateToView(i, packageManager, sinfo.packageName);
            }
        }
        /*for (; i < 4; ++i) {
            initField(i);
        }*/
        Log.d(LOG, "services: " + servicesMap);
    }

/*
    private void inflateToView(int rowCtr, PackageManager packageManager, String packageName) {
        try {
            ApplicationInfo info = packageManager.getApplicationInfo(packageName, 0);
            Resources res = packageManager.getResourcesForApplication(info);
            XmlResourceParser xres = res.getLayout(R.layout.activity_main);
            int parentId = selectRow(rowCtr);
            ViewGroup parentView = (ViewGroup) findViewById(parentId);
            parentView.removeAllViews();
            View view = inflater.inflate(xres, parentView);
            adjustSubViewIds(parentView, idxToIdOffset(rowCtr));
        } catch (PackageManager.NameNotFoundException ex) {
            Log.e(LOG, "NameNotFoundException", ex);
        }
    }
*/

    /*private void initField(int rowCtr) {
        int parentId = selectRow(rowCtr);
        ViewGroup parentView = (ViewGroup) findViewById(parentId);
        TextView tv = new TextView(this);
        tv.setText("-slot" + Integer.toString(rowCtr + 1) + "-");
        parentView.removeAllViews();
        parentView.addView(tv);
    }*/

    /*private int selectRow(int rowCtr) {
        int rowId = R.id.slot1;
        switch (rowCtr) {
            case 0:
                rowId = R.id.slot1;
                break;
            case 1:
                rowId = R.id.slot2;
                break;
            case 2:
                rowId = R.id.slot3;
                break;
            case 3:
                rowId = R.id.slot4;
                break;
        }
        return rowId;
    }
*/
    /*private void adjustSubViewIds(ViewGroup parent, int idOffset) {
        for (int i = 0; i < parent.getChildCount(); ++i) {
            View v = parent.getChildAt(i);
            if (v instanceof ViewGroup)
                adjustSubViewIds((ViewGroup) v, idOffset);
            else {
                int id = v.getId();
                if (id != View.NO_ID)
                    v.setId(id + idOffset);
            }
        }
    }*/



    class PackageBroadcastReceiver extends BroadcastReceiver {
        private static final String LOG_TAG = "PBR";

        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "onReceive: " + intent);
            releasePlugins();
            populatePluginList();
            bindPlugins();
        }
    }

}
