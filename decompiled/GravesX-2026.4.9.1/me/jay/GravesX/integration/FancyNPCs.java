package me.jay.GravesX.integration;

import com.ranull.graves.Graves;
import com.ranull.graves.manager.EntityDataManager;
import com.ranull.graves.type.Grave;
import de.oliver.fancynpcs.api.FancyNpcsPlugin;
import de.oliver.fancynpcs.api.Npc;
import de.oliver.fancynpcs.api.NpcAttribute;
import de.oliver.fancynpcs.api.NpcData;
import de.oliver.fancynpcs.api.NpcManager;
import de.oliver.fancynpcs.api.skins.SkinData;
import de.oliver.fancynpcs.api.skins.SkinData.SkinVariant;
import de.oliver.fancynpcs.api.utils.NpcEquipmentSlot;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
import me.jay.GravesX.listener.integration.fancynpcs.NpcInteractListener;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.EquipmentSlot;

public final class FancyNPCs extends EntityDataManager {
   private static final String POSE_ATTRIBUTE_NAME = "pose";
   private final Graves plugin;
   private final NpcInteractListener npcInteractListener;

   public FancyNPCs(Graves plugin) {
      super(plugin);
      this.plugin = plugin;
      this.npcInteractListener = new NpcInteractListener(plugin);
      plugin.getServer().getPluginManager().registerEvents(this.npcInteractListener, plugin);
   }

   public void unregisterListeners() {
      if (this.npcInteractListener != null) {
         HandlerList.unregisterAll(this.npcInteractListener);
      }
   }

   public void createCorpse(UUID uuid, Location location, Grave grave) {
      this.plugin
         .getSchedulerManager()
         .runTask(
            () -> {
               if (this.plugin.getConfigManager().getConfigSection("fancynpcs.corpse.enabled", grave).getBoolean("fancynpcs.corpse.enabled")
                  && grave.getOwnerType() == EntityType.PLAYER) {
                  Location npcLocation = location.clone();

                  try {
                     double x = this.plugin.getConfigManager().getConfigSection("fancynpcs.corpse.offset.x", grave).getDouble("fancynpcs.corpse.offset.x");
                     double y = this.plugin.getConfigManager().getConfigSection("fancynpcs.corpse.offset.y", grave).getDouble("fancynpcs.corpse.offset.y");
                     double z = this.plugin.getConfigManager().getConfigSection("fancynpcs.corpse.offset.z", grave).getDouble("fancynpcs.corpse.offset.z");
                     npcLocation.add(x, y, z);
                  } catch (IllegalArgumentException var14) {
                     npcLocation.add(-0.5, 0.0, -0.5);
                  }

                  String ownerIdStr = grave.getOwnerUUID().toString();
                  String npcIdStr = uuid.toString().replace("-", "");
                  NpcManager npcManager = FancyNpcsPlugin.get().getNpcManager();
                  NpcData npcData = new NpcData(npcIdStr, grave.getOwnerUUID(), npcLocation);
                  Npc npc = (Npc)FancyNpcsPlugin.get().getNpcAdapter().apply(npcData);

                  try {
                     SkinData skin = new SkinData(ownerIdStr, SkinVariant.AUTO, toValidBase64Texture(grave.getOwnerTexture()), grave.getOwnerTextureSignature());
                     npc.getData().setSkin(skin.getIdentifier());
                  } catch (Exception var13) {
                     this.plugin.getLogger().warning("Failed to load NPC skin for grave " + grave.getUUID() + ": " + var13.getMessage());
                     this.plugin.logStackTrace(var13);
                  }

                  npcManager.registerNpc(npc);
                  npc.create();
                  npc.spawnForAll();
                  NpcAttribute poseAttribute = FancyNpcsPlugin.get().getAttributeManager().getAttributeByName(EntityType.PLAYER, "pose");

                  try {
                     npc.getData()
                        .getAttributes()
                        .put(
                           poseAttribute,
                           this.plugin
                              .getConfigManager()
                              .getConfigSection("fancynpcs.corpse.pose", grave)
                              .getString("fancynpcs.corpse.pose", "swimming")
                              .toLowerCase()
                        );
                  } catch (Exception var12) {
                     this.plugin
                        .getLogger()
                        .severe(
                           "Failed to use pose "
                              + this.plugin
                                 .getConfigManager()
                                 .getConfigSection("fancynpcs.corpse.pose", grave)
                                 .getString("fancynpcs.corpse.pose", "swimming")
                                 .toLowerCase()
                              + ". "
                              + var12.getMessage()
                        );
                     this.plugin.logStackTrace(var12);
                     npc.getData().getAttributes().put(poseAttribute, "swimming");
                  }

                  npc.getData().setCollidable(false);
                  npc.getData().setShowInTab(false);
                  npc.getData().setDisplayName("<empty>");
                  if (this.plugin.getConfigManager().getConfigSection("fancynpcs.corpse.armor", grave).getBoolean("fancynpcs.corpse.armor")) {
                     this.equipArmor(npc, grave);
                  }

                  if (this.plugin.getConfigManager().getConfigSection("fancynpcs.corpse.hand", grave).getBoolean("fancynpcs.corpse.hand")) {
                     this.equipHands(npc, grave);
                  }

                  npc.updateForAll();
               }
            }
         );
   }

