package com.artillexstudios.axafkzone.zones;

import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Zones {
   private static final ConcurrentHashMap<String, Zone> zones = new ConcurrentHashMap<>();

   @NotNull
   public static ConcurrentHashMap<String, Zone> getZones() {
      return zones;
   }

   public static void addZone(@NotNull Zone zone) {
      zones.put(zone.getName(), zone);
   }

   @Nullable
   public static Zone getZoneByName(@NotNull String name) {
      return !zones.containsKey(name) ? null : zones.get(name);
   }

   public static void removeZone(@NotNull Zone zone) {
      zones.remove(zone.getName());
      zone.disable();
   }
}
