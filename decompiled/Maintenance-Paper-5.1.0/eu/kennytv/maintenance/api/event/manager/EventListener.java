package eu.kennytv.maintenance.api.event.manager;

public abstract class EventListener<T extends MaintenanceEvent> {
   public abstract void onEvent(T var1);
}
