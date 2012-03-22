package net.axelschumacher.accountoid;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;

import net.axelschumacher.accountoid.Accountoid.Account;
import net.axelschumacher.accountoid.Accountoid.Categories;
import net.axelschumacher.accountoid.Accountoid.Currencies;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Edit a single transaction
 */
public class EditTransactionActivity extends Activity {
	/** log tag */
	private static final String TAG = "EditTransactionActivity";

	/** Model */
	private Model model;

	/** Insert or edit */
	private int state;

	private static final int STATE_EDIT = 0;
	private static final int STATE_INSERT = 1;

	/** Query cursor for account table */
	private Cursor cursorAccount = null;

	/** Query cursor for category table */
	private Cursor cursorCategory = null;

	/** Query cursor for currency table */
	private Cursor cursorCurrency = null;

	/** Amount input */
	private EditText amountEditText;

	/** Description input */
	private EditText descriptionEditText;

	/** Date input */
	private DatePicker dateDatePicker;

	/** Category input */
	private Spinner categorySpinner;

	/** Category selected index */
	private int selectedCategory;

	/** State input */
	private Spinner stateSpinner;

	/** Currency input */
	private Spinner currencySpinner;

	/** Currency selected index */
	private int selectedCurrency;

	// TODO handle savedInstanceState

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Program model
		model = new Model(this);

		// Knowing where we come from
		final Intent intent = getIntent();

		// Categories cursor
		cursorCategory = model.getDataBase().getCategories();

		// Currencies cursor
		cursorCurrency = model.getDataBase().getCurrencies();

		// fetching action type
		final String action = intent.getAction();
		if (Intent.ACTION_EDIT.equals(action)) {
			state = STATE_EDIT;
			long id = intent.getLongExtra(Accountoid.INTENT_ID_NAME, 0);
			cursorAccount = model.getDataBase().getAccount(id);
			if (cursorAccount.getCount() != 1) {
				Log.e(TAG, "Given ID not acceptable");
				finish();
				return;
			}

		} else if (Intent.ACTION_INSERT.equals(action)) {
			state = STATE_INSERT;
		} else {
			// Whoops, unknown action! Bail.
			Log.e(TAG, "Unknown action, exiting");
			finish();
			return;
		}

		// Setting the layout
		setContentView(R.layout.transaction_editor);

