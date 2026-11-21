package com.ranull.graves.integration;

import com.ranull.graves.Graves;
import com.ranull.graves.data.ChunkData;
import com.ranull.graves.data.EntityData;
import com.ranull.graves.listener.integration.playernpc.NPCInteractListener;
import com.ranull.graves.manager.EntityDataManager;
import com.ranull.graves.type.Grave;
import dev.sergiferry.playernpc.api.NPCLib;
import dev.sergiferry.playernpc.api.NPC.Global;
import dev.sergiferry.playernpc.api.NPC.Pose;
import dev.sergiferry.playernpc.api.NPC.Skin;
import dev.sergiferry.playernpc.api.NPC.Skin.Custom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.Map.Entry;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.EquipmentSlot;

@Deprecated(
   since = "4.9.9.1"
)
public class PlayerNPC extends EntityDataManager {
   private final Graves plugin;
   private final NPCLib npcLib;
   private final NPCInteractListener npcInteractListener;

   @Deprecated(
      since = "4.9.9.1"
   )
   public PlayerNPC(Graves plugin) {
      super(plugin);
      this.plugin = plugin;
      this.npcLib = NPCLib.getInstance();
      this.npcInteractListener = new NPCInteractListener(plugin, this);
      if (!this.npcLib.isRegistered(plugin)) {
         this.npcLib.registerPlugin(plugin);
      }

      this.registerListeners();
   }

   @Deprecated(
      since = "4.9.9.1"
   )
   public void registerListeners() {
      this.plugin.getServer().getPluginManager().registerEvents(this.npcInteractListener, this.plugin);
   }

   @Deprecated(
      since = "4.9.9.1"
   )
   public void unregisterListeners() {
      if (this.npcInteractListener != null) {
         HandlerList.unregisterAll(this.npcInteractListener);
      }
   }

   @Deprecated(
      since = "4.9.9.1"
   )
   public void createCorpses() {
      for (ChunkData chunkData : this.plugin.getCacheManager().getChunkMap().values()) {
         for (EntityData entityData : chunkData.getEntityDataMap().values()) {
            if (entityData.getType() == EntityData.Type.PLAYERNPC) {
               Grave grave = this.plugin.getCacheManager().getGraveMap().get(entityData.getUUIDGrave());
               if (grave != null) {
                  this.createCorpse(entityData.getUUIDEntity(), entityData.getLocation(), grave, false);
               }
            }
         }
      }
   }

   @Deprecated(
      since = "4.9.9.1"
   )
   public void createCorpse(Location location, Grave grave) {
      this.createCorpse(UUID.randomUUID(), location, grave, true);
   }

