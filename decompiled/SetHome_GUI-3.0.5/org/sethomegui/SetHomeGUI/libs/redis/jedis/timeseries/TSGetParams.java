package org.sethomegui.SetHomeGUI.libs.redis.jedis.timeseries;

import org.sethomegui.SetHomeGUI.libs.redis.jedis.CommandArguments;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.args.Rawable;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.params.IParams;

public class TSGetParams implements IParams {
   private boolean latest;

   public static TSGetParams getParams() {
      return new TSGetParams();
   }

   public TSGetParams latest() {
      this.latest = true;
      return this;
   }

   @Override
   public void addParams(CommandArguments args) {
      if (this.latest) {
         args.add((Rawable)TimeSeriesProtocol.TimeSeriesKeyword.LATEST);
      }
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         TSGetParams that = (TSGetParams)o;
         return this.latest == that.latest;
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Boolean.hashCode(this.latest);
   }
}
