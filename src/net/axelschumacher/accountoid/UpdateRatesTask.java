package net.axelschumacher.accountoid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import net.axelschumacher.accountoid.Accountoid.Currencies;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

/**
 * Update rates in an asynchronous way
 */
public class UpdateRatesTask extends AsyncTask<Object, Object, Object> {
	private static final String TAG = "UpdateRatesTask";

	private Context context;

	private Model model;

	public UpdateRatesTask(Context c) {
		context = c;
		model = new Model(c);
	}

	// http://josscrowcroft.github.com/open-exchange-rates/
	private static final String ALL_USD_URL = "https://raw.github.com/currencybot/open-exchange-rates/master/latest.json";

	private JSONObject updateJSON() throws ClientProtocolException, IOException {
		String feed = readFeed();
		JSONObject rates = null;
		try {
			JSONObject values = (JSONObject) new JSONTokener(feed).nextValue();
			rates = values.getJSONObject("rates");
			Model.lastRateUpdate = values.getLong("timestamp");
			Log.d(TAG, "Timestamp: "+Model.lastRateUpdate);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rates;
	}

	private String readFeed() throws ClientProtocolException, IOException {
		StringBuilder builder = new StringBuilder();
		HttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(ALL_USD_URL);
		HttpResponse response = client.execute(httpGet);
		StatusLine statusLine = response.getStatusLine();
		int statusCode = statusLine.getStatusCode();
		if (statusCode == 200) {
			HttpEntity entity = response.getEntity();
			InputStream content = entity.getContent();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					content));
			String line;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
		} else {
			Log.e(TAG, "Failed to download file");
		}

		return builder.toString();
	}

	@Override
	protected void onPostExecute(Object result) {
		boolean success = (Boolean) result;
		if (success) {
			Toast toast = Toast.makeText(context, R.string.rates_update_done,
					Toast.LENGTH_SHORT);
			toast.show();
		} else {
			Toast toast = Toast.makeText(context, R.string.rates_update_failed,
					Toast.LENGTH_LONG);
			toast.show();
		}
	}

	@Override
	protected Object doInBackground(Object... params) {
		try {
			JSONObject result = updateJSON();
			Cursor c = model.getDataBase().getCurrencies();
			c.moveToFirst();
			do {
				String code = c.getString(c.getColumnIndex(Currencies.CODE));
				Log.d(TAG, "Treating code " + code);
				double rate = result.getDouble(code);
				long id = c.getLong(c.getColumnIndex(Currencies._ID));
				Log.d(TAG, "Value of " + code + " in USD: " + rate);
				ContentValues v = new ContentValues();
				v.put(Currencies._ID, id);
				v.put(Currencies.CODE, code);
				v.put(Currencies.VALUE, rate);
				model.getDataBase().updateCurrencyRate(id, v);
				Log.d(TAG, "Updated " + code);
			} while (c.moveToNext());
			return Boolean.TRUE;
		} catch (Exception e) {
			Log.d(TAG, e.getMessage());
			return Boolean.FALSE;
		}
	}
}
