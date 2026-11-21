package org.sethomegui.SetHomeGUI.libs.redis.jedis.json;

import org.sethomegui.SetHomeGUI.libs.redis.jedis.CommandArguments;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.params.IParams;

public class JsonSetParams implements IParams {
   private boolean nx = false;
   private boolean xx = false;

   public static JsonSetParams jsonSetParams() {
      return new JsonSetParams();
   }

   public JsonSetParams nx() {
      this.nx = true;
      this.xx = false;
      return this;
   }

   public JsonSetParams xx() {
      this.nx = false;
      this.xx = true;
      return this;
   }

   @Override
   public void addParams(CommandArguments args) {
      if (this.nx) {
         args.add("NX");
      }

      if (this.xx) {
         args.add("XX");
      }
   }
}
