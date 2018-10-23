package com.threetree.ttreeplugin;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.threetree.pluginutil.annotation.Cut;
import com.threetree.pluginutil.annotation.Debounce;
import com.threetree.pluginutil.annotation.Permission;
import com.threetree.pluginutil.annotation.TimeCost;
import com.threetree.pluginutil.permission.PermissionConsts;
import com.threetree.ttreeplugin.annotation.TestTree;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private int mCount;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        testPermission();
        findViewById(R.id.text_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                testInterceptForClass();
//                testIntercept();
            }
        });
    }

    //测试自定义注解
    @TestTree
    private void test()
    {

    }

    private void testBoolean(boolean b)
    {

    }

    //测试关键字匹配
    private void testContainName(String name)
    {

    }

    //测试类名匹配
    private void testClassName()
    {

    }

    //测试切面
    @Cut
    public int testCut()
    {
        return 0;
    }

    //测试耗时和权限
    @TimeCost
    @Permission({PermissionConsts.STORAGE, PermissionConsts.CAMERA})
    public void testPermission()
    {
        int id = 2;
    }

    //测试重写MethodVisitor
    public void testOverride()
    {

    }

    //测试埋点
    @Override
    public void onClick(View v)
    {
        int id = v.getId();
    }

    @Debounce(time = 1000)
    public int testIntercept()
    {
        mCount++;
        Toast.makeText(MainActivity.this,"test:"+mCount,Toast.LENGTH_SHORT).show();
        return 0;
    }

    public int testInterceptForClass()
    {
        int id = 234;
        Toast.makeText(MainActivity.this,"test:"+id,Toast.LENGTH_SHORT).show();
        return id;
    }
}
