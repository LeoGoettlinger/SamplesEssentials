package com.github.retrooper.packetevents.protocol.npc;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityHeadLook;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRelativeMove;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRelativeMoveAndRotation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRotation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityTeleport;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfo;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoRemove;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnPlayer;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.Nullable;

public class NPC {
   private final int id;
   private final UserProfile profile;
   private GameMode gamemode;
   private Component tabName;
   private NamedTextColor nameColor;
   private Component prefixName;
   private Component suffixName;
   private String teamName;
   private int displayPing = 0;
   private Location location = new Location(0.0, 0.0, 0.0, 0.0F, 0.0F);
   private ItemStack mainHand = null;
   private ItemStack offHand = null;
   private ItemStack helmet = null;
   private ItemStack chestPlate = null;
   private ItemStack leggings = null;
   private ItemStack boots = null;
   private final Set<Object> channels = new HashSet<>();

   public NPC(
      UserProfile profile,
      int entityId,
      GameMode gamemode,
      @Nullable Component tabName,
      @Nullable NamedTextColor nameColor,
      @Nullable Component prefixName,
      @Nullable Component suffixName
   ) {
      this.profile = profile;
      this.id = entityId;
      this.gamemode = gamemode;
      this.tabName = tabName;
      this.nameColor = nameColor;
      this.prefixName = prefixName;
      this.suffixName = suffixName;
      this.teamName = "npc-" + this.id;
   }

   public NPC(UserProfile profile, int entityId, @Nullable Component tabName) {
      this(profile, entityId, GameMode.SURVIVAL, tabName, null, null, null);
   }

   public NPC(UserProfile profile, int entityId) {
      this(profile, entityId, null);
   }

   public boolean hasSpawned(Object channel) {
      return this.channels.contains(channel);
   }

   public void spawn(Object channel) {
      if (!this.hasSpawned(channel)) {
         PacketWrapper<?> playerInfo;
         if (PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_19_3)) {
            playerInfo = new WrapperPlayServerPlayerInfoUpdate(WrapperPlayServerPlayerInfoUpdate.Action.ADD_PLAYER, this.getModernPlayerInfoData());
         } else {
            playerInfo = new WrapperPlayServerPlayerInfo(WrapperPlayServerPlayerInfo.Action.ADD_PLAYER, this.getLegacyPlayerInfoData());
         }

         PacketEvents.getAPI().getProtocolManager().sendPacket(channel, playerInfo);
         PacketWrapper<?> spawnPacket;
         if (PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_20_2)) {
            spawnPacket = new WrapperPlayServerSpawnEntity(
               this.getId(), this.getProfile().getUUID(), EntityTypes.PLAYER, this.getLocation(), this.getLocation().getYaw(), 0, null
            );
         } else {
            spawnPacket = new WrapperPlayServerSpawnPlayer(this.getId(), this.getProfile().getUUID(), this.getLocation());
         }

         PacketEvents.getAPI().getProtocolManager().sendPacket(channel, spawnPacket);
         if (this.getNameColor() != null || this.getPrefixName() != null || this.getSuffixName() != null) {
            PacketEvents.getAPI().getProtocolManager().sendPacket(channel, (PacketWrapper<?>)this.generateTeamsData());
         }

