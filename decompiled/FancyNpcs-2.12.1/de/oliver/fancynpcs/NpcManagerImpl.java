package de.oliver.fancynpcs;

import de.oliver.fancyanalytics.logger.ExtendedFancyLogger;
import de.oliver.fancyanalytics.logger.properties.ThrowableProperty;
import de.oliver.fancylib.serverSoftware.ServerSoftware;
import de.oliver.fancynpcs.api.Npc;
import de.oliver.fancynpcs.api.NpcAttribute;
import de.oliver.fancynpcs.api.NpcData;
import de.oliver.fancynpcs.api.NpcManager;
import de.oliver.fancynpcs.api.actions.ActionTrigger;
import de.oliver.fancynpcs.api.actions.NpcAction;
import de.oliver.fancynpcs.api.actions.types.UnknownActionAction;
import de.oliver.fancynpcs.api.data.property.NpcVisibility;
import de.oliver.fancynpcs.api.events.NpcsLoadedEvent;
import de.oliver.fancynpcs.api.skins.SkinData;
import de.oliver.fancynpcs.api.skins.SkinLoadException;
import de.oliver.fancynpcs.api.utils.NpcEquipmentSlot;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.ApiStatus;

public class NpcManagerImpl implements NpcManager {
   private final JavaPlugin plugin;
   private final ExtendedFancyLogger logger;
   private final Function<NpcData, Npc> npcAdapter;
   private final File npcConfigFile;
   private final Map<String, Npc> npcs;
   private boolean isLoaded;

   public NpcManagerImpl(JavaPlugin plugin, Function<NpcData, Npc> npcAdapter) {
      this.plugin = plugin;
      this.logger = FancyNpcs.getInstance().getFancyLogger();
      this.npcAdapter = npcAdapter;
      this.npcs = new ConcurrentHashMap<>();
      this.npcConfigFile = new File("plugins" + File.separator + "FancyNpcs" + File.separator + "npcs.yml");
      this.isLoaded = false;
   }

   @Override
   public void registerNpc(Npc npc) {
      if (!FancyNpcs.PLAYER_NPCS_FEATURE_FLAG.isEnabled()
         && this.getAllNpcs().stream().anyMatch(npc1 -> npc1.getData().getName().equals(npc.getData().getName()))) {
         throw new IllegalStateException("An NPC with this name already exists");
      } else {
         this.npcs.put(npc.getData().getId(), npc);
      }
   }

   @Override
   public void removeNpc(Npc npc) {
      this.npcs.remove(npc.getData().getId());
      YamlConfiguration npcConfig = YamlConfiguration.loadConfiguration(this.npcConfigFile);
      npcConfig.set("npcs." + npc.getData().getId(), null);

      try {
         npcConfig.save(this.npcConfigFile);
      } catch (IOException var4) {
         this.logger.error("Could not save npc config file", ThrowableProperty.of(var4));
      }
   }

   @ApiStatus.Internal
   @Override
   public Npc getNpc(int entityId) {
      for (Npc npc : this.getAllNpcs()) {
         if (npc.getEntityId() == entityId) {
            return npc;
         }
      }

      return null;
   }

   @Override
   public Npc getNpc(String name) {
      for (Npc npc : this.getAllNpcs()) {
         if (npc.getData().getName().equalsIgnoreCase(name)) {
            return npc;
         }
      }

      return null;
   }

   @Override
   public Npc getNpcById(String id) {
      for (Npc npc : this.getAllNpcs()) {
         if (npc.getData().getId().equals(id)) {
            return npc;
         }
      }

      return null;
   }

   @Override
   public Npc getNpc(String name, UUID creator) {
      for (Npc npc : this.getAllNpcs()) {
         if (npc.getData().getCreator().equals(creator) && npc.getData().getName().equalsIgnoreCase(name)) {
            return npc;
         }
      }

      return null;
   }

   @Override
   public Collection<Npc> getAllNpcs() {
      return new ArrayList<>(this.npcs.values());
   }

