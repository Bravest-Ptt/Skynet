package bravest.ptt.skynet.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static bravest.ptt.skynet.db.SkyNetDbUtils.DATABASE_NAME;


public class SkyNetDBHelper extends SQLiteOpenHelper {

    private static final String TAG = "SkyNetDBHelper";

    private static final int DATABASE_VERSION = 1;

    public SkyNetDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private void createTable(SQLiteDatabase db) {
        //Create filter_history
        db.execSQL("CREATE TABLE IF NOT EXISTS filter_history (" +
                "_id INTEGER PRIMARY KEY," +
                "date TimeStamp NOT NULL DEFAULT (datetime(CURRENT_TIMESTAMP,'localtime'))," +
                "domain TEXT NOT NULL," +
                "ip TEXT);");
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        createTable(sqLiteDatabase);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
