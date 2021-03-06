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

import java.io.Serializable;

public class DataSource {
  private String host;
  private int port;

  public static class ParseError extends Exception {
    public ParseError(String message) {
      super(message);
    }
  }

  public static DataSource fromPath(String path) throws ParseError {
    String[] components = path.split(":");

    if (components.length != 2) {
      throw new ParseError("Couldn't parse '" + path + "' as a host:port path");
    }

    try {
      return new DataSource(components[0], Integer.parseInt(components[1]));
    } catch(NumberFormatException e) {
      throw new ParseError("Couldn't parse port of '" + path + "': " + e.toString());
    }
  }

  DataSource(String h, int p) {
    host = h;
    port = p;
  }

  public String getHost() {
    return host;
  }
  public void setHost(String h) {
    host = h;
  }
  public int getPort() {
    return port;
  }
  public void setPort(int p) {
    port = p;
  }
  public String getPath() {
    return host + ":" + Integer.toString(port);
  }
};