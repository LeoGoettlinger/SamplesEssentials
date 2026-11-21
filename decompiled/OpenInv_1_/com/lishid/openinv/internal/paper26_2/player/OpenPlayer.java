package com.lishid.openinv.internal.paper26_2.player;

import com.lishid.openinv.event.OpenEvents;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.Util;
import net.minecraft.util.ProblemReporter.ScopedCollector;
import net.minecraft.world.level.storage.PlayerDataStorage;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueOutput;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.slf4j.Logger;

public class OpenPlayer extends CraftPlayer {
   @Unmodifiable
   protected static final Set<String> RESET_TAGS = Set.of(
      "CustomName",
      "CustomNameVisible",
      "Silent",
      "NoGravity",
      "Glowing",
      "TicksFrozen",
      "HasVisualFire",
      "Tags",
      "data",
      "Passengers",
      "warden_spawn_tracker",
      "entered_nether_pos",
      "last_explosion_impact_pos",
      "respawn",
      "raid_omen_position",
      "ender_pearls",
      "ShoulderEntityLeft",
      "ShoulderEntityRight",
      "LastDeathLocation",
      "current_explosion_impact_pos",
      "active_effects",
      "sleeping_pos",
      "Brain",
      "last_hurt_by_player",
      "last_hurt_by_player_memory_time",
      "last_hurt_by_mob",
      "ticks_since_last_hurt_by_mob",
      "equipment",
      "locator_bar_icon"
   );
   private final PlayerManager manager;

   protected OpenPlayer(CraftServer server, ServerPlayer entity, PlayerManager manager) {
      super(server, entity);
      this.manager = manager;
   }

   public void loadData() {
      this.manager.loadData(this.server.getServer(), this.getHandle());
   }

   public void saveData() {
      if (!OpenEvents.saveCancelled(this)) {
         ServerPlayer player = this.getHandle();
         Logger logger = LogUtils.getLogger();

         try {
            ScopedCollector scopedCollector = new ScopedCollector(player.problemPath(), logger);

            try {
               PlayerDataStorage worldNbtStorage = this.server.getServer().getPlayerList().playerIo;
               CompoundTag oldData = this.isOnline() ? null : (CompoundTag)worldNbtStorage.load(player.nameAndId()).orElse(null);
               CompoundTag playerData = this.getWritableTag(oldData);
               ValueOutput valueOutput = TagValueOutput.createWrappingWithContext(scopedCollector, player.registryAccess(), playerData);
               player.saveWithoutId(valueOutput);
               this.saveSafe(player, oldData, playerData, worldNbtStorage);
            } catch (Throwable var9) {
               try {
                  scopedCollector.close();
               } catch (Throwable var8) {
                  var9.addSuppressed(var8);
               }

               throw var9;
            }

            scopedCollector.close();
         } catch (Exception var10) {
            LogUtils.getLogger().warn("Failed to save player data for {}: {}", player.getScoreboardName(), var10);
         }
      }
   }

   protected void saveSafe(
      @NotNull ServerPlayer player, @Nullable CompoundTag oldData, @NotNull CompoundTag playerData, @NotNull PlayerDataStorage worldNbtStorage
   ) throws IOException {
      this.revertSpecialValues(playerData, oldData);
      Path playerDataDir = worldNbtStorage.getPlayerDir().toPath();
      Path tempFile = Files.createTempFile(playerDataDir, player.getStringUUID() + "-", ".dat");
      NbtIo.writeCompressed(playerData, tempFile);
      Path dataFile = playerDataDir.resolve(player.getStringUUID() + ".dat");
      Path backupFile = playerDataDir.resolve(player.getStringUUID() + ".dat_old");
      this.safeReplaceFile(dataFile, tempFile, backupFile);
   }

   protected void safeReplaceFile(@NotNull Path dataFile, @NotNull Path tempFile, @NotNull Path backupFile) {
      Util.safeReplaceFile(dataFile, tempFile, backupFile);
   }

   @Contract("null -> new")
   @NotNull
   protected CompoundTag getWritableTag(@Nullable CompoundTag oldData) {
      if (oldData == null) {
         return new CompoundTag();
      } else {
         oldData = oldData.copy();
         oldData.keySet().removeIf(key -> RESET_TAGS.contains(key) || key.startsWith("Bukkit") || key.startsWith("Paper") && key.length() > 5);
         return oldData;
      }
   }

   protected void revertSpecialValues(@NotNull CompoundTag newData, @Nullable CompoundTag oldData) {
      if (oldData != null) {
         this.copyValue(oldData, newData, "bukkit", "lastPlayed", NumericTag.class);
         this.copyValue(oldData, newData, "Paper", "LastSeen", NumericTag.class);
         this.copyValue(oldData, newData, "Paper", "LastLogin", NumericTag.class);
      }
   }

   private <T extends Tag> void copyValue(
      @NotNull CompoundTag source, @NotNull CompoundTag target, @NotNull String container, @NotNull String key, @NotNull Class<T> tagType
   ) {
      CompoundTag oldContainer = this.getTag(source, container, CompoundTag.class);
      CompoundTag newContainer = this.getTag(target, container, CompoundTag.class);
      if (newContainer != null) {
         this.setTag(newContainer, key, this.getTag(oldContainer, key, tagType));
      }
   }

   @Nullable
   private <T extends Tag> T getTag(@Nullable CompoundTag container, @NotNull String key, @NotNull Class<T> dataType) {
      if (container == null) {
         return null;
      } else {
         Tag value = container.get(key);
         return value != null && dataType.isAssignableFrom(value.getClass()) ? dataType.cast(value) : null;
      }
   }

   private <T extends Tag> void setTag(@NotNull CompoundTag container, @NotNull String key, @Nullable T data) {
      if (data == null) {
         this.remove(container, key);
      } else {
         container.put(key, data);
      }
   }

   protected void remove(@NotNull CompoundTag tag, @NotNull String key) {
      tag.remove(key);
   }

   public static boolean isConnected(@Nullable ServerGamePacketListenerImpl connection) {
      return connection != null && !connection.isDisconnected();
   }
}
