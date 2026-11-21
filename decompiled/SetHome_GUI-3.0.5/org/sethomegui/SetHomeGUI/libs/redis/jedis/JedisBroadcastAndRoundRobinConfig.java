package org.sethomegui.SetHomeGUI.libs.redis.jedis;

public interface JedisBroadcastAndRoundRobinConfig {
   JedisBroadcastAndRoundRobinConfig.RediSearchMode getRediSearchModeInCluster();

   public static enum RediSearchMode {
      DEFAULT,
      LIGHT;
   }
}
