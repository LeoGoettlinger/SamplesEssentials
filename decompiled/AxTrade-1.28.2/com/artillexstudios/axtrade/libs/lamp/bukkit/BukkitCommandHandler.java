package com.artillexstudios.axtrade.libs.lamp.bukkit;

import com.artillexstudios.axtrade.libs.lamp.CommandHandler;
import com.artillexstudios.axtrade.libs.lamp.bukkit.core.BukkitHandler;
import java.util.Optional;
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
