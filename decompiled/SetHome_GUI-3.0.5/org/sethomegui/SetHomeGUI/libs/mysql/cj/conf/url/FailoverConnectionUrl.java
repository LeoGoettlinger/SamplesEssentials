package org.sethomegui.SetHomeGUI.libs.mysql.cj.conf.url;

import java.util.Properties;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.conf.ConnectionUrl;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.conf.ConnectionUrlParser;

public class FailoverConnectionUrl extends ConnectionUrl {
   public FailoverConnectionUrl(ConnectionUrlParser connStrParser, Properties info) {
      super(connStrParser, info);
      this.type = ConnectionUrl.Type.FAILOVER_CONNECTION;
   }
}
