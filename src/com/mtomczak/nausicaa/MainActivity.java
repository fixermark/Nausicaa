package com.mtomczak.nausicaa;

import android.app.Activity;
import android.widget.TextView;
import android.os.Bundle;
import android.util.Log;
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

  /** Address of Telemachus. TODO(mtomczak): Make configurable. **/
  private static final String telemachusAddress =
    "192.168.1.3:8085";

  /** Called when the activity is first created. */
  @Override
    public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    output = (TextView) findViewById(R.id.output);
    output.setKeepScreenOn(true);
    output.setText("Establishing connection...");
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

  @Override
    public void onResume() {
    super.onResume();
    try {
      URI uri = new URI("ws://" + telemachusAddress + "/datalink");
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
    public void onPause() {
    super.onPause();
    if (telemachus != null) {
      telemachus.close();
      telemachus = null;
    }
  }
}
