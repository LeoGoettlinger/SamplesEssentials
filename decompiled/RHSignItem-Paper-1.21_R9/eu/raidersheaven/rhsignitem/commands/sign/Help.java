package eu.raidersheaven.rhsignitem.commands.sign;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import eu.raidersheaven.rhsignitem.Main;
import eu.raidersheaven.rhsignitem.handlers.ColorFormatHandler;
import eu.raidersheaven.rhsignitem.handlers.LocaleHandler;
import eu.raidersheaven.rhsignitem.handlers.SoundHandler;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import java.util.List;
import org.bukkit.entity.Player;

public class Help {
   public static LiteralArgumentBuilder<CommandSourceStack> register(Main plugin) {
      return (LiteralArgumentBuilder<CommandSourceStack>)((LiteralArgumentBuilder)Commands.literal("help")
            .requires(
               stack -> stack.getSender().hasPermission("RHSignItem.*")
                  || stack.getSender().hasPermission("RHSignItem.command.*")
                  || stack.getSender().hasPermission("RHSignItem.command.sign")
            ))
         .executes(Help::displayConfigList);
   }

   public static int displayConfigList(CommandContext<CommandSourceStack> context) {
      List<String> usageMessage = LocaleHandler.signUsage;
      if (usageMessage.isEmpty()) {
         return 0;
      } else {
         usageMessage.forEach(
            message -> ((CommandSourceStack)context.getSource())
               .getSender()
               .sendMessage(ColorFormatHandler.formatComponent(message.replace("%prefix%", LocaleHandler.prefix)))
         );
         if (((CommandSourceStack)context.getSource()).getSender() instanceof Player player) {
            SoundHandler.success(player);
         }

         return 1;
      }
   }

   public static void noConsoleCommand(CommandContext<CommandSourceStack> context) {
      ((CommandSourceStack)context.getSource())
         .getSender()
         .sendMessage(ColorFormatHandler.formatComponent(LocaleHandler.noConsoleCommand.replace("%prefix%", LocaleHandler.prefix)));
   }
}
