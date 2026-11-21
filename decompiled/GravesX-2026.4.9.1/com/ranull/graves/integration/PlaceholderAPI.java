package com.ranull.graves.integration;

import com.ranull.graves.Graves;
import com.ranull.graves.type.Grave;
import com.ranull.graves.util.ExperienceUtil;
import com.ranull.graves.util.StringUtil;
import com.ranull.graves.util.UUIDUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.Function;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.expansion.Relational;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlaceholderAPI extends PlaceholderExpansion implements Relational {
   private final Graves plugin;
   private final Map<String, Function<String, String>> placeholderHandlers;

   public PlaceholderAPI(Graves plugin) {
      this.plugin = plugin;
      this.placeholderHandlers = this.initializePlaceholderHandlers();
   }

   public boolean persist() {
      return true;
   }

   public boolean canRegister() {
      return true;
   }

   @NotNull
   public String getAuthor() {
      return String.join(", ", this.plugin.getDescription().getAuthors());
   }

   @NotNull
   public String getIdentifier() {
      return "gravesx";
   }

   @NotNull
   public String getVersion() {
      return this.plugin.getDescription().getVersion();
   }

   public String onPlaceholderRequest(Player player, @NotNull String identifier) {
      String id = identifier.toLowerCase();
      if (player != null && id.equals("count")) {
         return String.valueOf(this.plugin.getGraveManager().getGraveCount(player));
      } else {
         Function<String, String> handler = this.placeholderHandlers.get(id);
         if (handler != null) {
            return handler.apply(id);
         } else {
            for (Entry<String, Function<String, String>> entry : this.placeholderHandlers.entrySet()) {
               if (id.startsWith(entry.getKey())) {
                  return entry.getValue().apply(id);
               }
            }

            return null;
         }
      }
   }

   public String onPlaceholderRequest(Player playerOne, Player playerTwo, String identifier) {
      return this.onPlaceholderRequest(playerOne, identifier);
   }

   private Map<String, Function<String, String>> initializePlaceholderHandlers() {
      Map<String, Function<String, String>> handlers = new HashMap<>();
      handlers.put("author", id -> this.getAuthor());
      handlers.put("version", id -> this.getVersion());
      handlers.put("count_total", id -> String.valueOf(this.plugin.getCacheManager().getGraveMap().size()));
      handlers.put("owner_name_", id -> {
         UUID uuid = UUIDUtil.getUUID(id.replace("owner_name_", ""));
         return this.getOwnerDetail(uuid, Grave::getOwnerName);
      });
      handlers.put("owner_type_", id -> {
         UUID uuid = UUIDUtil.getUUID(id.replace("owner_type_", ""));
         return this.getOwnerDetail(uuid, grave -> {
            EntityType type = grave.getOwnerType();
            return type != null ? type.name() : "";
         });
      });
      handlers.put("owner_uuid_", id -> {
         UUID uuid = UUIDUtil.getUUID(id.replace("owner_uuid_", ""));
         return this.getOwnerDetail(uuid, grave -> {
            UUID ownerUUID = grave.getOwnerUUID();
            return ownerUUID != null ? ownerUUID.toString() : "";
         });
      });
      handlers.put("killer_name_", id -> {
         UUID uuid = UUIDUtil.getUUID(id.replace("killer_name_", ""));
         return this.getOwnerDetail(uuid, Grave::getKillerName);
      });
      handlers.put("killer_type_", id -> {
         UUID uuid = UUIDUtil.getUUID(id.replace("killer_type_", ""));
         return this.getOwnerDetail(uuid, grave -> {
            EntityType type = grave.getKillerType();
            return type != null ? type.name() : "";
         });
      });
      handlers.put("killer_uuid_", id -> {
         UUID uuid = UUIDUtil.getUUID(id.replace("killer_uuid_", ""));
         return this.getOwnerDetail(uuid, grave -> {
            UUID killerUUID = grave.getKillerUUID();
            return killerUUID != null ? killerUUID.toString() : "";
         });
      });
      handlers.put("item_", id -> {
         UUID uuid = UUIDUtil.getUUID(id.replace("item_", ""));
         return this.getOwnerDetail(uuid, grave -> String.valueOf(grave.getItemAmount()));
      });
      handlers.put("experience_", id -> {
         UUID uuid = UUIDUtil.getUUID(id.replace("experience_", ""));
         return this.getOwnerDetail(uuid, grave -> String.valueOf(grave.getExperience()));
      });
      handlers.put("level_", id -> {
         UUID uuid = UUIDUtil.getUUID(id.replace("level_", ""));
         return this.getOwnerDetail(uuid, grave -> String.valueOf(ExperienceUtil.getLevelFromExperience(grave.getExperience())));
      });
      handlers.put("time_creation_formatted", id -> {
         UUID uuid = UUIDUtil.getUUID(id.replace("time_creation_formatted", ""));
         return this.getOwnerDetail(uuid, grave -> StringUtil.getDateString(grave, grave.getTimeCreation(), this.plugin));
      });
      handlers.put("time_creation_", id -> {
         UUID uuid = UUIDUtil.getUUID(id.replace("time_creation_", ""));
         return this.getOwnerDetail(uuid, grave -> String.valueOf(grave.getTimeCreation() / 1000L));
      });
      handlers.put("time_alive_remaining_formatted_", id -> {
         UUID uuid = UUIDUtil.getUUID(id.replace("time_alive_remaining_formatted_", ""));
         return this.getOwnerDetail(uuid, grave -> StringUtil.getTimeString(grave, grave.getTimeAliveRemaining(), this.plugin));
      });
      handlers.put("time_alive_remaining_", id -> {
         UUID uuid = UUIDUtil.getUUID(id.replace("time_alive_remaining_", ""));
         return this.getOwnerDetail(uuid, grave -> String.valueOf(grave.getTimeAliveRemaining() / 1000L));
      });
      handlers.put("time_protection_remaining_formatted_", id -> {
         UUID uuid = UUIDUtil.getUUID(id.replace("time_protection_remaining_formatted_", ""));
         return this.getOwnerDetail(uuid, grave -> StringUtil.getTimeString(grave, grave.getTimeProtectionRemaining(), this.plugin));
      });
      handlers.put("time_protection_remaining_", id -> {
         UUID uuid = UUIDUtil.getUUID(id.replace("time_protection_remaining_", ""));
         return this.getOwnerDetail(uuid, grave -> String.valueOf(grave.getTimeProtectionRemaining() / 1000L));
      });
      handlers.put("time_lived_formatted_", id -> {
         UUID uuid = UUIDUtil.getUUID(id.replace("time_lived_formatted_", ""));
         return this.getOwnerDetail(uuid, grave -> StringUtil.getTimeString(grave, grave.getLivedTime(), this.plugin));
      });
      handlers.put("time_lived_", id -> {
         UUID uuid = UUIDUtil.getUUID(id.replace("time_lived_", ""));
         return this.getOwnerDetail(uuid, grave -> String.valueOf(grave.getLivedTime() / 1000L));
      });
      return handlers;
   }

   private String getOwnerDetail(UUID uuid, Function<Grave, String> function) {
      if (uuid != null && this.plugin.getCacheManager().getGraveMap().containsKey(uuid)) {
         Grave grave = this.plugin.getCacheManager().getGraveMap().get(uuid);
         return function.apply(grave);
      } else {
         return "";
      }
   }
}
