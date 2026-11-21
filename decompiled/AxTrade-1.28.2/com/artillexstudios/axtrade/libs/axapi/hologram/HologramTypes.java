package com.artillexstudios.axtrade.libs.axapi.hologram;

import com.artillexstudios.axtrade.libs.axapi.hologram.page.DroppedItemHologramPage;
import com.artillexstudios.axtrade.libs.axapi.hologram.page.TextDisplayHologramPage;
import com.artillexstudios.axtrade.libs.axapi.items.WrappedItemStack;

public final class HologramTypes {
   public static final HologramType<String> TEXT = new HologramType<>(TextDisplayHologramPage::new);
   public static final HologramType<WrappedItemStack> ITEM_STACK = new HologramType<>(DroppedItemHologramPage::new);
}
