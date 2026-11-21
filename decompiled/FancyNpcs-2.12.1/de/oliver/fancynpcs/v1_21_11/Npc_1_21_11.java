package de.oliver.fancynpcs.v1_21_11;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JavaOps;
import de.oliver.fancylib.ReflectionUtils;
import de.oliver.fancynpcs.api.FancyNpcsPlugin;
import de.oliver.fancynpcs.api.Npc;
import de.oliver.fancynpcs.api.NpcAttribute;
import de.oliver.fancynpcs.api.NpcData;
import de.oliver.fancynpcs.api.events.NpcSpawnEvent;
import de.oliver.fancynpcs.api.utils.NpcEquipmentSlot;
import de.oliver.fancynpcs.libs.chatcolorhandler.paper.PaperColor;
import io.papermc.paper.adventure.PaperAdventure;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import net.kyori.adventure.text.Component;
import net.minecraft.Optionull;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.RemoteChatSession;
import net.minecraft.network.chat.RemoteChatSession.Data;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Action;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Entry;
import net.minecraft.network.syncher.SynchedEntityData.DataItem;
import net.minecraft.network.syncher.SynchedEntityData.DataValue;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.Display.TextDisplay;
import net.minecraft.world.entity.EntityType.EntityFactory;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team.CollisionRule;
import net.minecraft.world.scores.Team.Visibility;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.entity.Player;

public class Npc_1_21_11 extends Npc {
   private final String localName = this.generateLocalName();
   private final UUID uuid = UUID.randomUUID();
   private Entity npc;
   private TextDisplay sittingVehicle;

   public Npc_1_21_11(NpcData data) {
      super(data);
   }

   @Override
   public void create() {
      MinecraftServer minecraftServer = ((CraftServer)Bukkit.getServer()).getServer();
      ServerLevel serverLevel = ((CraftWorld)this.data.getLocation().getWorld()).getHandle();
      GameProfile gameProfile = new GameProfile(this.uuid, this.localName);
      if (this.data.getType() == org.bukkit.entity.EntityType.PLAYER) {
         this.npc = new ServerPlayer(minecraftServer, serverLevel, new GameProfile(this.uuid, ""), ClientInformation.createDefault());
         ((ServerPlayer)this.npc).gameProfile = gameProfile;
      } else {
         Optional<Reference<EntityType<?>>> entityTypeReference = BuiltInRegistries.ENTITY_TYPE
            .get(CraftNamespacedKey.toMinecraft(this.data.getType().getKey()));
         EntityType<?> nmsType = (EntityType<?>)entityTypeReference.get().value();
         EntityFactory factory = (EntityFactory)ReflectionUtils.getValue(nmsType, "factory");
         this.npc = factory.create(nmsType, serverLevel);
         this.isTeamCreated.clear();
      }
   }

