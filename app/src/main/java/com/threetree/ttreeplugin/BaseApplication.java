package com.threetree.ttreeplugin;

import android.app.Application;
import android.util.Log;
import android.view.View;

import com.threetree.pluginutil.TtreePlugin;
import com.threetree.pluginutil.permission.PermissionUtil;

import java.util.List;

/**
 * Created by Administrator on 2018/8/29.
 */

public class BaseApplication extends Application {
    @Override
    public void onCreate()
    {
        super.onCreate();
        TtreePlugin.init(this);
        TtreePlugin.setOnPermissionDeniedListener(new PermissionUtil.OnPermissionDeniedListener() {
            @Override
            public void onDenied(List<String> permissionsDenied)
            {

            }
        });
        TtreePlugin.setOnCutListener(new TtreePlugin.IOnCutListener() {
            @Override
            public void onCutEnter(int type)
            {
                Log.e("onCutEnter","type=" + type);
            }

            @Override
            public void onCutExit(int type)
            {
                Log.e("onCutExit","type=" + type);
            }
        });

        TtreePlugin.setOnTrackListener(new TtreePlugin.IOnTrackListener() {
            @Override
            public void onTrackEnter(String className,String methodName, Object[] objects)
            {
                if("onClick".equals(methodName))
                {
                    if(objects != null && objects[0] instanceof View)
                    {
                        View view = (View)objects[0];
                        Log.e("onClickEnter","view=" + view.getId());
                    }
                }
            }

            @Override
            public void onTrackExit(String className,String methodName, Object[] objects)
            {
                if("onClick".equals(methodName))
                {
                    if(objects != null && objects[0] instanceof View)
                    {
                        View view = (View)objects[0];
                        Log.e("onClickExit","view=" + view.getId());
                    }
                }
            }
        });
    }
}
