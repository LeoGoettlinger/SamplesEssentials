package org.sethomegui.SetHomeGUI.libs.redis.jedis.search;

import org.sethomegui.SetHomeGUI.libs.redis.jedis.CommandArguments;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.args.Rawable;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.params.IParams;

public class FTProfileParams implements IParams {
   private boolean limited;

   public static FTProfileParams profileParams() {
      return new FTProfileParams();
   }

   public FTProfileParams limited() {
      this.limited = true;
      return this;
   }

   @Override
   public void addParams(CommandArguments args) {
      if (this.limited) {
         args.add((Rawable)SearchProtocol.SearchKeyword.LIMITED);
      }
   }
}
