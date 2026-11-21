package org.sethomegui.SetHomeGUI.libs.redis.jedis.bloom;

import org.sethomegui.SetHomeGUI.libs.redis.jedis.CommandArguments;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.Protocol;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.args.Rawable;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.params.IParams;

public class CFReserveParams implements IParams {
   private Long bucketSize;
   private Integer maxIterations;
   private Integer expansion;

   public static CFReserveParams reserveParams() {
      return new CFReserveParams();
   }

   public CFReserveParams bucketSize(long bucketSize) {
      this.bucketSize = bucketSize;
      return this;
   }

   public CFReserveParams maxIterations(int maxIterations) {
      this.maxIterations = maxIterations;
      return this;
   }

   public CFReserveParams expansion(int expansion) {
      this.expansion = expansion;
      return this;
   }

   @Override
   public void addParams(CommandArguments args) {
      if (this.bucketSize != null) {
         args.add((Rawable)RedisBloomProtocol.RedisBloomKeyword.BUCKETSIZE).add(Protocol.toByteArray(this.bucketSize));
      }

      if (this.maxIterations != null) {
         args.add((Rawable)RedisBloomProtocol.RedisBloomKeyword.MAXITERATIONS).add(Protocol.toByteArray(this.maxIterations));
      }

      if (this.expansion != null) {
         args.add((Rawable)RedisBloomProtocol.RedisBloomKeyword.EXPANSION).add(Protocol.toByteArray(this.expansion));
      }
   }
}
