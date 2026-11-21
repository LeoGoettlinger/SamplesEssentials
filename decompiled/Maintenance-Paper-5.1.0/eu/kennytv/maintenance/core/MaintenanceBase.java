package eu.kennytv.maintenance.core;

import eu.kennytv.maintenance.api.Maintenance;

@FunctionalInterface
public interface MaintenanceBase {
   Maintenance getApi();
}
