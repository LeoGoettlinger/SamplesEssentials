package org.sethomegui.SetHomeGUI.libs.redis.jedis.params;

import java.util.Objects;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.CommandArguments;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.Protocol;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.args.Rawable;

public class LPosParams implements IParams {
   private Integer rank;
   private Integer maxlen;

   public static LPosParams lPosParams() {
      return new LPosParams();
   }

   public LPosParams rank(int rank) {
      this.rank = rank;
      return this;
   }

   public LPosParams maxlen(int maxLen) {
      this.maxlen = maxLen;
      return this;
   }

   @Override
   public void addParams(CommandArguments args) {
      if (this.rank != null) {
         args.add((Rawable)Protocol.Keyword.RANK).add(this.rank);
      }

      if (this.maxlen != null) {
         args.add((Rawable)Protocol.Keyword.MAXLEN).add(this.maxlen);
      }
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         LPosParams that = (LPosParams)o;
         return Objects.equals(this.rank, that.rank) && Objects.equals(this.maxlen, that.maxlen);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.rank, this.maxlen);
   }
}