   public void createBedrockcompatCorpse(UUID uuid, Location location, Grave grave) {
      this.plugin
         .getSchedulerManager()
         .runTask(
            () -> {
               if (this.plugin.getConfigManager().getConfigSection("fancynpcs.corpse.enabled", grave).getBoolean("fancynpcs.corpse.enabled")
                  && grave.getOwnerType() == EntityType.PLAYER) {
                  Location npcLocation = location.clone();

                  try {
                     double x = this.plugin.getConfigManager().getConfigSection("fancynpcs.corpse.offset.x", grave).getDouble("fancynpcs.corpse.offset.x");
                     double y = this.plugin.getConfigManager().getConfigSection("fancynpcs.corpse.offset.y", grave).getDouble("fancynpcs.corpse.offset.y");
                     double z = this.plugin.getConfigManager().getConfigSection("fancynpcs.corpse.offset.z", grave).getDouble("fancynpcs.corpse.offset.z");
                     npcLocation.add(x, y, z);
                  } catch (IllegalArgumentException var14) {
                     npcLocation.add(-0.5, 0.0, -0.5);
                  }

                  String npcIdStr = uuid.toString().replace("-", "");
                  String ownerIdStr = grave.getOwnerUUID().toString();
                  NpcManager npcManager = FancyNpcsPlugin.get().getNpcManager();
                  NpcData npcData = new NpcData(npcIdStr, grave.getOwnerUUID(), npcLocation);
                  Npc npc = (Npc)FancyNpcsPlugin.get().getNpcAdapter().apply(npcData);

                  try {
                     npc.getData().setSkin(ownerIdStr);
                  } catch (Exception var13) {
                     this.plugin.getLogger().warning("Failed to apply Bedrock skin for grave " + grave.getUUID() + ": " + var13.getMessage());
                     this.plugin.logStackTrace(var13);
                  }

                  npcManager.registerNpc(npc);
                  npc.create();
                  npc.spawnForAll();
                  NpcAttribute poseAttribute = FancyNpcsPlugin.get().getAttributeManager().getAttributeByName(EntityType.PLAYER, "pose");

                  try {
                     npc.getData()
                        .getAttributes()
                        .put(
                           poseAttribute,
                           this.plugin
                              .getConfigManager()
                              .getConfigSection("fancynpcs.corpse.pose", grave)
                              .getString("fancynpcs.corpse.pose", "swimming")
                              .toLowerCase()
                        );
                  } catch (Exception var12) {
                     this.plugin
                        .getLogger()
                        .severe(
                           "Failed to use pose "
                              + this.plugin
                                 .getConfigManager()
                                 .getConfigSection("fancynpcs.corpse.pose", grave)
                                 .getString("fancynpcs.corpse.pose", "swimming")
                                 .toLowerCase()
                              + ". "
                              + var12.getMessage()
                        );
                     this.plugin.logStackTrace(var12);
                     npc.getData().getAttributes().put(poseAttribute, "swimming");
                  }

                  npc.getData().setCollidable(false);
                  npc.getData().setShowInTab(false);
                  npc.getData().setDisplayName("<empty>");
                  if (this.plugin.getConfigManager().getConfigSection("fancynpcs.corpse.armor", grave).getBoolean("fancynpcs.corpse.armor")) {
                     this.equipArmor(npc, grave);
                  }

                  if (this.plugin.getConfigManager().getConfigSection("fancynpcs.corpse.hand", grave).getBoolean("fancynpcs.corpse.hand")) {
                     this.equipHands(npc, grave);
                  }

                  npc.updateForAll();
               }
            }
         );
   }

