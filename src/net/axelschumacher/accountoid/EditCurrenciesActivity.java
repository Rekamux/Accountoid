package net.axelschumacher.accountoid;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Currency;

import net.axelschumacher.accountoid.Accountoid.Currencies;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity used to edit currencies
 */
public class EditCurrenciesActivity extends ListActivity {
	/** Debug TAG */
	private static final String TAG = "EditCurrenciesActivity";

	/** Model */
	private Model model;

	/** List adapter */
	private SimpleCursorAdapter adapter;

	/** Menu buttons indexes */
	public static final int MENU_ITEM_DELETE = 1;
	public static final int MENU_UPDATE_RATES = 2;

	/** Dialogs indexes */
	public static final int DIALOG_CANNOT_FIND_CURRENCY = 1;
	public static final int DIALOG_CURRENCY_ALREADY_EXISTS = 2;
	public static final int DIALOG_USED_CURRENCY = 3;

	/** Bundle data */
	private final static String BUNDLE_BEING_DELETED_ID = "ID";
	private final static String BUNDLE_ACCOUNTS_COUNT = "COUNT";
	
	/** Time to wait between two updates */
	private static final long TIME_TO_WAIT = 3600*24;

	/** Timestamp file name */
	String TIMESTAMP_FILENAME = "timestamp";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.currencies);

		setTitle(R.string.currency_title);

		model = new Model(this);

		// Inform the list we provide context menus for items
		getListView().setOnCreateContextMenuListener(this);

		Cursor cursor = model.getDataBase().getCurrencies();
		adapter = new SimpleCursorAdapter(this, R.layout.currencies_cols,
				cursor, new String[] { Currencies.CODE, Currencies.VALUE },
				new int[] { R.id.currencies_cols_text1 });

		// To have a personalized render
		adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
			public boolean setViewValue(View view, Cursor cursor, int column) {
				TextView tv = (TextView) view;
				String currencyCode = cursor.getString(cursor
						.getColumnIndex(Currencies.CODE));
				Currency currency = Currency.getInstance(currencyCode);
				Currency usd = Currency.getInstance("USD");
				float value = cursor.getFloat(cursor
						.getColumnIndex(Currencies.VALUE));
				String text;
				if (value != 0)
					text = new DecimalFormat("#.####").format(1.0 / value)
							+ usd.getSymbol();
				else
					text = "";
				tv.setText(currency.getSymbol() + " (" + currencyCode
						+ ") (Value: " + text + ")");
				return true;
			}
		});
		setListAdapter(adapter);

		// Add a choice listener on the list if action is ACTION_PICK
		getListView().setOnItemClickListener(
				new AdapterView.OnItemClickListener() {
					public void onItemClick(AdapterView<?> adapter, View v,
							int pos, long id) {
						Log.d(TAG, "Clicked on item "+id+" at position "+pos);
						if (getIntent().getAction().equals(Intent.ACTION_PICK)) {
							setResult(RESULT_OK, new Intent().putExtra(
									Accountoid.INTENT_ID_NAME, id));
							finish();
						}
					}
				});

		updateRates(TIME_TO_WAIT);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		model.getDataBase().closeDataBase();
	}

	@Override
	protected void onPause() {
		super.onPause();
		model.getDataBase().closeDataBase();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Update view
		adapter.changeCursor(model.getDataBase().getCurrencies());
	}

	@Override
	protected Dialog onCreateDialog(int id, Bundle b) {
		Log.d(TAG, "onCreateDialog id=" + id + " bundle=" + b);
		if (b == null)
			return onCreateDialog(id);
		if (id == DIALOG_USED_CURRENCY) {
			final long beingDeleted = b.getLong(BUNDLE_BEING_DELETED_ID);
			final int count = b.getInt(BUNDLE_ACCOUNTS_COUNT);
			AlertDialog.Builder alert = new Builder(this);
			alert.setMessage(count + " "
					+ getString(R.string.delete_currency_confirm_message));
			alert.setTitle(R.string.delete_currency_confirm_title);
			alert.setPositiveButton(R.string.yes, new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					removeCurrency(beingDeleted);
				}
			});
			alert.setCancelable(true);
			alert.setNegativeButton(R.string.no, null);
			return alert.create();
		}
		return null;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Log.d(TAG, "onCreateDialog id=" + id);
		AlertDialog.Builder alert = new Builder(this);
		alert.setCancelable(true);
		alert.setPositiveButton(R.string.ok, null);
		alert.setTitle(R.string.dialog_error);
		switch (id) {
		case DIALOG_CANNOT_FIND_CURRENCY:
			alert.setMessage(R.string.dialog_cannot_find_currency);
			break;
		case DIALOG_CURRENCY_ALREADY_EXISTS:
			alert.setMessage(R.string.dialog_currency_already_exists);
			break;

		default:
			return null;
		}
		return alert.create();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(0, MENU_UPDATE_RATES, 0, R.string.browse_update_rates);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_UPDATE_RATES:
			updateRates(0);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * @return the last timestamp stored in the settings, or -1 if not found
	 */
	private long getTimestampFromSettings()
	{
		adapter.changeCursor(model.getDataBase().getCurrencies());
		SharedPreferences settings = getSharedPreferences(
				Accountoid.PREFS_NAME, 0);
		return settings.getLong(
				Accountoid.UPDATE_RATES_TIMESTAMP, -1);
		
	}

	/**
	 * Updates rates in background
	 * @param secondsToWait seconds to wait since the last known update
	 */
	private void updateRates(long secondsToWait) {
		long timestamp = getTimestampFromSettings();
		long now = System.currentTimeMillis()/1000L;
		Log.d(TAG, "timestamp: "+timestamp+" now: "+now+" ttw:"+secondsToWait);
		if (now-timestamp < secondsToWait)
		{
			Log.d(TAG, "Update rates, havn't waited enough, return");
			return;
		}
		Toast toast = Toast.makeText(this, R.string.start_update,
				Toast.LENGTH_SHORT);
		toast.show();
		UpdateRatesTask task = new UpdateRatesTask(this) {
			@Override
			protected void onPostExecute(Object result) {
				super.onPostExecute(result);
				long timestamp = getTimestampFromSettings();
				// Update view
				if (timestamp != -1) {
					TextView lastRate = (TextView) findViewById(R.id.currency_last_update);
					Calendar c = Calendar.getInstance();
					c.setTimeInMillis(timestamp * 1000L);
					String date = model.getDateFormat().format(c.getTime());
					lastRate.setText(getString(R.string.last_update) + ": "
							+ date);
				} else
					Log.e(TAG, "preference "
							+ Accountoid.UPDATE_RATES_TIMESTAMP
							+ " can't be found");
			}
		};
		Object[] o = null;
		task.execute(o);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view,
			ContextMenuInfo menuInfo) {
		AdapterView.AdapterContextMenuInfo info;
		try {
			info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		} catch (ClassCastException e) {
			Log.e(TAG, "bad menuInfo", e);
			return;
		}

		Cursor cursor = (Cursor) adapter.getItem(info.position);
		if (cursor == null) {
			// For some reason the requested item isn't available, do nothing
			return;
		}

		// Setup the menu header
		menu.setHeaderTitle(cursor.getString(cursor
				.getColumnIndex(Currencies.CODE)));

		// Add a menu item to delete the currency
		menu.add(0, MENU_ITEM_DELETE, 0, R.string.currency_delete);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info;
		try {
			info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		} catch (ClassCastException e) {
			Log.e(TAG, "bad menuInfo", e);
			return false;
		}

		switch (item.getItemId()) {
		case MENU_ITEM_DELETE: {
			tryToRemoveCurrency(info.id);
			return true;
		}
		}
		return false;
	}

	/**
	 * Try to remove given currency
	 * 
	 * @param id
	 */
	private void tryToRemoveCurrency(long id) {
		int accountsWithCurrency = model.getDataBase().getAccountsWithCurrency(
				id);

		if (accountsWithCurrency >= 1) {
			Bundle b = new Bundle(2);
			b.putLong(BUNDLE_BEING_DELETED_ID, id);
			b.putInt(BUNDLE_ACCOUNTS_COUNT, accountsWithCurrency);
			// To ensure a updated call to onCreateDialog(int, Bundle)
			removeDialog(DIALOG_USED_CURRENCY);
			showDialog(DIALOG_USED_CURRENCY, b);
			return;
		}
		removeCurrency(id);
	}

	private void removeCurrency(long id) {
		model.getDataBase().deleteCurrency(id);
		model.getDataBase().deleteAccountUsingCurrency(id);
		// Update view
		adapter.changeCursor(model.getDataBase().getCurrencies());
	}

	/**
	 * Try to add a currency
	 */
	public void addCurrency(View view) {
		EditText t = (EditText) findViewById(R.id.currency_edit);
		String code = t.getEditableText().toString();
		t.setText("");
		// Remove trailing spaces
		code = code.replaceAll("\\s+$", "");
		ContentValues value = new ContentValues();
		value.put(Currencies.CODE, code);
		try {
			model.getDataBase().insertCurrency(value);
		} catch (SQLException e) {
			// If the currency already exists
			Log.d(TAG,
					"Sorry for the print stack trace, obviously from SQL code");
			showDialog(DIALOG_CURRENCY_ALREADY_EXISTS);
			return;
		} catch (IllegalArgumentException e) {
			// If the code is not valid
			showDialog(DIALOG_CANNOT_FIND_CURRENCY);
			return;
		}
		// Update view
		adapter.changeCursor(model.getDataBase().getCurrencies());
		// We fetch rates for the new currency
		updateRates(0);
	}
}
