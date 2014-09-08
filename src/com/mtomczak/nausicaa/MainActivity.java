package com.mtomczak.nausicaa;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import com.google.android.glass.view.WindowUtils;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;
import org.json.JSONTokener;

public class MainActivity extends Activity
  implements TextToSpeech.OnInitListener {
  private TextView output;
  private WebSocketClient telemachus = null;
  private TextToSpeech tts = null;

  private boolean circularOrbit = true;
  private boolean atmosphericDragThreat = true;
  private boolean electricChargeUnderFiftyPercent = false;

  private DataSource telemachusAddress = null;
  private HashMap<String, Double> atmosphericData = null;
  private static final String ADDRESS_PREF="TelemachusAddress";

  private static final double ECCENTRICITY_THRESHOLD = 0.09;

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
    tts = new TextToSpeech(this, this);
    initAtmosphericData();
  }

  private void initAtmosphericData() {
    atmosphericData = new HashMap();
      // Moho has no atmosphere
    atmosphericData.put("Eve", new Double(90000.0));
      // Gilly has no atmosphere
    atmosphericData.put("Kerbin", new Double(70000.0));
      // Mun has no atmosphere
      // Minmus has no atmosphere
    atmosphericData.put("Duna", new Double(50000.0));
      // Ike has no atmosphere
    // Dres has no atmosphere
    atmosphericData.put("Jool", new Double(200000.0));
      atmosphericData.put("Laythe", new Double(50000.0));
      // Vall has no atmosphere
      // Tylo has no atmosphere
      // Bop  has no atmosphere
      // Pol  has no atmosphere
    // Eeloo has no atmosphere
  }

  // init for text to speech engine
  @Override
    public void onInit(int status) {
    if (status == TextToSpeech.SUCCESS) {
      int result = tts.setLanguage(Locale.US);
    }
  }

  @Override
    public void onDestroy() {
    if (tts != null) {
      tts.stop();
      tts.shutdown();
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
    return new DecimalFormat("#,000.##").format(in);
  }

  // Formats the JSON-return data for the text view
  private String displayJson(String raw) {
    String out = "";
    try {
      JSONObject data = (JSONObject)(new JSONTokener(raw).nextValue());
      String bodyName = data.getString("v.body");
      out += "[" + bodyName + "]\n";
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
      if (apoapsis + periapsis != 0) {
	double eccentricity = (apoapsis - periapsis) / (apoapsis + periapsis);
	out += "Eccentricity: " +
	  formatDouble(eccentricity) + "\n";
	boolean circular = eccentricity > 0 && eccentricity < ECCENTRICITY_THRESHOLD;
	if (circular && !circularOrbit) {
	  say("Circular orbit achieved.");
	}
	circularOrbit = circular;
      }
      if (atmosphericData.containsKey(bodyName)) {
	boolean dragThreat = periapsis > 0 && periapsis <
	  (atmosphericData.get(bodyName) * 0.75);
	if (dragThreat && !atmosphericDragThreat) {
	  say("Warning: orbit unstable; periapsis within atmosphere.");
	}
	atmosphericDragThreat = dragThreat;
      }
      double electricCharge = data.getDouble("r.resource[ElectricCharge]");
      double electricChargeMax = data.getDouble("r.resourceMax[ElectricCharge]");
      double electricChargePercent = electricCharge / electricChargeMax;
      boolean electricChargeLow = electricChargePercent < 0.5;
      if (electricChargeLow && !electricChargeUnderFiftyPercent) {
	say("Warning: Electric charge has fallen to under fifty percent.");
	stopTimeWarp();
      }
      electricChargeUnderFiftyPercent = electricChargeLow;
      out += "Electric %: " + formatDouble(electricChargePercent * 100);

      return out;
    } catch(Exception e) {
      Log.e("Nausicaa", e.toString());
      return "<<PARSE ERROR>>";
    }
  }

  private void establishConnection() {
    try {
      if (telemachus != null) {
	telemachus.closeConnection(1000, "Success");
	telemachus = null;
      }
      Log.i("Nausicaa", "Establishing connection to " + telemachusAddress.getPath());
      output.setText("Establishing connection to " + telemachusAddress.getPath());
      URI uri = new URI("ws://" + telemachusAddress.getPath() + "/datalink");
      telemachus = new WebSocketClient(uri) {
	  @Override
	    public void onOpen(ServerHandshake serverHandshake) {
	    setOutput("Connected.");
	    String sendString = "{\"+\":[\"v.body\",\"v.altitude\",\"v.orbitalVelocity\"," +
	      "\"v.verticalSpeed\",\"o.ApA\",\"o.PeA\"," +
	      "\"r.resource[ElectricCharge]\",\"r.resourceMax[ElectricCharge]\"],\"rate\":500}";
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
      telemachus.closeConnection(1000, "Success");
      telemachus = null;
    }
  }

  private void say(String msg) {
    if (tts != null) {
      tts.speak(msg, TextToSpeech.QUEUE_ADD, null);
    }
  }

  /** @brief Requests a halt to timewarp.
   */
  private void stopTimeWarp() {
    if (telemachus != null) {
      telemachus.send("{\"run\":[\"t.timeWarp[0]\"]}");
    }
  }
}
