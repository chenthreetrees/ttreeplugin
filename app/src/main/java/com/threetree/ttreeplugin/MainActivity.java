package com.threetree.ttreeplugin;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.threetree.ttreeplugin.annotation.Permission;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        test();
        test2();
    }

    @Permission
    public void test()
    {

    }

    public void test2()
    {

    }

    @Override
    public void onClick(View v)
    {

    }
}
