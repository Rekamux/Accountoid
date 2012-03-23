package net.axelschumacher.accountoid;

import java.util.Currency;
import java.util.Locale;

import net.axelschumacher.accountoid.Accountoid.Account;
import net.axelschumacher.accountoid.Accountoid.Categories;
import net.axelschumacher.accountoid.Accountoid.Currencies;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

public class AccountoidDataBase {

    private static final String TAG = "AccountoidDataBase";

	private static final String DATABASE_NAME = "accountoid.database";
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

	private static class DatabaseHelper extends SQLiteOpenHelper {
	    
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase database) {
			database.execSQL("CREATE TABLE " + ACCOUNT_TABLE_NAME + " ("
					+ Account._ID + " INTEGER PRIMARY KEY,"
					+ Account.CATEGORY + " INTEGER,"
					+ Account.CURRENCY + " INTEGER,"
					+ Account.DATE + " INTEGER,"
					+ Account.STATE + " INTEGER,"
					+ Account.DESCRIPTION + " TEXT,"
					+ Account.AMOUNT + " FLOAT"
					+ ");");
			database.execSQL("CREATE TABLE " + CATEGORIES_TABLE_NAME + " ("
					+ Categories._ID + " INTEGER PRIMARY KEY,"
					+ Categories.NAME + " TEXT UNIQUE"
					+ ");");
			database.execSQL("CREATE TABLE " + CURRENCIES_TABLE_NAME + " ("
					+ Currencies._ID + " INTEGER PRIMARY KEY,"
					+ Currencies.CODE + " TEXT UNIQUE,"
					+ Currencies.VALUE + " FLOAT"
					+ ");");
			
			// Insert at least the local currency
			ContentValues value = new ContentValues();
			value.put(Currencies.CODE, Currency.getInstance(Locale.getDefault()).getCurrencyCode());
			database.insert(CURRENCIES_TABLE_NAME, null, value);
		}

