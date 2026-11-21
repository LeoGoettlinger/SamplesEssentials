package com.lishid.openinv.internal.paper26_2.player;

import com.lishid.openinv.internal.ISpecialInventory;
import com.lishid.openinv.internal.paper26_2.container.BaseOpenInventory;
import com.lishid.openinv.internal.paper26_2.container.OpenEnderChest;
import com.lishid.openinv.internal.paper26_2.container.menu.OpenChestMenu;
import com.lishid.openinv.util.JulLoggerAdapter;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import io.papermc.paper.adventure.PaperAdventure;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.kyori.adventure.text.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.server.MinecraftServer;
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
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.LevelData.RespawnData;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class PlayerManager implements com.lishid.openinv.internal.PlayerManager {
   protected final Logger logger;
   @Nullable
   protected Field bukkitEntity;

   public PlayerManager(Logger logger) {
      this.logger = logger;

      try {
         this.bukkitEntity = Entity.class.getDeclaredField("bukkitEntity");
      } catch (NoSuchFieldException var3) {
         logger.warning("Unable to obtain field to inject custom save process - certain player data may be lost when saving!");
         logger.log(Level.WARNING, var3.getMessage(), (Throwable)var3);
         this.bukkitEntity = null;
      }
   }

   public static ServerPlayer getHandle(Player player) {
      if (player instanceof CraftPlayer craftPlayer) {
         return craftPlayer.getHandle();
      } else {
         Server server = player.getServer();
         ServerPlayer nmsPlayer = null;
         if (server instanceof CraftServer craftServer) {
            nmsPlayer = craftServer.getHandle().getPlayer(player.getUniqueId());
         }

         if (nmsPlayer == null) {
            throw new RuntimeException("Unable to fetch EntityPlayer from Player implementation " + player.getClass().getName());
         } else {
            return nmsPlayer;
         }
      }
   }

   @Nullable
   @Override
   public Player loadPlayer(OfflinePlayer offline) {
      if (Bukkit.getServer() instanceof CraftServer craftServer) {
         MinecraftServer server = craftServer.getServer();
         ServerLevel worldServer = server.getLevel(net.minecraft.world.level.Level.OVERWORLD);
         if (worldServer == null) {
            return null;
         } else {
            ServerPlayer entity = this.createNewPlayer(server, worldServer, offline);
            this.removeAdvancementListeners(entity);
            return this.loadData(server, entity) ? entity.getBukkitEntity() : null;
         }
      } else {
         return null;
      }
   }

   protected ServerPlayer createNewPlayer(MinecraftServer server, ServerLevel worldServer, OfflinePlayer offline) {
      GameProfile profile = new GameProfile(offline.getUniqueId(), offline.getName() != null ? offline.getName() : offline.getUniqueId().toString());
      ClientInformation dummyInfo = new ClientInformation(
         "en_us", 1, ChatVisiblity.HIDDEN, false, 0, ServerPlayer.DEFAULT_MAIN_HAND, true, false, ParticleStatus.MINIMAL
      );
      ServerPlayer entity = new ServerPlayer(server, worldServer, profile, dummyInfo);

      try {
         this.injectPlayer(server, entity);
      } catch (IllegalAccessException var8) {
         this.logger.log(Level.WARNING, var8, () -> "Unable to inject ServerPlayer, certain player data may be lost when saving!");
      }

      return entity;
   }

   protected void removeAdvancementListeners(ServerPlayer entity) {
      entity.getAdvancements().clearTriggers();
   }

   protected boolean loadData(MinecraftServer server, ServerPlayer player) {
      ScopedCollector scopedCollector = new ScopedCollector(player.problemPath(), new JulLoggerAdapter(this.logger));

      boolean var8;
      label30: {
         try {
            CompoundTag loadedData = (CompoundTag)server.getPlayerList().playerIo.load(player.nameAndId()).orElse(null);
            if (loadedData == null) {
               var8 = false;
               break label30;
            }

            ValueInput valueInput = TagValueInput.create(scopedCollector, player.registryAccess(), loadedData);
            player.load(valueInput);
            this.parseWorld(server, player, valueInput);
         } catch (Throwable var7) {
            try {
               scopedCollector.close();
            } catch (Throwable var6) {
               var7.addSuppressed(var6);
            }

            throw var7;
         }

         scopedCollector.close();
         return true;
      }

      scopedCollector.close();
      return var8;
   }

   protected void parseWorld(MinecraftServer server, ServerPlayer player, ValueInput loadedData) {
      Optional<Long> msbs = loadedData.getLong("WorldUUIDMost");
      Optional<Long> lsbs = loadedData.getLong("WorldUUIDLeast");
      World bukkitWorld;
      if (msbs.isPresent() && lsbs.isPresent()) {
         bukkitWorld = Bukkit.getServer().getWorld(new UUID(msbs.get(), lsbs.get()));
      } else {
         bukkitWorld = loadedData.getString("world").<World>map(Bukkit::getWorld).orElse(null);
      }

      if (bukkitWorld == null) {
         this.spawnInDefaultWorld(server, player);
      } else {
         player.setServerLevel(((CraftWorld)bukkitWorld).getHandle());
      }
   }

   protected void spawnInDefaultWorld(MinecraftServer server, ServerPlayer player) {
      ServerLevel level = server.getLevel(net.minecraft.world.level.Level.OVERWORLD);
      if (level != null) {
         RespawnData respawnData = this.getRespawnData(level);
         player.snapTo(this.getAdjustedSpawnLocation(player, level, respawnData), respawnData.yaw(), 0.0F);
         this.setServerLevel(player, level);
      } else {
         this.logger.warning("Tried to load player with invalid world when no fallback was available!");
      }
   }

   protected RespawnData getRespawnData(ServerLevel level) {
      return level.getRespawnData();
   }

   protected Vec3 getAdjustedSpawnLocation(ServerPlayer player, ServerLevel level, RespawnData respawnData) {
      return Vec3.atBottomCenterOf(player.adjustSpawnLocation(level, respawnData.pos()));
   }

   protected void setServerLevel(ServerPlayer player, ServerLevel level) {
      player.setServerLevel(level);
   }

   protected void injectPlayer(MinecraftServer server, ServerPlayer player) throws IllegalAccessException {
      if (this.bukkitEntity != null) {
         this.bukkitEntity.setAccessible(true);
         this.bukkitEntity.set(player, new OpenPlayer(server.server, player, this));
      }
   }

   @Override
   public Player inject(Player player) {
      try {
         ServerPlayer nmsPlayer = getHandle(player);
         if (nmsPlayer.getBukkitEntity() instanceof OpenPlayer openPlayer) {
            return openPlayer;
         } else {
            org.slf4j.Logger logger = LogUtils.getLogger();
            ScopedCollector scopedCollector = new ScopedCollector(nmsPlayer.problemPath(), logger);

            CraftPlayer var10;
            try {
               CompoundTag extraData = new CompoundTag();
               ValueOutput output = TagValueOutput.createWrappingWithContext(scopedCollector, nmsPlayer.registryAccess(), extraData);
               nmsPlayer.getBukkitEntity().setExtraData(output);
               MinecraftServer server = nmsPlayer.level().getServer();
               this.injectPlayer(server, nmsPlayer);
               CraftPlayer newPlayer = nmsPlayer.getBukkitEntity();
               ValueInput input = TagValueInput.create(scopedCollector, nmsPlayer.registryAccess(), extraData);
               newPlayer.readExtraData(input);
               var10 = newPlayer;
            } catch (Throwable var12) {
               try {
                  scopedCollector.close();
               } catch (Throwable var11) {
                  var12.addSuppressed(var11);
               }

               throw var12;
            }

            scopedCollector.close();
            return var10;
         }
      } catch (IllegalAccessException var13) {
         this.logger.log(Level.WARNING, var13, () -> "Unable to inject ServerPlayer, certain player data may be lost when saving!");
         return player;
      }
   }

   @Nullable
   @Override
   public InventoryView openInventory(Player bukkitPlayer, ISpecialInventory inventory, boolean viewOnly) {
      ServerPlayer player = getHandle(bukkitPlayer);
      if (!OpenPlayer.isConnected(player.connection)) {
         return null;
      } else {
         OpenChestMenu<?> menu;
         net.minecraft.network.chat.Component title;
         if (inventory instanceof BaseOpenInventory playerInv) {
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
            Pair<Component, AbstractContainerMenu> pair = CraftEventFactory.callInventoryOpenEventWithTitle(player, menu);
            AbstractContainerMenu opened = (AbstractContainerMenu)pair.getSecond();
            if (opened == null) {
               return null;
            } else {
               Component newTitle = (Component)pair.getFirst();
               if (newTitle != null) {
                  title = PaperAdventure.asVanilla(newTitle);
               }

               player.containerMenu = opened;
               player.connection.send(new ClientboundOpenScreenPacket(opened.containerId, opened.getType(), title));
               player.initMenu(opened);
               return opened.getBukkitView();
            }
         }
      }
   }
}