   @Override
   public void spawn(Player player) {
      ServerPlayer serverPlayer = ((CraftPlayer)player).getHandle();
      if (this.npc != null) {
         if (this.data.getLocation().getWorld().getName().equalsIgnoreCase(serverPlayer.level().getWorld().getName())) {
            if (this.data.getSkinData() != null && this.data.getSkinData().hasTexture()) {
               String value = this.data.getSkinData().getTextureValue();
               String signature = this.data.getSkinData().getTextureSignature();
               PropertyMap propertyMap = new PropertyMap(ImmutableMultimap.of("textures", new Property("textures", value, signature)));
               Collection<Property> textures = ((ServerPlayer)this.npc).getGameProfile().properties().get("textures");
               if (textures.isEmpty()) {
                  ((ServerPlayer)this.npc).gameProfile = new GameProfile(this.uuid, this.localName, propertyMap);
               } else {
                  Property prop = textures.iterator().next();
                  if (!prop.value().equals(value)) {
                     ((ServerPlayer)this.npc).gameProfile = new GameProfile(this.uuid, this.localName, propertyMap);
                  }
               }
            }

            if (new NpcSpawnEvent(this, player).callEvent()) {
               List<Packet<? super ClientGamePacketListener>> packets = new ArrayList<>();
               if (this.npc instanceof ServerPlayer npcPlayer) {
                  EnumSet<Action> actions = EnumSet.noneOf(Action.class);
                  actions.add(Action.ADD_PLAYER);
                  actions.add(Action.UPDATE_DISPLAY_NAME);
                  if (this.data.isShowInTab()) {
                     actions.add(Action.UPDATE_LISTED);
                  }

                  ClientboundPlayerInfoUpdatePacket playerInfoPacket = new ClientboundPlayerInfoUpdatePacket(actions, this.getEntry(npcPlayer, serverPlayer));
                  packets.add(playerInfoPacket);
                  if (this.data.isSpawnEntity()) {
                     this.npc.setPos(this.data.getLocation().x(), this.data.getLocation().y(), this.data.getLocation().z());
                  }
               }

               ClientboundAddEntityPacket addEntityPacket = new ClientboundAddEntityPacket(
                  this.npc.getId(),
                  this.npc.getUUID(),
                  this.data.getLocation().x(),
                  this.data.getLocation().y(),
                  this.data.getLocation().z(),
                  this.data.getLocation().getPitch(),
                  this.data.getLocation().getYaw(),
                  this.npc.getType(),
                  0,
                  Vec3.ZERO,
                  this.data.getLocation().getYaw()
               );
               packets.add(addEntityPacket);
               this.isVisibleForPlayer.put(player.getUniqueId(), true);
               int removeNpcsFromPlayerlistDelay = FancyNpcsPlugin.get().getFancyNpcConfig().getRemoveNpcsFromPlayerlistDelay();
               if (!this.data.isShowInTab() && removeNpcsFromPlayerlistDelay > 0) {
                  FancyNpcsPlugin.get().getNpcThread().schedule(() -> {
                     ClientboundPlayerInfoRemovePacket playerInfoRemovePacket = new ClientboundPlayerInfoRemovePacket(List.of(this.npc.getUUID()));
                     this.runOnPlayerScheduler(serverPlayer.getBukkitEntity(), () -> serverPlayer.connection.send(playerInfoRemovePacket));
                  }, (long)removeNpcsFromPlayerlistDelay, TimeUnit.MILLISECONDS);
               }

               ClientboundBundlePacket bundlePacket = new ClientboundBundlePacket(packets);
               this.runOnPlayerScheduler(serverPlayer.getBukkitEntity(), () -> serverPlayer.connection.send(bundlePacket));
               this.update(player);
            }
         }
      }
   }

   @Override
   public void remove(Player player) {
      if (this.npc != null) {
         ServerPlayer serverPlayer = ((CraftPlayer)player).getHandle();
         if (this.npc instanceof ServerPlayer npcPlayer) {
            ClientboundPlayerInfoRemovePacket playerInfoRemovePacket = new ClientboundPlayerInfoRemovePacket(List.of(npcPlayer.getUUID()));
            this.runOnPlayerScheduler(serverPlayer.getBukkitEntity(), () -> serverPlayer.connection.send(playerInfoRemovePacket));
         }

         ClientboundRemoveEntitiesPacket removeEntitiesPacket = new ClientboundRemoveEntitiesPacket(new int[]{this.npc.getId()});
         this.runOnPlayerScheduler(serverPlayer.getBukkitEntity(), () -> serverPlayer.connection.send(removeEntitiesPacket));
         if (this.sittingVehicle != null) {
            ClientboundRemoveEntitiesPacket removeSittingVehiclePacket = new ClientboundRemoveEntitiesPacket(new int[]{this.sittingVehicle.getId()});
            this.runOnPlayerScheduler(serverPlayer.getBukkitEntity(), () -> serverPlayer.connection.send(removeSittingVehiclePacket));
         }

         this.isVisibleForPlayer.put(serverPlayer.getUUID(), false);
      }
   }

