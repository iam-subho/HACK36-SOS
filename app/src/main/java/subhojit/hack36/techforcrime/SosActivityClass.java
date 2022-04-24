package subhojit.hack36.techforcrime;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

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

public class SosActivityClass extends AppCompatActivity {
    private int mVolumeCount1 = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        //use the PRIORITY_BALANCED_POWER_ACCURACY so that the service doesn't use unnecessary power via GPS
        //it will only use GPS at this very moment
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY, new

                   CancellationToken() {
                    @Override
                    public boolean isCancellationRequested() {
                        return false;
                    }

                    @NonNull
                    @Override
                    public CancellationToken onCanceledRequested(@NonNull OnTokenCanceledListener
                                                                         onTokenCanceledListener) {
                        return null;
                    }
                }).

                addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        //check if location is null
                        //for both the cases we will create different messages
                        if (location != null) {

                            //get the SMSManager
                            SmsManager smsManager = SmsManager.getDefault();
                            //get the list of all the contacts in Database
                            DbHelper db = new DbHelper(getApplicationContext());
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
                            DbHelper db = new DbHelper(getApplicationContext());
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
                        mVolumeCount1 = 1;
                    }

                }).

                addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Check: ", "OnFailure");
                        String message = "I am in DANGER, i need help. Please urgently reach me out.\n" + "GPS was turned off.Couldn't find location. Call your nearest Police Station. SOS";
                        SmsManager smsManager = SmsManager.getDefault();
                        DbHelper db = new DbHelper(getApplicationContext());
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
                        mVolumeCount1 = 1;
                    }
                });

        if (mVolumeCount1 == 1) {
            mVolumeCount1 = 0;
            changeActivity();
        }

    }


    public void sendwhatsapp(String message,String MobileNumber) throws UnsupportedEncodingException, InterruptedException {
        AccessibilityServiceManager serviceManager = new AccessibilityServiceManager(SosActivityClass.this);

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
