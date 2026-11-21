package org.sethomegui.SetHomeGUI.libs.redis.jedis.params;

import java.util.Objects;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.CommandArguments;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.Protocol;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.args.Rawable;

public class ZAddParams implements IParams {
   private Protocol.Keyword existence;
   private Protocol.Keyword comparison;
   private boolean change;

   public static ZAddParams zAddParams() {
      return new ZAddParams();
   }

   public ZAddParams nx() {
      this.existence = Protocol.Keyword.NX;
      return this;
   }

   public ZAddParams xx() {
      this.existence = Protocol.Keyword.XX;
      return this;
   }

   public ZAddParams gt() {
      this.comparison = Protocol.Keyword.GT;
      return this;
   }

   public ZAddParams lt() {
      this.comparison = Protocol.Keyword.LT;
      return this;
   }

   public ZAddParams ch() {
      this.change = true;
      return this;
   }

   @Override
   public void addParams(CommandArguments args) {
      if (this.existence != null) {
         args.add((Rawable)this.existence);
      }

      if (this.comparison != null) {
         args.add((Rawable)this.comparison);
      }

      if (this.change) {
         args.add((Rawable)Protocol.Keyword.CH);
      }
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         ZAddParams that = (ZAddParams)o;
         return this.change == that.change && this.existence == that.existence && this.comparison == that.comparison;
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.existence, this.comparison, this.change);
   }
}
