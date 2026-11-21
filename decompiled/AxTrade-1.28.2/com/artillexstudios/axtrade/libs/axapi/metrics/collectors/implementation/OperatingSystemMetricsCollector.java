package com.artillexstudios.axtrade.libs.axapi.metrics.collectors.implementation;

import com.artillexstudios.axtrade.libs.axapi.metrics.collectors.MetricsCollector;
import com.artillexstudios.axtrade.libs.axapi.utils.logging.LogUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.lang.reflect.Method;

public final class OperatingSystemMetricsCollector implements MetricsCollector {
   private String operatingSystem;

   public OperatingSystemMetricsCollector(Object systemInfo) {
      try {
         Method operatingSystemGetter = Class.forName("oshi.SystemInfo").getDeclaredMethod("getOperatingSystem");
         Object operatingSystem = operatingSystemGetter.invoke(systemInfo);
         Method operatingSystemVersionGetter = Class.forName("oshi.software.os.OperatingSystem").getDeclaredMethod("getVersionInfo");
         this.operatingSystem = operatingSystemVersionGetter.invoke(operatingSystem).toString() + "|" + System.getProperty("os.name");
      } catch (Exception var5) {
         LogUtils.error("Failed to load OperatingSystem type!");
      }
   }

   @Override
   public void collect(JsonArray data) {
      if (this.operatingSystem != null) {
         JsonObject object = new JsonObject();
         object.addProperty("@type", "operating-system");
         object.addProperty("operating-system", this.operatingSystem);
         data.add(object);
      }
   }
}
