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