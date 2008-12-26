package net.androidchat.client;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Hashtable;
import java.util.Vector;

public class ACDatabaseAdapter extends SQLiteOpenHelper {

	private static final String DB_NAME = "servers.db";
	private static final int DB_VERSION = 1;
	private static final String DB_TABLE = "server_list";
	
	private Context mContext;
	
	private static final String TABLE_CREATE = 
		"create table server_list (_id INTEGER PRIMARY KEY, title TEXT NOT NULL, address TEXT NOT NULL);";
	
	public ACDatabaseAdapter(Context c) {
		super(c, DB_NAME, null, DB_VERSION);
		
		mContext = c;
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(TABLE_CREATE);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//nothing yet.
	}
	
	public long addServer(String title, String address) {
		SQLiteDatabase db = getWritableDatabase();
		
		ContentValues values = new ContentValues();
		values.put("title", title);
		values.put("address", address);
		
		long id = db.insert(DB_TABLE, null, values);
		
		db.close();
		
		return id;
	}
	
	public int deleteServer(long id) {
		
		SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(DB_TABLE, "_id = ?", new String[] { String.valueOf(id) });
        db.close();
        
        return result;
	}
	
	public Vector<String> getTitles() {
		SQLiteDatabase db = getReadableDatabase();
		Vector<String> titles = new Vector<String>();
		
		Cursor c = db.query(DB_TABLE, null, null, null, null, null, null);
		
		while (c.moveToNext()) {
			titles.add(c.getString(1));
		}
		
		c.close();
		db.close();
		
		return titles;
	}
	
	public Vector<String> getAddresses() {
		SQLiteDatabase db = getReadableDatabase();
		Vector<String> addresses = new Vector<String>();
		
		Cursor c = db.query(DB_TABLE, null, null, null, null, null, null);
		
		while (c.moveToNext()) {
			addresses.add(c.getString(2));
		}
		
		c.close();
		db.close();
		
		return addresses;
	}
}
