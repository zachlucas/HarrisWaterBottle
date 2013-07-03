package edu.psu.database;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;						// Database version
    private static final String DATABASE_NAME = "harrisWaterData";		// Database name
    private static final String TABLE_NAME = "waterDrinkingData";		// Table name
    private static final String KEY_ID = "id";							// Primary key field
    private static final String KEY_DATE = "date";						// Date field (stores dates in epoch time in ms)
    private static final String KEY_AMOUNT = "amount";					// Gulp size (in oz)
	
    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

	@Override
	public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                					+ KEY_ID + " INTEGER PRIMARY KEY,"
            						+ KEY_DATE + " INTEGER,"
            						+ KEY_AMOUNT + " REAL" + ")";
        db.execSQL(CREATE_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
 
        onCreate(db);
	}
	
	// Add a single gulp entry into the database
	public void addGulp(final Gulp gulp) {
		SQLiteDatabase db;			// Database to write to
		ContentValues values;		// Values to add to database
		
		db = this.getWritableDatabase();
		
		values = new ContentValues();
	    values.put(KEY_DATE, gulp.getDate().getTime());
	    values.put(KEY_AMOUNT, gulp.getAmount());
	 
	    db.insert(TABLE_NAME, null, values);
	    db.close();
	}
	
	// Get a list of all the gulps listed in the database inbetween
	// startTime and endTime (inclusive)
	public List<Gulp> getGulps(final Date startTime, final Date endTime) {
		SQLiteDatabase db;		// Database to read from
		String query;			// SELECT statement to query database with
		Cursor cursor;			// Cursor used to iterator through query results
		List<Gulp> gulps;		// Stores all data returned from query
		Gulp gulp;				// A single gulp used to store data and
								//    then insert it into gulps
		
	    db = this.getReadableDatabase();
	 
	    query = "SELECT " + KEY_DATE + ", " + KEY_AMOUNT
	    				+ " FROM " + TABLE_NAME
	    				+ " WHERE " + KEY_DATE + " >= " + startTime.getTime()
	    				+ " AND " + KEY_DATE + "<= " + endTime.getTime();
	    
	    cursor = db.rawQuery(query, null);
	 
	    gulps = new ArrayList<Gulp>();
	    
	    if (cursor.moveToFirst()) {
	        do {
	            gulp = new Gulp();
	            gulp.setDate(new Date(Long.parseLong(cursor.getString(0))));
	            gulp.setAmount(Double.parseDouble(cursor.getString(1)));

	            gulps.add(gulp);
	        } while (cursor.moveToNext());
	    }
	 
	    return gulps;
	}
}