   private void equipArmor(Npc npc, Grave grave) {
      if (grave.getEquipmentMap().containsKey(EquipmentSlot.HEAD)) {
         npc.getData().addEquipment(NpcEquipmentSlot.HEAD, grave.getEquipmentMap().get(EquipmentSlot.HEAD));
      }

      if (grave.getEquipmentMap().containsKey(EquipmentSlot.CHEST)) {
         npc.getData().addEquipment(NpcEquipmentSlot.CHEST, grave.getEquipmentMap().get(EquipmentSlot.CHEST));
      }

      if (grave.getEquipmentMap().containsKey(EquipmentSlot.LEGS)) {
         npc.getData().addEquipment(NpcEquipmentSlot.LEGS, grave.getEquipmentMap().get(EquipmentSlot.LEGS));
      }

      if (grave.getEquipmentMap().containsKey(EquipmentSlot.FEET)) {
         npc.getData().addEquipment(NpcEquipmentSlot.FEET, grave.getEquipmentMap().get(EquipmentSlot.FEET));
      }
   }

   private void equipHands(Npc npc, Grave grave) {
      if (grave.getEquipmentMap().containsKey(EquipmentSlot.HAND)) {
         npc.getData().addEquipment(NpcEquipmentSlot.MAINHAND, grave.getEquipmentMap().get(EquipmentSlot.HAND));
      }

      if (this.plugin.getVersionManager().hasSecondHand() && grave.getEquipmentMap().containsKey(EquipmentSlot.OFF_HAND)) {
         npc.getData().addEquipment(NpcEquipmentSlot.OFFHAND, grave.getEquipmentMap().get(EquipmentSlot.OFF_HAND));
      }
   }

   public void removeCorpse(Grave grave) {
      String id = grave.getUUID().toString().replace("-", "");
      NpcManager npcManager = FancyNpcsPlugin.get().getNpcManager();
      Npc npc = npcManager.getNpc(id);
      if (npc != null) {
         npc.removeForAll();
         npcManager.removeNpc(npc);
      }
   }

   public boolean hasCorpse(Grave grave) {
      String id = grave.getUUID().toString().replace("-", "");
      NpcManager npcManager = FancyNpcsPlugin.get().getNpcManager();
      return npcManager.getNpc(id) != null;
   }

   private static String toValidBase64Texture(String graveTexture) {
      if (graveTexture != null && !graveTexture.isEmpty()) {
         boolean isAlreadyBase64 = graveTexture.startsWith("eyJ");
         if (isAlreadyBase64) {
            return graveTexture;
         } else {
            String json = String.format("{\"textures\":{\"SKIN\":{\"url\":\"%s\"}}}", graveTexture);
            return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
         }
      } else {
         return "";
      }
   }
}
