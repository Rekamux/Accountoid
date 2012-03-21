package net.axelschumacher.accountoid;

import java.util.Random;

import net.axelschumacher.accountoid.Accountoid.Account;
import android.app.ListActivity;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class BrowseActivity extends ListActivity {
    
	/** Debug TAG */
	private static final String TAG = "BrowseActivity";
    
	/** DB handler */
	private AccountoidDataBase db;
	
	/** List adapter */
	private SimpleCursorAdapter adapter;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        db = new AccountoidDataBase(this);

        // Inform the list we provide context menus for items
        getListView().setOnCreateContextMenuListener(this);
        Cursor cursor = db.getAccountList();
        adapter = new SimpleCursorAdapter(
        		this, R.layout.browse, cursor, 
        		new String[]{Account.AMMOUNT}, 
        		new int[]{android.R.id.list});
        
        // To have a personalized render
        // http://stackoverflow.com/questions/4776936/modifying-simplecursoradapter-data
        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int column) {
                    TextView tv = (TextView) view;
                    float ammount = cursor.getFloat(cursor.getColumnIndex(Account.AMMOUNT));
                    String description = cursor.getString(cursor.getColumnIndex(Account.DESCRIPTION));
                    tv.setText(ammount+" ("+description+")");
                    if (ammount <= 0.0)
                    	tv.setTextColor(Color.RED);
                    else
                    	tv.setTextColor(Color.GREEN);
                    return true;
            }
        });
        setListAdapter(adapter);
    }
    
    public static final int MENU_ITEM_INSERT = 1;
    public static final int MENU_ITEM_DELETE = 2;
    public static final int MENU_ITEM_DELETE_ALL = 3;
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add(0, MENU_ITEM_INSERT, 0, R.string.browse_add);
        menu.add(0, MENU_ITEM_INSERT, 0, R.string.browse_add);

        return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        Log.v(TAG, "onPrepareOptionMenu");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case MENU_ITEM_INSERT:
            // Launch activity to insert a new item
            addAmmount();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    public void addAmmount()
    {
    	Log.d(TAG, "Add ammount");
		ContentValues value = new ContentValues();
		value.put(Account.DESCRIPTION, "BLAH");
		Random r = new Random();
		value.put(Account.AMMOUNT, (r.nextFloat()-0.5)*100.0);
		db.insertAccount(value);
		// Update view
		adapter.changeCursor(db.getAccountList());
    }
    
    public void removeAmmount(long id)
    {
    	Log.d(TAG, "Remove ammount "+id);
        db.deleteAccount(id);
		// Update view
    	// If I used a provider, it would have been done automatically
		adapter.changeCursor(db.getAccountList());
    }
    
    public void removeAllAccounts()
    {
    	Log.d(TAG, "Remove all ammounts");
        db.deleteAllAccounts();
		// Update view
		adapter.changeCursor(db.getAccountList());
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info;
        try {
             info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        } catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfo", e);
            return;
        }

        Cursor cursor = (Cursor) getListAdapter().getItem(info.position);
        if (cursor == null) {
            // For some reason the requested item isn't available, do nothing
            return;
        }

        // Setup the menu header
        menu.setHeaderTitle(cursor.getString(cursor.getColumnIndex(Account.DESCRIPTION)));

        // Add a menu item to delete the note
        menu.add(0, MENU_ITEM_DELETE, 0, R.string.browse_delete);
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
            	removeAmmount(info.id);
                return true;
            }
        }
        return false;
    }
    
    
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	Log.v(TAG, "id "+id+" clicked");
    }
}
