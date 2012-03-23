package net.axelschumacher.accountoid;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Currency;

import android.content.Context;

/**
 * Model knowing the real stuffs
 */
public class Model {
	@SuppressWarnings("unused")
	private static final String TAG = "Model";

	public Model(Context context) {
		dataBase = new AccountoidDataBase(context);
		numberFormat = NumberFormat.getCurrencyInstance();
		dateFormat = android.text.format.DateFormat.getDateFormat(context);
	}

	private AccountoidDataBase dataBase;

	private NumberFormat numberFormat;

	private DateFormat dateFormat;

	public DateFormat getDateFormat() {
		return dateFormat;
	}

	public AccountoidDataBase getDataBase() {
		return dataBase;
	}

	public NumberFormat getNumberFormat(Currency c) {
		if (c != null) {
			numberFormat.setCurrency(c);
		}
		return numberFormat;
	}
}
