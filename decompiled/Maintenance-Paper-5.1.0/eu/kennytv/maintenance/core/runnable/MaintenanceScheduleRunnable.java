package eu.kennytv.maintenance.core.runnable;

import eu.kennytv.maintenance.core.MaintenancePlugin;
import eu.kennytv.maintenance.core.Settings;
import java.time.Duration;
import net.kyori.adventure.text.Component;

public final class MaintenanceScheduleRunnable extends MaintenanceRunnable {
   private final int maintenanceDuration;

   public MaintenanceScheduleRunnable(MaintenancePlugin plugin, Settings settings, int secondsToEnable, int maintenanceDuration) {
      super(plugin, settings, secondsToEnable, true);
      this.maintenanceDuration = maintenanceDuration;
   }

   @Override
   protected void finish() {
      super.finish();
      this.plugin.startMaintenanceRunnable(Duration.ofSeconds(this.maintenanceDuration), false);
   }

   @Override
   protected Component getStartMessage() {
      return this.settings.getMessage("scheduletimerBroadcast", "%TIME%", this.getTime(), "%DURATION%", this.plugin.getFormattedTime(this.maintenanceDuration));
   }
}
