package net.axelschumacher.accountoid;

import java.util.Calendar;
import java.util.Date;

import net.axelschumacher.accountoid.Accountoid.Account;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
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

	/** Query cursor */
	private Cursor cursor = null;

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

		model = new Model(this);

		final Intent intent = getIntent();

		// fetching action type
		final String action = intent.getAction();
		if (Intent.ACTION_EDIT.equals(action)) {
			state = STATE_EDIT;
			long id = intent.getLongExtra(Accountoid.INTENT_ID_NAME, 0);
			cursor = model.getDataBase().getAccount(id);
			if (cursor.getCount() != 1) {
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
				if (!hasFocus)
				{
					try {
						float f = Float.parseFloat(amountEditText.getText().toString());
						Log.d(TAG, Float.toString(f));
						amountEditText.setText(model.getDecimalFormat().format(f));
					} catch (NumberFormatException e) {
					}
				}
			}
		});

		descriptionEditText = (EditText) findViewById(R.id.description_field_edit);
		dateDatePicker = (DatePicker) findViewById(R.id.date_field_edit);
		categorySpinner = (Spinner) findViewById(R.id.category_field_edit);
		stateSpinner = (Spinner) findViewById(R.id.state_field_edit);
		int year, monthOfYear, dayOfMonth;
		// If we create a new transaction, now will be used
		if (state == STATE_INSERT) {
			Calendar c = Calendar.getInstance();
			year = c.get(Calendar.YEAR);
			monthOfYear = c.get(Calendar.MONTH);
			dayOfMonth = c.get(Calendar.DAY_OF_MONTH);
		} else {
			Date date = new Date(cursor.getLong(cursor.getColumnIndex(Account.DATE)));
			year = date.getYear();
			monthOfYear = date.getMonth();
			dayOfMonth = date.getDay();
		}
		dateDatePicker.updateDate(year, monthOfYear, dayOfMonth);
		
		currencySpinner = (Spinner) findViewById(R.id.currency_field_edit);
	}

}