		// Getting the inputs
		amountEditText = (EditText) findViewById(R.id.amount_field_edit);
		// Format the input
		amountEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					try {
						amountEditText
								.setText(formatAmountFromCurrencyAndAmount());
					} catch (NumberFormatException e) {
					}
				}
			}
		});

		descriptionEditText = (EditText) findViewById(R.id.description_field_edit);

		dateDatePicker = (DatePicker) findViewById(R.id.date_field_edit);
		int year, monthOfYear, dayOfMonth;
		// If we create a new transaction, now will be used
		if (state == STATE_INSERT) {
			Calendar c = Calendar.getInstance();
			year = c.get(Calendar.YEAR);
			monthOfYear = c.get(Calendar.MONTH);
			dayOfMonth = c.get(Calendar.DAY_OF_MONTH);
		} else {
			Date date = new Date(cursorAccount.getLong(cursorAccount
					.getColumnIndex(Account.DATE)));
			year = date.getYear();
			monthOfYear = date.getMonth();
			dayOfMonth = date.getDay();
		}
		dateDatePicker.updateDate(year, monthOfYear, dayOfMonth);

		categorySpinner = (Spinner) findViewById(R.id.category_field_edit);
		startManagingCursor(cursorCategory);
		String[] columns = new String[] { Categories.NAME };
		int[] to = new int[] { android.R.id.text1 };
		SimpleCursorAdapter categoryAdapter = new SimpleCursorAdapter(this,
				android.R.layout.simple_spinner_dropdown_item, cursorCategory,
				columns, to);
		categorySpinner.setAdapter(categoryAdapter);
		categorySpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view,
					int pos, long id) {
				Cursor c = (Cursor) parent.getItemAtPosition(pos);
				selectedCategory = c.getInt(c
						.getColumnIndexOrThrow(Currencies._ID));
			}

			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

		stateSpinner = (Spinner) findViewById(R.id.state_field_edit);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.states, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		stateSpinner.setAdapter(adapter);
		stateSpinner.setSelection(Accountoid.DEFAULT_STATE.ordinal());

		currencySpinner = (Spinner) findViewById(R.id.currency_field_edit);
		startManagingCursor(cursorCurrency);
		SimpleCursorAdapter currencyAdapter = new SimpleCursorAdapter(this,
				android.R.layout.simple_spinner_dropdown_item, cursorCurrency,
				new String[] { Currencies.CODE },
				new int[] { android.R.id.text1 });
		// Change the code to the currency string
		currencyAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
			public boolean setViewValue(View view, Cursor cursor, int column) {
				TextView tv = (TextView) view;
				String code = cursor.getString(cursor
						.getColumnIndex(Currencies.CODE));
				tv.setText(Currency.getInstance(code).getSymbol());
				return true;
			}
		});
		currencySpinner.setAdapter(currencyAdapter);
		currencySpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view,
					int pos, long id) {
				Cursor c = (Cursor) parent.getItemAtPosition(pos);
				selectedCurrency = c.getInt(c
						.getColumnIndexOrThrow(Currencies._ID));
			}

			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
	}

	/**
	 * Add a category
	 * 
	 * @param v
	 */
	public void addCategory(View v) {
		ContentValues value = new ContentValues();
		value.put(Categories.NAME, "Food");
		model.getDataBase().insertCategory(value);
		((SimpleCursorAdapter) categorySpinner.getAdapter()).changeCursor(model
				.getDataBase().getCategories());
	}

	/**
	 * Add a currency
	 * 
	 * @param v
	 */
	public void addCurrency(View v) {
		ContentValues value = new ContentValues();
		value.put(Currencies.CODE, "EUR");
		model.getDataBase().insertCurrency(value);
		((SimpleCursorAdapter) currencySpinner.getAdapter()).changeCursor(model
				.getDataBase().getCurrencies());
	}

	/**
	 * Format a string of float according to selected currency and
	 * amountTextEdit value
	 * 
	 * @throws NumberFormatException
	 */
	public String formatAmountFromCurrencyAndAmount() {
		// Start with parsing to avoid unnecessary computations in case of
		// throwing
		Log.d(TAG, "formatAmountFromCurrencyAndAmount");
		float amount = Float.parseFloat(amountEditText.getEditableText()
				.toString());
		Currency currency = model.getDataBase().getCurrencyFromIndex(
				selectedCurrency);
		DecimalFormat df = model.getDecimalFormat(currency);
		return df.format(amount);
	}

	/**
	 * Save transaction
	 */
	public void saveTransaction(View v) {
		Log.v(TAG, "Save transaction");
		ContentValues value = new ContentValues();

		// Parse amount and potentially handle a NumberFormatException
		float amount;
		try {
			amount = Float.parseFloat(formatAmountFromCurrencyAndAmount());
			if (amount == 0)
				throw new NumberFormatException("Amount cannot be null");
		} catch (NumberFormatException e) {
			showDialog(DIALOG_COMPLAIN_AMOUNT);
			return;
		}
		value.put(Account.AMOUNT, amount);

		// Parse description and ask for it not to be empty
		String description = descriptionEditText.getEditableText().toString();
		if (description.isEmpty()) {
			showDialog(DIALOG_COMPLAIN_DESCRIPTION);
			return;
		}
		value.put(Account.DESCRIPTION, description);

		// If no currency selected, user will create at least one
		Object currencyObject = currencySpinner.getSelectedItem();
		if (currencyObject == null) {
			showDialog(DIALOG_COMPLAIN_CURRENCY);
			addCurrency(null);
			return;
		}
		long currency = currencySpinner.getSelectedItemId();
		value.put(Account.CURRENCY, currency);

		// If no category selected, user will create at least one
		Object categoryObject = categorySpinner.getSelectedItem();
		if (categoryObject == null) {
			showDialog(DIALOG_COMPLAIN_CATEGORY);
			addCategory(null);
			return;
		}
		long category = categorySpinner.getSelectedItemId();
		value.put(Account.CATEGORY, category);

		// Handle state choice
		long state = stateSpinner.getSelectedItemId();
		value.put(Account.STATE, state);

		model.getDataBase().insertAccount(value);
		returnBrowsing();
	}

	public static final int DIALOG_COMPLAIN_AMOUNT = 0;
	public static final int DIALOG_COMPLAIN_DESCRIPTION = 1;
	public static final int DIALOG_COMPLAIN_CURRENCY = 2;
	public static final int DIALOG_COMPLAIN_CATEGORY = 3;

	protected void returnBrowsing() {
		startActivity(new Intent(Intent.ACTION_DEFAULT, null, this,
				BrowseActivity.class));
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder alert = new Builder(this);
		alert.setPositiveButton(R.string.ok, null);
		alert.setCancelable(true);
		switch (id) {
		case DIALOG_COMPLAIN_AMOUNT:
			alert.setTitle(R.string.dialog_error);
			alert.setMessage(R.string.dialog_complain_amount_message);
			break;
		case DIALOG_COMPLAIN_DESCRIPTION:
			alert.setTitle(R.string.dialog_error);
			alert.setMessage(R.string.dialog_complain_description_message);
			break;
		case DIALOG_COMPLAIN_CURRENCY:
			alert.setTitle(R.string.dialog_create_currency);
			alert.setMessage(R.string.dialog_complain_currency_message);
			break;
		case DIALOG_COMPLAIN_CATEGORY:
			alert.setTitle(R.string.dialog_create_category);
			alert.setMessage(R.string.dialog_complain_category_message);
			break;

		default:
			return null;
		}
		return alert.create();
	}

	/**
	 * Cancel transaction
	 */
	public void cancelTransaction(View v) {
		Log.v(TAG, "Cancel transaction");
		// We simply return to the browsing activity
		returnBrowsing();
	}
}
