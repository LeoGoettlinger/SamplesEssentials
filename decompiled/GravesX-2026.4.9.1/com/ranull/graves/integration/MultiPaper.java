package com.ranull.graves.integration;

import com.ranull.graves.Graves;
import com.ranull.graves.data.BlockData;
import com.ranull.graves.data.EntityData;
import com.ranull.graves.data.HologramData;
import com.ranull.graves.libraries.multilib.MultiLib;
import com.ranull.graves.type.Grave;
import com.ranull.graves.util.Base64Util;
import com.ranull.graves.util.StringUtil;
import com.ranull.graves.util.UUIDUtil;
import java.util.List;
import java.util.UUID;
import org.bukkit.inventory.ItemStack;

public class MultiPaper {
   private final Graves plugin;

   public MultiPaper(Graves plugin) {
      this.plugin = plugin;
      this.registerListeners();
   }

   public String getLocalServerName() {
      return MultiLib.getLocalServerName();
   }

   public void notifyGraveCreation(Grave grave) {
      MultiLib.notify("graves:grave_create", Base64Util.objectToBase64(grave) + "|" + Base64Util.objectToBase64(grave.getInventoryItemStack()));
   }

   public void notifyGraveRemoval(Grave grave) {
      MultiLib.notify("graves:grave_remove", grave.getUUID().toString());
   }

   public void notifyBlockCreation(BlockData blockData) {
      MultiLib.notify("graves:block_create", Base64Util.objectToBase64(blockData));
   }

   public void notifyHologramCreation(HologramData hologramData) {
      MultiLib.notify("graves:hologram_create", Base64Util.objectToBase64(hologramData));
   }

   public void notifyEntityCreation(EntityData entityData) {
      MultiLib.notify("graves:entity_create", Base64Util.objectToBase64(entityData));
   }

   private void registerListeners() {
      MultiLib.onString(
         this.plugin,
         "graves:grave_create",
         data -> {
            String[] dataSplit = data.split("\\|", 2);
            if (dataSplit.length != 2) {
               this.plugin.debugMessage("MultiLib, ERROR grave_create is malformed ", 2);
            } else {
               Grave grave = (Grave)Base64Util.base64ToObject(dataSplit[0]);
               List<ItemStack> itemStackList = (List<ItemStack>)Base64Util.base64ToObject(dataSplit[1]);
               if (grave != null && itemStackList != null) {
                  String title;
                  if (this.plugin.getIntegrationManager().hasMiniMessage()) {
                     String newTitle = StringUtil.parseString(
                        this.plugin.getConfigManager().getConfigSection("gui.grave.title", grave).getString("gui.grave.title"),
                        grave.getLocationDeath(),
                        grave,
                        this.plugin
                     );
                     title = MiniMessage.parseString(newTitle);
                  } else {
                     title = StringUtil.parseString(
                        this.plugin.getConfigManager().getConfigSection("gui.grave.title", grave).getString("gui.grave.title"),
                        grave.getLocationDeath(),
                        grave,
                        this.plugin
                     );
                  }

                  Grave.StorageMode storageMode = this.plugin
                     .getGraveManager()
                     .getStorageMode(this.plugin.getConfigManager().getConfigSection("storage.mode", grave).getString("storage.mode"));
                  grave.setInventory(this.plugin.getGraveManager().createGraveInventory(grave, grave.getLocationDeath(), itemStackList, title, storageMode));
                  this.plugin.getDataManager().addGrave(grave);
                  this.plugin.debugMessage("MultiLib, importing grave " + grave.getUUID(), 2);
               } else {
                  this.plugin.debugMessage("MultiLib, ERROR grave_create is malformed ", 2);
               }
            }
         }
      );
      MultiLib.onString(this.plugin, "graves:block_create", data -> {
         BlockData blockData = (BlockData)Base64Util.base64ToObject(data);
         if (blockData != null) {
            this.plugin.getDataManager().addBlockData(blockData);
            this.plugin.debugMessage("MultiLib, importing block for grave " + blockData.getGraveUUID(), 2);
         } else {
            this.plugin.debugMessage("MultiLib, ERROR block_create is malformed ", 2);
         }
      });
      MultiLib.onString(this.plugin, "graves:hologram_create", data -> {
         HologramData hologramData = (HologramData)Base64Util.base64ToObject(data);
         if (hologramData != null) {
            this.plugin.getDataManager().addHologramData(hologramData);
            this.plugin.debugMessage("MultiLib, importing hologram for grave " + hologramData.getUUIDGrave(), 2);
         } else {
            this.plugin.debugMessage("MultiLib, ERROR hologram_create is malformed ", 2);
         }
      });
      MultiLib.onString(this.plugin, "graves:entity_create", data -> {
         EntityData entityData = (EntityData)Base64Util.base64ToObject(data);
         if (entityData != null) {
            this.plugin.getDataManager().addEntityData(entityData);
            this.plugin.debugMessage("MultiLib, importing entity for grave " + entityData.getUUIDGrave(), 2);
         } else {
            this.plugin.debugMessage("MultiLib, ERROR entity_create is malformed ", 2);
         }
      });
      MultiLib.onString(this.plugin, "graves:grave_remove", data -> {
         UUID uuid = UUIDUtil.getUUID(data);
         if (uuid != null) {
            if (this.plugin.getCacheManager().getGraveMap().containsKey(uuid)) {
               this.plugin.getDataManager().removeGrave(uuid);
               this.plugin.debugMessage("MultiLib, removing grave " + uuid, 2);
            }
         } else {
            this.plugin.debugMessage("MultiLib, ERROR grave_remove is malformed ", 2);
         }
      });
   }
}
