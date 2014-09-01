package com.mtomczak.nausicaa;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import com.google.android.glass.view.WindowUtils;
import java.util.Locale;

public class DataSourceActivity extends Activity
  implements TextToSpeech.OnInitListener {
  private DataSource source;
  private TextView sourceDisplay;
  private TextToSpeech tts = null;
  private boolean ttsInited = false;
  private String queuedSpeechMessages = null;

  private static final int HOST_QUERY = 0;
  private static final int PORT_QUERY = 1;

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.datasource);
    sourceDisplay = (TextView) findViewById(R.id.source);
    sourceDisplay.setKeepScreenOn(true);
    try {
      source = DataSource.fromPath(
	getIntent().getStringExtra(MainActivity.DATASOURCE_INTENT));
    } catch (DataSource.ParseError e) {
      Log.e("Nausicaa", e.toString());
      finish();
    }
    tts = new TextToSpeech(this, this);
    updateSource(source.getPath());
  }

  @Override
    public void onDestroy() {
    if (tts != null) {
      tts.stop();
      tts.shutdown();
    }
    super.onDestroy();
  }

  // init for text to speech engine
  @Override
    public void onInit(int status) {
    if (status == TextToSpeech.SUCCESS) {
      int result = tts.setLanguage(Locale.US);
    }
    ttsInited = true;
    if (queuedSpeechMessages != null) {
      tts.speak(queuedSpeechMessages, TextToSpeech.QUEUE_FLUSH, null);
      queuedSpeechMessages = null;
    }
  }

  @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.datasource, menu);
    return true;
  }

  @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.source_change_hostname_option) {
      readHostname();
    } else if (item.getItemId() == R.id.source_change_port_option) {
      readPort();
    }

    return true;
  }

  @Override
    public boolean onKeyDown(int keycode, KeyEvent event) {

    if (keycode == KeyEvent.KEYCODE_DPAD_CENTER) {
      openOptionsMenu();
      return true;
    }
    return super.onKeyDown(keycode, event);
  }

  @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (resultCode == RESULT_OK) {
      String result = data.getStringArrayListExtra(
	RecognizerIntent.EXTRA_RESULTS).get(0);
      switch(requestCode) {
      case HOST_QUERY:
        // TODO(mtomczak): Should only do these replacements when we have
        // confidence that we're looking at an IP address and not a domain name
	result = result.replaceAll("\\s+", "");
	result = result.replaceAll("One", "1");
	result = result.replaceAll("Two", "2");
	result = result.replaceAll("Three", "3");
	result = result.replaceAll("Four", "4");
	result = result.replaceAll("Five", "5");
	result = result.replaceAll("Six", "6");
	result = result.replaceAll("Seven", "7");
	result = result.replaceAll("Eight", "8");
	result = result.replaceAll("Nine", "9");
	result = result.replaceAll("Ten", " 0");
	source.setHost(result);
	break;
      case PORT_QUERY:
	try {
	  int port = Integer.parseInt(result);
	  source.setPort(port);
	} catch (NumberFormatException e) {
	  say("Unable to interpret as port number: " + result);
	}
	break;
      }
      updateSource(source.getPath());
    }
  }

  private void updateSource(final String text) {
    Log.i("Nausicaa", "Putting " + source.getPath() + " to " +
	  MainActivity.DATASOURCE_INTENT);
    getIntent().putExtra(MainActivity.DATASOURCE_INTENT, source.getPath());
    setResult(RESULT_OK, getIntent());
    runOnUiThread(new Runnable() {
	@Override
	  public void run() {
	    sourceDisplay.setText(text);
	}
      });
  }

  private void startSpeechListener(int data) {
    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
		    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
    startActivityForResult(intent, data);
  }

  private void readHostname() {
    startSpeechListener(HOST_QUERY);
  }

  private void readPort() {
    startSpeechListener(PORT_QUERY);
  }

  private void say(String msg) {
    if (ttsInited) {
      tts.speak(msg, TextToSpeech.QUEUE_FLUSH, null);
    } else {
      if (queuedSpeechMessages != null) {
	queuedSpeechMessages += "\n" + msg;
      } else {
	queuedSpeechMessages = msg;
      }
    }
  }
}