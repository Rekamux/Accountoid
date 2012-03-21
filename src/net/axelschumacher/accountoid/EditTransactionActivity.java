package net.axelschumacher.accountoid;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import net.axelschumacher.accountoid.Accountoid.Account;
import net.axelschumacher.accountoid.Accountoid.Categories;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

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

	/** State input */
	private Spinner stateSpinner;

	/** Currency input */
	private Spinner currencySpinner;

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
		amountEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					try {
						float f = Float.parseFloat(amountEditText.getText()
								.toString());
						Log.d(TAG, Float.toString(f));
						amountEditText.setText(model.getDecimalFormat().format(
								f));
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
		SimpleCursorAdapter mAdapter = new SimpleCursorAdapter(this,
				android.R.layout.simple_spinner_dropdown_item, cursorCategory,
				columns, to);
		categorySpinner.setAdapter(mAdapter);
		ContentValues value = new ContentValues();
		value.put(Categories.NAME, Integer.toString((new Random()).nextInt()));
		model.getDataBase().insertCategory(value);

		stateSpinner = (Spinner) findViewById(R.id.state_field_edit);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.states, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		stateSpinner.setAdapter(adapter);
		stateSpinner.setSelection(Accountoid.DEFAULT_STATE.ordinal());

		currencySpinner = (Spinner) findViewById(R.id.currency_field_edit);
	}

}
