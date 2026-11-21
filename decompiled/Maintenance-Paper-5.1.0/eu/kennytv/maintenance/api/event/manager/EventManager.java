package eu.kennytv.maintenance.api.event.manager;

public interface EventManager {
   void registerListener(EventListener var1, Class<? extends MaintenanceEvent> var2);

   void callEvent(MaintenanceEvent var1);
}
