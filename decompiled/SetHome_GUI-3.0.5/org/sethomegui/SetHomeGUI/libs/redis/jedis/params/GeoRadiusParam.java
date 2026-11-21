package org.sethomegui.SetHomeGUI.libs.redis.jedis.params;

import org.sethomegui.SetHomeGUI.libs.redis.jedis.CommandArguments;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.Protocol;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.args.Rawable;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.args.SortingOrder;

public class GeoRadiusParam implements IParams {
   private boolean withCoord = false;
   private boolean withDist = false;
   private boolean withHash = false;
   private Integer count = null;
   private boolean any = false;
   private SortingOrder sortingOrder = null;

   public static GeoRadiusParam geoRadiusParam() {
      return new GeoRadiusParam();
   }

   public GeoRadiusParam withCoord() {
      this.withCoord = true;
      return this;
   }

   public GeoRadiusParam withDist() {
      this.withDist = true;
      return this;
   }

   public GeoRadiusParam withHash() {
      this.withHash = true;
      return this;
   }

   public GeoRadiusParam sortAscending() {
      return this.sortingOrder(SortingOrder.ASC);
   }

   public GeoRadiusParam sortDescending() {
      return this.sortingOrder(SortingOrder.DESC);
   }

   public GeoRadiusParam sortingOrder(SortingOrder order) {
      this.sortingOrder = order;
      return this;
   }

   public GeoRadiusParam count(int count) {
      this.count = count;
      return this;
   }

   public GeoRadiusParam count(int count, boolean any) {
      this.count = count;
      this.any = any;
      return this;
   }

   public GeoRadiusParam any() {
      if (this.count == null) {
         throw new IllegalArgumentException("COUNT must be set before ANY to be set");
      } else {
         this.any = true;
         return this;
      }
   }

   @Override
   public void addParams(CommandArguments args) {
      if (this.withCoord) {
         args.add((Rawable)Protocol.Keyword.WITHCOORD);
      }

      if (this.withDist) {
         args.add((Rawable)Protocol.Keyword.WITHDIST);
      }

      if (this.withHash) {
         args.add((Rawable)Protocol.Keyword.WITHHASH);
      }

      if (this.count != null) {
         args.add((Rawable)Protocol.Keyword.COUNT).add(this.count);
         if (this.any) {
            args.add((Rawable)Protocol.Keyword.ANY);
         }
      }

      if (this.sortingOrder != null) {
         args.add((Rawable)this.sortingOrder);
      }
   }
}
