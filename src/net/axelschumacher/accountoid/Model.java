package net.axelschumacher.accountoid;

import java.text.DecimalFormat;
import java.util.Currency;

import android.content.Context;

/**
 * Model knowing the real stuffs
 */
public class Model {
	@SuppressWarnings("unused")
	private static final String TAG = "Model"; 
	
	public static long lastRateUpdate;

	/**
	 * Handle Currency decimal format
	 */
	@SuppressWarnings("serial")
	private class LocalDecimalFormat extends DecimalFormat {
		@SuppressWarnings("unused")
		private static final String TAG = "LocalDecimalFormat"; 
		private int lastFraction = -1;

		public LocalDecimalFormat(String string) {
			super(string);
		}

		public final LocalDecimalFormat setFraction(int f) {
			if (f == lastFraction)
				return this;
			String format = "#";
			if (f > 0)
				format = format.concat(".");
			for (int i = 0; i < f; i++)
				format = format.concat("#");
			lastFraction = f;
			return this;
		}

	}

	public Model(Context context) {
		db = new AccountoidDataBase(context);
		df = new LocalDecimalFormat("#.##");
	}

	private AccountoidDataBase db;

	private LocalDecimalFormat df;

	public AccountoidDataBase getDataBase() {
		return db;
	}

	public DecimalFormat getDecimalFormat(Currency c) {
		if (c != null) {
			int fraction = c.getDefaultFractionDigits();
			df.setFraction(fraction);
		}
		return df;
	}
}
