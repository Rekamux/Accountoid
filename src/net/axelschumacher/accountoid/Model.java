package net.axelschumacher.accountoid;

import java.text.DecimalFormat;

import android.content.Context;

/**
 * Model knowing the real stuffs
 */
public class Model {
	
	public Model(Context context) {
        db = new AccountoidDataBase(context);
        df = new DecimalFormat("#.##"); // TODO localize
	}

	private AccountoidDataBase db;
	
	private DecimalFormat df;
	
	public AccountoidDataBase getDataBase()
	{
		return db;
	}

	public DecimalFormat getDecimalFormat() {
		return df;
	}
}
