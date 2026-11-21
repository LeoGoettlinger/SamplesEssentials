package dev.cwhead.GravesX.integration;

import com.nexomc.nexo.mechanics.MechanicFactory;
import com.nexomc.nexo.mechanics.MechanicsManager;
import com.nexomc.nexo.mechanics.custom_block.noteblock.NoteBlockMechanicFactory;
import com.nexomc.nexo.mechanics.furniture.FurnitureMechanic;
import com.ranull.graves.Graves;
import com.ranull.graves.data.EntityData;
import com.ranull.graves.manager.EntityDataManager;
import com.ranull.graves.type.Grave;
import dev.cwhead.GravesX.listener.integration.nexo.EntityDamageListener;
import dev.cwhead.GravesX.listener.integration.nexo.HangingBreakListener;
import dev.cwhead.GravesX.listener.integration.nexo.PlayerInteractEntityListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.NoteBlock;
import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;

public class Nexo extends EntityDataManager {
   private final Graves plugin;
   private final Plugin nexoPlugin;
   private final PlayerInteractEntityListener playerInteractEntityListener;
   private final EntityDamageListener entityDamageListener;
   private final HangingBreakListener hangingBreakListener;

   public Nexo(Graves plugin, Plugin nexoPlugin) {
      super(plugin);
      this.plugin = plugin;
      this.nexoPlugin = nexoPlugin;
      this.playerInteractEntityListener = new PlayerInteractEntityListener(plugin, this);
      this.entityDamageListener = new EntityDamageListener(this);
      this.hangingBreakListener = new HangingBreakListener(this);
      this.saveData();
      this.registerListeners();
   }

   public void saveData() {
      if (this.plugin.getConfig().getBoolean("settings.integration.nexo.write")) {
         String pluginFolder = this.plugin.getPluginsFolder() + "/" + this.nexoPlugin.getName();

         try {
            File gravesDir = new File(pluginFolder + "/items");
            if (!gravesDir.exists()) {
               gravesDir.mkdirs();
            }

            File graveModelDir = new File(pluginFolder + "/pack/assets/minecraft/models/gravesx");
            if (!graveModelDir.exists()) {
               graveModelDir.mkdirs();
            }

            this.copyResource("data/plugin/" + this.nexoPlugin.getName().toLowerCase() + "/items/graves.yml", new File(gravesDir, "graves.yml"));
            this.copyResource("data/model/grave.json", new File(graveModelDir, "grave.json"));
         } catch (Exception var4) {
            this.plugin.getLogger().severe("Failed to save " + this.nexoPlugin.getName() + " data.");
            this.plugin.logStackTrace(var4);
         }

         this.plugin.debugMessage("Saving " + this.nexoPlugin.getName() + " data.", 1);
      }
   }

