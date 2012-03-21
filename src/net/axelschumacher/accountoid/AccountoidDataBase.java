package net.axelschumacher.accountoid;

import net.axelschumacher.accountoid.Accountoid.Account;
import net.axelschumacher.accountoid.Accountoid.Categories;
import net.axelschumacher.accountoid.Accountoid.Currencies;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class AccountoidDataBase extends ContentProvider {

    private static final String TAG = "AccountoidProvider";

	private static final String DATABASE_NAME = "accountoid.db";
	private static final int DATABASE_VERSION = 2;
	private static final String ACCOUNT_TABLE_NAME = "account";
	private static final String CATEGORIES_TABLE_NAME = "categories";
	private static final String CURRENCIES_TABLE_NAME = "currencies";
	
	public enum Tables
	{
		ACCOUNT,
		CATEGORIES,
		CURRENCIES
	}

	/**
	 * This class helps open, create, and upgrade the database file.
	 */
	private static class DatabaseHelper extends SQLiteOpenHelper {
	    
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + ACCOUNT_TABLE_NAME + " ("
					+ Account._ID + " INTEGER PRIMARY KEY,"
					+ Account.CATEGORY + " INTEGER,"
					+ Account.CURRENCY + " INTEGER,"
					+ Account.DATE + " INTEGER,"
					+ Account.STATE + " INTEGER,"
					+ Account.DESCRIPTION + " TEXT,"
					+ Account.AMMOUNT + " FLOAT"
					+ ");");
			db.execSQL("CREATE TABLE " + CATEGORIES_TABLE_NAME + " ("
					+ Categories._ID + " INTEGER PRIMARY KEY,"
					+ Categories.NAME + " TEXT"
					+ ");");
			db.execSQL("CREATE TABLE " + CURRENCIES_TABLE_NAME + " ("
					+ Currencies._ID + " INTEGER PRIMARY KEY,"
					+ Currencies.CODE + " TEXT,"
					+ Currencies.VALUE + " FLOAT"
					+ ");");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_NAME);
			onCreate(db);
		}
	}

    private DatabaseHelper openHelper;

	@Override
	public boolean onCreate() {
		openHelper = new DatabaseHelper(getContext());
		Log.d(TAG, "Created provider");
		return true;
	}
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(ACCOUNT_TABLE_NAME);

        // If no sort order is specified use the default
        String orderBy;
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = Accountoid.Account.DEFAULT_SORT_ORDER;
        } else {
            orderBy = sortOrder;
        }
        
        // Get the database and run the query
        SQLiteDatabase db = openHelper.getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);

        // Tell the cursor what uri to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
	}
    
	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		 SQLiteDatabase db = openHelper.getWritableDatabase();
		 int count;
		 String noteId = uri.getPathSegments().get(1);
		 count = db.delete(ACCOUNT_TABLE_NAME, Account._ID + "=" + noteId
                 + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
		 
		 getContext().getContentResolver().notifyChange(uri, null);
		 
		 return count;
	}

	@Override
	public String getType(Uri uri) {
		return Account.CONTENT_TYPE;
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {

		// Values to be inserted
        ContentValues values;
        if (initialValues != null)
            values = new ContentValues(initialValues);
        else
            values = new ContentValues();
        
        // Current time
        Long now = Long.valueOf(System.currentTimeMillis());
        
        // Make sure that the fields are all set
        if (!values.containsKey(Account.CATEGORY))
        	values.put(Account.CATEGORY, 0); // TODO
        if (!values.containsKey(Account.CURRENCY))
        	values.put(Account.CURRENCY, 0); // TODO
        if (!values.containsKey(Account.DATE))
        	values.put(Account.DATE, now);
        if (!values.containsKey(Account.DESCRIPTION))
        	throw new IllegalArgumentException("A description must be specified");
        if (!values.containsKey(Account.AMMOUNT))
        	throw new IllegalArgumentException("A ammount must be specified");
        if (!values.containsKey(Account.STATE))
        	values.put(Account.STATE, Accountoid.DEFAULT_STATE.ordinal());

        SQLiteDatabase db = openHelper.getWritableDatabase();
        long rowId = db.insert(ACCOUNT_TABLE_NAME, null, values);
        if (rowId > 0) {
            Uri noteUri = ContentUris.withAppendedId(Account.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(noteUri, null);
            return noteUri;
        }
        
        throw new SQLException("Failed to insert row into " + uri);
	}

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        SQLiteDatabase db = openHelper.getWritableDatabase();
        int count;
        count = db.update(ACCOUNT_TABLE_NAME, values, where, whereArgs);

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

}
