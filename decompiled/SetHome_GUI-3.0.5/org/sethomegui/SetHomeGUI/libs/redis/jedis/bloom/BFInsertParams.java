package org.sethomegui.SetHomeGUI.libs.redis.jedis.bloom;

import org.sethomegui.SetHomeGUI.libs.redis.jedis.CommandArguments;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.Protocol;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.args.Rawable;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.params.IParams;

public class BFInsertParams implements IParams {
   private Long capacity;
   private Double errorRate;
   private Integer expansion;
   private boolean noCreate = false;
   private boolean nonScaling = false;

   public static BFInsertParams insertParams() {
      return new BFInsertParams();
   }

   public BFInsertParams capacity(long capacity) {
      this.capacity = capacity;
      return this;
   }

   public BFInsertParams error(double errorRate) {
      this.errorRate = errorRate;
      return this;
   }

   public BFInsertParams expansion(int expansion) {
      this.expansion = expansion;
      return this;
   }

   public BFInsertParams noCreate() {
      this.noCreate = true;
      return this;
   }

   public BFInsertParams nonScaling() {
      this.nonScaling = true;
      return this;
   }

   @Override
   public void addParams(CommandArguments args) {
      if (this.capacity != null) {
         args.add((Rawable)RedisBloomProtocol.RedisBloomKeyword.CAPACITY).add(Protocol.toByteArray(this.capacity));
      }

      if (this.errorRate != null) {
         args.add((Rawable)RedisBloomProtocol.RedisBloomKeyword.ERROR).add(Protocol.toByteArray(this.errorRate));
      }

      if (this.expansion != null) {
         args.add((Rawable)RedisBloomProtocol.RedisBloomKeyword.EXPANSION).add(Protocol.toByteArray(this.expansion));
      }

      if (this.noCreate) {
         args.add((Rawable)RedisBloomProtocol.RedisBloomKeyword.NOCREATE);
      }

      if (this.nonScaling) {
         args.add((Rawable)RedisBloomProtocol.RedisBloomKeyword.NONSCALING);
      }
   }
}
