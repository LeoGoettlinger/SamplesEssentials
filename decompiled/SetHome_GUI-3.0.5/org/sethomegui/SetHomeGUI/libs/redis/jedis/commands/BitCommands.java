package org.sethomegui.SetHomeGUI.libs.redis.jedis.commands;

import java.util.List;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.args.BitCountOption;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.args.BitOP;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.params.BitPosParams;

public interface BitCommands {
   boolean setbit(String var1, long var2, boolean var4);

   boolean getbit(String var1, long var2);

   long bitcount(String var1);

   long bitcount(String var1, long var2, long var4);

   long bitcount(String var1, long var2, long var4, BitCountOption var6);

   long bitpos(String var1, boolean var2);

   long bitpos(String var1, boolean var2, BitPosParams var3);

   List<Long> bitfield(String var1, String... var2);

   List<Long> bitfieldReadonly(String var1, String... var2);

   long bitop(BitOP var1, String var2, String... var3);
}
