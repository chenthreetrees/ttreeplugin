package com.threetree.ttreeplugin;

import android.app.Application;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

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

        TtreePlugin.setOnMethodListener(new TtreePlugin.IOnMethodListener() {
            @Override
            public boolean onMethodEnter(Object object, String className, String methodName, Object[] objects)
            {
                if("onClick".equals(methodName))
                {
                    if(objects != null && objects[0] instanceof View)
                    {
                        View view = (View)objects[0];
                        Log.e("onClickEnter","view=" + view.getId());
                    }
                }else if("testInterceptForClass".equals(methodName))
                {
                    return true;
                }
                return false;
            }

            @Override
            public void onMethodExit(Object object, String className, String methodName, Object[] objects)
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

        TtreePlugin.setOnInterceptListener(new TtreePlugin.IOnInterceptListener() {
            @Override
            public Object onIntercept(Object object, String className, String methodName, String annotationName, Object[] objects, String jsonValue, String returnType)
            {
                if("testInterceptForClass".equals(methodName))
                {
                    Toast.makeText(getApplicationContext(),"testInterceptForClass is intercepted",Toast.LENGTH_SHORT).show();
                    return 0;
                }
                return TtreePlugin.getReturnType(returnType);
            }
        });
    }
}
