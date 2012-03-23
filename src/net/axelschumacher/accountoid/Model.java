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

	/**
	 * Format the given float by handling negative ones and removing
	 * parentheses, according to currency
	 * 
	 * @param c
	 * @param f
	 * @return
	 */
	public String format(Currency c, float f, boolean removeSymbols) {
		if (c != null) {
			numberFormat.setCurrency(c);
		}
		boolean negative = f < 0;
		String formated = numberFormat.format(f);
		if (removeSymbols)
			formated = formated.replaceAll("[^0-9.-]", "");
		else
			formated = formated.replaceAll("[()]", "");
		if (negative) {
			String minus = "-";
			formated = minus.concat(formated);
		}
		return formated;
	}

	/**
	 * Format the given float by handling negative ones and removing
	 * parentheses, according to currency, but will not remove symbols
	 * 
	 * @param c
	 * @param f
	 * @return
	 */
	public String format(Currency c, float f) {
		return format(c, f, false);
	}
}
