package subhojit.hack36.techforcrime;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import subhojit.hack36.techforcrime.Contacts.DbHelper;

import subhojit.hack36.techforcrime.R;

import subhojit.hack36.techforcrime.ShakeServices.HiddenCamera;
import subhojit.hack36.techforcrime.ShakeServices.ReactivateService;
import subhojit.hack36.techforcrime.ShakeServices.SensorService;
import subhojit.hack36.techforcrime.ShakeServices.ShakeDetector;

import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.ResourceBundle;

public class MainActivity extends AppCompatActivity {

    private static final int IGNORE_BATTERY_OPTIMIZATION_REQUEST = 1002;
    public static SharedPreferences sharedpreferences;


    //create instances of various classes to be used
    Button button1,button1wa,buttonClick;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //check for runtime permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)== PackageManager.PERMISSION_DENIED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.SEND_SMS,Manifest.permission.READ_CONTACTS}, 100);
            }
        }

        //this is a special permission required only by devices using
        //Android Q and above. The Access Background Permission is responsible
        //for populating the dialog with "ALLOW ALL THE TIME" option
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 100);
        }

        //check for BatteryOptimization,
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (pm != null && !pm.isIgnoringBatteryOptimizations(getPackageName())) {
                askIgnoreOptimization();
            }
        }

        //start the service
        SensorService sensorService = new SensorService();
        Intent intent = new Intent(this, sensorService.getClass());
        if (!isMyServiceRunning(sensorService.getClass())) {
            startService(intent);
        }



        if (!isAccessibilityOn(this, MyAccessibilityService.class)) {
            Intent intentwa = new Intent (Settings.ACTION_ACCESSIBILITY_SETTINGS);
            this.startActivity (intentwa);
        }

        Intent service = new Intent(getApplicationContext(), MyService.class);
        startService(service);
        //Toast.makeText(this, "Volume !", Toast.LENGTH_SHORT).show();

        button1 = findViewById(R.id.Button1);
        button1wa = findViewById(R.id.Button1wa);
        buttonClick = findViewById(R.id.ButtonClick);


        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(v.getContext(), MyContact.class);
                startActivity(i);
            }
        });
        button1wa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent j = new Intent(v.getContext(), MyContactWhatsapp.class);
                startActivity(j);
            }
        });
        buttonClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    CheckWA();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        });


    }


    private boolean isAccessibilityOn(MainActivity mainActivity, Class<MyAccessibilityService> myAccessibilityServiceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (myAccessibilityServiceClass.getName().equals(service.service.getClassName())) {
                Log.i ("Service status", "Running");
                return true;
            }
        }
        Log.i ("Service status", "Not running");
        return false;
    }


    //method to check if the service is running
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("Service status", "Running");
                return true;
            }
        }
        Log.i ("Service status", "Not running");
        return false;
    }

    @Override
    protected void onDestroy() {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("restartservice");
        broadcastIntent.setClass(this, ReactivateService.class);
        this.sendBroadcast(broadcastIntent);
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==100){
            if(grantResults[0]==PackageManager.PERMISSION_DENIED){
                Toast.makeText(this, "Permissions Denied!\n Can't use the App!", Toast.LENGTH_SHORT).show();
            }
        }
    }



    //this method prompts the user to remove any battery optimisation constraints from the App
    private void askIgnoreOptimization() {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            @SuppressLint("BatteryLife") Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, IGNORE_BATTERY_OPTIMIZATION_REQUEST);
        }

    }
    public void CheckWA() throws InterruptedException, UnsupportedEncodingException {
        //location();
        /*Intent intent = new Intent(this, ScreenOffActivity.class);
        startActivity(intent);
        String number ="+917001667213";
        String message= "I am in DANGER, i need help. Please urgently reach me out.\n"+"GPS was turned off.Couldn't find location. Call your nearest Police Station. SOS";
        String url="https://api.whatsapp.com/send?phone="+number+"&text="+ URLEncoder.encode(message,"UTF-8");
        Intent whatsappintent = new Intent(Intent.ACTION_VIEW);
        Toast.makeText(this,url, Toast.LENGTH_LONG).show();
        whatsappintent.setPackage("com.whatsapp");
        whatsappintent.setData(Uri.parse(url));
        whatsappintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
       // getApplicationContext().startActivity(whatsappintent);
        Thread.sleep(2000);
        Intent intent = new Intent(this, SosActivityClass.class);
        startActivity(intent);*/
        Intent service = new Intent(getApplicationContext(), MyServiceButtonClick.class);
        startService(service);
        Intent serviceCam = new Intent(getApplicationContext(), HiddenCamera.class);
        startService(serviceCam);

    }

    public void CheckWAm(){
        int count=3;

        Intent intent = new Intent(this, ShakeDetector.class);
        intent.putExtra("mShakeCount",3);
        startActivity(intent);


    }

    public void location(){
        new GpsUtils(this).turnGPSOn(new GpsUtils.onGpsListener() {
            @Override
            public void gpsStatus(boolean isGPSEnable) {
                // turn on GPS
                boolean isGPS = isGPSEnable;
            }
        });
    }

    }