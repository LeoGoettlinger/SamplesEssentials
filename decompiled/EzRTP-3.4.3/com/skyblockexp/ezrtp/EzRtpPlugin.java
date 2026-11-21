package com.skyblockexp.ezrtp;

import com.skyblockexp.ezrtp.bootstrap.EzRtpPluginBootstrap;
import com.skyblockexp.ezrtp.message.MessageProvider;
import com.skyblockexp.ezrtp.performance.PerformanceMonitor;
import com.skyblockexp.ezrtp.teleport.RandomTeleportService;
import com.skyblockexp.ezrtp.unsafe.UnsafeLocationMonitor;
import org.bukkit.plugin.java.JavaPlugin;

public final class EzRtpPlugin extends JavaPlugin {
   private EzRtpPluginBootstrap bootstrap;

   public void onEnable() {
      this.bootstrap = new EzRtpPluginBootstrap(this);
      this.bootstrap.enable();
   }

   public void onDisable() {
      if (this.bootstrap != null) {
         this.bootstrap.disable();
      }
   }

   public void reloadPluginConfiguration() {
      if (this.bootstrap != null) {
         this.bootstrap.reloadPluginConfiguration();
      }
   }

   public MessageProvider getMessageProvider() {
      return this.bootstrap != null ? this.bootstrap.getMessageProvider() : null;
   }

   public RandomTeleportService getTeleportService() {
      return this.bootstrap != null ? this.bootstrap.getTeleportService() : null;
   }

   public PerformanceMonitor getPerformanceMonitor() {
      return this.bootstrap != null ? this.bootstrap.getPerformanceMonitor() : null;
   }

   public UnsafeLocationMonitor getUnsafeLocationMonitor() {
      return this.bootstrap != null ? this.bootstrap.getUnsafeLocationMonitor() : null;
   }
}
