package net.axelschumacher.accountoid;

import net.axelschumacher.accountoid.Accountoid.Account;
import net.axelschumacher.accountoid.Accountoid.Categories;
import net.axelschumacher.accountoid.Accountoid.Currencies;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

public class AccountoidDataBase {

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
					+ Account.AMOUNT + " FLOAT"
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
    
    public AccountoidDataBase(Context context) {
		openHelper = new DatabaseHelper(context);
		Log.d(TAG, "Created provider");
	}

	public boolean onCreate() {
		return true;
	}
	
	public Cursor getAccountList() {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(ACCOUNT_TABLE_NAME);

        String orderBy = Accountoid.Account.DEFAULT_SORT_ORDER;
        
        String projection[] = {Account._ID, Account.AMOUNT, Account.DESCRIPTION};
        
        // Get the database and run the query
        SQLiteDatabase db = openHelper.getReadableDatabase();
        Cursor c = qb.query(db, projection, null, null, null, null, orderBy);
        
        return c;
	}

	public Cursor getAccount(long id) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(ACCOUNT_TABLE_NAME);

        String orderBy = Accountoid.Account.DEFAULT_SORT_ORDER;
        
        String where = Account._ID+" = "+id;
        
        // We want everything
        SQLiteDatabase db = openHelper.getReadableDatabase();
        Cursor c = qb.query(db, null, where, null, null, null, orderBy);
        
        return c;
	}
    
	public boolean deleteAccount(long id) {
		 SQLiteDatabase db = openHelper.getWritableDatabase();
		 int count;
		 count = db.delete(ACCOUNT_TABLE_NAME, Account._ID + "=" + id, null);
		 
		 return count == 1;
	}

	public void deleteAllAccounts() {
		 openHelper.getWritableDatabase().delete(ACCOUNT_TABLE_NAME, null, null);
	}

	public Uri insertAccount(ContentValues initialValues) {

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
        if (!values.containsKey(Account.AMOUNT))
        	throw new IllegalArgumentException("A ammount must be specified");
        if (!values.containsKey(Account.STATE))
        	values.put(Account.STATE, Accountoid.DEFAULT_STATE.ordinal());

        SQLiteDatabase db = openHelper.getWritableDatabase();
        long rowId = db.insert(ACCOUNT_TABLE_NAME, null, values);
        if (rowId > 0) {
            Uri noteUri = ContentUris.withAppendedId(Account.CONTENT_URI, rowId);
            return noteUri;
        }
        
        throw new SQLException("Failed to insert row into " + ACCOUNT_TABLE_NAME);
	}

    public boolean updateAccount(int id, ContentValues values) {
        SQLiteDatabase db = openHelper.getWritableDatabase();
        int count;
        count = db.update(ACCOUNT_TABLE_NAME, values, Account._ID +"="+id, null);

        return count == 1;
    }

}
