package org.sethomegui.SetHomeGUI.libs.redis.jedis.params;

import java.util.Arrays;
import java.util.Objects;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.CommandArguments;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.Protocol;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.args.Rawable;

public class LolwutParams implements IParams {
   private Integer version;
   private String[] opargs;

   public LolwutParams version(int version) {
      this.version = version;
      return this;
   }

   @Deprecated
   public LolwutParams args(String... args) {
      return this.optionalArguments(args);
   }

   public LolwutParams optionalArguments(String... args) {
      this.opargs = args;
      return this;
   }

   @Override
   public void addParams(CommandArguments args) {
      if (this.version != null) {
         args.add((Rawable)Protocol.Keyword.VERSION).add(this.version);
         if (this.opargs != null && this.opargs.length > 0) {
            args.addObjects(this.opargs);
         }
      }
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         LolwutParams that = (LolwutParams)o;
         return Objects.equals(this.version, that.version) && Arrays.equals((Object[])this.opargs, (Object[])that.opargs);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int result = Objects.hash(this.version);
      return 31 * result + Arrays.hashCode((Object[])this.opargs);
   }
}
