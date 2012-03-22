package net.axelschumacher.accountoid;

import java.util.Currency;

import net.axelschumacher.accountoid.Accountoid.Currencies;
import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

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

	/** Dialogs indexes */
	public static final int DIALOG_CATEGORY_ALREADY_EXISTS = 1;
	public static final int DIALOG_USED_CATEGORY = 2;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.categories);

		setTitle(R.string.category_title);

		model = new Model(this);

		// Inform the list we provide context menus for items
		getListView().setOnCreateContextMenuListener(this);

		Cursor cursor = model.getDataBase().getCategories();
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

}
