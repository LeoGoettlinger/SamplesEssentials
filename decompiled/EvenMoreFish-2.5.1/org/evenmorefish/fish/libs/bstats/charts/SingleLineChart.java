package org.evenmorefish.fish.libs.bstats.charts;

import java.util.concurrent.Callable;
import org.evenmorefish.fish.libs.bstats.json.JsonObjectBuilder;

public class SingleLineChart extends CustomChart {
   private final Callable<Integer> callable;

   public SingleLineChart(String chartId, Callable<Integer> callable) {
      super(chartId);
      this.callable = callable;
   }

   @Override
   protected JsonObjectBuilder.JsonObject getChartData() throws Exception {
      int value = this.callable.call();
      return value == 0 ? null : new JsonObjectBuilder().appendField("value", value).build();
   }
}
