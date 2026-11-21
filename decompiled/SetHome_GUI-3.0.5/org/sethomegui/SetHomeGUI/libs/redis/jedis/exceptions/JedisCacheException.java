package org.sethomegui.SetHomeGUI.libs.redis.jedis.exceptions;

public class JedisCacheException extends JedisException {
   private static final long serialVersionUID = 3878126572474819403L;

   public JedisCacheException(String message) {
      super(message);
   }

   public JedisCacheException(Throwable cause) {
      super(cause);
   }

   public JedisCacheException(String message, Throwable cause) {
      super(message, cause);
   }
}
