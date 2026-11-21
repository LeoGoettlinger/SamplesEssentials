package com.lishid.openinv.internal.paper26_2.container.slot.placeholder;

import com.lishid.openinv.internal.paper26_2.player.OpenPlayer;
import java.util.EnumMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.NotNull;

public final class Placeholders {
   @NotNull
   static final EnumMap<GameType, ItemStack> BLOCKED_GAME_TYPE = new EnumMap<>(GameType.class);
   @NotNull
   public static ItemStack craftingOutput = ItemStack.EMPTY;
   @NotNull
   public static ItemStack cursor = ItemStack.EMPTY;
   @NotNull
   public static ItemStack drop = ItemStack.EMPTY;
   @NotNull
   public static ItemStack emptyHelmet = ItemStack.EMPTY;
   @NotNull
   public static ItemStack emptyChestplate = ItemStack.EMPTY;
   @NotNull
   public static ItemStack emptyLeggings = ItemStack.EMPTY;
   @NotNull
   public static ItemStack emptyBoots = ItemStack.EMPTY;
   @NotNull
   public static ItemStack emptyOffHand = ItemStack.EMPTY;
   @NotNull
   public static ItemStack notSlot = ItemStack.EMPTY;
   @NotNull
   public static ItemStack blockedOffline = ItemStack.EMPTY;

   public static ItemStack survivalOnly(@NotNull ServerPlayer serverPlayer) {
      return !OpenPlayer.isConnected(serverPlayer.connection)
         ? blockedOffline
         : BLOCKED_GAME_TYPE.getOrDefault(serverPlayer.gameMode.getGameModeForPlayer(), ItemStack.EMPTY);
   }

   private Placeholders() {
      throw new IllegalStateException("Cannot create instance of utility class.");
   }
}
