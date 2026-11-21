package eu.kennytv.maintenance.api;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

public final class MaintenanceProvider {
   private static Maintenance maintenance;

   private MaintenanceProvider() {
      throw new IllegalArgumentException();
   }

   @Internal
   public static void setMaintenance(Maintenance maintenance) {
      if (MaintenanceProvider.maintenance != null) {
         throw new IllegalArgumentException("MaintenanceProvider is already set!");
      } else {
         MaintenanceProvider.maintenance = maintenance;
      }
   }

   @Nullable
   public static Maintenance get() {
      return maintenance;
   }
}
