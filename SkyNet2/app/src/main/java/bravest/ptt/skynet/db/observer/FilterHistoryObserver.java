package bravest.ptt.skynet.db.observer;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;

import bravest.ptt.skynet.db.SkyNetDbUtils;

/**
 * Created by pengtian on 2017/8/3.
 */

public class FilterHistoryObserver extends ContentObserver {
    public static final int WHAT_FILTER_HISTORY_CHANGED = 1;

    private static final String TAG = "FilterHistoryObserver";

    private Handler mHandler;

    private Context mContext;

    /**
     * Creates a content observer.
     *
     * @param handler The handler to run {@link #onChange} on, or null if none.
     */
    public FilterHistoryObserver(Context context, Handler handler) {
        super(handler);

        mContext = context;
        mHandler = handler;
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);

        Cursor cursor = mContext.getContentResolver()
                .query(SkyNetDbUtils.FilterHistory.CONTENT_URI, null, null, null, null);
        if (cursor != null) {
            Message msg = Message.obtain();
            msg.arg1 = cursor.getCount();
            msg.what = WHAT_FILTER_HISTORY_CHANGED;
            mHandler.sendMessage(msg);
            cursor.close();
        }
    }
}