   private void copyResource(String resourcePath, File outFile) {
      try {
         try (InputStream in = this.plugin.getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (in != null) {
               Files.copy(in, outFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
               return;
            }

            this.plugin.debugMessage("Resource not found: " + resourcePath, 1);
         }
      } catch (IOException var8) {
         this.plugin.getLogger().info("An error occurred while copying over " + resourcePath + " to " + outFile + ".");
         this.plugin.logStackTrace((Exception)var8);
      }
   }

   public void registerListeners() {
      this.plugin.getServer().getPluginManager().registerEvents(this.playerInteractEntityListener, this.plugin);
      this.plugin.getServer().getPluginManager().registerEvents(this.entityDamageListener, this.plugin);
      this.plugin.getServer().getPluginManager().registerEvents(this.hangingBreakListener, this.plugin);
   }

   public void unregisterListeners() {
      if (this.playerInteractEntityListener != null) {
         HandlerList.unregisterAll(this.playerInteractEntityListener);
      }

      if (this.entityDamageListener != null) {
         HandlerList.unregisterAll(this.entityDamageListener);
      }

      if (this.hangingBreakListener != null) {
         HandlerList.unregisterAll(this.hangingBreakListener);
      }
   }

   public void createFurniture(Location location, Grave grave) {
      if (this.plugin.getConfigManager().getConfigSection("nexo.furniture.enabled", grave).getBoolean("nexo.furniture.enabled")) {
         try {
            String name = this.plugin.getConfigManager().getConfigSection("nexo.furniture.name", grave).getString("nexo.furniture.name", "");
            FurnitureMechanic furnitureMechanic = this.getFurnitureMechanic(name);
            if (furnitureMechanic != null && location.getWorld() != null) {
               location.getBlock().setType(Material.AIR);
               Entity furniture = furnitureMechanic.place(location, location.getYaw(), BlockFace.UP);
               if (furniture != null) {
                  this.createEntityData(location, furniture.getUniqueId(), grave.getUUID(), EntityData.Type.NEXO);
                  this.plugin
                     .debugMessage(
                        "Placing Nexo furniture for "
                           + grave.getUUID()
                           + " at "
                           + location.getWorld().getName()
                           + ", "
                           + (location.getBlockX() + 0.5)
                           + "x, "
                           + (location.getBlockY() + 0.5)
                           + "y, "
                           + (location.getBlockZ() + 0.5)
                           + "z",
                        1
                     );
               }
            }
         } catch (NoSuchMethodError var6) {
            this.plugin.warningMessage("This version of Minecraft does not support " + this.nexoPlugin.getName() + " furniture");
         }
      }
   }

   public void removeFurniture(Grave grave) {
      this.removeFurniture(this.getEntityDataMap(this.getLoadedEntityDataList(grave)));
   }

   public void removeFurniture(EntityData entityData) {
      this.removeFurniture(this.getEntityDataMap(List.of(entityData)));
   }

   public void removeFurniture(Map<EntityData, Entity> entityDataMap) {
      List<EntityData> entityDataList = new ArrayList<>();

      for (Entry<EntityData, Entity> entry : entityDataMap.entrySet()) {
         entry.getValue().remove();
         entityDataList.add(entry.getKey());
      }

      this.plugin.getDataManager().removeEntityData(entityDataList);
   }

   public void createBlock(Location location, Grave grave) {
      if (this.plugin.getConfigManager().getConfigSection("nexo.block.enabled", grave).getBoolean("nexo.block.enabled")) {
         String name = this.plugin.getConfigManager().getConfigSection("nexo.block.name", grave).getString("nexo.block.name", "");
         if (!name.isEmpty() && location.getWorld() != null) {
            Block block = location.getBlock();
            NoteBlockMechanicFactory.Companion.setBlockModel(block, name);
            this.plugin
               .debugMessage(
                  "Placing nexo block for "
                     + grave.getUUID()
                     + " at "
                     + location.getWorld().getName()
                     + ", "
                     + (location.getBlockX() + 0.5)
                     + "x, "
                     + (location.getBlockY() + 0.5)
                     + "y, "
                     + (location.getBlockZ() + 0.5)
                     + "z",
                  1
               );
         }
      }
   }

   public void removeBlock(Location location) {
      location.getBlock().setType(Material.AIR);
   }

   public boolean isCustomBlock(Location location) {
      if (location.getBlock().getBlockData() instanceof NoteBlock noteBlock) {
         int id = noteBlock.getInstrument().getType() * 25 + noteBlock.getNote().getId() + (noteBlock.isPowered() ? 400 : 0) - 26;
         String key = String.valueOf(id);
         NoteBlockMechanicFactory factory = NoteBlockMechanicFactory.Companion.instance();
         return factory != null && factory.getMechanic(key) != null;
      } else {
         return false;
      }
   }

   public FurnitureMechanic getFurnitureMechanic(String string) {
      MechanicFactory mechanicFactory = MechanicsManager.INSTANCE.getMechanicFactory("furniture");
      return mechanicFactory != null ? (FurnitureMechanic)mechanicFactory.getMechanic(string) : null;
   }

   public boolean hasFurniture(Grave grave) {
      if (grave == null) {
         return false;
      } else {
         Map<EntityData, Entity> map = this.getEntityDataMap(this.getLoadedEntityDataList(grave));
         if (map.isEmpty()) {
            return false;
         } else {
            for (Entry<EntityData, Entity> e : map.entrySet()) {
               EntityData data = e.getKey();
               Entity ent = e.getValue();
               if (data != null && ent != null && data.getType() == EntityData.Type.NEXO && ent.isValid() && !ent.isDead()) {
                  return true;
               }
            }

            return false;
         }
      }
   }

   public boolean hasBlock(Grave grave) {
      if (grave == null) {
         return false;
      } else {
         Location loc = grave.getLocationDeath();
         if (loc == null || loc.getWorld() == null) {
            loc = grave.getLocationDeath();
         }

         return loc != null && loc.getWorld() != null ? this.isCustomBlock(loc) : false;
      }
   }
}
