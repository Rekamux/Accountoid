package net.axelschumacher.accountoid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;

public class SplashActivity extends Activity implements View.OnTouchListener
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.splash);
        findViewById(R.id.imageView1).setOnTouchListener(this);
    }

	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {
		startActivity(new Intent(this, BrowseActivity.class));
		return true;
	}

}
