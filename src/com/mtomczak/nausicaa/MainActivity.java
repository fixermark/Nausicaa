/*
Copyright 2014 Mark T. Tomczak

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package com.mtomczak.nausicaa;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import com.google.android.glass.view.WindowUtils;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class MainActivity extends Activity {
  private AlertView alertView;
  private StatusView statusView;
  private DockingView dockingView;

  public enum NausicaaSubview {
    STATUS,
    DOCKING
  }

  private NausicaaSubview currentSubview = NausicaaSubview.STATUS;

  private Vector<TelemetryViewer> telemetryViewers = new Vector<TelemetryViewer>();
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

    // Wire up the views
    alertView = (AlertView) findViewById(R.id.alertview);
    statusView = (StatusView) findViewById(R.id.statusview);
    dockingView = (DockingView) findViewById(R.id.dockingview);
    statusView.setAlertOutput(alertView);
    dockingView.setAlertOutput(alertView);
    telemetryViewers.add(alertView);
    telemetryViewers.add(statusView);
    telemetryViewers.add(dockingView);

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
      telemachusAddress = new DataSource("192.168.1.4", 8085);
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
    public boolean onPreparePanel(int featureId, View view, Menu menu) {

    if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS) {
      MenuItem status = menu.findItem(R.id.status_view_option);
      status.setVisible(currentSubview != NausicaaSubview.STATUS);
      status.setEnabled(currentSubview != NausicaaSubview.STATUS);

      MenuItem docking = menu.findItem(R.id.docking_view_option);
      docking.setVisible(currentSubview != NausicaaSubview.DOCKING);
      docking.setEnabled(currentSubview != NausicaaSubview.DOCKING);

      return true;
    }
    return super.onPreparePanel(featureId, view, menu);
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
      if (item.getItemId() == R.id.docking_view_option) {
    	showSubview(NausicaaSubview.DOCKING);
      }
      if (item.getItemId() == R.id.status_view_option) {
    	showSubview(NausicaaSubview.STATUS);
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

  /**
   * Shows an alert in the UI.
   */
  private void alert(final String s) {
    runOnUiThread(new Runnable() {
    	@Override
    	  public void run() {
	  alertView.alert(s);
      	}
      });
  }

  private void establishConnection() {
    try {
      Log.i("Nausicaa", "Establishing connection to " + telemachusAddress.getPath());
      alert("Establishing connection to " + telemachusAddress.getPath());
      URI uri = new URI("ws://" + telemachusAddress.getPath() + "/datalink");
      telemachus.establishConnection(
	uri,
	// message handler
	new OutputInterface() {
	  @Override public void output(String msg) {
	    if (msg.equals("{}")) {
	      alert("((Awaiting data...))");
	    } else {
	      try {
		final JSONObject telemetry =
		  (JSONObject)(new JSONTokener(msg).nextValue());
		// Doctor telemetry with state values of the app itself
		telemetry.put(StatusView.TIME_WARP_KEY, timeWarpHalt.getEnabled());

		runOnUiThread(new Runnable() {
		    @Override
		      public void run() {
		      for (TelemetryViewer viewer : telemetryViewers) {
			viewer.update(telemetry);
		      }
		      try {
			for (StateNotifier notifier : notifiers) {
			  notifier.check(telemetry);
			}
		      } catch(JSONException e) {
			Log.e("Nausicaa", e.toString());
			String trace = "";
			for (StackTraceElement el: e.getStackTrace()) {
			  trace += el.toString();
			}
			Log.e("Nausicaa", trace);
			alert("<<PARSE ERROR>>");
		      }
		    }
		  });

	      } catch(JSONException e) {
		Log.e("Nausicaa", e.toString());
		String trace = "";
		for (StackTraceElement el: e.getStackTrace()) {
		  trace += el.toString();
		}
		Log.e("Nausicaa", trace);
		alert("<<PARSE ERROR>>");
	      }
	    }
	  }
	},
	// close handler
	new OutputInterface() {
	  @Override public void output(String msg) {
	    alert(msg);
	  }
	},
	// error handler
	new OutputInterface() {
	  @Override public void output(String msg) {
	    alert(msg);
	  }
	});
    } catch (final URISyntaxException e) {
      alert("Error:\n" + e.toString());
    }
  }

  /**
   * Selects which subview is currently shown
   */
  public void showSubview(NausicaaSubview view) {
    statusView.setVisibility(view == NausicaaSubview.STATUS ?
			     View.VISIBLE : View.GONE);
    dockingView.setVisibility(view == NausicaaSubview.DOCKING ?
			      View.VISIBLE : View.GONE);
    currentSubview = view;
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
