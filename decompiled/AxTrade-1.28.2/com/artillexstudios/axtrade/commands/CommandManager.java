package com.artillexstudios.axtrade.commands;

import com.artillexstudios.axtrade.AxTrade;
import com.artillexstudios.axtrade.libs.lamp.bukkit.BukkitCommandHandler;
import com.artillexstudios.axtrade.libs.lamp.orphan.Orphans;
import com.artillexstudios.axtrade.utils.CommandMessages;
import java.util.List;
import java.util.Locale;

public class CommandManager {
   private static BukkitCommandHandler handler = null;

   public static void load() {
      handler = BukkitCommandHandler.create(AxTrade.getInstance());
      handler.getTranslator().add(new CommandMessages());
      handler.setLocale(Locale.of("en", "US"));
      reload();
   }

   public static void reload() {
      handler.unregisterAllCommands();
      List<String> aliases = AxTrade.CONFIG.getStringList("command-aliases");
      if (!aliases.isEmpty()) {
         handler.register(new Object[]{Orphans.path(aliases.toArray(String[]::new)).handler(new Commands())});
      }

      handler.registerBrigadier();
   }
}