		@Override
		public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			database.execSQL("DROP TABLE IF EXISTS " + DATABASE_NAME);
			onCreate(database);
		}
	}

    private DatabaseHelper openHelper;
	
	private SQLiteDatabase database = null;
    
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

        String orderBy = Account.DEFAULT_SORT_ORDER;
        
        String projection[] = {Account._ID, Account.AMOUNT, Account.DESCRIPTION, Account.CURRENCY};
        
        openDataBase();
        Cursor c = qb.query(database, projection, null, null, null, null, orderBy);
        
        return c;
	}

	public Cursor getAccount(long id) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(ACCOUNT_TABLE_NAME);

        String orderBy = Account.DEFAULT_SORT_ORDER;
        
        String where = Account._ID+" = "+id;
        
        // We want everything
        openDataBase();
        Cursor c = qb.query(database, null, where, null, null, null, orderBy);
        
        return c;
	}
    
	public boolean deleteAccount(long id) {
		openDataBase();
		 int count;
		 count = database.delete(ACCOUNT_TABLE_NAME, Account._ID + "=" + id, null);
		 
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
        	throw new IllegalArgumentException("A category must be specified");
        if (!values.containsKey(Account.CURRENCY))
        	throw new IllegalArgumentException("A currency must be specified");
        if (!values.containsKey(Account.DATE))
        	values.put(Account.DATE, now);
        if (!values.containsKey(Account.DESCRIPTION))
        	throw new IllegalArgumentException("A description must be specified");
        if (!values.containsKey(Account.AMOUNT))
        	throw new IllegalArgumentException("An amount must be specified");
        if (!values.containsKey(Account.STATE))
        	values.put(Account.STATE, Accountoid.DEFAULT_STATE.ordinal());

        openDataBase();
        long rowId = database.insert(ACCOUNT_TABLE_NAME, null, values);
        if (rowId > 0) {
            Uri noteUri = ContentUris.withAppendedId(Account.CONTENT_URI, rowId);
            return noteUri;
        }
        
        throw new SQLException("Failed to insert row into " + ACCOUNT_TABLE_NAME);
	}

    public boolean updateAccount(long id, ContentValues values) {
    	openDataBase();
        int count;
        count = database.update(ACCOUNT_TABLE_NAME, values, Account._ID +"="+id, null);

        return count == 1;
    }

	/**
	 * Update a currency rate
	 * @param code
	 * @param rate
	 */
	public void updateCurrencyRate(long id, ContentValues values) {
		openDataBase();
        database.update(CURRENCIES_TABLE_NAME, values, Currencies._ID +"="+id, null);
	}

    /**
     * Return all categories
     * @return all categories
     */
	public Cursor getCategories() {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(CATEGORIES_TABLE_NAME);

        String orderBy = Categories.DEFAULT_SORT_ORDER;
        
        String projection[] = {Categories._ID, Categories.NAME};
        
        openDataBase();
        Cursor c = qb.query(database, projection, null, null, null, null, orderBy);
        
        return c;
	}

	/**
	 * Return all currencies
	 * @return all currencies
	 */
	public Cursor getCurrencies() {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(CURRENCIES_TABLE_NAME);

        String orderBy = Currencies.DEFAULT_SORT_ORDER;
        
        String projection[] = {Currencies._ID, Currencies.CODE, Currencies.VALUE};
        
        openDataBase();
        Cursor c = qb.query(database, projection, null, null, null, null, orderBy);
        
        return c;
	}

	public Uri insertCategory(ContentValues initialValues) {
        ContentValues values;
        if (initialValues != null)
            values = new ContentValues(initialValues);
        else
            values = new ContentValues();
        
        
        // Make sure that the fields are all set
        if (!values.containsKey(Categories.NAME))
        	throw new IllegalArgumentException("A name must be specified");

        Log.v(TAG, "Inserting category("+values.getAsString(Categories.NAME)+")");
        
        openDataBase();
        long rowId = database.insert(CATEGORIES_TABLE_NAME, null, values);
        if (rowId > 0) {
            Uri catUri = ContentUris.withAppendedId(Categories.CONTENT_URI, rowId);
            return catUri;
        }
        
        throw new SQLException("Failed to insert row into " + CATEGORIES_TABLE_NAME);
	}

	/**
	 * Insert a currency
	 * @throws IllegalArgumentException if {@link Currencies}.CODE field is not defined
	 * @throws SQLException if entry already exists
	 * @param initialValues
	 * @return the Uri
	 */
	public Uri insertCurrency(ContentValues initialValues) throws SQLiteConstraintException, IllegalArgumentException{
        ContentValues values;
        if (initialValues != null)
            values = new ContentValues(initialValues);
        else
            values = new ContentValues();
        
        // Make sure that the fields are all set
        if (!values.containsKey(Currencies.CODE))
        	throw new IllegalArgumentException("A code must be specified");

        String code = (String) values.get(Currencies.CODE);
        Currency.getInstance(code); // Will throw an Illegal argument exception if not valid
        
        Log.v(TAG, "Inserting currency("+Currency.getInstance(code)+")");
        
        openDataBase();
        long rowId;
        // How can I fore the printStackTrace if constraint error ? TODO
		rowId = database.insert(CURRENCIES_TABLE_NAME, null, values);
        if (rowId > 0) {
            Uri curUri = ContentUris.withAppendedId(Currencies.CONTENT_URI, rowId);
            return curUri;
        }
        
        throw new SQLiteConstraintException("Failed to insert row into " + CURRENCIES_TABLE_NAME);
		
	}

	/**
	 * Return a currency cursor from data base, given its index
	 * @param index
	 * @return cursor
	 */
	public Cursor getCurrencyCursorFromIndex(long index)
	{
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(CURRENCIES_TABLE_NAME);
        
        String projection[] = {Currencies._ID, Currencies.CODE};
        String where = Currencies._ID+"="+index;
        
        
        openDataBase();
        Cursor c = qb.query(database, projection, where, null, null, null, null);
        
        if (c.getCount() != 1)
        	throw new IndexOutOfBoundsException("No result found for this index");
		
        return c;
	}

	/**
	 * Return a currency code from data base, given its index
	 * @param index
	 * @return code
	 */
	public String getCurrencyCodeFromIndex(long index) {
        Cursor c = getCurrencyCursorFromIndex(index);
        int colIndex = c.getColumnIndex(Currencies.CODE);
        c.moveToFirst();
        return c.getString(colIndex);
	}

	/**
	 * Return a currency from data base, given its index
	 * @param l
	 * @return currency
	 */
	public Currency getCurrencyFromIndex(long l)
	{
        return Currency.getInstance(getCurrencyCodeFromIndex(l));
	}

	/**
	 * Delete the given currency
	 * @param id
	 */
	public boolean deleteCurrency(long id) {
		 SQLiteDatabase database = openHelper.getWritableDatabase();
		 int count;
		 count = database.delete(CURRENCIES_TABLE_NAME, Account._ID + "=" + id, null);
		 
		 return count == 1;
	}

	/**
	 * Delete the given category
	 * @param id
	 */
	public boolean deleteCategory(long id) {
		 SQLiteDatabase database = openHelper.getWritableDatabase();
		 int count;
		 count = database.delete(CATEGORIES_TABLE_NAME, Account._ID + "=" + id, null);
		 
		 return count == 1;
	}
	
	/**
	 * Delete all account using given currency id
	 * @param id
	 */
	public void deleteAccountUsingCurrency(long id)
	{
        String where = Account.CURRENCY+"="+id;
        
        openDataBase();
        database.delete(ACCOUNT_TABLE_NAME, where, null);
	}
	
	/**
	 * Delete all account using given category id
	 * @param id
	 */
	public void deleteAccountUsingCategory(long id) {
        String where = Account.CATEGORY+"="+id;
        
        openDataBase();
        database.delete(ACCOUNT_TABLE_NAME, where, null);
	}

	/**
	 * Return true if at least one transaction uses given currency id
	 * @param id
	 * @return
	 */
	public int getAccountsWithCurrency(long id) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(ACCOUNT_TABLE_NAME);
        
        String projection[] = {Account._ID};
        String where = Account.CURRENCY+"="+id;
        
        openDataBase();
        Cursor c = qb.query(database, projection, where, null, null, null, null);
        
        return c.getCount();
	}

	/**
	 * Return true if at least one transaction uses given category id
	 * @param id
	 * @return
	 */
	public int getAccountsWithCategory(long id) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(ACCOUNT_TABLE_NAME);
        
        String projection[] = {Account._ID};
        String where = Account.CATEGORY+"="+id;
        
        openDataBase(); 
        Cursor c = qb.query(database, projection, where, null, null, null, null);
        
        return c.getCount();
	}
	
	/**
	 * Mandatory call to avoid open never closed errors
	 */
	public void closeDataBase()
	{
		if (database != null)
		{
			database.close();
			database = null;
		}
	}
	
	private void openDataBase()
	{
		if (database == null)
			database = openHelper.getReadableDatabase();
	}
}
