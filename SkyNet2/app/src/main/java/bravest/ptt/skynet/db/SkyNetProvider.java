package bravest.ptt.skynet.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import static bravest.ptt.skynet.db.SkyNetDbUtils.DATABASE_NAME;

/**
 * Created by root on 1/4/17.
 */

public class SkyNetProvider extends ContentProvider {

    private static final String TAG = "SkyNetProvider";
    private static final String PACKAGE_NAME = "bravest.ptt.skynet";

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private SkyNetDBHelper mOpenHelper;

    static {
        sUriMatcher.addURI(PACKAGE_NAME, "filter_history", 1);
        sUriMatcher.addURI(PACKAGE_NAME, "filter_history/#", 2);
    }

    private void getDatabaseHelper() {
        if (!getContext().getDatabasePath(DATABASE_NAME).exists()) {
            mOpenHelper = new SkyNetDBHelper(getContext());
        }
    }

    @Override
    public boolean onCreate() {
        Log.d(TAG, "provider onCreate");
        mOpenHelper = new SkyNetDBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projectionIn, String selection, String[] selectionArgs, String sortOrder) {
        getDatabaseHelper();
        SQLiteQueryBuilder sqliteQueryBuilder = new SQLiteQueryBuilder();
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        int match = sUriMatcher.match(uri);

        Log.d(TAG, "provider query uri: " + uri + " match: " + match + " selection: " + selection + " selectionArgs: " + selectionArgs);

        switch (match) {
            case 1:
                sqliteQueryBuilder.setTables("filter_history");
                break;
            case 2:
                sqliteQueryBuilder.setTables("filter_history");
                sqliteQueryBuilder.appendWhere("_id=");
                sqliteQueryBuilder.appendWhere(uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown uri " + uri);
        }

        Cursor cursor = sqliteQueryBuilder.query(db, projectionIn, selection, selectionArgs, null, null, sortOrder);
        if (cursor != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        } else {
            Log.e(TAG, "query failed");
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case 1:
                return "vnd.android.cursor.dir/filter_history";
            case 2:
                return "vnd.android.cursor.item/filter_history";
            default:
                throw new IllegalArgumentException("Unknown URI");
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        getDatabaseHelper();
        int match = sUriMatcher.match(uri);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        Log.d(TAG, "insert: uri = " + uri + ", match = " + match);

        long rowId;
        switch (match) {
            case 1:
            case 2:
                rowId = db.insert("filter_history", null, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URL");
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, rowId);
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        int match = sUriMatcher.match(uri);
        getDatabaseHelper();
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        Log.d(TAG, "provider delete uri: " + uri + " where: " + where + " whereArgs: " + whereArgs + " match: " + match);

        int affectedRows;
        switch (match) {
            case 1:
                affectedRows = db.delete("filter_history", where, whereArgs);
                break;
            case 2:
                affectedRows = db.delete("filter_history", "_id=" + uri.getLastPathSegment() + " AND ( " + where + " )", whereArgs);
                break;
            default:
                throw new IllegalArgumentException("Cannot delete from URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return affectedRows;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        getDatabaseHelper();
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        int affectedRows;
        switch(match) {
            case 1:
                affectedRows = db.update("filter_history", values, where, whereArgs);
                break;
            case 2:
                affectedRows = db.update("filter_history", values, "_id" + uri.getLastPathSegment(), null);
                break;
            default:
                throw new UnsupportedOperationException("Cannot update URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return affectedRows;
    }
}
