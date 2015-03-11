package com.candoris.testbt.app;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by cmb on 3/11/15.
 * NOTE: This was created to add an additional option since the docs say you can
 * only store 20 records? Also the only query option i've found in the doc doesn't allow
 * querying and pushes data in strange streams that do NOT match the doc as far as I can tell.
 * Need to revisit that if I can get more docs in the future (after poc?)
 *
 * DATES: For Sqlite need to be like this:
 *    // set the format to sql date time
 *    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 *    Date date = new Date();
 *    ContentValues initialValues = new ContentValues();
 *    initialValues.put("date_created", dateFormat.format(date));
 *    long rowId = mDb.insert(DATABASE_TABLE, null, initialValues);
 *
 */
public class OxStorageHelper extends SQLiteOpenHelper{

    private static final String TAG = OxStorageHelper.class.getSimpleName();
    public static final String OXRECORD_TABLE_NAME= "oxrecord";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_HEART = "heart";
    public static final String COLUMN_SP02 = "sp02";
    public static final String COLUMN_START = "start_time";
    public static final String COLUMN_RECORDED = "recorded_time";

    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "oximeter.db";

    private static final String OXRECORD_TABLE_CREATE =
            "CREATE TABLE " + OXRECORD_TABLE_NAME + " (" +
                    COLUMN_ID + " integer primary key autoincrement, " +
                    COLUMN_HEART + " real not null, " +
                    COLUMN_SP02 + " real not null, " +
                    COLUMN_START + " text not null, " +
                    COLUMN_RECORDED + " text not null); ";

    public OxStorageHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(OXRECORD_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(TAG, "Upgrading database " + DB_NAME + " from version " + oldVersion +
        " to " + newVersion + " Destroying old data.");
        db.execSQL("DROP TABLE IF EXISTS " + OXRECORD_TABLE_NAME);
        onCreate(db);
    }
}
