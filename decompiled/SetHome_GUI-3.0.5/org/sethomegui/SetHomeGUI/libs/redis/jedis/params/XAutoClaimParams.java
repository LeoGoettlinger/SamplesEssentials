package org.sethomegui.SetHomeGUI.libs.redis.jedis.params;

import java.util.Objects;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.CommandArguments;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.Protocol;

public class XAutoClaimParams implements IParams {
   private Integer count;

   public static XAutoClaimParams xAutoClaimParams() {
      return new XAutoClaimParams();
   }

   public XAutoClaimParams count(int count) {
      this.count = count;
      return this;
   }

   @Override
   public void addParams(CommandArguments args) {
      if (this.count != null) {
         args.add(Protocol.Keyword.COUNT.getRaw()).add(this.count);
      }
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         XAutoClaimParams that = (XAutoClaimParams)o;
         return Objects.equals(this.count, that.count);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.count);
   }
}
