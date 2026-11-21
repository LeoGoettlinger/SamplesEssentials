package org.sethomegui.SetHomeGUI.libs.redis.jedis;

import java.net.Socket;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.exceptions.JedisConnectionException;

public interface JedisSocketFactory {
   Socket createSocket() throws JedisConnectionException;
}
