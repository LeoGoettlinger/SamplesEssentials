package eu.kennytv.maintenance.core.runnable;

import eu.kennytv.maintenance.core.MaintenancePlugin;
import eu.kennytv.maintenance.core.Settings;
import net.kyori.adventure.text.Component;

public class MaintenanceRunnable extends MaintenanceRunnableBase {
   public MaintenanceRunnable(MaintenancePlugin plugin, Settings settings, int seconds, boolean enable) {
      super(plugin, settings, seconds, enable);
   }

   @Override
   protected void finish() {
      this.plugin.setMaintenance(this.enable);
   }

   @Override
   protected Component getStartMessage() {
      return this.settings.getMessage("starttimerBroadcast", "%TIME%", this.getTime());
   }

   @Override
   protected Component getEndMessage() {
      return this.settings.getMessage("endtimerBroadcast", "%TIME%", this.getTime());
   }
}
