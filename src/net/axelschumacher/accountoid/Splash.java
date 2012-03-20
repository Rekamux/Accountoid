package net.axelschumacher.accountoid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;;

public class Splash extends Activity implements View.OnTouchListener, View.OnClickListener
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        findViewById(R.id.imageView1).setOnClickListener(this);
    }

	@Override
	public void onClick(View arg0) {
		startActivity(new Intent(getApplicationContext(), AccountoidActivity.class));
	}

	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {
		// TODO Auto-generated method stub
		return false;
	}

}
