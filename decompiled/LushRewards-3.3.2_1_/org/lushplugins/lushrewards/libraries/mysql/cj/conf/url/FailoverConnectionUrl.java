package org.lushplugins.lushrewards.libraries.mysql.cj.conf.url;

import java.util.Properties;
import org.lushplugins.lushrewards.libraries.mysql.cj.conf.ConnectionUrl;
import org.lushplugins.lushrewards.libraries.mysql.cj.conf.ConnectionUrlParser;

public class FailoverConnectionUrl extends ConnectionUrl {
   public FailoverConnectionUrl(ConnectionUrlParser connStrParser, Properties info) {
      super(connStrParser, info);
      this.type = ConnectionUrl.Type.FAILOVER_CONNECTION;
   }
}