   @Override
   public void lookAt(Player player, Location location) {
      if (this.npc != null) {
         ServerPlayer serverPlayer = ((CraftPlayer)player).getHandle();
         this.npc.setRot(location.getYaw(), location.getPitch());
         this.npc.setYHeadRot(location.getYaw());
         this.npc.setXRot(location.getPitch());
         this.npc.setYRot(location.getYaw());
         ClientboundTeleportEntityPacket teleportEntityPacket = new ClientboundTeleportEntityPacket(
            this.npc.getId(),
            new PositionMoveRotation(
               new Vec3(this.data.getLocation().getX(), this.data.getLocation().getY(), this.data.getLocation().getZ()),
               Vec3.ZERO,
               location.getYaw(),
               location.getPitch()
            ),
            Set.of(),
            false
         );
         this.runOnPlayerScheduler(serverPlayer.getBukkitEntity(), () -> serverPlayer.connection.send(teleportEntityPacket));
         float angelMultiplier = 0.7111111F;
         ClientboundRotateHeadPacket rotateHeadPacket = new ClientboundRotateHeadPacket(this.npc, (byte)(location.getYaw() * angelMultiplier));
         this.runOnPlayerScheduler(serverPlayer.getBukkitEntity(), () -> serverPlayer.connection.send(rotateHeadPacket));
      }
   }

