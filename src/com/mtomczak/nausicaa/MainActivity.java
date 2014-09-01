package com.mtomczak.nausicaa;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import com.google.android.glass.view.WindowUtils;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;
import org.json.JSONTokener;

public class MainActivity extends Activity
{
  private TextView output;
  private WebSocketClient telemachus = null;
  private Timer connectTimer = new Timer();

  private DataSource telemachusAddress = null;
  private static final String ADDRESS_PREF="TelemachusAddress";

  public static final String DATASOURCE_INTENT =
    "com.mtomczak.nausicaa.DATASOURCE";

  /** Called when the activity is first created. */
  @Override
    public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    getWindow().requestFeature(WindowUtils.FEATURE_VOICE_COMMANDS);
    setContentView(R.layout.main);
    output = (TextView) findViewById(R.id.output);
    output.setKeepScreenOn(true);
    SharedPreferences prefs = getPreferences(MODE_PRIVATE);
    if(prefs.contains(ADDRESS_PREF)) {
      try {
	telemachusAddress = DataSource.fromPath(prefs.getString(ADDRESS_PREF, ""));
      } catch(DataSource.ParseError e) {
	Log.e("Nausicaa", "Corrupt prefs: " + prefs.getString(ADDRESS_PREF, "<none>"));
	// ignore; the preference is corrupt and the default will take over.
      }
    }
    if (telemachusAddress == null) {
      telemachusAddress = new DataSource("192.168.1.3", 8085);
    }
  }

  @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
    if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS) {
      getMenuInflater().inflate(R.menu.main, menu);
      return true;
    }
    return super.onCreatePanelMenu(featureId, menu);
  }

  @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
    if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS) {
      if (item.getItemId() == R.id.set_telemetry_source_option) {

	Intent intent = new Intent(getBaseContext(), DataSourceActivity.class);
	intent.putExtra(DATASOURCE_INTENT, telemachusAddress.getPath());
	startActivityForResult(intent, 0);
      }
    }
    return true;
  }

  @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    Log.i("Nausicaa", "Procssing activity result");
    if (resultCode == RESULT_OK) {
      try {
	DataSource ds = DataSource.fromPath(data.getStringExtra(DATASOURCE_INTENT));
	telemachusAddress = ds;
	SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
	editor.putString(ADDRESS_PREF, telemachusAddress.getPath());
	editor.commit();
	establishConnection();
      } catch (DataSource.ParseError e) {
	Log.e("Nausicaa", "Bad data from preferences intent: " + e.toString());
      }
    }
  }

  private void setOutput(final String s) {
    runOnUiThread(new Runnable() {
	@Override
	  public void run() {
	  output.setText(s);
	}
      });
  }

  private String formatDouble(double in) {
    return new DecimalFormat("#.##").format(in);
  }

  // Formats the JSON-return data for the text view
  private String displayJson(String raw) {
    String out = "";
    try {
      JSONObject data = (JSONObject)(new JSONTokener(raw).nextValue());
      out += "Altitude: " + formatDouble(data.getDouble("v.altitude")) + "\n";
      out += "Velocity(orbit): " + formatDouble(data.getDouble("v.orbitalVelocity")) + "\n";
      out += "Speed(vert): " + formatDouble(data.getDouble("v.verticalSpeed")) + "\n";
      out += "Apoapsis: " + formatDouble(data.getDouble("o.ApA")) + "\n";
      out += "Periapsis: " + formatDouble(data.getDouble("o.PeA")) + "\n";
      return out;
    } catch(Exception e) {
      Log.e("Nausicaa", e.toString());
      return "<<PARSE ERROR>>";
    }
  }

  private void establishConnection() {
    try {
      if (telemachus != null) {
	telemachus.close();
	telemachus = null;
      }
      Log.i("Nausicaa", "Establishing connection to " + telemachusAddress.getPath());
      output.setText("Establishing connection to" + telemachusAddress.getPath());
      URI uri = new URI("ws://" + telemachusAddress.getPath() + "/datalink");
      telemachus = new WebSocketClient(uri) {
	  @Override
	    public void onOpen(ServerHandshake serverHandshake) {
	    setOutput("Connected.");
	    String sendString = "{\"+\":[\"v.altitude\",\"v.orbitalVelocity\"," +
	      "\"v.verticalSpeed\",\"o.ApA\",\"o.PeA\"],\"rate\":500}";
	    telemachus.send(sendString);
	  }

	  @Override
	    public void onMessage(String s) {
	    setOutput(displayJson(s));
	  }

	  @Override
	    public void onClose(int i, String s, boolean b) {
	    setOutput("Closed: " + Integer.toString(i) + " " + s);
	  }

	  @Override
	    public void onError(Exception e) {
	    setOutput(e.toString());
	  }
	};
      telemachus.connect();
    } catch (final URISyntaxException e) {
      setOutput("Error:\n" + e.toString());
    }
  }

  @Override
    public void onResume() {
    super.onResume();
    establishConnection();
  }

  @Override
    public void onPause() {
    super.onPause();
    if (telemachus != null) {
      telemachus.close();
      telemachus = null;
    }
  }
}
