package eu.kennytv.maintenance.api;

import eu.kennytv.maintenance.api.event.manager.EventManager;
import org.jetbrains.annotations.Nullable;

public interface Maintenance {
   void setMaintenance(boolean var1, @Nullable String var2);

   default void setMaintenance(boolean maintenance) {
      this.setMaintenance(maintenance, null);
   }

   boolean isMaintenance();

   boolean isTaskRunning();

   String getVersion();

   Settings getSettings();

   EventManager getEventManager();

   boolean isDebug();

   void setDebug(boolean var1);
}
