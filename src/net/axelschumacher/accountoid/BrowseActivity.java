package net.axelschumacher.accountoid;

import net.axelschumacher.accountoid.Accountoid.Account;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class BrowseActivity extends ListActivity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);

        // If no data was given in the intent (because we were started
        // as a MAIN activity), then use our default content provider.
        // MANDATORY for cursor creation
        Intent intent = getIntent();
        if (intent.getData() == null) {
            intent.setData(Account.CONTENT_URI);
        }

        // Inform the list we provide context menus for items
        getListView().setOnCreateContextMenuListener(this);
        Cursor cursor = managedQuery(getIntent().getData(),
        		new String[]{Account._ID, Account.AMMOUNT, Account.DESCRIPTION},
        		null, null, null);
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
        		this, R.layout.browse, cursor, 
        		new String[]{Account.AMMOUNT}, 
        		new int[]{R.id.browse_list});
        
        // To have a personal render
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
    
    public void addAmmount(View v)
    {
    }
    
}
