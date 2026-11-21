package org.sethomegui.SetHomeGUI.libs.redis.jedis.params;

import java.util.Objects;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.CommandArguments;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.Protocol;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.args.Rawable;

public class ZIncrByParams implements IParams {
   private Protocol.Keyword existance;

   public static ZIncrByParams zIncrByParams() {
      return new ZIncrByParams();
   }

   public ZIncrByParams nx() {
      this.existance = Protocol.Keyword.NX;
      return this;
   }

   public ZIncrByParams xx() {
      this.existance = Protocol.Keyword.XX;
      return this;
   }

   @Override
   public void addParams(CommandArguments args) {
      if (this.existance != null) {
         args.add((Rawable)this.existance);
      }

      args.add((Rawable)Protocol.Keyword.INCR);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         ZIncrByParams that = (ZIncrByParams)o;
         return this.existance == that.existance;
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.existance);
   }
}
