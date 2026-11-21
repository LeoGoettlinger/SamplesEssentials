package com.github.jikoo.openinv.internal.spigot26_2;

import com.github.jikoo.openinv.internal.spigot26_2.container.AnySilentContainer;
import com.github.jikoo.openinv.internal.spigot26_2.container.OpenEnderChest;
import com.github.jikoo.openinv.internal.spigot26_2.container.OpenInventory;
import com.github.jikoo.openinv.internal.spigot26_2.container.slot.placeholder.PlaceholderLoader;
import com.github.jikoo.openinv.internal.spigot26_2.player.PlayerManager;
import com.lishid.openinv.internal.Accessor;
import com.lishid.openinv.internal.IAnySilentContainer;
import com.lishid.openinv.internal.ISpecialEnderChest;
import com.lishid.openinv.internal.ISpecialInventory;
import com.lishid.openinv.internal.ISpecialPlayerInventory;
import com.lishid.openinv.util.lang.LanguageManager;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minecraft.world.Container;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InternalAccessor implements Accessor {
   @NotNull
   protected final Logger logger;
   @NotNull
   private final PlayerManager manager;
   @NotNull
   private final AnySilentContainer anySilentContainer;

   public InternalAccessor(@NotNull Logger logger, @NotNull LanguageManager lang) {
      this.logger = logger;
      this.manager = new PlayerManager(logger);
      this.anySilentContainer = new AnySilentContainer(logger, lang);
   }

   @NotNull
   public PlayerManager getPlayerManager() {
      return this.manager;
   }

   @NotNull
   @Override
   public IAnySilentContainer getAnySilentContainer() {
      return this.anySilentContainer;
   }

   @NotNull
   @Override
   public ISpecialPlayerInventory createPlayerInventory(@NotNull Player player) {
      return new OpenInventory(player);
   }

   @NotNull
   @Override
   public ISpecialEnderChest createEnderChest(@NotNull Player player) {
      return new OpenEnderChest(player);
   }

   @Nullable
   @Override
   public <T extends ISpecialInventory> T get(@NotNull Inventory bukkitInventory, @NotNull Class<T> clazz) {
      if (bukkitInventory instanceof CraftInventory craftInventory) {
         Container container = craftInventory.getInventory();
         return clazz.isInstance(container) ? clazz.cast(container) : null;
      } else {
         return null;
      }
   }

   @Override
   public void reload(@NotNull ConfigurationSection config) {
      ConfigurationSection placeholders = config.getConfigurationSection("placeholders");

      try {
         new PlaceholderLoader().load(placeholders);
      } catch (Exception var4) {
         this.logger.log(Level.WARNING, "Caught exception loading placeholder overrides!", (Throwable)var4);
      }
   }
}
