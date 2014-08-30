package com.mtomczak.nausicaa;

import android.app.Activity;
import android.widget.TextView;
import android.os.Bundle;

public class MainActivity extends Activity
{
  private TextView output;

  /** Address of Telemachus. TODO(mtomczak): Make configurable. **/
  private static final String telemachusAddress =
    "192.168.1.3:8085";

  /** Called when the activity is first created. */
  @Override
    public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    output = (TextView) findViewById(R.id.output);
    output.setText("Establishing connection...");
  }

  @Override
    public void onResume() {
    URI uri = new URI("ws://" + telemachusAddress);
  }
  @Override
    public void onPause() {
  }
}
