package net.axelschumacher.accountoid;

import net.axelschumacher.accountoid.Accountoid.Categories;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
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

	/** Dialogs indexes */
	public static final int DIALOG_CATEGORY_ALREADY_EXISTS = 1;
	public static final int DIALOG_USED_CATEGORY = 2;
	public static final int DIALOG_EMPTY_CATEGORY = 3;
	
	/** Bundle data */
	private final static String BUNDLE_BEING_DELETED_ID = "ID";
	private final static String BUNDLE_ACCOUNTS_COUNT = "COUNT";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.categories);

		setTitle(R.string.category_title);

		model = new Model(this);

		// Inform the list we provide context menus for items
		getListView().setOnCreateContextMenuListener(this);

		Cursor cursor = model.getDataBase().getCategories();
		adapter = new SimpleCursorAdapter(this, R.layout.categories_cols,
				cursor, new String[] { Categories.NAME },
				new int[] { R.id.categories_cols_text1 });

		// Add a choice listener on the list if action is ACTION_PICK
		getListView().setOnItemClickListener(
				new AdapterView.OnItemClickListener() {
					public void onItemClick(AdapterView<?> adapter, View v,
							int pos, long id) {
						Log.d(TAG, "Clicked on item "+id+" at position "+pos);
						if (getIntent().getAction().equals(Intent.ACTION_PICK)) {
							setResult(RESULT_OK, new Intent().putExtra(
									Accountoid.INTENT_ID_NAME, id));
							finish();
						}
					}
				});

		setListAdapter(adapter);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Update view
		adapter.changeCursor(model.getDataBase().getCategories());
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
	protected Dialog onCreateDialog(int id, Bundle b) {
		Log.d(TAG, "onCreateDialog id="+id+" bundle="+b);
		if (id == DIALOG_USED_CATEGORY)
		{
			final long beingDeleted = b.getLong(BUNDLE_BEING_DELETED_ID);
			final int count = b.getInt(BUNDLE_ACCOUNTS_COUNT); 
			AlertDialog.Builder alert = new Builder(this);
			alert.setMessage(count+" "+getString(R.string.delete_category_confirm_message));
			alert.setTitle(R.string.delete_category_confirm_title);
			alert.setPositiveButton(R.string.yes, new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					removeCategory(beingDeleted);
				}
			});
			alert.setCancelable(true);
			alert.setNegativeButton(R.string.no, null);
			return alert.create();
		}
		AlertDialog.Builder alert = new Builder(this);
		alert.setCancelable(true);
		alert.setPositiveButton(R.string.ok, null);
		alert.setTitle(R.string.dialog_error);
		switch (id) {
		case DIALOG_CATEGORY_ALREADY_EXISTS:
			alert.setMessage(R.string.dialog_category_already_exists);
			break;
		case DIALOG_EMPTY_CATEGORY:
			alert.setMessage(R.string.dialog_empty_category);
			break;

		default:
			return null;
		}
		return alert.create();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Log.d(TAG, "onCreateDialog id="+id);
		return null;
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
				.getColumnIndex(Categories.NAME)));

		// Add a menu item to delete the category
		menu.add(0, MENU_ITEM_DELETE, 0, R.string.category_delete);
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
			tryToRemoveCategory(info.id);
			return true;
		}
		}
		return false;
	}

	/**
	 * Try to remove given category
	 * 
	 * @param id
	 */
	private void tryToRemoveCategory(long id) {
		int accountsWithCategory = model.getDataBase().getAccountsWithCategory(id);
		
		if (accountsWithCategory>=1)
		{
			Bundle b = new Bundle(2);
			b.putLong(BUNDLE_BEING_DELETED_ID, id);
			b.putInt(BUNDLE_ACCOUNTS_COUNT, accountsWithCategory);
			// To ensure a updated call to onCreateDialog(int, Bundle)
			removeDialog(DIALOG_USED_CATEGORY);
			showDialog(DIALOG_USED_CATEGORY, b);
			return;
		}
		removeCategory(id);
	}

	/**
	 * Remove the given category and all entries using it
	 * @param id
	 */
	private void removeCategory(long id) {
		model.getDataBase().deleteCategory(id);
		model.getDataBase().deleteAccountUsingCategory(id);
		// Update view
		adapter.changeCursor(model.getDataBase().getCategories());
	}

	/**
	 * Try to add a category
	 */
	public void addCategory(View view) {
		EditText t = (EditText) findViewById(R.id.category_edit);
		String name = t.getEditableText().toString();
		t.setText("");
		// Remove trailing spaces
		name = name.replaceAll("\\s+$", "");
		if (name.isEmpty())
		{
			showDialog(DIALOG_EMPTY_CATEGORY);
			return;
		}
		ContentValues value = new ContentValues();
		value.put(Categories.NAME, name);
		try {
			model.getDataBase().insertCategory(value);
		} catch (SQLException e) {
			// If the category already exists
			Log.d(TAG, "Sorry for the print stack trace, obviously from SQL code");
			showDialog(DIALOG_CATEGORY_ALREADY_EXISTS);
			return;
		}
		// Update view
		adapter.changeCursor(model.getDataBase().getCategories());
	}
}
