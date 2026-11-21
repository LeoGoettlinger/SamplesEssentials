package org.sethomegui.SetHomeGUI.libs.redis.jedis.exceptions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.HostAndPort;

public class JedisBroadcastException extends JedisDataException {
   private static final String BROADCAST_ERROR_MESSAGE = "A failure occurred while broadcasting the command.";
   private final Map<HostAndPort, Object> replies = new HashMap<>();

   public JedisBroadcastException() {
      super("A failure occurred while broadcasting the command.");
   }

   public void addReply(HostAndPort node, Object reply) {
      this.replies.put(node, reply);
   }

   public Map<HostAndPort, Object> getReplies() {
      return Collections.unmodifiableMap(this.replies);
   }
}
