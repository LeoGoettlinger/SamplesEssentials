package org.sethomegui.SetHomeGUI.libs.redis.jedis.commands;

import org.sethomegui.SetHomeGUI.libs.redis.jedis.args.ClientAttributeOption;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.args.ClientPauseMode;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.args.ClientType;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.args.UnblockType;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.params.ClientKillParams;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.resps.TrackingInfo;

public interface ClientCommands {
   String clientKill(String var1);

   String clientKill(String var1, int var2);

   long clientKill(ClientKillParams var1);

   String clientGetname();

   String clientList();

   String clientList(ClientType var1);

   String clientList(long... var1);

   String clientInfo();

   String clientSetInfo(ClientAttributeOption var1, String var2);

   String clientSetname(String var1);

   long clientId();

   long clientUnblock(long var1);

   long clientUnblock(long var1, UnblockType var3);

   String clientPause(long var1);

   String clientPause(long var1, ClientPauseMode var3);

   String clientUnpause();

   String clientNoEvictOn();

   String clientNoEvictOff();

   String clientNoTouchOn();

   String clientNoTouchOff();

   TrackingInfo clientTrackingInfo();
}
