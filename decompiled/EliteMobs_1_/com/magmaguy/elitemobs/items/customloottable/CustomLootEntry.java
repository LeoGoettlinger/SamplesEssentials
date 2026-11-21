package com.magmaguy.elitemobs.items.customloottable;

import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Generated;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CustomLootEntry implements Serializable {
   private static final long serialVersionUID = -9184579043562020517L;
   private double chance = 1.0;
   private int amount = 1;
   private String permission = "";
   private int wave = -1;
   private int itemLevel = 1;

   public static void errorMessage(String rawString, String configFilename, String reason) {
      Logger.warn("Failed to parse entry " + rawString + " for file " + configFilename + " due to invalid: " + reason);
   }

   protected static Map<String, Object> fields(Object raw) {
      Map<String, Object> result = new LinkedHashMap<>();
      if (raw instanceof String value) {
         if (value.regionMatches(true, 0, "minecraft:", 0, 10)) {
            value = value.substring(10);
         }

         for (String token : value.split(":", -1)) {
            String[] pair = token.split("=", 2);
            if (pair.length != 2 || pair[0].isBlank() || pair[1].isBlank()) {
               throw new IllegalArgumentException("field " + pair[0] + " requires a value");
            }

            putField(result, pair[0], pair[1]);
         }
      } else {
         if (!(raw instanceof Map<?, ?> map)) {
            throw new IllegalArgumentException("loot entry must be a string or map");
         }

         for (Entry<?, ?> entry : map.entrySet()) {
            if (!(entry.getKey() instanceof String key)) {
               throw new IllegalArgumentException("loot field names must be strings");
            }

            putField(result, key, entry.getValue());
         }
      }

      return result;
   }

   private static void putField(Map<String, Object> fields, String key, Object value) {
      String normalized = key.toLowerCase(Locale.ROOT);
      if (fields.containsKey(normalized)) {
         throw new IllegalArgumentException("duplicate field " + key);
      } else {
         fields.put(normalized, value);
      }
   }

   protected final void commonFields(Map<String, Object> fields, boolean supportsPermission, String... specificFields) {
      Set<String> allowed = new HashSet<>(List.of("amount", "chance", "permission", "wave", "itemlevel"));
      allowed.addAll(List.of(specificFields));

      for (String key : fields.keySet()) {
         if (!allowed.contains(key)) {
            throw new IllegalArgumentException("unknown field " + key);
         }
      }

      if (fields.containsKey("amount")) {
         this.amount = integer(fields, "amount");
      }

      if (fields.containsKey("chance")) {
         Object value = fields.get("chance");

         try {
            this.chance = Double.parseDouble(String.valueOf(value));
         } catch (NumberFormatException var7) {
            throw new IllegalArgumentException("chance must be numeric", var7);
         }

         if (!Double.isFinite(this.chance)) {
            throw new IllegalArgumentException("chance must be finite");
         }
      }

      if (supportsPermission && fields.containsKey("permission")) {
         this.permission = text(fields, "permission");
      }

      if (fields.containsKey("wave")) {
         this.wave = integer(fields, "wave");
      }

      if (fields.containsKey("itemlevel")) {
         this.itemLevel = integer(fields, "itemlevel");
      }
   }

   protected static int integer(Map<String, Object> fields, String key) {
      try {
         return Integer.parseInt(String.valueOf(fields.get(key)));
      } catch (NumberFormatException var3) {
         throw new IllegalArgumentException(key + " must be an integer", var3);
      }
   }

   protected static String text(Map<String, Object> fields, String key) {
      if (fields.get(key) instanceof String value && !value.isBlank()) {
         return value;
      } else {
         throw new IllegalArgumentException(key + " must be a nonempty string");
      }
   }

   public boolean willDrop(Player player) {
      if (this.amount > 0 && Double.isFinite(this.chance) && !(this.chance <= 0.0)) {
         return this.permission.isEmpty() || player != null && player.hasPermission(this.permission)
            ? ThreadLocalRandom.current().nextDouble() < this.chance
            : false;
      } else {
         return false;
      }
   }

   public boolean locationDrop(int itemTier, Player player, Location dropLocation) {
      return false;
   }

   public boolean directDrop(int itemTier, Player player) {
      return false;
   }

   public boolean locationDrop(int itemTier, Player player, Location dropLocation, EliteEntity eliteEntity) {
      return this.locationDrop(itemTier, player, dropLocation);
   }

   public boolean directDrop(int itemTier, Player player, EliteEntity eliteEntity) {
      return this.directDrop(itemTier, player);
   }

   public ItemStack previewDrop(int itemTier, Player player) {
      return null;
   }

   @Generated
   public double getChance() {
      return this.chance;
   }

   @Generated
   public void setChance(double chance) {
      this.chance = chance;
   }

   @Generated
   public int getAmount() {
      return this.amount;
   }

   @Generated
   public void setAmount(int amount) {
      this.amount = amount;
   }

   @Generated
   public String getPermission() {
      return this.permission;
   }

   @Generated
   public void setPermission(String permission) {
      this.permission = permission;
   }

   @Generated
   public int getWave() {
      return this.wave;
   }

   @Generated
   public void setWave(int wave) {
      this.wave = wave;
   }

   @Generated
   public int getItemLevel() {
      return this.itemLevel;
   }

   @Generated
   public void setItemLevel(int itemLevel) {
      this.itemLevel = itemLevel;
   }
}
