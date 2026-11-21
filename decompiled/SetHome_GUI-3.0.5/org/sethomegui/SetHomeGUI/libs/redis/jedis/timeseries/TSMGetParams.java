package org.sethomegui.SetHomeGUI.libs.redis.jedis.timeseries;

import java.util.Arrays;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.CommandArguments;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.args.Rawable;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.params.IParams;

public class TSMGetParams implements IParams {
   private boolean latest;
   private boolean withLabels;
   private String[] selectedLabels;

   public static TSMGetParams multiGetParams() {
      return new TSMGetParams();
   }

   public TSMGetParams latest() {
      this.latest = true;
      return this;
   }

   public TSMGetParams withLabels(boolean withLabels) {
      this.withLabels = withLabels;
      return this;
   }

   public TSMGetParams withLabels() {
      return this.withLabels(true);
   }

   public TSMGetParams selectedLabels(String... labels) {
      this.selectedLabels = labels;
      return this;
   }

   @Override
   public void addParams(CommandArguments args) {
      if (this.latest) {
         args.add((Rawable)TimeSeriesProtocol.TimeSeriesKeyword.LATEST);
      }

      if (this.withLabels) {
         args.add((Rawable)TimeSeriesProtocol.TimeSeriesKeyword.WITHLABELS);
      } else if (this.selectedLabels != null) {
         args.add((Rawable)TimeSeriesProtocol.TimeSeriesKeyword.SELECTED_LABELS);

         for (String label : this.selectedLabels) {
            args.add(label);
         }
      }
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         TSMGetParams that = (TSMGetParams)o;
         return this.latest == that.latest && this.withLabels == that.withLabels && Arrays.equals((Object[])this.selectedLabels, (Object[])that.selectedLabels);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int result = Boolean.hashCode(this.latest);
      result = 31 * result + Boolean.hashCode(this.withLabels);
      return 31 * result + Arrays.hashCode((Object[])this.selectedLabels);
   }
}
