package org.sethomegui.SetHomeGUI.libs.redis.jedis.params;

import java.util.Objects;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.CommandArguments;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.Protocol;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.args.Rawable;

public class GeoAddParams implements IParams {
   private boolean nx = false;
   private boolean xx = false;
   private boolean ch = false;

   public static GeoAddParams geoAddParams() {
      return new GeoAddParams();
   }

   public GeoAddParams nx() {
      this.nx = true;
      return this;
   }

   public GeoAddParams xx() {
      this.xx = true;
      return this;
   }

   public GeoAddParams ch() {
      this.ch = true;
      return this;
   }

   @Override
   public void addParams(CommandArguments args) {
      if (this.nx) {
         args.add((Rawable)Protocol.Keyword.NX);
      } else if (this.xx) {
         args.add((Rawable)Protocol.Keyword.XX);
      }

      if (this.ch) {
         args.add((Rawable)Protocol.Keyword.CH);
      }
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         GeoAddParams that = (GeoAddParams)o;
         return this.nx == that.nx && this.xx == that.xx && this.ch == that.ch;
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.nx, this.xx, this.ch);
   }
}
