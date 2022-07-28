package com.example.smartchatters.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.smartchatters.R;
import com.example.smartchatters.logic.*;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private Usernode user;
    private String username;
    private ExecutorService executorService = Executors.newFixedThreadPool(5);
    final CountDownLatch latch = new CountDownLatch(1);
    private static final int STORAGE_PERMISSION_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        username=(String)getIntent().getSerializableExtra("username");
        Singleton.getInstance().setExecutorService(executorService);
        registerUser();
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar)findViewById(R.id.appToolbar));
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavigationView);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        NavController navController = (NavController)navHostFragment.getNavController();
        NavigationUI.setupWithNavController(bottomNavigationView,navController);
        Singleton.getInstance().setUser(user);
        checkPermission(STORAGE_PERMISSION_CODE);
    }

    public void checkPermission(int requestCode) {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
        {
            ActivityCompat.requestPermissions(MainActivity.this,new String [] {Manifest.permission.WRITE_EXTERNAL_STORAGE} ,requestCode);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(MainActivity.this, "Can't use the app without storage permission", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void registerUser() {
        executorService.execute(new Runnable() {
            public void run() {
                InputStream is = getResources().openRawResource(R.raw.confuser);
                user=new Usernode(new ProfileName(username,"-","-"), is);
                latch.countDown();
                return;
            }
        });
    }

}