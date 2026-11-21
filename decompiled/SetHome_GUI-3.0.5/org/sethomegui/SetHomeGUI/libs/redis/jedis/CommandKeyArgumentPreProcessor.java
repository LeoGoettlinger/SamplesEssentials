package org.sethomegui.SetHomeGUI.libs.redis.jedis;

import org.sethomegui.SetHomeGUI.libs.redis.jedis.annots.Experimental;

@Experimental
public interface CommandKeyArgumentPreProcessor {
   Object actualKey(Object var1);
}
