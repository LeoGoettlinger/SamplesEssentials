package com.magmaguy.elitemobs.instanced.arena;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

public class ArenaWaves {
   private final Map<Integer, List<ArenaEntity>> arenaEntities;

   public ArenaWaves(List<String> rawBosses) {
      Map<Integer, List<ArenaEntity>> waves = new HashMap<>();
      if (rawBosses == null) {
         throw new IllegalArgumentException("Arena bossList is missing");
      } else {
         for (String entry : rawBosses) {
            if (entry == null) {
               throw new IllegalArgumentException("Arena bossList contains a null entry");
            }

            Map<String, String> fields = new HashMap<>();
            String[] wave = entry.split(":", -1);
            int level = wave.length;
            int spawn = 0;

            while (spawn < level) {
               String field = wave[spawn];
               String[] pair = field.split("=", 2);
               if (pair.length == 2 && !pair[1].isBlank()) {
                  String key = pair[0].trim().toLowerCase(Locale.ROOT);
                  if (List.of("wave", "spawnpoint", "boss", "mythicmob", "level").contains(key) && fields.putIfAbsent(key, pair[1].trim()) == null) {
                     spawn++;
                     continue;
                  }

                  throw new IllegalArgumentException("Unknown or duplicate arena wave field: " + entry);
               }

               throw new IllegalArgumentException("Invalid arena wave: " + entry);
            }

            int wavex = integer(fields.get("wave"), entry);
            level = fields.containsKey("level") ? integer(fields.get("level"), entry) : -1;
            String spawnx = fields.get("spawnpoint");
            String boss = fields.get("boss");
            String mythic = fields.getOrDefault("mythicmob", "false");
            if (wavex < 1
               || level != -1 && level < 1
               || spawnx == null
               || boss == null
               || !mythic.equalsIgnoreCase("true") && !mythic.equalsIgnoreCase("false")) {
               throw new IllegalArgumentException("Invalid arena wave: " + entry);
            }

            ArenaEntity entity = new ArenaEntity(spawnx, wavex, boss);
            entity.setMythicMob(Boolean.parseBoolean(mythic));
            entity.setLevel(level);
            waves.computeIfAbsent(wavex, ignored -> new ArrayList<>()).add(entity);
         }

         waves.replaceAll((wavex, entries) -> List.copyOf(entries));
         this.arenaEntities = Map.copyOf(waves);
      }
   }

   private static int integer(String value, String entry) {
      try {
         return Integer.parseInt(value);
      } catch (NumberFormatException var3) {
         throw new IllegalArgumentException("Invalid arena wave number: " + entry, var3);
      }
   }

   void validate(int waveCount, ArenaContainer container) {
      if (waveCount >= 1 && this.arenaEntities.size() == waveCount) {
         for (Entry<Integer, List<ArenaEntity>> wave : this.arenaEntities.entrySet()) {
            if (wave.getKey() > waveCount) {
               throw new IllegalArgumentException("Arena wave exceeds waveCount: " + wave.getKey());
            }

            for (ArenaEntity entity : wave.getValue()) {
               if (container.spawnPoint(entity.getSpawnPointName()) == null) {
                  throw new IllegalArgumentException("Unknown arena spawn point: " + entity.getSpawnPointName());
               }
            }
         }
      } else {
         throw new IllegalArgumentException("Arena requires at least one boss in every configured wave");
      }
   }

   public List<ArenaEntity> getWaveEntities(int wave) {
      return this.arenaEntities.get(wave);
   }
}
