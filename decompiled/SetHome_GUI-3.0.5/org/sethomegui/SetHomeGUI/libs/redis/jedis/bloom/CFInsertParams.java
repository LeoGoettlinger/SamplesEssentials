package org.sethomegui.SetHomeGUI.libs.redis.jedis.bloom;

import org.sethomegui.SetHomeGUI.libs.redis.jedis.CommandArguments;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.Protocol;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.args.Rawable;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.params.IParams;

public class CFInsertParams implements IParams {
   private Long capacity;
   private boolean noCreate = false;

   public static CFInsertParams insertParams() {
      return new CFInsertParams();
   }

   public CFInsertParams capacity(long capacity) {
      this.capacity = capacity;
      return this;
   }

   public CFInsertParams noCreate() {
      this.noCreate = true;
      return this;
   }

   @Override
   public void addParams(CommandArguments args) {
      if (this.capacity != null) {
         args.add((Rawable)RedisBloomProtocol.RedisBloomKeyword.CAPACITY).add(Protocol.toByteArray(this.capacity));
      }

      if (this.noCreate) {
         args.add((Rawable)RedisBloomProtocol.RedisBloomKeyword.NOCREATE);
      }
   }
}
