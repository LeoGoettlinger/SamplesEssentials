package com.artillexstudios.axafkzone.libs.axapi.gui.inventory.provider.implementation;

import com.artillexstudios.axafkzone.libs.axapi.context.HashMapContext;
import com.artillexstudios.axafkzone.libs.axapi.gui.inventory.BakedGuiItem;
import com.artillexstudios.axafkzone.libs.axapi.gui.inventory.modifier.WrappedItemStackModifier;
import com.artillexstudios.axafkzone.libs.axapi.gui.inventory.provider.GuiItemProvider;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class EmptyGuiItemProvider implements GuiItemProvider {
   public static final EmptyGuiItemProvider INSTANCE = new EmptyGuiItemProvider();
   private static final BakedGuiItem EMPTY_ITEM = new BakedGuiItem(new ItemStack(Material.AIR), event -> {});
   private static final CompletableFuture<BakedGuiItem> FUTURE = CompletableFuture.completedFuture(EMPTY_ITEM);

   @Override
   public CompletableFuture<BakedGuiItem> provide(HashMapContext context, List<WrappedItemStackModifier> modifiers) {
      return FUTURE;
   }
}
