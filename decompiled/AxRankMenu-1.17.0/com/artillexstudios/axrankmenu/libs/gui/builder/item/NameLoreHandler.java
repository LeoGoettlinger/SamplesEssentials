package com.artillexstudios.axrankmenu.libs.gui.builder.item;

import java.util.List;
import java.util.function.Consumer;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public interface NameLoreHandler {
   void name(@NotNull ItemMeta var1, @NotNull Component var2);

   void lore(@NotNull ItemMeta var1, @NotNull List<Component> var2);

   void lore(@NotNull ItemMeta var1, @NotNull Consumer<List<Component>> var2);
}
