package com.artillexstudios.axtrade.libs.axapi.gui.inventory.implementation;

import com.artillexstudios.axtrade.libs.axapi.context.HashMapContext;
import com.artillexstudios.axtrade.libs.axapi.gui.inventory.Gui;
import com.artillexstudios.axtrade.libs.axapi.gui.inventory.GuiItem;
import com.artillexstudios.axtrade.libs.axapi.gui.inventory.modifier.WrappedItemStackModifier;
import com.artillexstudios.axtrade.libs.axapi.gui.inventory.provider.GuiItemProvider;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import java.util.List;
import java.util.function.Function;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;

public class StaticGui extends Gui {
   public StaticGui(
      Player player,
      Function<HashMapContext, Component> titleProvider,
      InventoryType type,
      int rows,
      Int2ObjectArrayMap<GuiItemProvider> providers,
      HashMapContext context,
      List<WrappedItemStackModifier> modifiers
   ) {
      super(player, titleProvider, type, rows, context, modifiers);
      this.providers.clear();
      this.providers.putAll(providers);
   }

   @Override
   public void setItem(int slot, GuiItem item) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void setItem(int slot, GuiItemProvider provider) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void setItem(int slot, GuiItem item, Function<GuiItem, GuiItemProvider> provider) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void updateTitle() {
   }

   @Override
   public Int2ObjectArrayMap<GuiItemProvider> getProviders() {
      return super.getProviders();
   }
}
