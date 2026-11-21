package com.ranull.graves.integration;

import com.ranull.graves.Graves;
import com.ranull.graves.data.EntityData;
import com.ranull.graves.listener.integration.oraxen.EntityDamageListener;
import com.ranull.graves.listener.integration.oraxen.HangingBreakListener;
import com.ranull.graves.listener.integration.oraxen.PlayerInteractEntityListener;
import com.ranull.graves.manager.EntityDataManager;
import com.ranull.graves.type.Grave;
import io.th0rgal.oraxen.mechanics.MechanicFactory;
import io.th0rgal.oraxen.mechanics.MechanicsManager;
import io.th0rgal.oraxen.mechanics.provided.gameplay.furniture.FurnitureMechanic;
import io.th0rgal.oraxen.mechanics.provided.gameplay.noteblock.NoteBlockMechanic;
import io.th0rgal.oraxen.mechanics.provided.gameplay.noteblock.NoteBlockMechanicFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.NoteBlock;
import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;

@Deprecated
public class Oraxen extends EntityDataManager {
   private final Graves plugin;
   private final Plugin oraxenPlugin;
   private final PlayerInteractEntityListener playerInteractEntityListener;
   private final EntityDamageListener entityDamageListener;
   private final HangingBreakListener hangingBreakListener;

   @Deprecated
   public Oraxen(Graves plugin, Plugin oraxenPlugin) {
      super(plugin);
      this.plugin = plugin;
      this.oraxenPlugin = oraxenPlugin;
      this.playerInteractEntityListener = new PlayerInteractEntityListener(plugin, this);
      this.entityDamageListener = new EntityDamageListener(this);
      this.hangingBreakListener = new HangingBreakListener(this);
      this.saveData();
      this.registerListeners();
   }

   @Deprecated
   public void saveData() {
      if (this.plugin.getConfig().getBoolean("settings.integration.oraxen.write")) {
         String pluginFolder = this.plugin.getPluginsFolder() + "/" + this.oraxenPlugin.getName();

         try {
            File itemsDir = new File(pluginFolder + "/items");
            if (!itemsDir.exists()) {
               itemsDir.mkdirs();
            }

            File modelDir = new File(pluginFolder + "/pack/assets/minecraft/models/gravesx");
            if (!modelDir.exists()) {
               modelDir.mkdirs();
            }

            this.copyResource("data/plugin/" + this.oraxenPlugin.getName().toLowerCase() + "/items/graves.yml", new File(itemsDir, "graves.yml"));
            this.copyResource("data/model/grave.json", new File(modelDir, "grave.json"));
         } catch (Exception var4) {
            this.plugin.getLogger().severe("Failed to save " + this.oraxenPlugin.getName() + " data.");
            this.plugin.logStackTrace(var4);
         }

         this.plugin.debugMessage("Saving " + this.oraxenPlugin.getName() + " data.", 1);
      }
   }

   @Deprecated
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
         this.plugin.getLogger().info("An error occurred while copying " + resourcePath + " to " + outFile + ".");
         this.plugin.logStackTrace((Exception)var8);
      }
   }

   @Deprecated
   public void registerListeners() {
      this.plugin.getServer().getPluginManager().registerEvents(this.playerInteractEntityListener, this.plugin);
      this.plugin.getServer().getPluginManager().registerEvents(this.entityDamageListener, this.plugin);
      this.plugin.getServer().getPluginManager().registerEvents(this.hangingBreakListener, this.plugin);
   }

   @Deprecated
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

   @Deprecated
   public void createFurniture(Location location, Grave grave) {
      if (this.plugin.getConfig("oraxen.furniture.enabled", grave).getBoolean("oraxen.furniture.enabled")) {
         try {
            String name = this.plugin.getConfig("oraxen.furniture.name", grave).getString("oraxen.furniture.name", "");
            FurnitureMechanic furnitureMechanic = this.getFurnitureMechanic(name);
            if (furnitureMechanic != null && location.getWorld() != null) {
               location.getBlock().setType(Material.AIR);
               Entity furniture = furnitureMechanic.place(location, location.getYaw(), BlockFace.UP);
               if (furniture != null) {
                  this.createEntityData(location, furniture.getUniqueId(), grave.getUUID(), EntityData.Type.ORAXEN);
                  this.plugin
                     .debugMessage(
                        "Placing Oraxen furniture for "
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
            this.plugin.warningMessage("This version of Minecraft does not support " + this.oraxenPlugin.getName() + " furniture");
         }
      }
   }

   @Deprecated
   public void removeFurniture(Grave grave) {
      this.removeFurniture(this.getEntityDataMap(this.getLoadedEntityDataList(grave)));
   }

   @Deprecated
   public void removeFurniture(EntityData entityData) {
      this.removeFurniture(this.getEntityDataMap(Collections.singletonList(entityData)));
   }

   @Deprecated
   public void removeFurniture(Map<EntityData, Entity> entityDataMap) {
      List<EntityData> entityDataList = new ArrayList<>();

      for (Entry<EntityData, Entity> entry : entityDataMap.entrySet()) {
         entry.getValue().remove();
         entityDataList.add(entry.getKey());
      }

      this.plugin.getDataManager().removeEntityData(entityDataList);
   }

   @Deprecated
   public void createBlock(Location location, Grave grave) {
      if (this.plugin.getConfig("oraxen.block.enabled", grave).getBoolean("oraxen.block.enabled")) {
         String name = this.plugin.getConfig("oraxen.block.name", grave).getString("oraxen.block.name", "");
         NoteBlockMechanic noteBlockMechanic = this.getNoteBlockMechanic(name);
         if (noteBlockMechanic != null && location.getWorld() != null) {
            location.getBlock().setBlockData(NoteBlockMechanicFactory.createNoteBlockData(noteBlockMechanic.getCustomVariation()), false);
            this.plugin
               .debugMessage(
                  "Placing Oraxen block for "
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

   @Deprecated
   public boolean isCustomBlock(Location location) {
      if (location.getBlock().getBlockData() instanceof NoteBlock) {
         NoteBlock noteBlock = (NoteBlock)location.getBlock().getBlockData();
         int key = noteBlock.getInstrument().getType() * 25 + noteBlock.getNote().getId() + (noteBlock.isPowered() ? 400 : 0) - 26;
         return NoteBlockMechanicFactory.getBlockMechanic(key) != null;
      } else {
         return false;
      }
   }

   @Deprecated
   public void removeBlock(Location location) {
      location.getBlock().setType(Material.AIR);
   }

   @Deprecated
   public FurnitureMechanic getFurnitureMechanic(String string) {
      MechanicFactory mechanicFactory = MechanicsManager.getMechanicFactory("furniture");
      return mechanicFactory != null ? (FurnitureMechanic)mechanicFactory.getMechanic(string) : null;
   }

   @Deprecated
   public NoteBlockMechanic getNoteBlockMechanic(String string) {
      MechanicFactory mechanicFactory = MechanicsManager.getMechanicFactory("noteblock");
      return mechanicFactory != null ? (NoteBlockMechanic)mechanicFactory.getMechanic(string) : null;
   }

   @Deprecated
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
               if (data != null && ent != null && data.getType() == EntityData.Type.ORAXEN && ent.isValid() && !ent.isDead()) {
                  return true;
               }
            }

            return false;
         }
      }
   }

   @Deprecated
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
