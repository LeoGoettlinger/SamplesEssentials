package com.lishid.openinv.internal.paper1_21_11.container;

import com.lishid.openinv.internal.paper1_21_11.container.menu.OpenEnderChestMenu;
import com.lishid.openinv.internal.paper26_2.container.menu.OpenChestMenu;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class OpenEnderChest extends com.lishid.openinv.internal.paper26_2.container.OpenEnderChest {
   public OpenEnderChest(Player player) {
      super(player);
   }

   @Nullable
   @Override
   public OpenChestMenu<?> createMenu(net.minecraft.world.entity.player.Player player, int i, boolean viewOnly) {
      return player instanceof ServerPlayer serverPlayer ? new OpenEnderChestMenu(this, serverPlayer, i, viewOnly) : null;
   }
}
