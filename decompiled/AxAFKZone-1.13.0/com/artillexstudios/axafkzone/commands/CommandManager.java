package com.artillexstudios.axafkzone.commands;

import com.artillexstudios.axafkzone.AxAFKZone;
import com.artillexstudios.axafkzone.libs.axapi.utils.StringUtils;
import com.artillexstudios.axafkzone.libs.lamp.bukkit.BukkitCommandActor;
import com.artillexstudios.axafkzone.libs.lamp.bukkit.BukkitCommandHandler;
import com.artillexstudios.axafkzone.libs.lamp.command.CommandActor;
import com.artillexstudios.axafkzone.libs.lamp.exception.CommandErrorException;
import com.artillexstudios.axafkzone.libs.lamp.orphan.Orphans;
import com.artillexstudios.axafkzone.utils.CommandMessages;
import com.artillexstudios.axafkzone.zones.Zone;
import com.artillexstudios.axafkzone.zones.Zones;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class CommandManager {
   private static BukkitCommandHandler handler = null;

   public static void load() {
      handler = BukkitCommandHandler.create(AxAFKZone.getInstance());
      handler.getTranslator().add(new CommandMessages());
      handler.setLocale(Locale.of("en", "US"));
      handler.registerValueResolver(
         Zone.class,
         resolver -> {
            String zoneName = resolver.popForParameter();
            Zone zone = Zones.getZoneByName(zoneName);
            if (zone == null) {
               resolver.<CommandActor>actor()
                  .as(BukkitCommandActor.class)
                  .getSender()
                  .sendMessage(
                     StringUtils.formatToString(AxAFKZone.CONFIG.getString("prefix") + AxAFKZone.LANG.getString("zone.not-found").replace("%name%", zoneName))
                  );
               throw new CommandErrorException();
            } else {
               return zone;
            }
         }
      );
      handler.getAutoCompleter()
         .registerParameterSuggestions(
            Zone.class, (args, sender, command) -> Zones.getZones().values().stream().map(Zone::getName).collect(Collectors.toList())
         );
      reload();
   }

   public static void reload() {
      handler.unregisterAllCommands();
      List<String> aliases = AxAFKZone.CONFIG.getStringList("command-aliases");
      if (!aliases.isEmpty()) {
         handler.register(new Object[]{Orphans.path(aliases.toArray(String[]::new)).handler(new Commands())});
      }

      handler.registerBrigadier();
   }
}
