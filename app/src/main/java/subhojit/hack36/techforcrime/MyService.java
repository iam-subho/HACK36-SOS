package subhojit.hack36.techforcrime;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnTokenCanceledListener;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import subhojit.hack36.techforcrime.Contacts.ContactModel;
import subhojit.hack36.techforcrime.Contacts.DbHelper;
import subhojit.hack36.techforcrime.ShakeServices.SensorService;

import static android.content.ContentValues.TAG;


public class MyService extends Service
{
    private long mVolumeTimestamp;
    private int mVolumeCount=0;
    private int mVolumeCount1=0;
    private static final int VOLUME_SLOP_TIME_MS =200;
    private static final int VOLUME_COUNT_RESET_TIME_MS = 5000;
    private static BroadcastReceiver m_ScreenOffReceiver;


    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

    @Override
    public void onCreate()
    {
        registerScreenOffReceiver();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy()
    {
        //unregisterReceiver(m_ScreenOffReceiver);
        //m_ScreenOffReceiver = null;
        Intent broadcastIntent = new Intent("restart.volume.service.again");
        sendBroadcast(broadcastIntent);
    }


    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent restartServiceTask = new Intent(getApplicationContext(), this.getClass());
        restartServiceTask.setPackage(getPackageName());
        PendingIntent restartPendingIntent = PendingIntent.getService(getApplicationContext(), 1, restartServiceTask, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager myAlarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        myAlarmService.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1000,
                restartPendingIntent);

        super.onTaskRemoved(rootIntent);

    }

    private void registerScreenOffReceiver()

