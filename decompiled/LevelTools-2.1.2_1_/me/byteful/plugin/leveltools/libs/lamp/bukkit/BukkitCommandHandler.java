package me.byteful.plugin.leveltools.libs.lamp.bukkit;

import java.util.Optional;
import me.byteful.plugin.leveltools.libs.lamp.CommandHandler;
import me.byteful.plugin.leveltools.libs.lamp.bukkit.core.BukkitHandler;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public interface BukkitCommandHandler extends CommandHandler {
   @NotNull
   Optional<BukkitBrigadier> getBrigadier();

   boolean isBrigadierSupported();

   BukkitCommandHandler registerBrigadier();

   void enableAdventure();

   void enableAdventure(@NotNull BukkitAudiences audiences);

   @NotNull
   Plugin getPlugin();

   @NotNull
   static BukkitCommandHandler create(@NotNull Plugin plugin) {
      return new BukkitHandler(plugin);
   }
}
