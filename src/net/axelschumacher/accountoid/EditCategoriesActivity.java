package net.axelschumacher.accountoid;

import android.app.ListActivity;
import android.widget.SimpleCursorAdapter;

/**
 * Allow user to edit categories
 */
public class EditCategoriesActivity extends ListActivity {
	/** Debug TAG */
	private static final String TAG = "EditCategoriesActivity";

	/** Model */
	private Model model;

	/** List adapter */
	private SimpleCursorAdapter adapter;

	/** Menu buttons indexes */
	public static final int MENU_ITEM_DELETE = 1;

}
