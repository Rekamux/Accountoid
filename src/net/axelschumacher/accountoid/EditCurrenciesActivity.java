package net.axelschumacher.accountoid;

import java.util.Currency;

import net.axelschumacher.accountoid.Accountoid.Currencies;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

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
	public static final int MENU_ITEM_INSERT = 1;
	public static final int MENU_ITEM_DELETE = 2;

	/** Dialogs indexes */
	public static final int DIALOG_LAST_ALIVE = 1;
	public static final int DIALOG_CANNOT_FIND_CURRENCY = 2;
	public static final int DIALOG_CANNOT_CURRENCY_ALREADY_EXISTS = 3;

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
				cursor, new String[] { Currencies.CODE },
				new int[] { R.id.currencies_cols_text1 });

		// To have a personalized render
		adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
			public boolean setViewValue(View view, Cursor cursor, int column) {
				TextView tv = (TextView) view;
				String currencyCode = cursor.getString(cursor
						.getColumnIndex(Currencies.CODE));
				Currency currency = Currency.getInstance(currencyCode);
				tv.setText(currency.getSymbol() + " (" + currencyCode + ")");
				return true;
			}
		});
		setListAdapter(adapter);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Update view
		adapter.changeCursor(model.getDataBase().getCurrencies());
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder alert = new Builder(this);
		alert.setCancelable(true);
		alert.setPositiveButton(R.string.ok, null);
		alert.setTitle(R.string.dialog_error);
		switch (id) {
		case DIALOG_CANNOT_FIND_CURRENCY:
			alert.setMessage(R.string.dialog_cannot_find_currency);
			break;
		case DIALOG_LAST_ALIVE:
			alert.setMessage(R.string.dialog_last_currency);
			break;
		case DIALOG_CANNOT_CURRENCY_ALREADY_EXISTS:
			alert.setMessage(R.string.dialog_currency_already_exists);
			break;

		default:
			return null;
		}
		return alert.create();
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
			removeCurrency(info.id);
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
	private void removeCurrency(long id) {
		if (model.getDataBase().getCurrencies().getCount() <= 1) {
			showDialog(DIALOG_LAST_ALIVE);
			return;
		}
		model.getDataBase().deleteCurrency(id);
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
		} catch (SQLiteConstraintException e) {
			// If the currency already exists
			Log.d(TAG, "Sorry for the print stack trace, obviously from SQL code");
			showDialog(DIALOG_CANNOT_CURRENCY_ALREADY_EXISTS);
			return;
		} catch (IllegalArgumentException e) {
			// If the code is not valid
			showDialog(DIALOG_CANNOT_FIND_CURRENCY);
			return;
		}
		// Update view
		adapter.changeCursor(model.getDataBase().getCurrencies());
	}
}
