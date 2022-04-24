package subhojit.hack36.techforcrime;

import android.Manifest;
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
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.androidhiddencamera.config.CameraFacing;
import com.androidhiddencamera.config.CameraImageFormat;
import com.androidhiddencamera.config.CameraResolution;
import com.androidhiddencamera.config.CameraRotation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnTokenCanceledListener;
import com.androidhiddencamera.HiddenCameraService;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import subhojit.hack36.techforcrime.Contacts.ContactModel;
import subhojit.hack36.techforcrime.Contacts.DbHelper;

import static android.content.ContentValues.TAG;


public class MyServiceButtonClick extends Service implements LocationListener {

    private int mCountSMS = 0;
    private int mCountWhatsapp = 0;
    private double lat=-2500;
    private double longd=-2501;


    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        Toast.makeText(this, " MyService Created ", Toast.LENGTH_LONG).show();
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, (LocationListener) this);
        BroadcastReceiver m_ScreenOffReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Intent.ACTION_SHUTDOWN.equals(intent.getAction())) {
                    Log.e("Shut Action","Down");
                    context.stopService(new Intent(context, MyServiceButtonClick.class));
                    //changeActivity();
                } else {
                    // Your tasks for boot up
                }
            }
        };
    }
    @Override
    public void onLocationChanged(Location location) {
        lat=location.getLatitude();
        longd=location.getLongitude();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Toast.makeText(this, " MyService Started", Toast.LENGTH_LONG).show();
        try {
            registerScreenOffReceiver();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @SuppressLint("MissingPermission")
    private void registerScreenOffReceiver() throws InterruptedException {

                            DbHelper db=new DbHelper(this);
                            int CountSMSContact=db.count();
                            int CountWAContact=db.countwa();
                            Log.e("Location",Double.toString(lat));
                            if (lat !=-2500 ) {

                                //get the SMSManager
                                SmsManager smsManager = SmsManager.getDefault();
                                //get the list of all the contacts in Database
                               // DbHelper db = new DbHelper(MyServiceButtonClick.this);
                                List<ContactModel> list = db.getAllContacts();
                                List<ContactModel> listwa = db.getAllContactswa();
                                //send SMS to each contact

                                for (ContactModel c : list) {
                                    String message = "Hey, " + c.getName() + "I am in DANGER, i need help. Please urgently reach me out. Here are my coordinates.\n " + "http://maps.google.com/?q=" + lat + "," + longd + " SOS";
                                    smsManager.sendTextMessage(c.getPhoneNo(), null, message, null, null);
                                    mCountSMS++;
                                }
                                for (ContactModel c : listwa) {
                                    String message = "Hey, " + c.getName() + "I am in DANGER, i need help. Please urgently reach me out. Here are my coordinates.\n " + "http://maps.google.com/?q=" + lat + "," + longd + " SOS";
                                    try {
                                        sendwhatsapp(message, c.getPhoneNo());
                                    } catch (UnsupportedEncodingException unsupportedEncodingException) {
                                        unsupportedEncodingException.printStackTrace();
                                    } catch (InterruptedException interruptedException) {
                                        interruptedException.printStackTrace();
                                    }
                                    mCountWhatsapp++;
                                }

                            } else {
                                String message = "I am in DANGER, i need help. Please urgently reach me out.\n" + "GPS was turned off.Couldn't find location. Call your nearest Police Station. SOS";
                                SmsManager smsManager = SmsManager.getDefault();
                               // DbHelper db = new DbHelper(MyServiceButtonClick.this);
                                List<ContactModel> list = db.getAllContacts();
                                List<ContactModel> listwa = db.getAllContactswa();
                                for (ContactModel c : list) {
                                    smsManager.sendTextMessage(c.getPhoneNo(), null, message, null, null);
                                    Log.e("Message",c.getPhoneNo());
                                    mCountSMS++;
                                }
                                for (ContactModel c : listwa) {
                                    try {
                                        sendwhatsapp(message, c.getPhoneNo());
                                    } catch (UnsupportedEncodingException unsupportedEncodingException) {
                                        unsupportedEncodingException.printStackTrace();
                                    } catch (InterruptedException interruptedException) {
                                        interruptedException.printStackTrace();
                                    }
                                    mCountWhatsapp++;
                                }

                            }



                    if(CountWAContact== mCountWhatsapp && CountSMSContact==mCountSMS){
                        mCountWhatsapp=0;
                        mCountSMS=0;
                        Thread.sleep(10000);
                        changeActivity();
                    }




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
            Thread.sleep(3000);
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