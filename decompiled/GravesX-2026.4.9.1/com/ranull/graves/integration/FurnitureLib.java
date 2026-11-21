package com.ranull.graves.integration;

import com.ranull.graves.Graves;
import com.ranull.graves.data.EntityData;
import com.ranull.graves.listener.integration.furniturelib.ProjectBreakListener;
import com.ranull.graves.listener.integration.furniturelib.ProjectClickListener;
import com.ranull.graves.manager.EntityDataManager;
import com.ranull.graves.type.Grave;
import com.ranull.graves.util.StringUtil;
import de.Ste3et_C0st.FurnitureLib.Crafting.Project;
import de.Ste3et_C0st.FurnitureLib.Utilitis.LocationUtil;
import de.Ste3et_C0st.FurnitureLib.main.FurniturePlugin;
import de.Ste3et_C0st.FurnitureLib.main.ObjectID;
import de.Ste3et_C0st.FurnitureLib.main.entity.fContainerEntity;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class FurnitureLib extends EntityDataManager {
   private final Graves plugin;
   private final de.Ste3et_C0st.FurnitureLib.main.FurnitureLib furnitureLib;
   private final ProjectClickListener projectClickListener;
   private final ProjectBreakListener projectBreakListener;

   public FurnitureLib(Graves plugin) {
      super(plugin);
      this.plugin = plugin;
      this.furnitureLib = de.Ste3et_C0st.FurnitureLib.main.FurnitureLib.getInstance();
      this.projectClickListener = new ProjectClickListener(plugin, this);
      this.projectBreakListener = new ProjectBreakListener(this);
      this.registerListeners();
      new FurnitureLib.Furniture(plugin);
   }

   public void registerListeners() {
      this.plugin.getServer().getPluginManager().registerEvents(this.projectClickListener, this.plugin);
      this.plugin.getServer().getPluginManager().registerEvents(this.projectBreakListener, this.plugin);
   }

   public void unregisterListeners() {
      if (this.projectClickListener != null) {
         HandlerList.unregisterAll(this.projectClickListener);
      }

      if (this.projectBreakListener != null) {
         HandlerList.unregisterAll(this.projectBreakListener);
      }
   }

   public boolean canBuild(Location location, Player player) {
      return this.furnitureLib.getPermManager().useProtectionLib() && this.furnitureLib.getPermManager().canBuild(player, location);
   }

   public void createFurniture(Location location, Grave grave) {
      if (this.plugin.getConfigManager().getConfigSection("furniturelib.enabled", grave).getBoolean("furniturelib.enabled")) {
         String name = this.plugin.getConfigManager().getConfigSection("furniturelib.name", grave).getString("furniturelib.name", "");
         Project project = this.furnitureLib.getFurnitureManager().getProject(name);
         if (project != null && project.haveModelSchematic()) {
            location.getBlock().setType(Material.AIR);

            try {
               ObjectID objectID = new ObjectID(project.getName(), project.getPlugin().getName(), location);
               location.setYaw(this.furnitureLib.getLocationUtil().FaceToYaw(LocationUtil.yawToFace(location.getYaw()).getOppositeFace()));
               this.furnitureLib.spawn(project, objectID);
               objectID.setUUID(UUID.randomUUID());
               objectID.getBlockList()
                  .stream()
                  .filter(signLocation -> signLocation.getBlock().getType().name().contains("SIGN"))
                  .forEach(
                     signLocation -> this.setSign(
                        signLocation.getBlock(),
                        this.plugin.getConfigManager().getConfigSection("furniturelib.line", grave).getStringList("furniturelib.line"),
                        grave
                     )
                  );
               if (this.plugin.getConfigManager().getConfigSection("furniturelib.head.replace", grave).getBoolean("furniturelib.head.replace")) {
                  objectID.getPacketList()
                     .stream()
                     .filter(fE -> fE instanceof fContainerEntity)
                     .map(fE -> (fContainerEntity)fE)
                     .forEach(fContainerEntity -> this.setSkull(fContainerEntity, grave));
               }

               this.furnitureLib.getFurnitureManager().addObjectID(objectID);
               this.createEntityData(objectID.getStartLocation(), objectID.getUUID(), grave.getUUID(), EntityData.Type.FURNITURELIB);
            } catch (IllegalArgumentException var6) {
               if (!var6.getMessage().contains("SADDLE")) {
                  this.plugin.getLogger().warning("Failed to create furniture " + name + ".");
                  this.plugin.logStackTrace((Exception)var6);
               }
            }
         } else {
            this.plugin.debugMessage("Can't find FurnitureLib furniture " + name, 1);
         }
      }
   }

   public void removeFurniture(Grave grave) {
      this.removeFurniture(this.getLoadedEntityDataList(grave));
   }

   public void removeFurniture(EntityData entityData) {
      this.removeFurniture(List.of(entityData));
   }

   public void removeFurniture(List<EntityData> entityDataList) {
      List<EntityData> removeEntityDataList = new ArrayList<>();

      for (EntityData entityData : entityDataList) {
         for (ObjectID objectID : this.furnitureLib.getFurnitureManager().getObjectList()) {
            if (objectID.getUUID() != null && objectID.getUUID().equals(entityData.getUUIDEntity())) {
               this.furnitureLib.getFurnitureManager().remove(objectID);
               removeEntityDataList.add(entityData);
            }
         }
      }

      this.plugin.getDataManager().removeEntityData(removeEntityDataList);
   }

   public boolean hasFurniture(Grave grave) {
      if (grave == null) {
         return false;
      } else {
         Location loc = grave.getLocationDeath();
         if (loc == null || loc.getWorld() == null) {
            loc = grave.getLocationDeath();
         }

         if (loc != null && loc.getWorld() != null) {
            int gx = loc.getBlockX();
            int gy = loc.getBlockY();
            int gz = loc.getBlockZ();
            String worldName = loc.getWorld().getName();

            try {
               for (ObjectID objectID : this.furnitureLib.getFurnitureManager().getObjectList()) {
                  Location start = objectID.getStartLocation();
                  if (start != null
                     && start.getWorld() != null
                     && worldName.equals(start.getWorld().getName())
                     && start.getBlockX() == gx
                     && start.getBlockY() == gy
                     && start.getBlockZ() == gz) {
                     return true;
                  }

                  for (Location part : objectID.getBlockList()) {
                     if (part != null
                        && part.getWorld() != null
                        && worldName.equals(part.getWorld().getName())
                        && part.getBlockX() == gx
                        && part.getBlockY() == gy
                        && part.getBlockZ() == gz) {
                        return true;
                     }
                  }
               }
            } catch (Throwable var12) {
            }

            return false;
         } else {
            return false;
         }
      }
   }

   private void setSign(Block block, List<String> stringList, Grave grave) {
      if (block.getState() instanceof Sign sign) {
         int counter = 0;

         for (String string : stringList) {
            if (counter > 4) {
               break;
            }

            if (this.plugin.getIntegrationManager().hasMiniMessage()) {
               String newLine = StringUtil.parseString(string, block.getLocation(), grave, this.plugin);
               sign.setLine(counter, MiniMessage.parseString(newLine));
            } else {
               sign.setLine(counter, StringUtil.parseString(string, block.getLocation(), grave, this.plugin));
            }

            counter++;
         }

         sign.update(true, false);
      }
   }

   private void setSkull(fContainerEntity fEntity, Grave grave) {
      List<String> materialList = this.plugin
         .getConfigManager()
         .getConfigSection("furniturelib.head.material", grave)
         .getStringList("furniturelib.head.material");
      ItemStack itemStack = this.plugin.getCompatibility().getSkullItemStack(grave, this.plugin);
      if (fEntity.getItemInMainHand() != null
         && materialList.contains(fEntity.getItemInMainHand().getType().name())
         && this.isSkullTextureBlank(fEntity.getItemInMainHand())) {
         fEntity.setItemInMainHand(itemStack);
      }

      if (fEntity.getItemInOffHand() != null
         && materialList.contains(fEntity.getItemInOffHand().getType().name())
         && this.isSkullTextureBlank(fEntity.getItemInOffHand())) {
         fEntity.setItemInOffHand(itemStack);
      }

      if (fEntity.getHelmet() != null && materialList.contains(fEntity.getHelmet().getType().name()) && this.isSkullTextureBlank(fEntity.getHelmet())) {
         fEntity.setHelmet(itemStack);
      }

      if (fEntity.getChestPlate() != null
         && materialList.contains(fEntity.getChestPlate().getType().name())
         && this.isSkullTextureBlank(fEntity.getChestPlate())) {
         fEntity.setChestPlate(itemStack);
      }

      if (fEntity.getLeggings() != null && materialList.contains(fEntity.getLeggings().getType().name()) && this.isSkullTextureBlank(fEntity.getLeggings())) {
         fEntity.setLeggings(itemStack);
      }

      if (fEntity.getBoots() != null && materialList.contains(fEntity.getBoots().getType().name()) && this.isSkullTextureBlank(fEntity.getBoots())) {
         fEntity.setBoots(itemStack);
      }
   }

   private boolean isSkullTextureBlank(ItemStack itemStack) {
      return this.plugin.getCompatibility().getSkullTexture(itemStack) == null;
   }

   public class Furniture extends FurniturePlugin {
      public Furniture(Plugin plugin) {
         super(plugin);
         this.register();
      }

      public void registerProjects() {
         String path = "data" + File.separator + "plugin" + File.separator + "furniturelib";

         try {
            new Project("Grave1", this.getPlugin(), this.getResource(path + File.separator + "Grave1.dModel"));
            new Project("Grave2", this.getPlugin(), this.getResource(path + File.separator + "Grave2.dModel"));
            new Project("Grave3", this.getPlugin(), this.getResource(path + File.separator + "Grave3.dModel"));
            new Project("Skull1", this.getPlugin(), this.getResource(path + File.separator + "Skull1.dModel"));
         } catch (Exception var3) {
            FurnitureLib.this.plugin.warningMessage(var3.getMessage());
         }
      }

      public void applyPluginFunctions() {
         FurnitureLib.this.furnitureLib
            .getFurnitureManager()
            .getProjects()
            .stream()
            .filter(project -> project.getPlugin().getName().equals(this.getPlugin().getDescription().getName()))
            .forEach(Project::applyFunction);
      }

      public void onFurnitureLateSpawn(ObjectID objectID) {
      }
   }
}