   @Override
   public void saveNpcs(boolean force) {
      if (this.isLoaded) {
         if (!this.npcConfigFile.exists()) {
            try {
               this.npcConfigFile.createNewFile();
            } catch (IOException var14) {
               this.logger.error("Could not create npc config file", ThrowableProperty.of(var14));
               return;
            }
         }

         YamlConfiguration npcConfig = YamlConfiguration.loadConfiguration(this.npcConfigFile);

         for (Npc npc : this.getAllNpcs()) {
            if (npc.isSaveToFile()) {
               boolean shouldSave = force || npc.isDirty();
               if (shouldSave) {
                  NpcData data = npc.getData();
                  npcConfig.set("npcs." + data.getId() + ".name", data.getName());
                  npcConfig.set("npcs." + data.getId() + ".creator", data.getCreator().toString());
                  npcConfig.set("npcs." + data.getId() + ".displayName", data.getDisplayName());
                  npcConfig.set("npcs." + data.getId() + ".type", data.getType().name());
                  npcConfig.set("npcs." + data.getId() + ".location.world", data.getLocation().getWorld().getName());
                  npcConfig.set("npcs." + data.getId() + ".location.x", data.getLocation().getX());
                  npcConfig.set("npcs." + data.getId() + ".location.y", data.getLocation().getY());
                  npcConfig.set("npcs." + data.getId() + ".location.z", data.getLocation().getZ());
                  npcConfig.set("npcs." + data.getId() + ".location.yaw", data.getLocation().getYaw());
                  npcConfig.set("npcs." + data.getId() + ".location.pitch", data.getLocation().getPitch());
                  npcConfig.set("npcs." + data.getId() + ".showInTab", data.isShowInTab());
                  npcConfig.set("npcs." + data.getId() + ".spawnEntity", data.isSpawnEntity());
                  npcConfig.set("npcs." + data.getId() + ".collidable", data.isCollidable());
                  npcConfig.set("npcs." + data.getId() + ".glowing", data.isGlowing());
                  npcConfig.set("npcs." + data.getId() + ".glowingColor", data.getGlowingColor().toString());
                  npcConfig.set("npcs." + data.getId() + ".turnToPlayer", data.isTurnToPlayer());
                  npcConfig.set("npcs." + data.getId() + ".turnToPlayerDistance", data.getTurnToPlayerDistance());
                  npcConfig.set("npcs." + data.getId() + ".messages", null);
                  npcConfig.set("npcs." + data.getId() + ".playerCommands", null);
                  npcConfig.set("npcs." + data.getId() + ".serverCommands", null);
                  npcConfig.set("npcs." + data.getId() + ".sendMessagesRandomly", null);
                  npcConfig.set("npcs." + data.getId() + ".interactionCooldown", data.getInteractionCooldown());
                  npcConfig.set("npcs." + data.getId() + ".scale", data.getScale());
                  npcConfig.set("npcs." + data.getId() + ".visibility_distance", data.getVisibilityDistance());
                  npcConfig.set("npcs." + data.getId() + ".visibility", data.getVisibility().name());
                  if (data.getSkinData() != null) {
                     npcConfig.set("npcs." + data.getId() + ".skin.identifier", data.getSkinData().getIdentifier());
                     npcConfig.set("npcs." + data.getId() + ".skin.variant", data.getSkinData().getVariant().name());
                  } else {
                     npcConfig.set("npcs." + data.getId() + ".skin.identifier", null);
                  }

                  npcConfig.set("npcs." + data.getId() + ".skin.mirrorSkin", data.isMirrorSkin());
                  if (data.getEquipment() != null) {
                     for (Entry<NpcEquipmentSlot, ItemStack> entry : data.getEquipment().entrySet()) {
                        npcConfig.set("npcs." + data.getId() + ".equipment." + entry.getKey().name(), entry.getValue());
                     }
                  }

                  for (NpcAttribute attribute : FancyNpcs.getInstance().getAttributeManager().getAllAttributesForEntityType(data.getType())) {
                     String value = data.getAttributes().getOrDefault(attribute, null);
                     npcConfig.set("npcs." + data.getId() + ".attributes." + attribute.getName(), value);
                  }

                  npcConfig.set("npcs." + data.getId() + ".actions", null);

                  for (Entry<ActionTrigger, List<NpcAction.NpcActionData>> entry : npc.getData().getActions().entrySet()) {
                     for (NpcAction.NpcActionData actionData : entry.getValue()) {
                        if (actionData != null) {
                           if (actionData.action() instanceof UnknownActionAction unknownActionAction) {
                              npcConfig.set(
                                 "npcs." + data.getId() + ".actions." + entry.getKey().name() + "." + actionData.order() + ".action",
                                 unknownActionAction.getUnknownActionName()
                              );
                              npcConfig.set(
                                 "npcs." + data.getId() + ".actions." + entry.getKey().name() + "." + actionData.order() + ".value",
                                 unknownActionAction.getUnknownActionValue()
                              );
                           } else {
                              npcConfig.set(
                                 "npcs." + data.getId() + ".actions." + entry.getKey().name() + "." + actionData.order() + ".action",
                                 actionData.action().getName()
                              );
                              npcConfig.set(
                                 "npcs." + data.getId() + ".actions." + entry.getKey().name() + "." + actionData.order() + ".value", actionData.value()
                              );
                           }
                        }
                     }
                  }

                  npc.setDirty(false);
               }
            }
         }

         try {
            npcConfig.save(this.npcConfigFile);
         } catch (IOException var13) {
            var13.printStackTrace();
         }
      }
   }

