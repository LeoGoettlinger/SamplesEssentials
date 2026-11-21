package org.sethomegui.SetHomeGUI.libs.redis.jedis.params;

import org.sethomegui.SetHomeGUI.libs.redis.jedis.CommandArguments;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.Protocol;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.args.Rawable;

public class GeoRadiusStoreParam implements IParams {
   private boolean store = false;
   private boolean storeDist = false;
   private String key;

   public static GeoRadiusStoreParam geoRadiusStoreParam() {
      return new GeoRadiusStoreParam();
   }

   public GeoRadiusStoreParam store(String key) {
      if (key != null) {
         this.store = true;
         this.key = key;
      }

      return this;
   }

   public GeoRadiusStoreParam storeDist(String key) {
      if (key != null) {
         this.storeDist = true;
         this.key = key;
      }

      return this;
   }

   @Override
   public void addParams(CommandArguments args) {
      if (this.storeDist) {
         args.add((Rawable)Protocol.Keyword.STOREDIST).key(this.key);
      } else {
         if (!this.store) {
            throw new IllegalArgumentException(this.getClass().getSimpleName() + " must has store or storedist option");
         }

         args.add((Rawable)Protocol.Keyword.STORE).key(this.key);
      }
   }
}
