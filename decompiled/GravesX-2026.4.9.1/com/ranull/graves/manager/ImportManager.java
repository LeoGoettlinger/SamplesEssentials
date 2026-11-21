package com.ranull.graves.manager;

import com.ranull.graves.Graves;
import com.ranull.graves.type.Grave;
import com.ranull.graves.util.StringUtil;
import com.ranull.graves.util.UUIDUtil;
import com.ranull.graves.util.YAMLUtil;
import dev.cwhead.GravesX.util.SkinTextureUtil_post_1_21_9;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.jay.GravesX.util.SkinSignatureUtil;
import me.jay.GravesX.util.SkinTextureUtil;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ImportManager {
   private final Graves plugin;
   private static final Pattern FILENAME_PATTERN = Pattern.compile("^(.+?)_(.+?)_(-?\\d+)_(-?\\d+)_(-?\\d+)\\.ya?ml$", 2);

   public ImportManager(Graves plugin) {
      this.plugin = plugin;
   }

   public long countAngelChestImportableOnly() {
      File[] files = this.listAngelChestFiles();
      if (files != null && files.length != 0) {
         long ok = 0L;

         for (File f : files) {
            FileConfiguration ac = this.loadFile(f);
            if (ac != null) {
               World world = this.resolveWorldForScan(ac, f.getName());
               if (world != null) {
                  Integer x = ac.isInt("x") ? ac.getInt("x") : null;
                  Integer y = ac.isInt("y") ? ac.getInt("y") : null;
                  Integer z = ac.isInt("z") ? ac.getInt("z") : null;
                  if (x == null && ac.isInt("customblock.location.x")) {
                     x = ac.getInt("customblock.location.x");
                  }

                  if (y == null && ac.isInt("customblock.location.y")) {
                     y = ac.getInt("customblock.location.y");
                  }

                  if (z == null && ac.isInt("customblock.location.z")) {
                     z = ac.getInt("customblock.location.z");
                  }

                  if (x == null || y == null || z == null) {
                     int[] coords = this.parseCoordsFromFilename(f.getName());
                     if (coords != null) {
                        if (x == null) {
                           x = coords[0];
                        }

                        if (y == null) {
                           y = coords[1];
                        }

                        if (z == null) {
                           z = coords[2];
                        }
                     }
                  }

                  if (x != null && y != null && z != null) {
                     ok++;
                  }
               }
            }
         }

         return ok;
      } else {
         return 0L;
      }
   }

   public List<Grave> importExternalPluginAngelChest() {
      return new ArrayList<>(this.importAngelChest());
   }

   public String countAngelChestStatusText() {
      File[] files = this.listAngelChestFiles();
      if (files != null && files.length != 0) {
         int total = files.length;
         int valid = 0;
         int importable = 0;
         int missingWorld = 0;
         int invalid = 0;

         for (File f : files) {
            FileConfiguration ac = this.loadFile(f);
            if (ac == null) {
               invalid++;
            } else {
               valid++;
               World world = this.resolveWorldForScan(ac, f.getName());
               if (world != null) {
                  importable++;
               } else {
                  missingWorld++;
               }
            }
         }

         StringBuilder sb = new StringBuilder(256);
         sb.append("AngelChest Import Scan\n");
         sb.append("  Total files: ").append(total).append('\n');
         sb.append("  Valid YAML: ").append(valid).append('\n');
         sb.append("  Importable: ").append(importable).append('\n');
         sb.append("  Missing world: ").append(missingWorld).append('\n');
         sb.append("  Invalid YAML: ").append(invalid);
         return sb.toString();
      } else {
         return "No files found in plugins/AngelChest/angelchests. Returning as none.";
      }
   }

   public String listAngelChestMissingWorldText() {
      File[] files = this.listAngelChestFiles();
      if (files != null && files.length != 0) {
         StringBuilder sb = new StringBuilder(512);
         int rows = 0;

         for (File f : files) {
            FileConfiguration ac = this.loadFile(f);
            if (ac != null) {
               World resolved = this.resolveWorldForScan(ac, f.getName());
               if (resolved == null) {
                  String ownerName = this.parseOwnerFromFilename(f.getName());
                  if (ownerName == null || ownerName.isEmpty()) {
                     String logfile = ac.getString("logfile", "");
                     String[] split = logfile != null ? logfile.split("_") : new String[0];
                     if (split.length > 0 && split[0] != null && !split[0].isEmpty()) {
                        ownerName = split[0];
                     }
                  }

                  UUID ownerUUID = UUIDUtil.getUUID(ac.getString("owner", null));
                  UUID worldUUID1 = UUIDUtil.getUUID(ac.getString("worldid", null));
                  UUID worldUUID2 = UUIDUtil.getUUID(ac.getString("customblock.location.worldid", null));
                  String worldFromFile = this.parseWorldFromFilename(f.getName());
                  String worldFromLogfile = null;
                  String logfile = ac.getString("logfile", "");
                  String[] split = logfile != null ? logfile.split("_") : new String[0];
                  if (split.length > 1) {
                     worldFromLogfile = split[1];
                  }

                  Integer x = ac.isInt("x") ? ac.getInt("x") : null;
                  Integer y = ac.isInt("y") ? ac.getInt("y") : null;
                  Integer z = ac.isInt("z") ? ac.getInt("z") : null;
                  if (x == null && ac.isInt("customblock.location.x")) {
                     x = ac.getInt("customblock.location.x");
                  }

                  if (y == null && ac.isInt("customblock.location.y")) {
                     y = ac.getInt("customblock.location.y");
                  }

                  if (z == null && ac.isInt("customblock.location.z")) {
                     z = ac.getInt("customblock.location.z");
                  }

                  if (x == null || y == null || z == null) {
                     int[] coords = this.parseCoordsFromFilename(f.getName());
                     if (coords != null) {
                        if (x == null) {
                           x = coords[0];
                        }

                        if (y == null) {
                           y = coords[1];
                        }

                        if (z == null) {
                           z = coords[2];
                        }
                     }
                  }

                  if (rows == 0) {
                     sb.append("AngelChest Missing-World Report\n");
                     sb.append("  (These graves cannot import until the referenced world exists)\n");
                  }

                  rows++;
                  sb.append("• File: ").append(f.getName()).append('\n');
                  sb.append("    Owner: ").append(nullOr(ownerName)).append("  UUID: ").append(nullOr(ownerUUID)).append('\n');
                  sb.append("    World UUIDs: primary=").append(nullOr(worldUUID1)).append(" secondary=").append(nullOr(worldUUID2)).append('\n');
                  sb.append("    World names: file=").append(nullOr(worldFromFile)).append(" logfile=").append(nullOr(worldFromLogfile)).append('\n');
                  sb.append("    Coords: ").append(nullOr(x)).append(',').append(nullOr(y)).append(',').append(nullOr(z)).append('\n');
               }
            }
         }

         return rows == 0 ? "AngelChest Missing-World Report\n  None — all referenced worlds are present." : sb.toString();
      } else {
         return "No files found in plugins/AngelChest/angelchests. Returning as none.";
      }
   }

   private static String nullOr(Object o) {
      return o == null ? "-" : String.valueOf(o);
   }

   private List<Grave> importAngelChest() {
      List<Grave> graveList = new ArrayList<>();
      File angelChest = new File(this.plugin.getPluginsFolder(), "AngelChest");
      if (!angelChest.exists()) {
         return graveList;
      } else {
         File angelChests = new File(angelChest, "angelchests");
         if (!angelChests.exists()) {
            return graveList;
         } else {
            File[] files = angelChests.listFiles((dir, name) -> {
               String lower = name.toLowerCase(Locale.ROOT);
               return (lower.endsWith(".yml") || lower.endsWith(".yaml")) && !name.startsWith(".");
            });
            if (files == null) {
               return graveList;
            } else {
               for (File file : files) {
                  Grave grave = this.convertAngelChestToGrave(file);
                  if (grave != null) {
                     graveList.add(grave);
                  }
               }

               return graveList;
            }
         }
      }
   }

   public Grave convertAngelChestToGrave(File file) {
      FileConfiguration ac = this.loadFile(file);
      if (ac == null) {
         return null;
      } else {
         Grave grave = new Grave(UUID.randomUUID());
         grave.setOwnerType(EntityType.PLAYER);
         UUID ownerUUID = UUIDUtil.getUUID(ac.getString("owner", null));
         grave.setOwnerUUID(ownerUUID);
         String ownerNameFromFile = this.parseOwnerFromFilename(file.getName());
         if (ownerNameFromFile != null && !ownerNameFromFile.isEmpty()) {
            grave.setOwnerName(ownerNameFromFile);
         } else {
            String logfile = ac.getString("logfile", "");
            String[] split = logfile.split("_");
            if (split.length > 0 && split[0] != null && !split[0].isEmpty()) {
               grave.setOwnerName(split[0]);
            }
         }

         if (ownerUUID != null) {
            Player player = null;

            try {
               player = this.plugin.getSchedulerManager().callSyncMethod(() -> this.plugin.getServer().getPlayer(ownerUUID)).get();
            } catch (ExecutionException | InterruptedException var25) {
               throw new RuntimeException(var25);
            }

            if (player != null) {
               try {
                  if (this.plugin.getVersionManager().isPost1_21_9()) {
                     grave.setOwnerTexture(SkinTextureUtil_post_1_21_9.getTexture(player));
                  } else {
                     grave.setOwnerTexture(SkinTextureUtil.getTexture(player));
                  }
               } catch (Throwable var24) {
               }

               try {
                  grave.setOwnerTextureSignature(SkinSignatureUtil.getSignature(player));
               } catch (Throwable var23) {
               }
            }
         }

         World world = this.resolveWorldForScan(ac, file.getName());
         Integer x = ac.isInt("x") ? ac.getInt("x") : null;
         Integer y = ac.isInt("y") ? ac.getInt("y") : null;
         Integer z = ac.isInt("z") ? ac.getInt("z") : null;
         if (x == null && ac.isInt("customblock.location.x")) {
            x = ac.getInt("customblock.location.x");
         }

         if (y == null && ac.isInt("customblock.location.y")) {
            y = ac.getInt("customblock.location.y");
         }

         if (z == null && ac.isInt("customblock.location.z")) {
            z = ac.getInt("customblock.location.z");
         }

         if (x == null || y == null || z == null) {
            int[] coords = this.parseCoordsFromFilename(file.getName());
            if (coords != null) {
               if (x == null) {
                  x = coords[0];
               }

               if (y == null) {
                  y = coords[1];
               }

               if (z == null) {
                  z = coords[2];
               }
            }
         }

         if (world != null && x != null && y != null && z != null) {
            grave.setLocationDeath(new Location(world, x.intValue(), y.intValue(), z.intValue()));
         }

         grave.setTimeCreation(ac.getLong("created", System.currentTimeMillis()));
         if (ac.getBoolean("infinite", false)) {
            grave.setTimeAlive(-1L);
         } else {
            long alive = this.resolveTimeAliveMillis(ac, grave);
            grave.setTimeAlive(alive);
            grave.setTimeAliveRemaining(alive);
         }

         grave.setProtection(ac.getBoolean("isProtected", false));
         long protectionTimeRemaining = ac.getLong("unlockIn", 0L);
         if (protectionTimeRemaining == -1L) {
            grave.setTimeProtection(-1L);
         } else {
            grave.setTimeProtection(this.resolveProtectionMillis(ac, grave));
         }

         grave.setExperience(ac.getInt("experience", 0));
         if (ac.isConfigurationSection("deathCause")) {
            String damageCause = ac.getString("deathCause.damageCause", "VOID");
            String killer = ac.getString("deathCause.killer", "null");
            grave.setKillerName(!"null".equalsIgnoreCase(killer) ? killer : StringUtil.format(damageCause));
         }

         List<ItemStack> armor = this.readItemList(ac, "armorInv", true);
         List<ItemStack> storage = this.readItemList(ac, "storageInv", false);
         List<ItemStack> extra = this.readItemList(ac, "extraInv", false);
         List<ItemStack> overflow = this.readItemList(ac, "overflowInv", false);
         EnumMap<EquipmentSlot, ItemStack> equip = new EnumMap<>(EquipmentSlot.class);
         if (armor != null) {
            if (armor.size() > 0 && armor.get(0) != null) {
               equip.put(EquipmentSlot.HEAD, armor.get(0));
            }

            if (armor.size() > 1 && armor.get(1) != null) {
               equip.put(EquipmentSlot.CHEST, armor.get(1));
            }

            if (armor.size() > 2 && armor.get(2) != null) {
               equip.put(EquipmentSlot.LEGS, armor.get(2));
            }

            if (armor.size() > 3 && armor.get(3) != null) {
               equip.put(EquipmentSlot.FEET, armor.get(3));
            }
         }

         if (extra != null && !extra.isEmpty() && extra.get(0) != null) {
            equip.put(EquipmentSlot.OFF_HAND, extra.get(0));
         }

         grave.setEquipmentMap(equip);
         List<ItemStack> itemStackList = new ArrayList<>();
         if (!armor.isEmpty()) {
            itemStackList.addAll(armor);
         }

         if (!storage.isEmpty()) {
            itemStackList.addAll(storage);
         }

         if (!extra.isEmpty()) {
            itemStackList.addAll(extra);
         }

         if (!overflow.isEmpty()) {
            itemStackList.addAll(overflow);
         }

         if (!itemStackList.isEmpty() && grave.getLocationDeath() != null) {
            String title = StringUtil.parseString(
               this.plugin.getConfigManager().getConfigSection("gui.grave.title", grave).getString("gui.grave.title", "Grave"),
               grave.getLocationDeath(),
               grave,
               this.plugin
            );
            Grave.StorageMode storageMode = this.plugin
               .getGraveManager()
               .getStorageMode(this.plugin.getConfigManager().getConfigSection("storage.mode", grave).getString("storage.mode", "INVENTORY"));
            Inventory inventory = null;

            try {
               inventory = this.plugin
                  .getSchedulerManager()
                  .callSyncMethod(() -> this.plugin.getGraveManager().createGraveInventory(grave, grave.getLocationDeath(), itemStackList, title, storageMode))
                  .get();
            } catch (ExecutionException | InterruptedException var22) {
               throw new RuntimeException(var22);
            }

            grave.setInventory(inventory);
         }

         return grave;
      }
   }

   private FileConfiguration loadFile(File file) {
      if (file == null || !file.exists()) {
         return null;
      } else if (!YAMLUtil.isValidYAML(file)) {
         return null;
      } else {
         try {
            return YamlConfiguration.loadConfiguration(file);
         } catch (Exception var3) {
            return null;
         }
      }
   }

   private List<ItemStack> readItemList(FileConfiguration cfg, String path, boolean reverseIfList) {
      if (cfg != null && path != null && cfg.contains(path)) {
         Object raw = cfg.get(path);
         List<ItemStack> out = new ArrayList<>();
         if (raw instanceof List<?> list) {
            boolean anyStacks = false;

            for (Object o : list) {
               if (o instanceof ItemStack is) {
                  out.add(is);
                  anyStacks = true;
               }
            }

            if (anyStacks) {
               if (reverseIfList && !out.isEmpty()) {
                  Collections.reverse(out);
               }

               return out;
            }

            for (Object ox : list) {
               if (ox instanceof Map<?, ?> map) {
                  try {
                     Map<String, Object> m = new LinkedHashMap<>();

                     for (Entry<?, ?> e : map.entrySet()) {
                        if (e.getKey() != null) {
                           m.put(String.valueOf(e.getKey()), e.getValue());
                        }
                     }

                     ItemStack is = ItemStack.deserialize(m);
                     out.add(is);
                  } catch (Throwable var14) {
                  }
               }
            }
         }

         return out;
      } else {
         return Collections.emptyList();
      }
   }

   private World resolveWorldForScan(FileConfiguration ac, String fileName) {
      UUID worldUUID = UUIDUtil.getUUID(ac.getString("worldid", null));
      World world = null;

      try {
         world = worldUUID != null ? this.plugin.getSchedulerManager().callSyncMethod(() -> this.plugin.getServer().getWorld(worldUUID)).get() : null;
      } catch (ExecutionException | InterruptedException var12) {
         throw new RuntimeException(var12);
      }

      if (world == null) {
         UUID worldUUID2 = UUIDUtil.getUUID(ac.getString("customblock.location.worldid", null));

         try {
            world = worldUUID2 != null ? this.plugin.getSchedulerManager().callSyncMethod(() -> this.plugin.getServer().getWorld(worldUUID2)).get() : null;
         } catch (ExecutionException | InterruptedException var11) {
            throw new RuntimeException(var11);
         }
      }

      if (world == null) {
         String worldNameFromFile = this.parseWorldFromFilename(fileName);
         if (worldNameFromFile != null) {
            String name = worldNameFromFile;

            try {
               world = this.plugin.getSchedulerManager().callSyncMethod(() -> this.plugin.getServer().getWorld(name)).get();
            } catch (ExecutionException | InterruptedException var10) {
               throw new RuntimeException(var10);
            }
         }
      }

      if (world == null) {
         String logfile = ac.getString("logfile", "");
         String[] split = logfile.split("_");
         if (split.length > 1) {
            String name = split[1];

            try {
               world = this.plugin.getSchedulerManager().callSyncMethod(() -> this.plugin.getServer().getWorld(name)).get();
            } catch (ExecutionException | InterruptedException var9) {
               throw new RuntimeException(var9);
            }
         }
      }

      return world;
   }

   private String parseOwnerFromFilename(String name) {
      Matcher m = FILENAME_PATTERN.matcher(name);
      return m.matches() ? m.group(1) : null;
   }

   private String parseWorldFromFilename(String name) {
      Matcher m = FILENAME_PATTERN.matcher(name);
      return m.matches() ? m.group(2) : null;
   }

   private int[] parseCoordsFromFilename(String name) {
      Matcher m = FILENAME_PATTERN.matcher(name);
      if (!m.matches()) {
         return null;
      } else {
         try {
            int x = Integer.parseInt(m.group(3));
            int y = Integer.parseInt(m.group(4));
            int z = Integer.parseInt(m.group(5));
            return new int[]{x, y, z};
         } catch (Exception var6) {
            return null;
         }
      }
   }

   private File[] listAngelChestFiles() {
      File base = new File(this.plugin.getPluginsFolder(), "AngelChest");
      if (!base.exists()) {
         return null;
      } else {
         File dir = new File(base, "angelchests");
         return !dir.exists() ? null : dir.listFiles((d, name) -> {
            String lower = name.toLowerCase(Locale.ROOT);
            return (lower.endsWith(".yml") || lower.endsWith(".yaml")) && !name.startsWith(".");
         });
      }
   }

   private long resolveTimeAliveMillis(FileConfiguration ac, Grave grave) {
      if (ac.getBoolean("infinite", false)) {
         return -1L;
      } else {
         long secondsLeft = ac.getLong("secondsLeft", -1L);
         return secondsLeft >= 0L ? secondsLeft * 1000L : this.plugin.getConfigManager().getConfigSection("grave.time", grave).getInt("grave.time", 0) * 1000L;
      }
   }

   private long resolveProtectionMillis(FileConfiguration ac, Grave grave) {
      long secondsLeft = ac.getLong("unlockIn", -1L);
      return secondsLeft >= 0L ? secondsLeft * 1000L : this.plugin.getConfigManager().getConfigSection("grave.time", grave).getInt("grave.time", 0) * 1000L;
   }
}
