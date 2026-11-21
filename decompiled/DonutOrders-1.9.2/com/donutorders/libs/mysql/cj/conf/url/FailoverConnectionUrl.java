package com.donutorders.libs.mysql.cj.conf.url;

import com.donutorders.libs.mysql.cj.conf.ConnectionUrl;
import com.donutorders.libs.mysql.cj.conf.ConnectionUrlParser;
import java.util.Properties;

public class FailoverConnectionUrl extends ConnectionUrl {
   public FailoverConnectionUrl(ConnectionUrlParser connStrParser, Properties info) {
      super(connStrParser, info);
      this.type = ConnectionUrl.Type.FAILOVER_CONNECTION;
   }
}
