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
import java.util.Vector;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;
import org.json.JSONTokener;

public class MainActivity extends Activity {
  private TextView output;
  private Telemachus telemachus = null;

  private DataSource telemachusAddress = null;

  private SpeechOutput tts = null;
  private TimeWarpHaltOutput timeWarpHalt = null;
  private Vector<StateNotifier> notifiers = new Vector<StateNotifier> ();

  private static final String ADDRESS_PREF="TelemachusAddress";
  private static final String TIME_SCALE_PREF = "StopTimeScaleOnAlert";

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

    telemachus = new Telemachus();
    tts = new SpeechOutput(this);
    timeWarpHalt = new TimeWarpHaltOutput(telemachus);

    SharedPreferences prefs = getPreferences(MODE_PRIVATE);
    if(prefs.contains(ADDRESS_PREF)) {
      try {
	telemachusAddress = DataSource.fromPath(prefs.getString(ADDRESS_PREF, ""));
      } catch(DataSource.ParseError e) {
	Log.e("Nausicaa", "Corrupt prefs: " + prefs.getString(ADDRESS_PREF, "<none>"));
	// ignore; the preference is corrupt and the default will take over.
      }
    }
    if (prefs.contains(TIME_SCALE_PREF)) {
      try {
	timeWarpHalt.setEnabled(prefs.getBoolean(TIME_SCALE_PREF, true));
      } catch (ClassCastException e) {
	Log.e("Nausicaa", "Corrupt prefs: " + TIME_SCALE_PREF + "\n" + e.toString());
	timeWarpHalt.setEnabled(true);
      }
    }
    if (telemachusAddress == null) {
      telemachusAddress = new DataSource("192.168.1.3", 8085);
    }

    initStateNotifiers();
  }

  /**
   * Initializes the state notifiers, which are used when telemetry comes in
   * to determine if we need to send a notification to an output.
   */
  private void initStateNotifiers() {
    Vector<OutputInterface> speak = new Vector<OutputInterface>();
    speak.add(tts);

    Vector<OutputInterface> speakAndKillTimeWarp =
      new Vector<OutputInterface>(speak);
    speakAndKillTimeWarp.add(timeWarpHalt);

    notifiers.add(new StateNotifier(
		    new StateChecks.CircularOrbit(),
		    speak,
		    "Circular orbit achieved."));
    notifiers.add(new StateNotifier(
		    new StateChecks.AtmosphereStrike(),
		    speak,
		    "Warning: orbit unstable; periapsis within atmosphere."));
    notifiers.add(new StateNotifier(
		    new StateChecks.ElectricityCritical(),
		    speakAndKillTimeWarp,
		    "Warning: Electric charge has fallen to under fifty percent."));
  }


  @Override
    public void onDestroy() {
    if (!notifiers.isEmpty()) {
      notifiers.clear();
    }
    if (timeWarpHalt != null) {
      timeWarpHalt = null;
    }
    if (tts != null) {
      tts.stop();
      tts = null;
    }
    if (telemachus != null) {
      telemachus.close();
      telemachus = null;
    }
    super.onDestroy();
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
      if (item.getItemId() == R.id.toggle_time_scale_option) {
	toggleStopTimeScalePreference();
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

  private void toggleStopTimeScalePreference() {
    timeWarpHalt.setEnabled(!timeWarpHalt.getEnabled());
    SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
    editor.putBoolean(TIME_SCALE_PREF, timeWarpHalt.getEnabled());
    editor.commit();
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
    return new DecimalFormat("#,000.##").format(in);
  }

  // Formats the JSON-return data for the text view
  private String displayJson(JSONObject data) {
    String out = "";
    try {
      int paused = data.getInt("p.paused");
      if (paused == 1) {
	return "<<GAME PAUSED>>";
      }
      // Bit o' "realism..." There's no way for ground control to distinguish
      // between no carrier for missing ship and no carrier for missing
      // antenna.
      if (paused == 2 || paused == 3) {
	return "<<NO CARRIER>>";
      }
      String bodyName = data.getString("v.body");
      out += "[" + bodyName + "]";
      if (timeWarpHalt.getEnabled()) {
	out += " [T]";
      }
      out += "\n";
      out += "Altitude: " + formatDouble(data.getDouble("v.altitude")) + "\n";
      out += "Velocity(orbit): " + formatDouble(data.getDouble("v.orbitalVelocity")) + "\n";
      out += "Speed(vert): " + formatDouble(data.getDouble("v.verticalSpeed")) + "\n";
      out += "Apoapsis: ";
      double apoapsis = data.getDouble("o.ApA");
      if (apoapsis < 0) {
	out += "[escaping]\n";
      } else {
	out += formatDouble(data.getDouble("o.ApA")) + "\n";
      }
      double periapsis = data.getDouble("o.PeA");
      out += "Periapsis: " + formatDouble(periapsis) + "\n";
      double electricCharge = data.getDouble("r.resource[ElectricCharge]");
      double electricChargeMax = data.getDouble("r.resourceMax[ElectricCharge]");
      double electricChargePercent = electricCharge / electricChargeMax;
      out += "Electric %: " + formatDouble(electricChargePercent * 100) + "\n";
      double fuel = data.getDouble("r.resource[LiquidFuel]");
      double fuelMax = data.getDouble("r.resourceMax[LiquidFuel]");
      out += "Fuel %: " + formatDouble(fuel / fuelMax * 100);

      return out;
    } catch(Exception e) {
      Log.e("Nausicaa", e.toString());
      return "<<PARSE ERROR>>";
    }
  }

  private void establishConnection() {
    try {
      Log.i("Nausicaa", "Establishing connection to " + telemachusAddress.getPath());
      output.setText("Establishing connection to " + telemachusAddress.getPath());
      URI uri = new URI("ws://" + telemachusAddress.getPath() + "/datalink");
      telemachus.establishConnection(
	uri,
	// message handler
	new OutputInterface() {
	  @Override public void output(String msg) {
	    if (msg.equals("{}")) {
	      setOutput("((Awaiting data...))");
	    } else {
	      try {
		JSONObject telemetry = (JSONObject)(new JSONTokener(msg).nextValue());
		setOutput(displayJson(telemetry));

		for (StateNotifier notifier : notifiers) {
		  notifier.check(telemetry);
		}
	      } catch(Exception e) {
		Log.e("Nausicaa", e.toString());
		setOutput("<<PARSE ERROR>>");
	      }
	    }
	  }
	},
	// close handler
	new OutputInterface() {
	  @Override public void output(String msg) {
	    setOutput(msg);
	  }
	},
	// error handler
	new OutputInterface() {
	  @Override public void output(String msg) {
	    setOutput(msg);
	  }
	});
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
    }
  }
}
