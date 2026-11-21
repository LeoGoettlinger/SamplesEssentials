package com.oheers.fish.commands.main.subcommand;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.oheers.fish.api.economy.selling.SellHelper;
import com.oheers.fish.commands.EMFCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.evenmorefish.fish.libs.daisylib.command.CommandUtils;
import org.jspecify.annotations.NonNull;

public class SellAllSubcommand implements EMFCommand {
   private final String name;

   public SellAllSubcommand(@NonNull String name) {
      this.name = name;
   }

   public ArgumentBuilder<CommandSourceStack, ?> get() {
      return ((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal(this.name).requires(stack -> stack.getSender().hasPermission("emf.sellall")))
            .executes(ctx -> {
               Player player = CommandUtils.requirePlayer(ctx);
               this.execute(player, player);
               return 1;
            }))
         .then(
            ((RequiredArgumentBuilder)Commands.argument("target", ArgumentTypes.player()).requires(stack -> stack.getSender().hasPermission("emf.admin")))
               .executes(
                  ctx -> {
                     Player target = CommandUtils.parsePlayerArgument(
                        (CommandSourceStack)ctx.getSource(), (PlayerSelectorArgumentResolver)ctx.getArgument("target", PlayerSelectorArgumentResolver.class)
                     );
                     this.execute(((CommandSourceStack)ctx.getSource()).getSender(), target);
                     return 1;
                  }
               )
         );
   }

   private void execute(@NonNull CommandSender sender, @NonNull Player target) {
      if (this.isEconomyEnabled(sender)) {
         SellHelper.get().sell(target.getInventory(), target);
      }
   }
}
