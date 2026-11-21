package com.magmaguy.elitemobs.magmacore.config;

import com.magmaguy.elitemobs.magmacore.util.Logger;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;

public class UnusedNodeHandler {
   private UnusedNodeHandler() {
   }

   public static Configuration clearNodes(FileConfiguration configuration) {
      if (configuration.getDefaults() == null) {
         return configuration;
      } else {
         for (String actual : configuration.getKeys(false)) {
            boolean keyExists = false;

            for (String defaults : configuration.getDefaults().getKeys(true)) {
               if (actual.equals(defaults)) {
                  keyExists = true;
                  break;
               }
            }

            if (!keyExists) {
               configuration.set(actual, null);
               Logger.info("Deleting unused config values.");
            }
         }

         return configuration;
      }
   }
}
