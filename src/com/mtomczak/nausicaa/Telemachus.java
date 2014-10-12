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

import android.util.Log;

import java.net.URI;
import java.net.URISyntaxException;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

/**
 * Transmission channel for input / output to Telemachus
 */
public class Telemachus implements OutputInterface {
  private WebSocketClient telemachus = null;

  public void establishConnection(
    URI uri,
    final OutputInterface messageHandler,
    final OutputInterface closeHandler,
    final OutputInterface errorHandler) throws URISyntaxException {
    if (telemachus != null) {
      telemachus.closeConnection(1000, "Success");
      telemachus = null;
    }
    telemachus = new WebSocketClient(uri) {
	@Override
	  public void onOpen(ServerHandshake serverHandshake) {
	  String sendString = "{\"+\":[\"p.paused\",\"v.body\",\"v.altitude\"," +
	    "\"v.orbitalVelocity\"," +
	    "\"v.verticalSpeed\",\"o.ApA\",\"o.PeA\"," +
	    "\"r.resource[ElectricCharge]\",\"r.resource[LiquidFuel]\"," +
	    "\"r.resourceMax[ElectricCharge]\"," +
	    "\"r.resourceMax[LiquidFuel]\"," +
	    "\"r.resource[MonoPropellant]\"," +
	    "\"r.resourceMax[MonoPropellant]\"," +
	    "\"dock.ax\",\"dock.ay\",\"tar.distance\"," +
	    "\"dock.x\",\"dock.y\"]," +
	    "\"rate\":500}";
	  telemachus.send(sendString);
	}

	@Override
	  public void onMessage(String s) {
	  messageHandler.output(s);
	}

	@Override
	  public void onClose(int i, String s, boolean b) {
	  closeHandler.output("Closed: " + Integer.toString(i) + " " + s);
	}

	@Override
	  public void onError(Exception e) {
	  errorHandler.output(e.toString());
	}
      };
    telemachus.connect();
  }

  public void close() {
    if (telemachus != null) {
      telemachus.closeConnection(1000, "Success");
      telemachus = null;
    }
  }

  @Override
    public void output(String msg) {
    if (telemachus != null) {
      Log.i("Nausicaa", "Sending " + msg);
      telemachus.send(msg);
    }
  }
}