    {
        DevicePolicyManager policyManager = (DevicePolicyManager) MyService.this.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName adminReceiverGPS = new ComponentName(this,ScreenOffAdminReceiver.class);

             m_ScreenOffReceiver = new BroadcastReceiver()
            {
            @SuppressLint("MissingPermission")
            @Override
            public void onReceive(Context context, Intent intent)
            {
                Log.d(TAG, "ACTION_SCREEN_OFF");
                // do something, e.g. send Intent to main app
                if (intent.getAction().equals("android.media.VOLUME_CHANGED_ACTION")) {
                    final long now = System.currentTimeMillis();
                    // ignore shake events too close to each other (500ms)
                    if (mVolumeTimestamp + VOLUME_SLOP_TIME_MS > now) {
                        return;
                    }
                    // reset the shake count after 3 seconds of no shakes
                    if (mVolumeTimestamp + VOLUME_COUNT_RESET_TIME_MS < now) {
                        mVolumeCount = 0;
                    }
                    Log.e(TAG, "onReceive in service:" + String.valueOf(mVolumeCount));
                    mVolumeTimestamp = now;
                    mVolumeCount++;
                    if(mVolumeCount==2){
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            //policyManager.setLocationEnabled(adminReceiverGPS,true);
                            Log.e("Build Version",String.valueOf(Build.VERSION.SDK_INT));
                            Log.e("Build Version2",String.valueOf(Build.VERSION_CODES.R));
                        }
                    }
                    if(mVolumeCount==3){
                    //create FusedLocationProviderClient to get the user location

                    FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
                    //use the PRIORITY_BALANCED_POWER_ACCURACY so that the service doesn't use unnecessary power via GPS
                    //it will only use GPS at this very moment
                    fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY, new CancellationToken() {
                        @Override
                        public boolean isCancellationRequested() {
                            return false;
                        }

                        @NonNull
                        @Override
                        public CancellationToken onCanceledRequested(@NonNull OnTokenCanceledListener onTokenCanceledListener) {
                            return null;
                        }
                    }).addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            //check if location is null
                            //for both the cases we will create different messages
                            if (location != null) {

                                //get the SMSManager
                                SmsManager smsManager = SmsManager.getDefault();
                                //get the list of all the contacts in Database
                                DbHelper db = new DbHelper(MyService.this);
                                List<ContactModel> list = db.getAllContacts();
                                List<ContactModel> listwa = db.getAllContactswa();
                                //send SMS to each contact

                                for (ContactModel c : list) {
                                    String message = "Hey, " + c.getName() + "I am in DANGER, i need help. Please urgently reach me out. Here are my coordinates.\n " + "http://maps.google.com/?q=" + location.getLatitude() + "," + location.getLongitude() + " SOS";
                                    smsManager.sendTextMessage(c.getPhoneNo(), null, message, null, null);
                                }
                                for (ContactModel c : listwa) {
                                    String message = "Hey, " + c.getName() + "I am in DANGER, i need help. Please urgently reach me out. Here are my coordinates.\n " + "http://maps.google.com/?q=" + location.getLatitude() + "," + location.getLongitude() + " SOS";
                                    try {
                                        sendwhatsapp(message, c.getPhoneNo());
                                    } catch (UnsupportedEncodingException unsupportedEncodingException) {
                                        unsupportedEncodingException.printStackTrace();
                                    } catch (InterruptedException interruptedException) {
                                        interruptedException.printStackTrace();
                                    }
                                }
                            } else {
                                String message = "I am in DANGER, i need help. Please urgently reach me out.\n" + "GPS was turned off.Couldn't find location. Call your nearest Police Station. SOS";
                                SmsManager smsManager = SmsManager.getDefault();
                                DbHelper db = new DbHelper(MyService.this);
                                List<ContactModel> list = db.getAllContacts();
                                List<ContactModel> listwa = db.getAllContactswa();
                                for (ContactModel c : list) {
                                    smsManager.sendTextMessage(c.getPhoneNo(), null, message, null, null);
                                }
                                for (ContactModel c : listwa) {
                                    try {
                                        sendwhatsapp(message, c.getPhoneNo());
                                    } catch (UnsupportedEncodingException unsupportedEncodingException) {
                                        unsupportedEncodingException.printStackTrace();
                                    } catch (InterruptedException interruptedException) {
                                        interruptedException.printStackTrace();
                                    }
                                }
                            }
                            mVolumeCount1=1;
                        }

                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("Check: ", "OnFailure");
                            String message = "I am in DANGER, i need help. Please urgently reach me out.\n" + "GPS was turned off.Couldn't find location. Call your nearest Police Station. SOS";
                            SmsManager smsManager = SmsManager.getDefault();
                            DbHelper db = new DbHelper(MyService.this);
                            List<ContactModel> list = db.getAllContacts();
                            List<ContactModel> listwa = db.getAllContactswa();
                            for (ContactModel c : list) {
                                smsManager.sendTextMessage(c.getPhoneNo(), null, message, null, null);
                            }
                            for (ContactModel c : listwa) {
                                try {
                                    sendwhatsapp(message, c.getPhoneNo());
                                } catch (UnsupportedEncodingException unsupportedEncodingException) {
                                    unsupportedEncodingException.printStackTrace();
                                } catch (InterruptedException interruptedException) {
                                    interruptedException.printStackTrace();
                                }
                            }
                            mVolumeCount1=1;
                        }
                    });
                    mVolumeCount=0;
                    if(mVolumeCount1==1){
                        mVolumeCount1=0;
                        changeActivity();
                    }

                  }

                }
            }
        };
        //IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        registerReceiver(m_ScreenOffReceiver, new IntentFilter("android.media.VOLUME_CHANGED_ACTION"));


    }

    public void sendwhatsapp(String message,String MobileNumber) throws UnsupportedEncodingException, InterruptedException {
        AccessibilityServiceManager serviceManager = new AccessibilityServiceManager(this);

        if (serviceManager.hasAccessibilityServicePermission(MyAccessibilityService.class)){
            String number =MobileNumber;
            String url="https://api.whatsapp.com/send?phone="+number+"&text="+ URLEncoder.encode(message,"UTF-8");
            Intent whatsappintent = new Intent(Intent.ACTION_VIEW);
            whatsappintent.setPackage("com.whatsapp");
            whatsappintent.setData(Uri.parse(url));
            whatsappintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getApplicationContext().startActivity(whatsappintent);
            Thread.sleep(2000);
            //sendBroadcastMessage();
            //Log.i(url.toString());
            //Toast.makeText(this,url, Toast.LENGTH_LONG).show();
        }else{
           // serviceManager.requestUserForAccessibilityService(MyService.this);
        }


    }

    public void changeActivity(){
        Intent intent = new Intent(this, ScreenOffActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(intent);
    }

}