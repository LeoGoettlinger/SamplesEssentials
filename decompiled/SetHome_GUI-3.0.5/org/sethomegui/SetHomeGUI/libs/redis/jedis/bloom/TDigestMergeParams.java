package org.sethomegui.SetHomeGUI.libs.redis.jedis.bloom;

import org.sethomegui.SetHomeGUI.libs.redis.jedis.CommandArguments;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.Protocol;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.args.Rawable;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.params.IParams;

public class TDigestMergeParams implements IParams {
   private Integer compression;
   private boolean override = false;

   public static TDigestMergeParams mergeParams() {
      return new TDigestMergeParams();
   }

   public TDigestMergeParams compression(int compression) {
      this.compression = compression;
      return this;
   }

   public TDigestMergeParams override() {
      this.override = true;
      return this;
   }

   @Override
   public void addParams(CommandArguments args) {
      if (this.compression != null) {
         args.add((Rawable)RedisBloomProtocol.RedisBloomKeyword.COMPRESSION).add(Protocol.toByteArray(this.compression));
      }

      if (this.override) {
         args.add((Rawable)RedisBloomProtocol.RedisBloomKeyword.OVERRIDE);
      }
   }
}
