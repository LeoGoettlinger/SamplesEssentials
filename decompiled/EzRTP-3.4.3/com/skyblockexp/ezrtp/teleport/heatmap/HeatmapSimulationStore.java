package com.skyblockexp.ezrtp.teleport.heatmap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;
import org.bukkit.Location;

public final class HeatmapSimulationStore {
   private static final int DEFAULT_CAPACITY = 5000;
   private final Map<String, ConcurrentLinkedDeque<Location>> samplesPerWorld = new ConcurrentHashMap<>();
   private final int perWorldCapacity;

   public HeatmapSimulationStore() {
      this(5000);
   }

   public HeatmapSimulationStore(int perWorldCapacity) {
      this.perWorldCapacity = Math.max(1, perWorldCapacity);
   }

   public int getPerWorldCapacity() {
      return this.perWorldCapacity;
   }

   public int addSamples(String worldName, Collection<Location> samples) {
      if (worldName != null && samples != null && !samples.isEmpty()) {
         String key = normalize(worldName);
         List<Location> cleanSamples = samples.stream().filter(Objects::nonNull).<Location>map(Location::clone).collect(Collectors.toList());
         if (cleanSamples.isEmpty()) {
            return 0;
         } else {
            ConcurrentLinkedDeque<Location> buffer = this.samplesPerWorld.computeIfAbsent(key, k -> new ConcurrentLinkedDeque<>());

            for (Location sample : cleanSamples) {
               buffer.addLast(sample);
            }

            while (buffer.size() > this.perWorldCapacity) {
               buffer.pollFirst();
            }

            return cleanSamples.size();
         }
      } else {
         return 0;
      }
   }

   public List<Location> getSamples(String worldName) {
      if (worldName == null) {
         return Collections.emptyList();
      } else {
         ConcurrentLinkedDeque<Location> buffer = this.samplesPerWorld.get(normalize(worldName));
         if (buffer != null && !buffer.isEmpty()) {
            List<Location> copy = new ArrayList<>(buffer.size());

            for (Location location : buffer) {
               copy.add(location.clone());
            }

            return copy;
         } else {
            return Collections.emptyList();
         }
      }
   }

   public int clearWorld(String worldName) {
      if (worldName == null) {
         return 0;
      } else {
         ConcurrentLinkedDeque<Location> removed = this.samplesPerWorld.remove(normalize(worldName));
         return removed != null ? removed.size() : 0;
      }
   }

   public void clearAll() {
      this.samplesPerWorld.clear();
   }

   private static String normalize(String worldName) {
      return worldName.toLowerCase(Locale.ROOT);
   }
}
