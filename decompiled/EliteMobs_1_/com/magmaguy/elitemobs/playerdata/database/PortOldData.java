package com.magmaguy.elitemobs.playerdata.database;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public class PortOldData {
   public PortOldData() {
      File playerCache = new File(MetadataHandler.PLUGIN.getDataFolder().getPath() + "/data/playerCache.yml");
      File playerMoneyData = new File(MetadataHandler.PLUGIN.getDataFolder().getPath() + "/data/playerMoneyData.yml");
      if (playerCache.exists() || playerMoneyData.exists()) {
         List<PlayerDataRepository.LegacyPlayerData> legacyPlayers = new ArrayList<>();

         try {
            Map<UUID, Object> names = this.loadLegacyValues(playerCache);
            Map<UUID, Object> balances = this.loadLegacyValues(playerMoneyData);
            HashSet<UUID> uuids = new HashSet<>(names.keySet());
            uuids.addAll(balances.keySet());

            for (UUID uuid : uuids) {
               if (!(names.getOrDefault(uuid, "PlaceholderName") instanceof String displayName)) {
                  throw new InvalidConfigurationException("Invalid display name for " + uuid + " in " + playerCache.getName());
               }

               if (!(balances.getOrDefault(uuid, 0.0) instanceof Number number)) {
                  throw new InvalidConfigurationException("Invalid currency for " + uuid + " in " + playerMoneyData.getName());
               }

               double currency = number.doubleValue();
               double cents = currency * 100.0;
               if (!Double.isFinite(currency) || cents >= 9.223372E18F || cents < -9.223372E18F) {
                  throw new InvalidConfigurationException("Currency is outside the supported range for " + uuid + " in " + playerMoneyData.getName());
               }

               legacyPlayers.add(new PlayerDataRepository.LegacyPlayerData(uuid, displayName, currency));
            }
         } catch (InvalidConfigurationException | IOException var18) {
            Logger.warn("Failed to read legacy player data; no players were imported and source files were preserved.");
            Logger.warn(var18.getClass().getName() + ": " + var18.getMessage());
            return;
         }

         try {
            if (!legacyPlayers.isEmpty()) {
               PlayerDataRepository.importLegacy(legacyPlayers);
            }

            this.deleteConfigs(playerCache, playerMoneyData);
         } catch (Exception var17) {
            Logger.warn("Failed to transactionally import legacy player data; source files were preserved.");
            Logger.warn(var17.getClass().getName() + ": " + var17.getMessage());
         }
      }
   }

   private void deleteConfigs(File playerCache, File playerMoneyData) {
      this.deleteConfig(playerCache);
      this.deleteConfig(playerMoneyData);
   }

   private void deleteConfig(File file) {
      if (file.exists() && file.isFile()) {
         if (file.delete()) {
            Logger.warn("Deleted data file " + file.getName() + " - was no longer in use, moved to the player database");
         } else {
            Logger.warn("Legacy player data was imported, but " + file.getName() + " could not be deleted.");
         }
      }
   }

   private Map<UUID, Object> loadLegacyValues(File source) throws IOException, InvalidConfigurationException {
      Map<UUID, Object> values = new HashMap<>();
      if (!source.exists()) {
         return values;
      } else {
         YamlConfiguration configuration = new YamlConfiguration();
         configuration.load(source);

         for (Entry<String, Object> entry : configuration.getValues(false).entrySet()) {
            UUID uuid;
            try {
               uuid = UUID.fromString(entry.getKey());
            } catch (IllegalArgumentException var8) {
               throw new InvalidConfigurationException("Invalid player UUID '" + entry.getKey() + "' in " + source.getName(), var8);
            }

            if (values.containsKey(uuid)) {
               throw new InvalidConfigurationException("Duplicate player UUID " + uuid + " in " + source.getName());
            }

            values.put(uuid, entry.getValue());
         }

         return values;
      }
   }
}
