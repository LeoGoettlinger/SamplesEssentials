package eu.raidersheaven.rhsignitem.commands.sign;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import eu.raidersheaven.rhsignitem.Main;
import eu.raidersheaven.rhsignitem.configurations.ConfigFile;
import eu.raidersheaven.rhsignitem.handlers.ColorFormatHandler;
import eu.raidersheaven.rhsignitem.handlers.LocaleHandler;
import eu.raidersheaven.rhsignitem.handlers.SoundHandler;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import java.util.Objects;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Version {
   public static LiteralArgumentBuilder<CommandSourceStack> register(Main plugin) {
      return (LiteralArgumentBuilder<CommandSourceStack>)((LiteralArgumentBuilder)Commands.literal("version")
            .requires(
               stack -> stack.getSender().hasPermission("RHSignItem.*")
                  || stack.getSender().hasPermission("RHSignItem.command.*")
                  || stack.getSender().hasPermission("RHSignItem.command.sign.version")
            ))
         .executes(Version::showInfo);
   }

   private static int showInfo(CommandContext<CommandSourceStack> context) {
      String version = ((Main)JavaPlugin.getPlugin(Main.class)).getPluginMeta().getVersion();
      String apiVersion = ((Main)JavaPlugin.getPlugin(Main.class)).getPluginMeta().getAPIVersion();
      if (((CommandSourceStack)context.getSource()).getSender() instanceof Player) {
         SoundHandler.version((Player)((CommandSourceStack)context.getSource()).getSender());
      }

      if (Objects.equals(ConfigFile.locale, "de_DE")) {
         ((CommandSourceStack)context.getSource())
            .getSender()
            .sendMessage(
               ColorFormatHandler.formatComponent(
                  LocaleHandler.prefix
                     + "<reset><gray> Du nutzt Version <yellow>"
                     + version
                     + "<gray>, erstellt und getestet mit <white>Paper <yellow>"
                     + apiVersion
                     + "+"
               )
            );
         ((CommandSourceStack)context.getSource())
            .getSender()
            .sendMessage(
               ColorFormatHandler.formatComponent(LocaleHandler.prefix + "<reset><gray> Wenn Probleme auftauchen oder Du Fragen hast, kontaktiere mich.")
            );
      } else {
         ((CommandSourceStack)context.getSource())
            .getSender()
            .sendMessage(
               ColorFormatHandler.formatComponent(
                  LocaleHandler.prefix
                     + "<reset><gray> You are using version <yellow>"
                     + version
                     + "<gray>, made and tested for <white>Paper <yellow>"
                     + apiVersion
                     + "+"
               )
            );
         ((CommandSourceStack)context.getSource())
            .getSender()
            .sendMessage(ColorFormatHandler.formatComponent(LocaleHandler.prefix + "<reset><gray> If you encounter any problems, please contact me."));
      }

      return 1;
   }
}
