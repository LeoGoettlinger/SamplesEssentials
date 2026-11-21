package com.oheers.fish.commands.main.subcommand;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.oheers.fish.api.economy.Economy;
import com.oheers.fish.gui.guis.SellGui;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.abstracted.EMFMessage;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.evenmorefish.fish.libs.daisylib.command.CommandUtils;
import org.jspecify.annotations.NonNull;

public class ShopSubcommand {
   private final String name;

   public ShopSubcommand(@NonNull String name) {
      this.name = name;
   }

   public ArgumentBuilder<CommandSourceStack, ?> get() {
      return ((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal(this.name).requires(stack -> stack.getSender().hasPermission("emf.shop")))
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
      if (!Economy.getInstance().isEnabled()) {
         ConfigMessage.ECONOMY_DISABLED.getMessage().send(sender);
      } else {
         new SellGui(target, SellGui.SellState.NORMAL, null).open();
         if (!target.equals(sender)) {
            EMFMessage message = ConfigMessage.ADMIN_OPEN_FISH_SHOP.getMessage();
            message.setPlayer(target);
            message.send(sender);
         }
      }
   }
}
