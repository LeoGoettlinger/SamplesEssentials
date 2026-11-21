package com.github.jikoo.openinv.internal.spigot26_2.player;

import com.github.jikoo.openinv.internal.spigot26_2.container.OpenEnderChest;
import com.github.jikoo.openinv.internal.spigot26_2.container.OpenInventory;
import com.github.jikoo.openinv.internal.spigot26_2.container.menu.OpenChestMenu;
import com.lishid.openinv.internal.ISpecialInventory;
import com.lishid.openinv.util.JulLoggerAdapter;
import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter.ScopedCollector;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerManager implements com.lishid.openinv.internal.PlayerManager {
   @NotNull
   private final Logger logger;
   @Nullable
   private Field bukkitEntity;

   public PlayerManager(@NotNull Logger logger) {
      this.logger = logger;

      try {
         this.bukkitEntity = Entity.class.getDeclaredField("bukkitEntity");
      } catch (NoSuchFieldException var3) {
         logger.warning("Unable to obtain field to inject custom save process - certain player data may be lost when saving!");
         logger.log(Level.WARNING, var3.getMessage(), (Throwable)var3);
         this.bukkitEntity = null;
      }
   }

   @NotNull
   public static ServerPlayer getHandle(Player player) {
      if (player instanceof CraftPlayer craftPlayer) {
         return craftPlayer.getHandle();
      } else {
         Server server = player.getServer();
         ServerPlayer nmsPlayer = null;
         if (server instanceof CraftServer craftServer) {
            nmsPlayer = craftServer.getHandle().getPlayer(player.getUniqueId());
         }

         return nmsPlayer;
      }
   }

   @Nullable
   @Override
   public Player loadPlayer(@NotNull OfflinePlayer offline) {
      if (Bukkit.getServer() instanceof CraftServer craftServer) {
         MinecraftServer server = craftServer.getServer();
         ServerLevel worldServer = server.getLevel(net.minecraft.world.level.Level.OVERWORLD);
         if (worldServer == null) {
            return null;
         } else {
            ServerPlayer entity = this.createNewPlayer(server, worldServer, offline);
            this.removeListeners(entity.getAdvancements());
            return this.loadData(entity) ? entity.getBukkitEntity() : null;
         }
      } else {
         return null;
      }
   }

   @NotNull
   private ServerPlayer createNewPlayer(@NotNull MinecraftServer server, @NotNull ServerLevel worldServer, @NotNull OfflinePlayer offline) {
      GameProfile profile = new GameProfile(offline.getUniqueId(), offline.getName() != null ? offline.getName() : offline.getUniqueId().toString());
      ClientInformation dummyInfo = new ClientInformation(
         "en_us", 1, ChatVisiblity.HIDDEN, false, 0, ServerPlayer.DEFAULT_MAIN_HAND, true, false, ParticleStatus.MINIMAL
      );
      ServerPlayer entity = new ServerPlayer(server, worldServer, profile, dummyInfo);

      try {
         this.injectPlayer(entity);
      } catch (IllegalAccessException var8) {
         this.logger.log(Level.WARNING, var8, () -> "Unable to inject ServerPlayer, certain player data may be lost when saving!");
      }

      return entity;
   }

   protected void removeListeners(@NotNull PlayerAdvancements advancements) {
      advancements.clearTriggers();
   }

   boolean loadData(@NotNull ServerPlayer player) {
      ScopedCollector scopedCollector = new ScopedCollector(player.problemPath(), new JulLoggerAdapter(this.logger));

      boolean var7;
      label30: {
         try {
            CompoundTag loadedData = (CompoundTag)player.server.getPlayerList().playerIo.load(player.nameAndId()).orElse(null);
            if (loadedData == null) {
               var7 = false;
               break label30;
            }

            ValueInput valueInput = TagValueInput.create(scopedCollector, player.registryAccess(), loadedData);
            player.load(valueInput);
         } catch (Throwable var6) {
            try {
               scopedCollector.close();
            } catch (Throwable var5) {
               var6.addSuppressed(var5);
            }

            throw var6;
         }

         scopedCollector.close();
         return true;
      }

      scopedCollector.close();
      return var7;
   }

   private void injectPlayer(ServerPlayer player) throws IllegalAccessException {
      if (this.bukkitEntity != null) {
         this.bukkitEntity.setAccessible(true);
         this.bukkitEntity.set(player, new OpenPlayer(player.server.server, player, this));
      }
   }

   @NotNull
   @Override
   public Player inject(@NotNull Player player) {
      try {
         ServerPlayer nmsPlayer = getHandle(player);
         if (nmsPlayer.getBukkitEntity() instanceof OpenPlayer openPlayer) {
            return openPlayer;
         } else {
            org.slf4j.Logger logger = LogUtils.getLogger();
            ScopedCollector scopedCollector = new ScopedCollector(nmsPlayer.problemPath(), logger);

            CraftPlayer var8;
            try {
               TagValueOutput output = TagValueOutput.createWithContext(scopedCollector, nmsPlayer.registryAccess());
               nmsPlayer.getBukkitEntity().setExtraData(output);
               this.injectPlayer(nmsPlayer);
               CraftPlayer newPlayer = nmsPlayer.getBukkitEntity();
               ValueInput input = TagValueInput.create(scopedCollector, nmsPlayer.registryAccess(), output.buildResult());
               newPlayer.readExtraData(input);
               var8 = newPlayer;
            } catch (Throwable var10) {
               try {
                  scopedCollector.close();
               } catch (Throwable var9) {
                  var10.addSuppressed(var9);
               }

               throw var10;
            }

            scopedCollector.close();
            return var8;
         }
      } catch (IllegalAccessException var11) {
         this.logger.log(Level.WARNING, var11, () -> "Unable to inject ServerPlayer, certain player data may be lost when saving!");
         return player;
      }
   }

   @Nullable
   @Override
   public InventoryView openInventory(@NotNull Player bukkitPlayer, @NotNull ISpecialInventory inventory, boolean viewOnly) {
      ServerPlayer player = getHandle(bukkitPlayer);
      if (!OpenPlayer.isConnected(player.connection)) {
         return null;
      } else {
         OpenChestMenu<?> menu;
         Component title;
         if (inventory instanceof OpenInventory playerInv) {
            menu = playerInv.createMenu(player, player.nextContainerCounter(), viewOnly);
            title = playerInv.getTitle(player, menu);
         } else {
            if (!(inventory instanceof OpenEnderChest enderChest)) {
               return null;
            }

            menu = enderChest.createMenu(player, player.nextContainerCounter(), viewOnly);
            title = enderChest.getTitle(menu);
         }

         if (menu == null) {
            return null;
         } else {
            menu.setTitle(title);
            AbstractContainerMenu opened = CraftEventFactory.callInventoryOpenEvent(player, menu, false);
            if (opened == null) {
               return null;
            } else {
               player.containerMenu = opened;
               player.connection.send(new ClientboundOpenScreenPacket(opened.containerId, opened.getType(), opened.getTitle()));
               player.initMenu(opened);
               return opened.getBukkitView();
            }
         }
      }
   }
}
