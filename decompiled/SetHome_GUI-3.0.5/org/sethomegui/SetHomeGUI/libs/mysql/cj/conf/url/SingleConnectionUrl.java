package org.sethomegui.SetHomeGUI.libs.mysql.cj.conf.url;

import java.util.Properties;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.conf.ConnectionUrl;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.conf.ConnectionUrlParser;

public class SingleConnectionUrl extends ConnectionUrl {
   public SingleConnectionUrl(ConnectionUrlParser connStrParser, Properties info) {
      super(connStrParser, info);
      this.type = ConnectionUrl.Type.SINGLE_CONNECTION;
   }
}
