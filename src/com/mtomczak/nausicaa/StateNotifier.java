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

import java.util.Iterator;
import java.util.List;
import org.json.JSONObject;
import org.json.JSONException;

/**
 * Checks some specific state. If the state is true, signals the outputs with a
 * specified message.
 */
public class StateNotifier {
  private StateCheck statecheck;
  private List<OutputInterface> outputs;
  private String msg;
  private boolean lastCheck = false;

  public StateNotifier(StateCheck sc, List<OutputInterface> o, String  m) {
    statecheck = sc;
    outputs = o;
    msg = m;
  }

  /**
   * Check the state.
   *
   * @param telemetry Telemetry to check. May be null (in which case, no message
   *   sent).
   */
  public void check(JSONObject state) throws JSONException {
    if (state == null) {
      return;
    }
    boolean currentCheck = statecheck.check(state);
    if (currentCheck && !lastCheck) {
      for (OutputInterface o : outputs) {
	o.output(msg);
      }
    }
    lastCheck = currentCheck;
  }
}