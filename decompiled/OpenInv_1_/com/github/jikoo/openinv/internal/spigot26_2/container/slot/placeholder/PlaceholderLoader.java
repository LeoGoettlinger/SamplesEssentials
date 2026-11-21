package com.github.jikoo.openinv.internal.spigot26_2.container.slot.placeholder;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

public class PlaceholderLoader extends PlaceholderLoaderBase {
   @NotNull
   @Override
   protected Item getWhiteBanner() {
      return (Item)Items.BANNER.white();
   }

   @NotNull
   @Override
   protected Item getWhiteGlassPane() {
      return (Item)Items.STAINED_GLASS_PANE.white();
   }
}
