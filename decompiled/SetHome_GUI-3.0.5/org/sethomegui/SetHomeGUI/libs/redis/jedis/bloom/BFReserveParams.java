package org.sethomegui.SetHomeGUI.libs.redis.jedis.bloom;

import org.sethomegui.SetHomeGUI.libs.redis.jedis.CommandArguments;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.Protocol;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.args.Rawable;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.params.IParams;

public class BFReserveParams implements IParams {
   private Integer expansion;
   private boolean nonScaling = false;

   public static BFReserveParams reserveParams() {
      return new BFReserveParams();
   }

   public BFReserveParams expansion(int expansion) {
      this.expansion = expansion;
      return this;
   }

   public BFReserveParams nonScaling() {
      this.nonScaling = true;
      return this;
   }

   @Override
   public void addParams(CommandArguments args) {
      if (this.expansion != null) {
         args.add((Rawable)RedisBloomProtocol.RedisBloomKeyword.EXPANSION).add(Protocol.toByteArray(this.expansion));
      }

      if (this.nonScaling) {
         args.add((Rawable)RedisBloomProtocol.RedisBloomKeyword.NONSCALING);
      }
   }
}
