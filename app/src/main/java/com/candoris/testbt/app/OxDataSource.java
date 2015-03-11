package com.candoris.testbt.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by cmb on 3/11/15.
 */
public class OxDataSource {

    private static final String TAG = OxDataSource.class.getSimpleName();

    private SQLiteDatabase database;
    private OxStorageHelper dbHelper;
    private String[] allColumns = { dbHelper.COLUMN_ID, dbHelper.COLUMN_HEART, dbHelper.COLUMN_SP02,
        dbHelper.COLUMN_START, dbHelper.COLUMN_RECORDED};

    public OxDataSource(Context context) {
        dbHelper = new OxStorageHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public OxRecord createOxRecord(String heartRate, String sp02, Date start, Date recorded) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        ContentValues vals = new ContentValues();
        vals.put(OxStorageHelper.COLUMN_HEART, heartRate);
        vals.put(OxStorageHelper.COLUMN_SP02, sp02);
        vals.put(OxStorageHelper.COLUMN_START, simpleDateFormat.format(start));
        vals.put(OxStorageHelper.COLUMN_RECORDED, simpleDateFormat.format(recorded));
        long insertId = database.insert(OxStorageHelper.OXRECORD_TABLE_NAME, null, vals);
        Cursor cursor = database.query(OxStorageHelper.OXRECORD_TABLE_NAME, allColumns,
                OxStorageHelper.COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();

        OxRecord oxRecord = cursorToOxRecord(cursor);
        cursor.close();
        return oxRecord;
    }

    public List<OxRecord> getAllOxRecords() {
        List<OxRecord> records = new ArrayList<OxRecord>();

        Cursor cursor = database.query(OxStorageHelper.OXRECORD_TABLE_NAME, allColumns,
                null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            OxRecord record = cursorToOxRecord(cursor);
            records.add(record);
            cursor.moveToNext();
        }

        cursor.close();
        return records;
    }

    public void deleteOxRecord(OxRecord record) {
        long id = record.getId();
        Log.i(TAG, "Deleted OxRecord: " + id);
        database.delete(OxStorageHelper.OXRECORD_TABLE_NAME, OxStorageHelper.COLUMN_ID +
            " = " + id, null);

    }

    private OxRecord cursorToOxRecord(Cursor cursor) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        OxRecord record = new OxRecord();
        record.setId(cursor.getLong(0));
        record.setHeartRate(cursor.getString(1));
        record.setSpO2(cursor.getString(2));
        try {
            record.setStartOfRecording(sdf.parse(cursor.getString(3)));
            record.setRecordedDate(sdf.parse(cursor.getString(4)));
        }
        catch (ParseException e) {
            Log.e(TAG, e.getMessage());
        }
        return record;
    }
}
