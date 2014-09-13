package com.mtomczak.nausicaa;

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
	    "\"r.resourceMax[ElectricCharge]\",\"r.resourceMax[LiquidFuel]\"]," +
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
      telemachus.send(msg);
    }
  }
}