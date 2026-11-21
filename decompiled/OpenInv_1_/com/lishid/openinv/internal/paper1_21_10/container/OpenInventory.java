package com.lishid.openinv.internal.paper1_21_10.container;

import com.lishid.openinv.internal.paper1_21_11.container.menu.OpenInventoryMenu;
import com.lishid.openinv.internal.paper26_2.container.BaseOpenInventory;
import com.lishid.openinv.internal.paper26_2.container.menu.OpenChestMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.FontDescription.Resource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OpenInventory extends BaseOpenInventory {
   public OpenInventory(@NotNull Player bukkitPlayer) {
      super(bukkitPlayer);
   }

   @NotNull
   @Override
   public Component getTitle(@Nullable ServerPlayer viewer, @Nullable OpenChestMenu<?> menu) {
      MutableComponent component = Component.empty();
      if (this.owner.equals(viewer)) {
         component.append(
            Component.translatableWithFallback("openinv.container.inventory.self", "")
               .withStyle(style -> style.withFont(new Resource(ResourceLocation.parse("openinv:font/inventory"))).withColor(ChatFormatting.WHITE))
         );
      } else {
         component.append(
            Component.translatableWithFallback("openinv.container.inventory.other", "")
               .withStyle(style -> style.withFont(new Resource(ResourceLocation.parse("openinv:font/inventory"))).withColor(ChatFormatting.WHITE))
         );
      }

      if (menu != null && menu.isViewOnly()) {
         component.append(Component.translatableWithFallback("openinv.container.inventory.viewonly", "[RO] "));
      } else {
         component.append(Component.translatableWithFallback("openinv.container.inventory.editable", ""));
      }

      component.append(Component.translatableWithFallback("openinv.container.inventory.prefix", "", new Object[]{this.owner.getName()}))
         .append(Component.translatable("container.inventory"))
         .append(Component.translatableWithFallback("openinv.container.inventory.suffix", " - %s", new Object[]{this.owner.getName()}));
      return component;
   }

   @Nullable
   @Override
   public OpenChestMenu<?> createMenu(net.minecraft.world.entity.player.Player player, int i, boolean viewOnly) {
      return player instanceof ServerPlayer serverPlayer ? new OpenInventoryMenu(this, serverPlayer, i, viewOnly) : null;
   }
}