         this.channels.add(channel);
      }
   }

   public void despawn(Object channel) {
      if (this.hasSpawned(channel)) {
         WrapperPlayServerDestroyEntities destroyEntities = new WrapperPlayServerDestroyEntities(this.getId());
         PacketEvents.getAPI().getProtocolManager().sendPacket(channel, (PacketWrapper<?>)destroyEntities);
         this.channels.remove(channel);
      }
   }

   public void despawnAll() {
      for (Object channel : this.channels) {
         WrapperPlayServerDestroyEntities destroyEntities = new WrapperPlayServerDestroyEntities(this.getId());
         PacketEvents.getAPI().getProtocolManager().sendPacket(channel, (PacketWrapper<?>)destroyEntities);
      }

      this.channels.clear();
   }

   public void teleport(Location to) {
      this.setLocation(to);

      for (Object channel : this.channels) {
         WrapperPlayServerEntityTeleport entityTeleport = new WrapperPlayServerEntityTeleport(this.getId(), to, true);
         PacketEvents.getAPI().getProtocolManager().sendPacket(channel, (PacketWrapper<?>)entityTeleport);
      }
   }

   public void updateLocation(Location to) {
      Location from = this.getLocation();
      this.setLocation(to);
      double distXAbs = Math.abs(to.getPosition().getX() - from.getPosition().getX());
      double distYAbs = Math.abs(to.getPosition().getY() - from.getPosition().getY());
      double distZAbs = Math.abs(to.getPosition().getZ() - from.getPosition().getZ());
      boolean shouldUseEntityTeleport = distXAbs > 8.0 || distYAbs > 8.0 || distZAbs > 8.0;

      for (Object channel : this.channels) {
         if (shouldUseEntityTeleport) {
            WrapperPlayServerEntityTeleport entityTeleport = new WrapperPlayServerEntityTeleport(this.getId(), to, true);
            PacketEvents.getAPI().getProtocolManager().sendPacket(channel, (PacketWrapper<?>)entityTeleport);
         } else {
            boolean rotationChanged = to.getYaw() != from.getYaw() || to.getPitch() != from.getPitch();
            boolean positionChanged = to.getPosition().getX() != from.getPosition().getX()
               || to.getPosition().getY() != from.getPosition().getY()
               || to.getPosition().getZ() != from.getPosition().getZ();
            double deltaX = positionChanged ? to.getPosition().getX() - from.getPosition().getX() : 0.0;
            double deltaY = positionChanged ? to.getPosition().getY() - from.getPosition().getY() : 0.0;
            double deltaZ = positionChanged ? to.getPosition().getZ() - from.getPosition().getZ() : 0.0;
            if (positionChanged && rotationChanged) {
               WrapperPlayServerEntityRelativeMoveAndRotation entityRelativeMoveAndRotation = new WrapperPlayServerEntityRelativeMoveAndRotation(
                  this.getId(), deltaX, deltaY, deltaZ, to.getYaw(), to.getPitch(), true
               );
               PacketEvents.getAPI().getProtocolManager().sendPacket(channel, (PacketWrapper<?>)entityRelativeMoveAndRotation);
               WrapperPlayServerEntityHeadLook headYaw = new WrapperPlayServerEntityHeadLook(this.getId(), to.getYaw());
               PacketEvents.getAPI().getProtocolManager().sendPacket(channel, (PacketWrapper<?>)headYaw);
            } else if (positionChanged) {
               WrapperPlayServerEntityRelativeMove entityRelativeMove = new WrapperPlayServerEntityRelativeMove(this.getId(), deltaX, deltaY, deltaZ, true);
               PacketEvents.getAPI().getProtocolManager().sendPacket(channel, (PacketWrapper<?>)entityRelativeMove);
            } else if (rotationChanged) {
               WrapperPlayServerEntityRotation entityRotation = new WrapperPlayServerEntityRotation(this.getId(), to.getYaw(), to.getPitch(), true);
               PacketEvents.getAPI().getProtocolManager().sendPacket(channel, (PacketWrapper<?>)entityRotation);
               WrapperPlayServerEntityHeadLook headYaw = new WrapperPlayServerEntityHeadLook(this.getId(), to.getYaw());
               PacketEvents.getAPI().getProtocolManager().sendPacket(channel, (PacketWrapper<?>)headYaw);
            }
         }
      }
   }

   public void updateRotation(float yaw, float pitch) {
      this.getLocation().setYaw(yaw);
      this.getLocation().setPitch(pitch);

      for (Object channel : this.channels) {
         WrapperPlayServerEntityRotation entityRotation = new WrapperPlayServerEntityRotation(this.getId(), yaw, pitch, true);
         PacketEvents.getAPI().getProtocolManager().sendPacket(channel, (PacketWrapper<?>)entityRotation);
         WrapperPlayServerEntityHeadLook headYaw = new WrapperPlayServerEntityHeadLook(this.getId(), yaw);
         PacketEvents.getAPI().getProtocolManager().sendPacket(channel, (PacketWrapper<?>)headYaw);
      }
   }

   public void updateTabPing(int ping) {
      this.setDisplayPing(ping);

      for (Object channel : this.channels) {
         PacketWrapper<?> playerInfo;
         if (PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_19_3)) {
            playerInfo = new WrapperPlayServerPlayerInfoUpdate(WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LATENCY, this.getModernPlayerInfoData());
         } else {
            playerInfo = new WrapperPlayServerPlayerInfo(WrapperPlayServerPlayerInfo.Action.UPDATE_LATENCY, this.getLegacyPlayerInfoData());
         }

         PacketEvents.getAPI().getProtocolManager().sendPacket(channel, playerInfo);
      }
   }

   public void updateGameMode(GameMode gamemode) {
      this.setGameMode(gamemode);

      for (Object channel : this.channels) {
         PacketWrapper<?> playerInfo;
         if (PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_19_3)) {
            playerInfo = new WrapperPlayServerPlayerInfoUpdate(WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_GAME_MODE, this.getModernPlayerInfoData());
         } else {
            playerInfo = new WrapperPlayServerPlayerInfo(WrapperPlayServerPlayerInfo.Action.UPDATE_GAME_MODE, this.getLegacyPlayerInfoData());
         }

         PacketEvents.getAPI().getProtocolManager().sendPacket(channel, playerInfo);
      }
   }

   public void changeSkin(UUID skinUUID, List<TextureProperty> skinTextureProperties) {
      for (Object channel : this.channels) {
         PacketWrapper<?> playerInfoRemove;
         if (PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_19_3)) {
            playerInfoRemove = new WrapperPlayServerPlayerInfoRemove(this.getProfile().getUUID());
         } else {
            playerInfoRemove = new WrapperPlayServerPlayerInfo(WrapperPlayServerPlayerInfo.Action.REMOVE_PLAYER, this.getLegacyPlayerInfoData());
         }

         PacketEvents.getAPI().getProtocolManager().sendPacket(channel, playerInfoRemove);
         WrapperPlayServerDestroyEntities destroyEntities = new WrapperPlayServerDestroyEntities(this.getId());
         PacketEvents.getAPI().getProtocolManager().sendPacket(channel, (PacketWrapper<?>)destroyEntities);
         this.getProfile().setTextureProperties(skinTextureProperties);
         this.getProfile().setUUID(skinUUID);
         PacketWrapper<?> playerInfoAdd;
         if (PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_19_3)) {
            playerInfoAdd = new WrapperPlayServerPlayerInfoUpdate(WrapperPlayServerPlayerInfoUpdate.Action.ADD_PLAYER, this.getModernPlayerInfoData());
         } else {
            playerInfoAdd = new WrapperPlayServerPlayerInfo(WrapperPlayServerPlayerInfo.Action.ADD_PLAYER, this.getLegacyPlayerInfoData());
         }

         PacketEvents.getAPI().getProtocolManager().sendPacket(channel, playerInfoAdd);
         PacketWrapper<?> spawnPacket;
         if (PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_20_2)) {
            spawnPacket = new WrapperPlayServerSpawnEntity(
               this.getId(), this.getProfile().getUUID(), EntityTypes.PLAYER, this.getLocation(), this.getLocation().getYaw(), 0, null
            );
         } else {
            spawnPacket = new WrapperPlayServerSpawnPlayer(this.getId(), this.getProfile().getUUID(), this.getLocation());
         }

         PacketEvents.getAPI().getProtocolManager().sendPacket(channel, spawnPacket);
      }
   }

   public void updateNameTag() {
      for (Object channel : this.channels) {
         WrapperPlayServerTeams removeTeam = new WrapperPlayServerTeams(this.teamName, WrapperPlayServerTeams.TeamMode.REMOVE, Optional.empty());
         PacketEvents.getAPI().getProtocolManager().sendPacket(channel, (PacketWrapper<?>)removeTeam);
         if (this.getNameColor() != null || this.getPrefixName() != null || this.getSuffixName() != null) {
            PacketEvents.getAPI().getProtocolManager().sendPacket(channel, (PacketWrapper<?>)this.generateTeamsData());
         }
      }
   }

   public void updateEquipment() {
      for (Object channel : this.channels) {
         List<Equipment> equipmentList = new ArrayList<>();
         ItemStack handItem = this.getMainHand();
         if (handItem == null) {
            handItem = ItemStack.EMPTY;
         }

         equipmentList.add(new Equipment(EquipmentSlot.MAIN_HAND, handItem));
         if (PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_9)) {
            ItemStack offHandItem = this.getOffHand();
            if (offHandItem == null) {
               offHandItem = ItemStack.EMPTY;
            }

            equipmentList.add(new Equipment(EquipmentSlot.OFF_HAND, offHandItem));
         }

         ItemStack helmetItem = this.getHelmet();
         if (helmetItem == null) {
            helmetItem = ItemStack.EMPTY;
         }

         equipmentList.add(new Equipment(EquipmentSlot.HELMET, helmetItem));
         ItemStack chestPlateItem = this.getChestplate();
         if (chestPlateItem == null) {
            chestPlateItem = ItemStack.EMPTY;
         }

         equipmentList.add(new Equipment(EquipmentSlot.CHEST_PLATE, chestPlateItem));
         ItemStack leggingsItem = this.getLeggings();
         if (leggingsItem == null) {
            leggingsItem = ItemStack.EMPTY;
         }

         equipmentList.add(new Equipment(EquipmentSlot.LEGGINGS, leggingsItem));
         ItemStack bootsItem = this.getBoots();
         if (bootsItem == null) {
            bootsItem = ItemStack.EMPTY;
         }

         equipmentList.add(new Equipment(EquipmentSlot.BOOTS, bootsItem));
         WrapperPlayServerEntityEquipment equipmentPacket = new WrapperPlayServerEntityEquipment(this.getId(), equipmentList);
         PacketEvents.getAPI().getProtocolManager().sendPacket(channel, (PacketWrapper<?>)equipmentPacket);
      }
   }

   public ItemStack getMainHand() {
      return this.mainHand;
   }

   public void setMainHand(ItemStack mainHand) {
      this.mainHand = mainHand;
   }

   public ItemStack getOffHand() {
      return this.offHand;
   }

   public void setOffHand(ItemStack offHand) {
      this.offHand = offHand;
   }

   public ItemStack getHelmet() {
      return this.helmet;
   }

   public void setHelmet(ItemStack helmet) {
      this.helmet = helmet;
   }

   public ItemStack getChestplate() {
      return this.chestPlate;
   }

   public void setChestplate(ItemStack chestPlate) {
      this.chestPlate = chestPlate;
   }

   public ItemStack getLeggings() {
      return this.leggings;
   }

   public void setLeggings(ItemStack leggings) {
      this.leggings = leggings;
   }

   public ItemStack getBoots() {
      return this.boots;
   }

   public void setBoots(ItemStack boots) {
      this.boots = boots;
   }

   @Nullable
   public NamedTextColor getNameColor() {
      return this.nameColor;
   }

   public void setNameColor(@Nullable NamedTextColor nameColor) {
      this.nameColor = nameColor;
   }

   @Nullable
   public Component getPrefixName() {
      return this.prefixName;
   }

   public void setPrefixName(@Nullable Component namePrefix) {
      this.prefixName = namePrefix;
   }

   @Nullable
   public Component getSuffixName() {
      return this.suffixName;
   }

   public void setSuffixName(@Nullable Component nameSuffix) {
      this.suffixName = nameSuffix;
   }

   public String getTeamName() {
      return this.teamName;
   }

   public void setTeamName(String teamName) {
      if (PacketEvents.getAPI().getServerManager().getVersion().isOlderThan(ServerVersion.V_1_18)) {
         this.teamName = teamName.substring(0, 16);
      } else {
         this.teamName = teamName;
      }
   }

   @Nullable
   public Component getTabName() {
      return this.tabName;
   }

   public void setTabName(@Nullable Component tabName) {
      this.tabName = tabName;
   }

   public int getId() {
      return this.id;
   }

   public UserProfile getProfile() {
      return this.profile;
   }

   public GameMode getGameMode() {
      return this.gamemode;
   }

   public void setGameMode(GameMode gamemode) {
      this.gamemode = gamemode;
   }

   private WrapperPlayServerTeams generateTeamsData() {
      return new WrapperPlayServerTeams(
         this.teamName,
         WrapperPlayServerTeams.TeamMode.CREATE,
         Optional.of(
            new WrapperPlayServerTeams.ScoreBoardTeamInfo(
               Component.text(this.teamName),
               this.prefixName,
               this.suffixName,
               WrapperPlayServerTeams.NameTagVisibility.ALWAYS,
               WrapperPlayServerTeams.CollisionRule.ALWAYS,
               this.nameColor,
               WrapperPlayServerTeams.OptionData.NONE
            )
         ),
         this.getProfile().getName()
      );
   }

   public WrapperPlayServerPlayerInfo.PlayerData getLegacyPlayerInfoData() {
      return new WrapperPlayServerPlayerInfo.PlayerData(this.getTabName(), this.getProfile(), this.getGameMode(), this.getDisplayPing());
   }

   public WrapperPlayServerPlayerInfoUpdate.PlayerInfo getModernPlayerInfoData() {
      return new WrapperPlayServerPlayerInfoUpdate.PlayerInfo(this.getProfile(), true, this.getDisplayPing(), this.getGameMode(), this.getTabName(), null);
   }

   public int getDisplayPing() {
      return this.displayPing;
   }

   public void setDisplayPing(int ping) {
      this.displayPing = ping;
   }

   public Location getLocation() {
      return this.location;
   }

   public void setLocation(Location location) {
      this.location = location;
   }

   public Set<Object> getChannels() {
      return this.channels;
   }
}
