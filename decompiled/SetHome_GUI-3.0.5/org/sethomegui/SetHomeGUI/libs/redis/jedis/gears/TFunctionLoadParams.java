package org.sethomegui.SetHomeGUI.libs.redis.jedis.gears;

import org.sethomegui.SetHomeGUI.libs.redis.jedis.CommandArguments;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.args.Rawable;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.params.IParams;

@Deprecated
public class TFunctionLoadParams implements IParams {
   private boolean replace = false;
   private String config;

   public static TFunctionLoadParams loadParams() {
      return new TFunctionLoadParams();
   }

   @Override
   public void addParams(CommandArguments args) {
      if (this.replace) {
         args.add((Rawable)RedisGearsProtocol.GearsKeyword.REPLACE);
      }

      if (this.config != null && !this.config.isEmpty()) {
         args.add((Rawable)RedisGearsProtocol.GearsKeyword.CONFIG).add(this.config);
      }
   }

   public TFunctionLoadParams replace() {
      this.replace = true;
      return this;
   }

   public TFunctionLoadParams config(String config) {
      this.config = config;
      return this;
   }
}
