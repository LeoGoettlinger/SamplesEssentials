package eu.kennytv.maintenance.api.event;

import eu.kennytv.maintenance.api.event.manager.MaintenanceEvent;

public record MaintenanceChangedEvent(boolean isMaintenance) implements MaintenanceEvent {
}