   @Deprecated(
      since = "4.9.9.1"
   )
   public void createCorpse(UUID uuid, Location location, Grave grave, boolean createEntityData) {
      this.plugin
         .getSchedulerManager()
         .runTask(
            () -> {
               if (this.plugin.getConfigManager().getConfigSection("playernpc.corpse.enabled", grave).getBoolean("playernpc.corpse.enabled")
                  && grave.getOwnerType() == EntityType.PLAYER) {
                  Player player = this.plugin.getServer().getPlayer(grave.getOwnerUUID());
                  Location npcLocation = location.clone();
                  if (player != null && npcLocation.getWorld() != null && this.npcLib.getGlobalNPC(this.plugin, grave.getUUID().toString()) == null) {
                     location.getBlock().setType(Material.AIR);
                     Pose pose = Pose.SWIMMING;

                     try {
                        String poseName = this.plugin.getConfigManager().getConfigSection("playernpc.corpse.pose", grave).getString("playernpc.corpse.pose");
                        if (poseName != null) {
                           pose = Pose.valueOf(poseName);
                        }
                     } catch (IllegalArgumentException var17) {
                     }

                     try {
                        double x = this.plugin.getConfigManager().getConfigSection("playernpc.corpse.offset.x", grave).getDouble("playernpc.corpse.offset.x");
                        double y = this.plugin.getConfigManager().getConfigSection("playernpc.corpse.offset.y", grave).getDouble("playernpc.corpse.offset.y");
                        double z = this.plugin.getConfigManager().getConfigSection("playernpc.corpse.offset.z", grave).getDouble("playernpc.corpse.offset.z");
                        npcLocation.add(x, y, z);
                     } catch (IllegalArgumentException var16) {
                        npcLocation.add(0.5, -0.2, 0.5);
                     }

                     Global npc = this.npcLib.generateGlobalNPC(this.plugin, grave.getUUID().toString(), npcLocation);

                     try {
                        Optional<Custom> loaded = Custom.getLoadedSkin(this.plugin, grave.getOwnerUUID().toString());
                        if (loaded.isPresent()) {
                           npc.setSkin((Skin)loaded.get());
                        } else {
                           this.applySkinFromGraveOrFallback(npc, grave);
                        }
                     } catch (Exception var15) {
                        this.applySkinFromGraveOrFallback(npc, grave);
                     }

                     npc.setPose(pose);
                     npc.setAutoCreate(true);
                     npc.setAutoShow(true);
                     npc.setCustomData(this.plugin, "grave_uuid", grave.getUUID().toString());
                     boolean collide = this.plugin
                        .getConfigManager()
                        .getConfigSection("playernpc.corpse.collide", grave)
                        .getBoolean("playernpc.corpse.collide");
                     npc.setCollidable(collide);
                     if (this.plugin.getConfigManager().getConfigSection("playernpc.corpse.armor", grave).getBoolean("playernpc.corpse.armor")) {
                        if (grave.getEquipmentMap().containsKey(EquipmentSlot.HEAD)) {
                           npc.setHelmet(grave.getEquipmentMap().get(EquipmentSlot.HEAD));
                        }

                        if (grave.getEquipmentMap().containsKey(EquipmentSlot.CHEST)) {
                           npc.setChestplate(grave.getEquipmentMap().get(EquipmentSlot.CHEST));
                        }

                        if (grave.getEquipmentMap().containsKey(EquipmentSlot.LEGS)) {
                           npc.setLeggings(grave.getEquipmentMap().get(EquipmentSlot.LEGS));
                        }

                        if (grave.getEquipmentMap().containsKey(EquipmentSlot.FEET)) {
                           npc.setBoots(grave.getEquipmentMap().get(EquipmentSlot.FEET));
                        }
                     }

                     if (this.plugin.getConfigManager().getConfigSection("playernpc.corpse.hand", grave).getBoolean("playernpc.corpse.hand")) {
                        if (grave.getEquipmentMap().containsKey(EquipmentSlot.HAND)) {
                           npc.setItemInMainHand(grave.getEquipmentMap().get(EquipmentSlot.HAND));
                        }

                        if (this.plugin.getVersionManager().hasSecondHand() && grave.getEquipmentMap().containsKey(EquipmentSlot.OFF_HAND)) {
                           npc.setItemInOffHand(grave.getEquipmentMap().get(EquipmentSlot.OFF_HAND));
                        }
                     }

                     if (this.plugin.getConfigManager().getConfigSection("playernpc.corpse.glow.enabled", grave).getBoolean("playernpc.corpse.glow.enabled")) {
                        try {
                           String colorName = this.plugin
                              .getConfigManager()
                              .getConfigSection("playernpc.corpse.glow.color", grave)
                              .getString("playernpc.corpse.glow.color");
                           if (colorName != null) {
                              npc.setGlowing(true, ChatColor.valueOf(colorName));
                           } else {
                              npc.setGlowing(true);
                           }
                        } catch (IllegalArgumentException var14) {
                           npc.setGlowing(true);
                        }
                     }

                     npc.forceUpdate();
                     if (npcLocation.getWorld() != null) {
                        this.plugin
                           .debugMessage(
                              "Spawning PlayerNPC NPC for "
                                 + grave.getUUID()
                                 + " at "
                                 + npcLocation.getWorld().getName()
                                 + ", "
                                 + (npcLocation.getBlockX() + 0.5)
                                 + "x, "
                                 + (npcLocation.getBlockY() + 0.5)
                                 + "y, "
                                 + (npcLocation.getBlockZ() + 0.5)
                                 + "z",
                              1
                           );
                     }

                     if (createEntityData) {
                        this.createEntityData(location, uuid, grave.getUUID(), EntityData.Type.PLAYERNPC);
                     }
                  }
               }
            }
         );
   }

   @Deprecated(
      since = "4.9.9.1"
   )
   public void removeCorpse(Grave grave) {
      Optional<Global> grab = this.npcLib.grabGlobalNPC(this.plugin, grave.getUUID().toString());
      if (grab.isPresent()) {
         this.npcLib.removeGlobalNPC(grab.get());
      }

      this.removeCorpse(this.getEntityDataNPCMap(this.getLoadedEntityDataList(grave)));
   }

