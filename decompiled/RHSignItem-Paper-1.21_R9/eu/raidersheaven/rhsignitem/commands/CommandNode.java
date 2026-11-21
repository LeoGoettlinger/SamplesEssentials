package eu.raidersheaven.rhsignitem.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import eu.raidersheaven.rhsignitem.Main;
import eu.raidersheaven.rhsignitem.commands.rename.Rename;
import eu.raidersheaven.rhsignitem.commands.sign.Delete;
import eu.raidersheaven.rhsignitem.commands.sign.Help;
import eu.raidersheaven.rhsignitem.commands.sign.Lock;
import eu.raidersheaven.rhsignitem.commands.sign.Sign;
import eu.raidersheaven.rhsignitem.commands.sign.Unlock;
import eu.raidersheaven.rhsignitem.commands.sign.Version;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

public class CommandNode {
   public static LiteralCommandNode<CommandSourceStack> createSign(Main plugin) {
      return ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal(
                                    "sign"
                                 )
                                 .requires(
                                    stack -> stack.getSender().hasPermission("RHSignItem.*")
                                       || stack.getSender().hasPermission("RHSignItem.command.*")
                                       || stack.getSender().hasPermission("RHSignItem.command.sign")
                                 ))
                              .executes(Sign::fastSignItem))
                           .then(Sign.register(plugin)))
                        .then(Help.register(plugin)))
                     .then(Unlock.register(plugin)))
                  .then(Lock.register(plugin)))
               .then(Delete.register(plugin)))
            .then(Version.register(plugin)))
         .build();
   }

   public static LiteralCommandNode<CommandSourceStack> createRename(Main plugin) {
      return ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("rename")
                  .requires(
                     stack -> stack.getSender().hasPermission("RHSignItem.*")
                        || stack.getSender().hasPermission("RHSignItem.command.*")
                        || stack.getSender().hasPermission("RHSignItem.command.rename")
                  ))
               .executes(Help::displayConfigList))
            .then(Rename.register(plugin)))
         .build();
   }
}
