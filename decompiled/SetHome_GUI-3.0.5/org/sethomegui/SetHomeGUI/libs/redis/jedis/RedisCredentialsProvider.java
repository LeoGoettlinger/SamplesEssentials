package org.sethomegui.SetHomeGUI.libs.redis.jedis;

import java.util.function.Supplier;

public interface RedisCredentialsProvider extends Supplier<RedisCredentials> {
   default void prepare() {
   }

   default void cleanUp() {
   }
}
