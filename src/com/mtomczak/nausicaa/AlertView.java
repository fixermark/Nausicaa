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

import android.content.Context;
import android.os.Looper;
import android.view.View;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.json.JSONObject;
import org.json.JSONException;

/**
 * View that shows either its child views or a text view indicating bad
 * telemetry state.
 */
public class AlertView extends LinearLayout implements TelemetryViewer {
  private TextView alertText;
  private View childViews;

  public static final String ALERT_TEXT_TAG = "alertText";
  public static final String CHILD_VIEW_TAG = "childViews";

  public AlertView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
    protected void onFinishInflate() {
    super.onFinishInflate();

    alertText = (TextView)findViewWithTag(ALERT_TEXT_TAG);

    childViews = findViewWithTag(CHILD_VIEW_TAG);
    alertText.setVisibility(View.GONE);
    childViews.setVisibility(View.GONE);
  }

  @Override
    public void update(JSONObject telemetry) {
    try {
      int paused = telemetry.getInt("p.paused");
      if (paused == 1) {
	alert("<<GAME PAUSED>>");
	return;
      }
      // Bit o' "realism..." There's no way for ground control to distinguish
      // between no carrier for missing ship and no carrier for missing
      // antenna.
      if (paused == 2 || paused == 3) {
	alert("<<NO CARRIER>>");
	return;
      }

      // No alerts on this telemetry, so let's hide the alert view.
      setAlertVisible(false);
    } catch (JSONException e) {
      Log.e("Nausicaa", e.toString());
      alert("<<PARSE ERROR>>");
    }
  }

  /**
   * Sets visibility of alert text. Must be called from UI thread.
   * @param visible If true, alert will show and suppress other views.
   */
  private void setAlertVisible(boolean visible) {
      alertText.setVisibility(visible ? View.VISIBLE : View.GONE);
      childViews.setVisibility(visible ? View.GONE : View.VISIBLE);
  }

  /**
   * Sets the error text. Must be run from UI thread.
   * @param msg Error text to display.
   */
  public void alert(String msg) {
    alertText.setText(msg);
    setAlertVisible(true);
  }
}