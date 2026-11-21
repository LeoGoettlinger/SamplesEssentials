package org.lushplugins.lushrewards.libraries.mysql.cj.conf.url;

import java.util.Properties;
import org.lushplugins.lushrewards.libraries.mysql.cj.conf.ConnectionUrl;
import org.lushplugins.lushrewards.libraries.mysql.cj.conf.ConnectionUrlParser;

public class SingleConnectionUrl extends ConnectionUrl {
   public SingleConnectionUrl(ConnectionUrlParser connStrParser, Properties info) {
      super(connStrParser, info);
      this.type = ConnectionUrl.Type.SINGLE_CONNECTION;
   }
}
