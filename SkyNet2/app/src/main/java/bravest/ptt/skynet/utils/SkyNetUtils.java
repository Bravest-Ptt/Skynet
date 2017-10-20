package bravest.ptt.skynet.utils;

import android.content.Context;
import android.content.Intent;
import android.net.VpnService;
import android.util.TypedValue;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by pengtian on 2017/7/17.
 */

public final class SkyNetUtils {

    public static int dp2px(Context context, int px) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, px, context.getResources().getDisplayMetrics());
    }

    public static void popSoftInput(final Context context, final EditText editText) {
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.requestFocus();

        //Delay , otherwise the edit text not ready and the keyboard will not pop up;
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.showSoftInput(editText, 0);
            }
        }, 300);
    }
}