   @Override
   public void loadNpcs() {
      this.npcs.clear();
      YamlConfiguration npcConfig = YamlConfiguration.loadConfiguration(this.npcConfigFile);
      if (!npcConfig.isConfigurationSection("npcs")) {
         this.setLoaded();
      } else {
         for (String id : npcConfig.getConfigurationSection("npcs").getKeys(false)) {
            String name = npcConfig.getString("npcs." + id + ".name");
            if (name == null) {
               name = id;
            }

            String creatorStr = npcConfig.getString("npcs." + id + ".creator");
            UUID creator = creatorStr == null ? null : UUID.fromString(creatorStr);
            String displayName = npcConfig.getString("npcs." + id + ".displayName", "<empty>");
            EntityType type = EntityType.valueOf(npcConfig.getString("npcs." + id + ".type", "PLAYER").toUpperCase());
            Location location = null;

            try {
               location = npcConfig.getLocation("npcs." + id + ".location");
            } catch (Exception var36) {
               this.logger.warn("Could not load location for npc '" + id + "'");
            }

            if (location == null) {
               String worldName = npcConfig.getString("npcs." + id + ".location.world");
               World world = Bukkit.getWorld(worldName);
               if (world == null) {
                  File worldFolder = new File(worldName);
                  if (worldFolder.exists() && worldFolder.isDirectory()) {
                     world = !ServerSoftware.isFolia() ? new WorldCreator(worldName).createWorld() : null;
                  }
               }

               if (world == null) {
                  this.logger.info("Could not load npc '" + id + "', because the world '" + worldName + "' is not loaded");
                  continue;
               }

               double x = npcConfig.getDouble("npcs." + id + ".location.x");
               double y = npcConfig.getDouble("npcs." + id + ".location.y");
               double z = npcConfig.getDouble("npcs." + id + ".location.z");
               float yaw = (float)npcConfig.getDouble("npcs." + id + ".location.yaw");
               float pitch = (float)npcConfig.getDouble("npcs." + id + ".location.pitch");
               location = new Location(world, x, y, z, yaw, pitch);
            }

            SkinData skin = null;
            String skinIdentifier = npcConfig.getString("npcs." + id + ".skin.identifier", npcConfig.getString("npcs." + id + ".skin.uuid", ""));
            String skinVariantStr = npcConfig.getString("npcs." + id + ".skin.variant", SkinData.SkinVariant.AUTO.name());
            SkinData.SkinVariant skinVariant = SkinData.SkinVariant.valueOf(skinVariantStr);
            if (!skinIdentifier.isEmpty()) {
               try {
                  skin = FancyNpcs.getInstance().getSkinManagerImpl().getByIdentifier(skinIdentifier, skinVariant);
                  skin.setIdentifier(skinIdentifier);
               } catch (SkinLoadException var37) {
                  this.logger.error("NPC named '" + name + "' identified by '" + id + "' could not have their skin loaded.");
                  this.logger.error("  " + var37.getReason() + " " + var37.getMessage());
               }
            }

            if (npcConfig.isSet("npcs." + id + ".skin.value") && npcConfig.isSet("npcs." + id + ".skin.signature")) {
               this.takeBackup(npcConfig);
               String value = npcConfig.getString("npcs." + id + ".skin.value");
               String signature = npcConfig.getString("npcs." + id + ".skin.signature");
               if (value != null && !value.isEmpty() && signature != null && !signature.isEmpty()) {
                  SkinData oldSkin = new SkinData(skinIdentifier, SkinData.SkinVariant.AUTO, value, signature);
                  FancyNpcs.getInstance().getSkinManagerImpl().getFileCache().addSkin(oldSkin);
                  FancyNpcs.getInstance().getSkinManagerImpl().getMemCache().addSkin(oldSkin);
               }
            }

            boolean mirrorSkin = npcConfig.getBoolean("npcs." + id + ".skin.mirrorSkin");
            boolean showInTab = npcConfig.getBoolean("npcs." + id + ".showInTab");
            boolean spawnEntity = npcConfig.getBoolean("npcs." + id + ".spawnEntity");
            boolean collidable = npcConfig.getBoolean("npcs." + id + ".collidable", true);
            boolean glowing = npcConfig.getBoolean("npcs." + id + ".glowing");
            NamedTextColor glowingColor = (NamedTextColor)NamedTextColor.NAMES.value(npcConfig.getString("npcs." + id + ".glowingColor", "white"));
            boolean turnToPlayer = npcConfig.getBoolean("npcs." + id + ".turnToPlayer");
            int turnToPlayerDistance = npcConfig.getInt("npcs." + id + ".turnToPlayerDistance", -1);
            Map<ActionTrigger, List<NpcAction.NpcActionData>> actions = new ConcurrentHashMap<>();
            ConfigurationSection actiontriggerSection = npcConfig.getConfigurationSection("npcs." + id + ".actions");
            if (actiontriggerSection != null) {
               actiontriggerSection.getKeys(false).forEach(trigger -> {
                  ActionTrigger actionTrigger = ActionTrigger.getByName(trigger);
                  if (actionTrigger == null) {
                     this.logger.warn("Could not find action trigger: " + trigger);
                  } else {
                     List<NpcAction.NpcActionData> actionList = new ArrayList<>();
                     ConfigurationSection actionsSection = npcConfig.getConfigurationSection("npcs." + id + ".actions." + trigger);
                     if (actionsSection != null) {
                        actionsSection.getKeys(false).forEach(order -> {
                           String actionName = npcConfig.getString("npcs." + id + ".actions." + trigger + "." + order + ".action");
                           String value = npcConfig.getString("npcs." + id + ".actions." + trigger + "." + order + ".value");
                           NpcAction action = FancyNpcs.getInstance().getActionManager().getActionByName(actionName);
                           if (action == null) {
                              this.logger.warn("Could not find action: " + actionName);
                              action = new UnknownActionAction(actionTrigger, actionName, value, Integer.parseInt(order));
                           }

                           try {
                              actionList.add(new NpcAction.NpcActionData(Integer.parseInt(order), action, value));
                           } catch (NumberFormatException var11x) {
                              this.logger.warn("Could not parse order: " + order);
                           }
                        });
                        actions.put(actionTrigger, actionList);
                     }
                  }
               });
            }

            float interactionCooldown = (float)npcConfig.getDouble("npcs." + id + ".interactionCooldown", 0.0);
            float scale = (float)npcConfig.getDouble("npcs." + id + ".scale", 1.0);
            int visibilityDistance = npcConfig.getInt("npcs." + id + ".visibility_distance", -1);
            String visibilityStr = npcConfig.getString("npcs." + id + ".visibility", "ALL");
            NpcVisibility visibility = NpcVisibility.byString(visibilityStr).orElse(NpcVisibility.ALL);
            Map<NpcAttribute, String> attributes = new HashMap<>();
            if (npcConfig.isConfigurationSection("npcs." + id + ".attributes")) {
               for (String attrName : npcConfig.getConfigurationSection("npcs." + id + ".attributes").getKeys(false)) {
                  NpcAttribute attribute = FancyNpcs.getInstance().getAttributeManager().getAttributeByName(type, attrName);
                  if (attribute == null) {
                     this.logger.warn("Could not find attribute: " + attrName);
                  } else {
                     String value = npcConfig.getString("npcs." + id + ".attributes." + attrName);
                     if (!attribute.isValidValue(value)) {
                        this.logger.warn("Invalid value for attribute: " + attrName);
                     } else {
                        attributes.put(attribute, value);
                     }
                  }
               }
            }

            NpcData data = new NpcData(
               id,
               name,
               creator,
               displayName,
               skin,
               location,
               showInTab,
               spawnEntity,
               collidable,
               glowing,
               glowingColor,
               type,
               new HashMap<>(),
               turnToPlayer,
               turnToPlayerDistance,
               null,
               actions,
               interactionCooldown,
               scale,
               visibilityDistance,
               attributes,
               mirrorSkin
            );
            Npc npc = this.npcAdapter.apply(data);
            if (npcConfig.isConfigurationSection("npcs." + id + ".equipment")) {
               for (String equipmentSlotStr : npcConfig.getConfigurationSection("npcs." + id + ".equipment").getKeys(false)) {
                  NpcEquipmentSlot equipmentSlot = NpcEquipmentSlot.parse(equipmentSlotStr);
                  ItemStack item = npcConfig.getItemStack("npcs." + id + ".equipment." + equipmentSlotStr);
                  npc.getData().addEquipment(equipmentSlot, item);
               }
            }

            npc.getData().setVisibility(visibility);
            npc.create();
            this.registerNpc(npc);
         }

         this.setLoaded();
      }
   }