   @Deprecated(
      since = "4.9.9.1"
   )
   public void removeCorpse(EntityData entityData) {
      this.removeCorpse(this.getEntityDataNPCMap(Collections.singletonList(entityData)));
   }

   @Deprecated(
      since = "4.9.9.1"
   )
   public void removeCorpse(Map<EntityData, Global> entityDataMap) {
      List<EntityData> entityDataList = new ArrayList<>();

      for (Entry<EntityData, Global> entry : entityDataMap.entrySet()) {
         this.npcLib.removeGlobalNPC(entry.getValue());
         entityDataList.add(entry.getKey());
      }

      this.plugin.getDataManager().removeEntityData(entityDataList);
   }

   @Deprecated(
      since = "4.9.9.1"
   )
   private Map<EntityData, Global> getEntityDataNPCMap(List<EntityData> entityDataList) {
      Map<EntityData, Global> entityDataMap = new LinkedHashMap<>();

      for (EntityData entityData : entityDataList) {
         for (Global npc : this.npcLib.getAllGlobalNPCs()) {
            if (npc.hasCustomData(this.plugin, "grave_uuid") && npc.getCustomDataKeys().contains(entityData.getUUIDGrave().toString())) {
               entityDataMap.put(entityData, npc);
            }
         }
      }

      return entityDataMap;
   }

   private void applySkinFromGraveOrFallback(Global npc, Grave grave) {
      try {
         if (grave.getOwnerTexture() != null && grave.getOwnerTextureSignature() != null && grave.getOwnerName() != null) {
            Skin skin = Custom.createCustomSkin(this.plugin, grave.getOwnerUUID().toString(), grave.getOwnerTexture(), grave.getOwnerTextureSignature());
            npc.setSkin(skin);
            return;
         }
      } catch (Exception var5) {
      }

      try {
         Skin fallback = Custom.createCustomSkin(
            this.plugin,
            "194ffca812294de7ab5386bb5c2686d3",
            "ewogICJ0aW1lc3RhbXAiIDogMTcwMDA3NTcyMjAzOSwKICAicHJvZmlsZUlkIiA6ICIxOTRmZmNhODEyMjk0ZGU3YWI1Mzg2YmI1YzI2ODZkMyIsCiAgInByb2ZpbGVOYW1lIiA6ICJDb3Jwc2UiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWJmZjJlYzQ0ZWM1MWIzMmVmMzc4YTY1NzIwYzA2MGYzYWRmNzQ5NDVkNDgwNmQ1YjQyZTI1Y2UxNzM2NGViZiIKICAgIH0KICB9Cn0=",
            "v2WrGsMU53dyK1xvx6xS5r41XM4mvR6tB/86Tf5CjtQtv5ozjhEaHARHqFChnTl4/oG238alBMoFw6punEdLLJ8vVYSAa0K8CSpm8RT/gGvxpd6JHGsvcOEWEOV2wv0cntBs9BgrvoKvdFz7WyzT7w1PyP/74waU/Z83lBMU9he71DOFgAVnWXIp2PIWttK89hpbSmkrrdMLQ18/bUURQnp082ZinlDa7G2OjRbdpxGluOCKU725rufdnMhMBj5FCuuW8FaApa+6vuDDg6puIJgOXwtRX5/ZTp22UwEaMSegM+aP7oENx3wmm6XHHs3fgsulquRmxDuhAZ+sMi8wnW6lZU+2FWpsIOh4Xehn426iDu5wl4/kFe4RzTXr7G6N4uncgDRVaQQwsM3L/A7TmRbs8rQVrphqhOMvZ5R9fVu668EbMtAJbobofNxsVTRsRA9o7jnusIhmrWwroqVVxpq4k517ZEzDbPHkH/2X/amc7IGoeSLLfngIRYD+n7EUzO5ErQWFS778DiCxtQHKNOrBc/D+Fg9HsoH/Z2rD5dUBcxQ5DhprgMGGbaLDoQXjFul0mkE4Rg5yubonK+Ccvwmtv2s37sj1FwEJwllSFxvhmjxifTjSCaVoXJnsGJEZf3Zok9g2qk9gBzbgM1V2Ub8iOMupRs4JET9WR8+XIEk="
         );
         npc.setSkin(fallback);
      } catch (Exception var4) {
      }
   }
}
