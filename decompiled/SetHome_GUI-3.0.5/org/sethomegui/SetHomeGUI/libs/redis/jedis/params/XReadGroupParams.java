package org.sethomegui.SetHomeGUI.libs.redis.jedis.params;

import java.util.Objects;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.CommandArguments;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.Protocol;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.args.Rawable;

public class XReadGroupParams implements IParams {
   private Integer count = null;
   private Integer block = null;
   private boolean noack = false;

   public static XReadGroupParams xReadGroupParams() {
      return new XReadGroupParams();
   }

   public XReadGroupParams count(int count) {
      this.count = count;
      return this;
   }

   public XReadGroupParams block(int block) {
      this.block = block;
      return this;
   }

   public XReadGroupParams noAck() {
      this.noack = true;
      return this;
   }

   @Override
   public void addParams(CommandArguments args) {
      if (this.count != null) {
         args.add((Rawable)Protocol.Keyword.COUNT).add(this.count);
      }

      if (this.block != null) {
         args.add((Rawable)Protocol.Keyword.BLOCK).add(this.block).blocking();
      }

      if (this.noack) {
         args.add((Rawable)Protocol.Keyword.NOACK);
      }
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         XReadGroupParams that = (XReadGroupParams)o;
         return this.noack == that.noack && Objects.equals(this.count, that.count) && Objects.equals(this.block, that.block);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.count, this.block, this.noack);
   }
}