   @Override
   public boolean isLoaded() {
      return this.isLoaded;
   }

   private void setLoaded() {
      this.isLoaded = true;
      new NpcsLoadedEvent().callEvent();
   }

   @Override
   public void reloadNpcs() {
      Collection<Npc> npcCopy = new ArrayList<>(this.getAllNpcs());
      this.npcs.clear();

      for (Npc npc : npcCopy) {
         npc.removeForAll();
      }

      this.loadNpcs();
   }

   private void takeBackup(YamlConfiguration npcConfig) {
      String folderPath = "plugins" + File.separator + "FancyNpcs" + File.separator + "/backups";
      File backupDir = new File(folderPath);
      if (!backupDir.exists()) {
         backupDir.mkdirs();
      }

      LocalDateTime now = LocalDateTime.now();
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
      String backupFileName = "npcs-" + formatter.format(now) + ".yml";
      File backupFile = new File(folderPath + File.separator + backupFileName);
      if (backupFile.exists()) {
         backupFile.delete();
      }

      try {
         backupFile.createNewFile();
      } catch (IOException var10) {
         this.logger.error("Could not create backup file for NPCs");
      }

      try {
         npcConfig.save(backupFile);
      } catch (IOException var9) {
         this.logger.error("Could not save backup file for NPCs");
      }
   }
}
