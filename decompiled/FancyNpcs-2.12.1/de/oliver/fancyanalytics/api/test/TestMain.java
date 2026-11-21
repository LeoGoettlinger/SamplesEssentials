package de.oliver.fancyanalytics.api.test;

import de.oliver.fancyanalytics.api.FancyAnalyticsAPI;
import de.oliver.fancyanalytics.api.metrics.MetricSupplier;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TestMain {
   public static void main(String[] args) {
      Thread.setDefaultUncaughtExceptionHandler(
         (thread, throwable) -> Logger.getGlobal().log(Level.SEVERE, "Uncaught exception in thread " + thread.getName(), throwable)
      );
      FancyAnalyticsAPI analyticsAPI = new FancyAnalyticsAPI("0d827cf6-8cff-4e2a-b9dc-c358389a0d09", "l_4AqDY2NWQyN2MzZmI0YTQwMWU5es2M");
      analyticsAPI.registerNumberMetric(new MetricSupplier<>("custom_metric", () -> new Random().nextDouble(500.0)));
      analyticsAPI.reportError(new RuntimeException("hello world"));
      analyticsAPI.initialize();
   }
}
