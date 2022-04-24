package subhojit.hack36.techforcrime;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyServiceButtonControl extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        context.startService(new Intent(context, MyServiceButtonClick.class));

    }
}