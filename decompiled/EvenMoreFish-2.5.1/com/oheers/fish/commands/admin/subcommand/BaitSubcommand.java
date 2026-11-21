package com.oheers.fish.commands.admin.subcommand;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.oheers.fish.FishUtils;
import com.oheers.fish.baits.BaitHandler;
import com.oheers.fish.commands.EMFCommand;
import com.oheers.fish.commands.arguments.BaitArgument;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.abstracted.EMFMessage;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.evenmorefish.fish.libs.daisylib.command.CommandUtils;
import org.jspecify.annotations.NonNull;

public class BaitSubcommand implements EMFCommand {
   private final String name;

   public BaitSubcommand(@NonNull String name) {
      this.name = name;
   }

   public LiteralArgumentBuilder<CommandSourceStack> get() {
      return (LiteralArgumentBuilder<CommandSourceStack>)Commands.literal(this.name)
         .then(
            ((RequiredArgumentBuilder)Commands.argument("bait", new BaitArgument()).executes(ctx -> this.execute(ctx, false)))
               .then(
                  ((RequiredArgumentBuilder)Commands.argument("quantity", IntegerArgumentType.integer(1)).executes(ctx -> this.execute(ctx, false)))
                     .then(Commands.argument("targets", ArgumentTypes.players()).executes(ctx -> this.execute(ctx, true)))
               )
         );
   }

   private int execute(@NonNull CommandContext<CommandSourceStack> ctx, boolean allowConsole) throws CommandSyntaxException {
      CommandSender sender = (CommandSender)(allowConsole ? ((CommandSourceStack)ctx.getSource()).getSender() : CommandUtils.requirePlayer(ctx));
      BaitHandler bait = (BaitHandler)ctx.getArgument("bait", BaitHandler.class);
      int quantity = CommandUtils.getArgumentOrDefault(ctx, "quantity", int.class, 1);
      PlayerSelectorArgumentResolver targets = CommandUtils.getArgumentOrNull(ctx, "targets", PlayerSelectorArgumentResolver.class);
      return this.execute(sender, bait, quantity, CommandUtils.parsePlayersArgument((CommandSourceStack)ctx.getSource(), targets));
   }

   private int execute(CommandSender sender, BaitHandler bait, int quantity, List<Player> targets) throws CommandSyntaxException {
      if (targets.isEmpty()) {
         if (!(sender instanceof Player player)) {
            throw CommandUtils.INVALID_TARGET.create();
         }

         targets = List.of(player);
      }

      for (Player target : targets) {
         ItemStack baitItem = bait.create(target);
         baitItem.setAmount(quantity);
         FishUtils.giveItems(List.of(baitItem), target);
      }

      EMFMessage message = ConfigMessage.ADMIN_GIVE_PLAYER_BAIT.getMessage();
      message.setVariable("{player}", this.getPlayersVariable(targets));
      message.setBait(bait);
      message.send(sender);
      return 1;
   }
}
