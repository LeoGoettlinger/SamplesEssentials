package org.sethomegui.SetHomeGUI.libs.redis.jedis;

import java.time.Duration;
import org.sethomegui.SetHomeGUI.libs.pool2.impl.GenericObjectPoolConfig;

public class ConnectionPoolConfig extends GenericObjectPoolConfig<Connection> {
   public ConnectionPoolConfig() {
      this.setTestWhileIdle(true);
      this.setMinEvictableIdleTime(Duration.ofMillis(60000L));
      this.setTimeBetweenEvictionRuns(Duration.ofMillis(30000L));
      this.setNumTestsPerEvictionRun(-1);
   }
}
