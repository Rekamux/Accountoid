package net.axelschumacher.accountoid;

import java.util.Calendar;
import java.util.Currency;

import net.axelschumacher.accountoid.Accountoid.Account;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class BrowseActivity extends ListActivity {
	/** Debug TAG */
	private static final String TAG = "BrowseActivity";

	/** Model */
	private Model model;

	/** List adapter */
	private SimpleCursorAdapter adapter;

	/** Menu buttons indexes */
	public static final int MENU_ITEM_INSERT = 1;
	public static final int MENU_ITEM_DELETE = 2;
	public static final int MENU_ITEM_DELETE_ALL = 3;
	public static final int MENU_ITEM_EDIT = 4;
	public static final int MENU_EDIT_CURRENCIES = 5;
	public static final int MENU_EDIT_CATEGORIES = 6;

	/** Dialogs indexes */
	public static final int DIALOG_DELETE_ALL = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.browse);

		setTitle(R.string.browse_title);

		model = new Model(this);

		// Inform the list we provide context menus for items
		getListView().setOnCreateContextMenuListener(this);

		Cursor cursor = model.getDataBase().getAccountList();
		adapter = new SimpleCursorAdapter(this, R.layout.browse_cols, cursor,
				new String[] { Account.AMOUNT },
				new int[] { R.id.browse_cols_text1 });

		// To have a personalized render
		// http://stackoverflow.com/questions/4776936/modifying-simplecursoradapter-data
		adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
			public boolean setViewValue(View view, Cursor cursor, int column) {
				TextView tv = (TextView) view;
				float amount = cursor.getFloat(cursor
						.getColumnIndex(Account.AMOUNT));
				String description = cursor.getString(cursor
						.getColumnIndex(Account.DESCRIPTION));
				int currencyId = cursor.getInt(cursor
						.getColumnIndex(Account.CURRENCY));
				long date = cursor.getLong(cursor.getColumnIndex(Account.DATE));
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(date * 1000L);
				Log.d(TAG, "For entry with currency " + currencyId);
				String currencyCode = model.getDataBase()
						.getCurrencyCodeFromIndex(currencyId);
				Currency currency = Currency.getInstance(currencyCode);
				tv.setText(model.getDecimalFormat(currency).format(amount)
						+ currency.getSymbol() + " (" + description + ") "
						+ model.getDateFormat().format(cal.getTime()));
				if (amount <= 0.0)
					tv.setTextColor(Color.RED);
				else
					tv.setTextColor(Color.GREEN);
				return true;
			}
		});
		setListAdapter(adapter);
		
		//Add a click listener on total
//		Text
	}

	@Override
	protected void onPause() {
		super.onPause();
		model.getDataBase().closeDataBase();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		model.getDataBase().closeDataBase();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Update view
		updateViews();
	}

	private void updateViews() {
		adapter.changeCursor(model.getDataBase().getAccountList());
		TextView sumTextView = (TextView) findViewById(R.id.browse_total);
		Float sum = model.getDataBase().getTransactionsSum();
		if (sum != null) {
			sumTextView.setText(getString(R.string.total)
					+ ": "+model.getDecimalFormat(null).format(sum)+Currency.getInstance("USD").getSymbol());
			if (sum < 0) {
				sumTextView.setTextColor(Color.RED);
			} else {
				sumTextView.setTextColor(Color.GREEN);

			}
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog box = null;
		switch (id) {
		case DIALOG_DELETE_ALL:

			AlertDialog.Builder alert = new Builder(this);
			alert.setMessage(R.string.delete_all_confirm_message);
			alert.setTitle(R.string.delete_all_confirm_title);
			alert.setPositiveButton(R.string.yes, new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					removeAllAccounts();
				}
			});
			alert.setCancelable(true);
			alert.setNegativeButton(R.string.no, null);
			box = alert.create();
			break;

		default:
			break;
		}
		return box;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(0, MENU_ITEM_INSERT, 0, R.string.browse_add);
		menu.add(0, MENU_ITEM_DELETE_ALL, 0, R.string.browse_delete_all);
		menu.add(0, MENU_EDIT_CATEGORIES, 0, R.string.browse_edit_categories);
		menu.add(0, MENU_EDIT_CURRENCIES, 0, R.string.browse_edit_currencies);

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ITEM_INSERT:
			// Launch activity to insert a new item
			addAmmount(getCurrentFocus());
			return true;
		case MENU_ITEM_DELETE_ALL:
			askRemoveAllAccounts();
			return true;
		case MENU_EDIT_CATEGORIES:
			editCategories();
			return true;
		case MENU_EDIT_CURRENCIES:
			editCurrencies();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void editCurrencies() {
		startActivity(new Intent(this, EditCurrenciesActivity.class));

	}

	private void editCategories() {
		startActivity(new Intent(this, EditCategoriesActivity.class));

	}

	public void addAmmount(View v) {
		Log.d(TAG, "Add ammount");
		startActivity(new Intent(Intent.ACTION_INSERT, null, this,
				EditTransactionActivity.class));
	}

	public void removeAmmount(long id) {
		Log.d(TAG, "Remove ammount " + id);
		model.getDataBase().deleteAccount(id);
		// Update view
		// If I used a provider, it would have been done automatically
		adapter.changeCursor(model.getDataBase().getAccountList());
	}

	public void askRemoveAllAccounts() {
		showDialog(DIALOG_DELETE_ALL);
	}

	public void removeAllAccounts() {
		Log.d(TAG, "Remove all ammounts");
		model.getDataBase().deleteAllAccounts();
		// Update view
		updateViews();
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
				.getColumnIndex(Account.DESCRIPTION)));

		// Add a menu item to delete the transaction
		menu.add(0, MENU_ITEM_DELETE, 0, R.string.browse_delete);
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
			removeAmmount(info.id);
			return true;
		}
		}
		return false;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Log.v(TAG, "id " + id + " clicked");
		Intent intent = new Intent(Intent.ACTION_EDIT, null, this,
				EditTransactionActivity.class);
		intent.putExtra(Accountoid.INTENT_ID_NAME, id);
		startActivity(intent);
	}
}