   @Override
   public void update(Player player, boolean swingArm) {
      if (this.npc != null) {
         if (this.isVisibleForPlayer.getOrDefault(player.getUniqueId(), false)) {
            ServerPlayer serverPlayer = ((CraftPlayer)player).getHandle();
            List<Packet<? super ClientGamePacketListener>> packets = new ArrayList<>();
            PlayerTeam team = new PlayerTeam(new Scoreboard(), "npc-" + this.localName);
            team.getPlayers().clear();
            team.getPlayers().add(this.npc instanceof ServerPlayer npcPlayer ? npcPlayer.getGameProfile().name() : this.npc.getStringUUID());
            team.setColor(PaperAdventure.asVanilla(this.data.getGlowingColor()));
            if (!this.data.isCollidable()) {
               team.setCollisionRule(CollisionRule.NEVER);
            }

            Component displayName = PaperColor.handler().translate(this.data.getDisplayName(), serverPlayer.getBukkitEntity());
            net.minecraft.network.chat.Component vanillaComponent = PaperAdventure.asVanilla(displayName);

            try {
               ComponentSerialization.CODEC.encodeStart(JavaOps.INSTANCE, vanillaComponent);
            } catch (Exception var16) {
               vanillaComponent = net.minecraft.network.chat.Component.literal("Invalid displayname (check MiniMessage syntax)");
            }

            if (!(this.npc instanceof ServerPlayer)) {
               this.npc.setCustomName(vanillaComponent);
               this.npc.setCustomNameVisible(true);
            } else {
               this.npc.setCustomName(null);
               this.npc.setCustomNameVisible(false);
            }

            if (this.data.getDisplayName().equalsIgnoreCase("<empty>")) {
               team.setNameTagVisibility(Visibility.NEVER);
               this.npc.setCustomName(null);
               this.npc.setCustomNameVisible(false);
            } else {
               team.setNameTagVisibility(Visibility.ALWAYS);
            }

            if (this.npc instanceof ServerPlayer npcPlayerx) {
               team.setPlayerPrefix(vanillaComponent);
               npcPlayerx.listName = vanillaComponent;
               EnumSet<Action> actions = EnumSet.noneOf(Action.class);
               actions.add(Action.UPDATE_DISPLAY_NAME);
               if (this.data.isShowInTab()) {
                  actions.add(Action.UPDATE_LISTED);
               }

               ClientboundPlayerInfoUpdatePacket playerInfoPacket = new ClientboundPlayerInfoUpdatePacket(actions, this.getEntry(npcPlayerx, serverPlayer));
               packets.add(playerInfoPacket);
            }

            boolean isTeamCreatedForPlayer = this.isTeamCreated.getOrDefault(player.getUniqueId(), false);
            packets.add(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, !isTeamCreatedForPlayer));
            this.isTeamCreated.put(player.getUniqueId(), true);
            this.npc.setGlowingTag(this.data.isGlowing());
            this.data.applyAllAttributes(this);
            List<Pair<EquipmentSlot, ItemStack>> equipmentList = new ArrayList<>();
            if (this.data.getEquipment() != null) {
               for (NpcEquipmentSlot slot : this.data.getEquipment().keySet()) {
                  equipmentList.add(new Pair(EquipmentSlot.byName(slot.toNmsName()), CraftItemStack.asNMSCopy(this.data.getEquipment().get(slot))));
               }
            }

            if (this.npc instanceof LivingEntity livingEntity) {
               ItemStack bodySlot = livingEntity.getItemBySlot(EquipmentSlot.BODY);
               if (!bodySlot.isEmpty()) {
                  equipmentList.add(new Pair(EquipmentSlot.BODY, bodySlot));
               }
            }

            if (!equipmentList.isEmpty()) {
               ClientboundSetEquipmentPacket setEquipmentPacket = new ClientboundSetEquipmentPacket(this.npc.getId(), equipmentList);
               packets.add(setEquipmentPacket);
            }

            if (this.npc instanceof ServerPlayer) {
               this.npc.getEntityData().set(net.minecraft.world.entity.player.Player.DATA_PLAYER_MODE_CUSTOMISATION, (byte)127);
            }

            this.refreshEntityData(player);
            if (this.data.isSpawnEntity() && this.data.getLocation() != null) {
               this.move(player, swingArm);
            }

            NpcAttribute playerPoseAttr = FancyNpcsPlugin.get().getAttributeManager().getAttributeByName(org.bukkit.entity.EntityType.PLAYER, "pose");
            if (this.data.getAttributes().containsKey(playerPoseAttr)) {
               String pose = this.data.getAttributes().get(playerPoseAttr);
               if (pose.equals("sitting")) {
                  this.setSitting(serverPlayer);
               } else if (this.sittingVehicle != null) {
                  ClientboundRemoveEntitiesPacket removeSittingVehiclePacket = new ClientboundRemoveEntitiesPacket(new int[]{this.sittingVehicle.getId()});
                  packets.add(removeSittingVehiclePacket);
               }
            }

            if (this.npc instanceof LivingEntity) {
               NpcAttribute customModelAttr = FancyNpcsPlugin.get()
                  .getAttributeManager()
                  .getAttributeByName(org.bukkit.entity.EntityType.PLAYER, "custom_model");
               boolean hasCustomModel = customModelAttr != null && this.data.getAttributes().containsKey(customModelAttr);
               List<AttributeInstance> changedAttributes = new ArrayList<>();
               if (!hasCustomModel) {
                  Reference<Attribute> scaleAttribute = (Reference<Attribute>)BuiltInRegistries.ATTRIBUTE.get(Identifier.parse("minecraft:scale")).get();
                  AttributeInstance attributeInstance = new AttributeInstance(scaleAttribute, var0 -> {});
                  attributeInstance.setBaseValue(this.data.getScale());
                  changedAttributes.add(attributeInstance);
               }

               if (!changedAttributes.isEmpty()) {
                  ClientboundUpdateAttributesPacket updateAttributesPacket = new ClientboundUpdateAttributesPacket(this.npc.getId(), changedAttributes);
                  packets.add(updateAttributesPacket);
               }
            }

            ClientboundBundlePacket bundlePacket = new ClientboundBundlePacket(packets);
            this.runOnPlayerScheduler(serverPlayer.getBukkitEntity(), () -> serverPlayer.connection.send(bundlePacket));
         }
      }
   }

   @Override
   protected void refreshEntityData(Player player) {
      if (this.isVisibleForPlayer.getOrDefault(player.getUniqueId(), false)) {
         ServerPlayer serverPlayer = ((CraftPlayer)player).getHandle();
         DataItem<?>[] itemsById = (DataItem<?>[])ReflectionUtils.getValue(this.npc.getEntityData(), "itemsById");
         List<DataValue<?>> entityData = new ArrayList<>();

         for (DataItem<?> dataItem : itemsById) {
            entityData.add(dataItem.value());
         }

         ClientboundSetEntityDataPacket setEntityDataPacket = new ClientboundSetEntityDataPacket(this.npc.getId(), entityData);
         this.runOnPlayerScheduler(serverPlayer.getBukkitEntity(), () -> serverPlayer.connection.send(setEntityDataPacket));
      }
   }

   @Override
   public void move(Player player, boolean swingArm) {
      if (this.npc != null) {
         ServerPlayer serverPlayer = ((CraftPlayer)player).getHandle();
         this.npc.setPosRaw(this.data.getLocation().x(), this.data.getLocation().y(), this.data.getLocation().z());
         this.npc.setRot(this.data.getLocation().getYaw(), this.data.getLocation().getPitch());
         this.npc.setYHeadRot(this.data.getLocation().getYaw());
         this.npc.setXRot(this.data.getLocation().getPitch());
         this.npc.setYRot(this.data.getLocation().getYaw());
         ClientboundTeleportEntityPacket teleportEntityPacket = new ClientboundTeleportEntityPacket(
            this.npc.getId(),
            new PositionMoveRotation(
               new Vec3(this.data.getLocation().getX(), this.data.getLocation().getY(), this.data.getLocation().getZ()),
               Vec3.ZERO,
               this.data.getLocation().getYaw(),
               this.data.getLocation().getPitch()
            ),
            Set.of(),
            false
         );
         this.runOnPlayerScheduler(serverPlayer.getBukkitEntity(), () -> serverPlayer.connection.send(teleportEntityPacket));
         float angelMultiplier = 0.7111111F;
         ClientboundRotateHeadPacket rotateHeadPacket = new ClientboundRotateHeadPacket(this.npc, (byte)(this.data.getLocation().getYaw() * angelMultiplier));
         this.runOnPlayerScheduler(serverPlayer.getBukkitEntity(), () -> serverPlayer.connection.send(rotateHeadPacket));
         if (swingArm && this.npc instanceof ServerPlayer) {
            ClientboundAnimatePacket animatePacket = new ClientboundAnimatePacket(this.npc, 0);
            this.runOnPlayerScheduler(serverPlayer.getBukkitEntity(), () -> serverPlayer.connection.send(animatePacket));
         }
      }
   }

   private Entry getEntry(ServerPlayer npcPlayer, ServerPlayer viewer) {
      GameProfile profile;
      if (!this.data.isMirrorSkin()) {
         profile = new GameProfile(npcPlayer.getGameProfile().id(), npcPlayer.getGameProfile().name(), npcPlayer.getGameProfile().properties());
      } else {
         Property textures = (Property)viewer.getGameProfile().properties().get("textures").iterator().next();
         PropertyMap propertyMap = new PropertyMap(ImmutableMultimap.of("textures", new Property("textures", textures.value(), textures.signature())));
         profile = new GameProfile(this.uuid, this.localName, propertyMap);
      }

      return new Entry(
         npcPlayer.getUUID(),
         profile,
         this.data.isShowInTab(),
         0,
         npcPlayer.gameMode.getGameModeForPlayer(),
         npcPlayer.getTabListDisplayName(),
         true,
         -1,
         (Data)Optionull.map(npcPlayer.getChatSession(), RemoteChatSession::asData)
      );
   }

   public void setSitting(ServerPlayer serverPlayer) {
      if (this.npc != null) {
         if (this.sittingVehicle == null) {
            this.sittingVehicle = new TextDisplay(EntityType.TEXT_DISPLAY, ((CraftWorld)this.data.getLocation().getWorld()).getHandle());
         }

         this.sittingVehicle.setPos(this.data.getLocation().x(), this.data.getLocation().y(), this.data.getLocation().z());
         ServerEntity serverEntity = new ServerEntity(serverPlayer.level(), this.sittingVehicle, 0, false, FakeSynchronizer.INSTANCE, Set.of());
         ClientboundAddEntityPacket addEntityPacket = new ClientboundAddEntityPacket(this.sittingVehicle, serverEntity);
         this.runOnPlayerScheduler(serverPlayer.getBukkitEntity(), () -> serverPlayer.connection.send(addEntityPacket));
         this.sittingVehicle.passengers = ImmutableList.of(this.npc);
         ClientboundSetPassengersPacket packet = new ClientboundSetPassengersPacket(this.sittingVehicle);
         this.runOnPlayerScheduler(serverPlayer.getBukkitEntity(), () -> serverPlayer.connection.send(packet));
      }
   }

   @Override
   public float getEyeHeight() {
      return this.npc.getEyeHeight();
   }

   @Override
   public int getEntityId() {
      return this.npc.getId();
   }

   public Entity getNpc() {
      return this.npc;
   }
}
