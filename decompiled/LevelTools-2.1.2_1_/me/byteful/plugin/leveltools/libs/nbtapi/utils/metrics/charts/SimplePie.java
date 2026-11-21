package me.byteful.plugin.leveltools.libs.nbtapi.utils.metrics.charts;

import java.util.concurrent.Callable;
import me.byteful.plugin.leveltools.libs.nbtapi.utils.metrics.json.JsonObjectBuilder;

public class SimplePie extends CustomChart {
   private final Callable<String> callable;

   public SimplePie(String chartId, Callable<String> callable) {
      super(chartId);
      this.callable = callable;
   }

   @Override
   protected JsonObjectBuilder.JsonObject getChartData() throws Exception {
      String value = this.callable.call();
      return value != null && !value.isEmpty() ? new JsonObjectBuilder().appendField("value", value).build() : null;
   }
}
