package com.github.jikoo.openinv.internal.spigot26_2.container;

import com.github.jikoo.openinv.internal.spigot26_2.container.menu.OpenChestMenu;
import com.github.jikoo.openinv.internal.spigot26_2.player.PlayerManager;
import com.lishid.openinv.internal.AnySilentContainerBase;
import com.lishid.openinv.util.ReflectionHelper;
import com.lishid.openinv.util.lang.LanguageManager;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.TrappedChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AnySilentContainer extends AnySilentContainerBase {
   @NotNull
   private final Logger logger;
   @NotNull
   private final LanguageManager lang;
   @Nullable
   private Field serverPlayerGameModeGameType;

   public AnySilentContainer(@NotNull Logger logger, @NotNull LanguageManager lang) {
      this.logger = logger;
      this.lang = lang;

      try {
         try {
            this.serverPlayerGameModeGameType = ServerPlayerGameMode.class.getDeclaredField("gameModeForPlayer");
            this.serverPlayerGameModeGameType.setAccessible(true);
         } catch (NoSuchFieldException var4) {
            logger.warning("The field ServerPlayerGameMode#gameModeForPlayer is no longer present!");
            logger.warning("Please report this at https://github.com/Jikoo/OpenInv/issues");
            logger.warning("Attempting to fall through using reflection. Please verify that SilentContainer does not fail.");
            this.serverPlayerGameModeGameType = ReflectionHelper.grabFieldByType(ServerPlayerGameMode.class, GameType.class);
         }
      } catch (SecurityException var5) {
         logger.warning("Unable to directly write player game mode! SilentContainer will fail.");
         logger.log(Level.WARNING, "Error obtaining GameType field", (Throwable)var5);
      }
   }

   @Override
   public boolean activateContainer(@NotNull Player bukkitPlayer, boolean silentchest, @NotNull Block bukkitBlock) {
      if (silentchest && bukkitBlock.getType() == Material.ENDER_CHEST) {
         bukkitPlayer.openInventory(bukkitPlayer.getEnderChest());
         bukkitPlayer.incrementStatistic(Statistic.ENDERCHEST_OPENED);
         return true;
      } else {
         ServerPlayer player = PlayerManager.getHandle(bukkitPlayer);
         net.minecraft.world.level.Level level = player.level();
         BlockPos blockPos = new BlockPos(bukkitBlock.getX(), bukkitBlock.getY(), bukkitBlock.getZ());
         BlockEntity blockEntity = level.getBlockEntity(blockPos);
         if (blockEntity == null) {
            return false;
         } else if (blockEntity instanceof EnderChestBlockEntity enderChestTile) {
            PlayerEnderChestContainer enderChest = player.getEnderChestInventory();
            enderChest.setActiveChest(enderChestTile);
            player.openMenu(new SimpleMenuProvider((containerCounter, playerInventory, ignored) -> {
               MenuType<?> containers = OpenChestMenu.getChestMenuType(enderChest.getContainerSize());
               int rows = enderChest.getContainerSize() / 9;
               return new ChestMenu(containers, containerCounter, playerInventory, enderChest, rows);
            }, Component.translatable("container.enderchest")));
            bukkitPlayer.incrementStatistic(Statistic.ENDERCHEST_OPENED);
            return true;
         } else if (blockEntity instanceof MenuProvider menuProvider) {
            BlockState blockState = level.getBlockState(blockPos);
            net.minecraft.world.level.block.Block block = blockState.getBlock();
            if (block instanceof ChestBlock chestBlock) {
               menuProvider = chestBlock.getMenuProvider(blockState, level, blockPos, true);
               if (menuProvider == null) {
                  this.lang.sendSystemMessage(bukkitPlayer, "messages.error.lootNotGenerated");
                  return false;
               }

               if (block instanceof TrappedChestBlock) {
                  bukkitPlayer.incrementStatistic(Statistic.TRAPPED_CHEST_TRIGGERED);
               } else {
                  bukkitPlayer.incrementStatistic(Statistic.CHEST_OPENED);
               }
            }

            if (block instanceof ShulkerBoxBlock) {
               bukkitPlayer.incrementStatistic(Statistic.SHULKER_BOX_OPENED);
            }

            if (block instanceof BarrelBlock) {
               bukkitPlayer.incrementStatistic(Statistic.OPEN_BARREL);
            }

            if (!silentchest || player.gameMode.getGameModeForPlayer() == GameType.SPECTATOR) {
               player.openMenu(menuProvider);
               return true;
            } else if (this.serverPlayerGameModeGameType == null) {
               return false;
            } else if (blockEntity instanceof RandomizableContainerBlockEntity lootable && lootable.lootTable != null) {
               this.lang.sendSystemMessage(bukkitPlayer, "messages.error.lootNotGenerated");
               return false;
            } else {
               GameType gameType = player.gameMode.getGameModeForPlayer();
               this.forceGameType(player, GameType.SPECTATOR);
               player.openMenu(menuProvider);
               this.forceGameType(player, gameType);
               return true;
            }
         } else {
            return false;
         }
      }
   }

   @Override
   public void deactivateContainer(@NotNull Player bukkitPlayer) {
      if (this.serverPlayerGameModeGameType != null && bukkitPlayer.getGameMode() != GameMode.SPECTATOR) {
         ServerPlayer player = PlayerManager.getHandle(bukkitPlayer);
         GameType gameType = player.gameMode.getGameModeForPlayer();
         this.forceGameType(player, GameType.SPECTATOR);
         player.containerMenu.transferTo(player.inventoryMenu, player.getBukkitEntity());
         player.doCloseContainer();
         this.forceGameType(player, gameType);
      }
   }

   private void forceGameType(ServerPlayer player, GameType gameMode) {
      if (this.serverPlayerGameModeGameType != null) {
         try {
            this.serverPlayerGameModeGameType.setAccessible(true);
            this.serverPlayerGameModeGameType.set(player.gameMode, gameMode);
         } catch (IllegalAccessException | IllegalArgumentException var4) {
            this.logger.log(Level.WARNING, "Error bypassing GameModeChangeEvent", (Throwable)var4);
         }
      }
   }

   @Override
   protected org.bukkit.block.BlockState getState(@NotNull Block block) {
      return block.getState();
   }

   @Nullable
   @Override
   protected InventoryHolder getHolder(@NotNull Inventory inventory) {
      return inventory.getHolder();
   }
}
