package com.magmaguy.elitemobs.advancedcombat.weapons;

import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.util.Objects;
import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServiceRegisterEvent;
import org.bukkit.event.server.ServiceUnregisterEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public final class AdvancedMagicWeaponIntegration implements Listener, AutoCloseable {
   public static final String SPELLCASTER_ROOT_ID = "spellcaster";
   public static final String UNAVAILABLE_REASON = "Requires FreeMinecraftModels with its bundled staff and wand.";
   private static final String FMM_PLUGIN_NAME = "FreeMinecraftModels";
   private static final String MAGIC_SERVICE_CLASS = "com.magmaguy.freeminecraftmodels.api.magic.MagicWeaponService";
   private static final long READINESS_WARNING_DELAY_TICKS = 600L;
   private final Plugin plugin;
   private volatile AdvancedMagicWeaponIntegration.Connection connection;
   private BukkitTask readinessWarningTask;
   private volatile boolean started;
   private volatile boolean closed;
   private boolean warningSent;

   public AdvancedMagicWeaponIntegration(Plugin plugin) {
      this.plugin = Objects.requireNonNull(plugin, "plugin");
   }

   public void start() {
      if (!this.closed && !this.started) {
         this.started = true;
         Bukkit.getPluginManager().registerEvents(this, this.plugin);
         this.connect(true);
      } else {
         throw new IllegalStateException("Magic weapon integration cannot start");
      }
   }

   public boolean isAvailable() {
      AdvancedMagicWeaponIntegration.Connection active = this.connection;
      return this.started && !this.closed && active != null && active.isOperational();
   }

   public Optional<String> unavailableReason(String rootFormId) {
      return "spellcaster".equals(rootFormId) && !this.isAvailable()
         ? Optional.of("Requires FreeMinecraftModels with its bundled staff and wand.")
         : Optional.empty();
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPluginEnabled(PluginEnableEvent event) {
      if ("FreeMinecraftModels".equals(event.getPlugin().getName())) {
         this.connect(false);
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPluginDisabled(PluginDisableEvent event) {
      if ("FreeMinecraftModels".equals(event.getPlugin().getName())) {
         this.cancelReadinessWarning();
         this.disconnect();
         this.warnUnavailable();
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onServiceRegistered(ServiceRegisterEvent event) {
      if ("com.magmaguy.freeminecraftmodels.api.magic.MagicWeaponService".equals(event.getProvider().getService().getName())) {
         this.connect(false);
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onServiceUnregistered(ServiceUnregisterEvent event) {
      if ("com.magmaguy.freeminecraftmodels.api.magic.MagicWeaponService".equals(event.getProvider().getService().getName())) {
         this.disconnect();
         this.scheduleReadinessWarning();
      }
   }

   private synchronized void connect(boolean warnIfMissing) {
      if (this.started && !this.closed) {
         Plugin fmm = Bukkit.getPluginManager().getPlugin("FreeMinecraftModels");
         if (fmm != null && fmm.isEnabled()) {
            AdvancedMagicWeaponIntegration.Connection active = this.connection;
            if (active != null && active.isOperational()) {
               this.onConnected();
            } else {
               this.disconnect();

               try {
                  AdvancedMagicWeaponIntegration.ConnectAttempt attempt = FmmMagicWeaponAdapter.connect(this.plugin);
                  if (attempt.connection() != null) {
                     this.connection = attempt.connection();
                     this.onConnected();
                     return;
                  }

                  switch (attempt.status()) {
                     case CONNECTED:
                        throw new IllegalStateException("Connected without an adapter");
                     case WAITING_FOR_SERVICE:
                        this.scheduleReadinessWarning();
                        break;
                     case CONTENT_UNAVAILABLE:
                        this.cancelReadinessWarning();
                        break;
                     case INCOMPATIBLE:
                     case REGISTRATION_REJECTED:
                        this.warnUnavailable();
                  }
               } catch (LinkageError var5) {
                  this.warnUnavailable();
               } catch (RuntimeException var6) {
                  Logger.warn("Could not connect EliteMobs magic scaling to FreeMinecraftModels: " + var6.getMessage());
                  this.warnUnavailable();
               }
            }
         } else {
            this.disconnect();
            if (warnIfMissing) {
               this.warnUnavailable();
            }
         }
      }
   }

   private void onConnected() {
      this.warningSent = false;
      this.cancelReadinessWarning();
   }

   private synchronized void disconnect() {
      AdvancedMagicWeaponIntegration.Connection active = this.connection;
      this.connection = null;
      if (active != null) {
         try {
            active.close();
         } catch (Exception var3) {
         }
      }
   }

   private synchronized void scheduleReadinessWarning() {
      if (!this.closed && this.readinessWarningTask == null) {
         this.readinessWarningTask = Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            synchronized (this) {
               this.readinessWarningTask = null;
            }

            this.connect(false);
            if (!this.isAvailable()) {
               this.cancelReadinessWarning();
               this.warnUnavailable();
            }
         }, 600L);
      }
   }

   private synchronized void cancelReadinessWarning() {
      if (this.readinessWarningTask != null) {
         this.readinessWarningTask.cancel();
         this.readinessWarningTask = null;
      }
   }

   private synchronized void warnUnavailable() {
      if (Bukkit.getPluginManager().isPluginEnabled("FreeMinecraftModels")) {
         if (!this.warningSent && !this.closed) {
            this.warningSent = true;
            this.cancelReadinessWarning();
            Logger.warn("Spellcaster is locked. Install or update FreeMinecraftModels with its bundled staff and wand, then restart the server.");
         }
      }
   }

   @Override
   public synchronized void close() {
      if (!this.closed) {
         this.closed = true;
         this.started = false;
         this.cancelReadinessWarning();
         this.disconnect();
         HandlerList.unregisterAll(this);
      }
   }

   record ConnectAttempt(AdvancedMagicWeaponIntegration.ConnectStatus status, AdvancedMagicWeaponIntegration.Connection connection) {
      ConnectAttempt(AdvancedMagicWeaponIntegration.ConnectStatus status, AdvancedMagicWeaponIntegration.Connection connection) {
         Objects.requireNonNull(status, "status");
         if (status == AdvancedMagicWeaponIntegration.ConnectStatus.CONNECTED != (connection != null)) {
            throw new IllegalArgumentException("Only connected attempts carry an adapter");
         } else {
            this.status = status;
            this.connection = connection;
         }
      }

      static AdvancedMagicWeaponIntegration.ConnectAttempt connected(AdvancedMagicWeaponIntegration.Connection connection) {
         return new AdvancedMagicWeaponIntegration.ConnectAttempt(
            AdvancedMagicWeaponIntegration.ConnectStatus.CONNECTED, Objects.requireNonNull(connection, "connection")
         );
      }

      static AdvancedMagicWeaponIntegration.ConnectAttempt unavailable(AdvancedMagicWeaponIntegration.ConnectStatus status) {
         return new AdvancedMagicWeaponIntegration.ConnectAttempt(status, null);
      }
   }

   static enum ConnectStatus {
      CONNECTED,
      WAITING_FOR_SERVICE,
      CONTENT_UNAVAILABLE,
      INCOMPATIBLE,
      REGISTRATION_REJECTED;
   }

   interface Connection extends AutoCloseable {
      boolean isOperational();

      @Override
      void close();
   }
}
