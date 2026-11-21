package com.artillexstudios.axtrade.libs.axapi.gui.inventory.builder;

import com.artillexstudios.axtrade.libs.axapi.gui.inventory.GuiBuilder;
import com.artillexstudios.axtrade.libs.axapi.gui.inventory.implementation.PaginatedGui;
import com.artillexstudios.axtrade.libs.axapi.gui.inventory.provider.GuiItemProvider;
import java.util.HashMap;
import java.util.function.Function;
import org.bukkit.entity.Player;

public class PaginatedGuiBuilder extends GuiBuilder<PaginatedGui, PaginatedGuiBuilder> {
   private final HashMap<Class<?>, Function<?, GuiItemProvider>> providers = new HashMap<>();

   public <T> PaginatedGuiBuilder withProvider(Class<T> clazz, Function<T, GuiItemProvider> provider) {
      this.providers.put(clazz, provider);
      return this;
   }

   public PaginatedGui build(Player player) {
      return new PaginatedGui(player, this.titleProvider, this.type, this.rows, this.providers, this.context, this.modifiers);
   }
}
