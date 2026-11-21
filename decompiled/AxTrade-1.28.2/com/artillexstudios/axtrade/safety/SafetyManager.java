package com.artillexstudios.axtrade.safety;

import com.artillexstudios.axtrade.AxTrade;
import com.artillexstudios.axtrade.libs.axapi.executor.ExceptionReportingScheduledThreadPool;
import com.artillexstudios.axtrade.libs.axapi.utils.ExceptionUtils;
import com.artillexstudios.axtrade.libs.axapi.utils.Version;
import com.artillexstudios.axtrade.libs.axapi.utils.http.Requests;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.bukkit.plugin.java.JavaPlugin;

public enum SafetyManager {
   TRADING,
   CURRENCY_SELECTOR,
   SHULKER_VIEWER;

   private static final JavaPlugin instance = AxTrade.getInstance();
   private static final Gson gson = new GsonBuilder().create();
   private static ScheduledExecutorService service = null;
   private static Notifier notifier = null;
   private static boolean safe = true;
   private boolean enabled = true;

   public void set(boolean enabled) {
      this.enabled = enabled;
   }

   public boolean get() {
      return this.enabled;
   }

   public static boolean isSafe() {
      return safe;
   }

   private static void check() {
      String str = "https://api.artillex-studios.com/safety/?plugin=%s&version=%s&mc=%s"
         .formatted(instance.getName(), instance.getDescription().getVersion(), Version.getProtocolVersion());

      JsonArray disabled;
      try {
         String body = Requests.get(str, Map.of()).body();
         disabled = (JsonArray)gson.fromJson(body, JsonArray.class);
      } catch (Exception var6) {
         return;
      }

      if (disabled != null) {
         for (SafetyManager value : values()) {
            value.set(true);
         }

         safe = true;

         for (JsonElement jsonElement : disabled) {
            ExceptionUtils.catching(() -> valueOf(jsonElement.getAsString()).set(false));
            safe = false;
         }

         if (!safe) {
            notifier.sendAlert();
         }
      }
   }

   public static void start() {
      if (AxTrade.CONFIG.getBoolean("enable-safety", true)) {
         if (service == null) {
            notifier = new Notifier();
            service = new ExceptionReportingScheduledThreadPool(0, Thread.ofVirtual().factory());
            service.scheduleAtFixedRate(SafetyManager::check, 0L, 3L, TimeUnit.MINUTES);
         }
      }
   }

   public static void stop() {
      if (service != null) {
         service.shutdownNow();
      }
   }
}
