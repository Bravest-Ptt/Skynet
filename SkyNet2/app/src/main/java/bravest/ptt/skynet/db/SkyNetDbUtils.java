package bravest.ptt.skynet.db;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by root on 1/4/17.
 */

public class SkyNetDbUtils {

    private static final String AUTHORITY = "bravest.ptt.skynet";

    private static final String TAG = "SkyNetDbUtils";

    public static final String DATABASE_NAME = "skynet.db";

    public interface FilterHistory extends BaseColumns {
        String TABLE_NAME = "filter_history";
        Uri CONTENT_URI = Uri.parse("content://bravest.ptt.skynet/filter_history");
        String DATE = "date";
        String DOMAIN = "domain";
        String IP = "ip";
    }
}
