package subhojit.hack36.techforcrime;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

public class ScreenOffActivity extends Activity {

    static final String LOG_TAG = "ScreenOffActivity";
    private ComponentName mComponentName;
    private static final int ADMIN_INTENT = 15;
    private static final String description = "Administration Permission needed to lock your screen";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_screen_off);
        turnScreenOffAndExit();
    }

    private void turnScreenOffAndExit() {
        // first lock screen
        turnScreenOff(getApplicationContext());

        // then provide feedback
        ((Vibrator) getSystemService(Context.VIBRATOR_SERVICE)).vibrate(50);

        // schedule end of activity
        final Activity activity = this;
        Thread t = new Thread() {
            public void run() {
                try {
                    sleep(500);
                } catch (InterruptedException e) {
                    /* ignore this */
                }
                activity.finish();
            }
        };
        t.start();
    }

    /**
     * Turns the screen off and locks the device, provided that proper rights
     * are given.
     *
     * @param context
     *            - The application context
     */
    public void turnScreenOff(final Context context) {
        DevicePolicyManager policyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName adminReceiver = new ComponentName(context,ScreenOffAdminReceiver.class);
        ComponentName adminReceiverGPS = new ComponentName(context,ScreenOffAdminReceiver.class);
        boolean admin = policyManager.isAdminActive(adminReceiver);
        //boolean admingps = policyManager.isAdminActive(adminReceiver);
        if (admin) {
            Log.i(LOG_TAG, "Going to sleep now.");


            policyManager.lockNow();
            policyManager.getKeyguardDisabledFeatures(adminReceiver);
        } else {
            Log.i(LOG_TAG, "Not an admin");
            Toast.makeText(context, R.string.device_admin_not_enabled,Toast.LENGTH_LONG).show();
            showInstallAdminAlert();
        }
    }

    private void showInstallAdminAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.confirm_admin_text));
        builder.setCancelable(true)
                .setPositiveButton(getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mComponentName);
                                intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, description);
                                startActivityForResult(intent, ADMIN_INTENT);
                            }
                        }).setNegativeButton(getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //hacer algo en el cancelar?
                    }
                });

        AlertDialog alert = builder.create();
        alert.setTitle(getString(R.string.confirm_admin));
        alert.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ADMIN_INTENT) {
            if (resultCode == RESULT_OK) {

            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.noadmin), Toast.LENGTH_SHORT).show();
            }
        }
    }

}