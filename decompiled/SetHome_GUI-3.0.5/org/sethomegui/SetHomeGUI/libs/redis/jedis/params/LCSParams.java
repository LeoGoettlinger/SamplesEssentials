package org.sethomegui.SetHomeGUI.libs.redis.jedis.params;

import java.util.Objects;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.CommandArguments;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.Protocol;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.args.Rawable;

public class LCSParams implements IParams {
   private boolean len = false;
   private boolean idx = false;
   private Long minMatchLen;
   private boolean withMatchLen = false;

   public static LCSParams LCSParams() {
      return new LCSParams();
   }

   public LCSParams len() {
      this.len = true;
      return this;
   }

   public LCSParams idx() {
      this.idx = true;
      return this;
   }

   public LCSParams minMatchLen(long minMatchLen) {
      this.minMatchLen = minMatchLen;
      return this;
   }

   public LCSParams withMatchLen() {
      this.withMatchLen = true;
      return this;
   }

   @Override
   public void addParams(CommandArguments args) {
      if (this.len) {
         args.add((Rawable)Protocol.Keyword.LEN);
      }

      if (this.idx) {
         args.add((Rawable)Protocol.Keyword.IDX);
      }

      if (this.minMatchLen != null) {
         args.add((Rawable)Protocol.Keyword.MINMATCHLEN).add(this.minMatchLen);
      }

      if (this.withMatchLen) {
         args.add((Rawable)Protocol.Keyword.WITHMATCHLEN);
      }
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         LCSParams lcsParams = (LCSParams)o;
         return this.len == lcsParams.len
            && this.idx == lcsParams.idx
            && this.withMatchLen == lcsParams.withMatchLen
            && Objects.equals(this.minMatchLen, lcsParams.minMatchLen);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.len, this.idx, this.minMatchLen, this.withMatchLen);
   }
}
