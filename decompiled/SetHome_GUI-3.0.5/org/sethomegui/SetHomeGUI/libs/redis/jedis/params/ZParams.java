package org.sethomegui.SetHomeGUI.libs.redis.jedis.params;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.CommandArguments;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.Protocol;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.args.Rawable;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.util.SafeEncoder;

public class ZParams implements IParams {
   private final List<Object> params = new ArrayList<>();

   public ZParams weights(double... weights) {
      this.params.add(Protocol.Keyword.WEIGHTS);

      for (double weight : weights) {
         this.params.add(weight);
      }

      return this;
   }

   public ZParams aggregate(ZParams.Aggregate aggregate) {
      this.params.add(Protocol.Keyword.AGGREGATE);
      this.params.add(aggregate);
      return this;
   }

   @Override
   public void addParams(CommandArguments args) {
      args.addObjects(this.params);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         ZParams zParams = (ZParams)o;
         return Objects.equals(this.params, zParams.params);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.params);
   }

   public static enum Aggregate implements Rawable {
      SUM,
      MIN,
      MAX;

      private final byte[] raw = SafeEncoder.encode(this.name());

      @Override
      public byte[] getRaw() {
         return this.raw;
      }
   }
}
