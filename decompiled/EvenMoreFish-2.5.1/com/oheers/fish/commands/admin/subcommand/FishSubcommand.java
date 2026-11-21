package com.oheers.fish.commands.admin.subcommand;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.api.fishing.items.IRarity;
import com.oheers.fish.commands.EMFCommand;
import com.oheers.fish.commands.arguments.FishArgument;
import com.oheers.fish.commands.arguments.RarityArgument;
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

public class FishSubcommand implements EMFCommand {
   private final String name;

   public FishSubcommand(@NonNull String name) {
      this.name = name;
   }

   public LiteralArgumentBuilder<CommandSourceStack> get() {
      return (LiteralArgumentBuilder<CommandSourceStack>)Commands.literal(this.name)
         .then(
            Commands.argument("rarity", new RarityArgument())
               .then(
                  ((RequiredArgumentBuilder)Commands.argument("fish", new FishArgument()).executes(ctx -> this.execute(ctx, false)))
                     .then(
                        ((RequiredArgumentBuilder)Commands.argument("amount", IntegerArgumentType.integer(1)).executes(ctx -> this.execute(ctx, false)))
                           .then(Commands.argument("targets", ArgumentTypes.players()).executes(ctx -> this.execute(ctx, true)))
                     )
               )
         );
   }

   private int execute(@NonNull CommandContext<CommandSourceStack> ctx, boolean allowConsole) throws CommandSyntaxException {
      CommandSender sender = (CommandSender)(allowConsole ? ((CommandSourceStack)ctx.getSource()).getSender() : CommandUtils.requirePlayer(ctx));
      IRarity rarity = (IRarity)ctx.getArgument("rarity", IRarity.class);
      String fishStr = (String)ctx.getArgument("fish", String.class);
      int amount = CommandUtils.getArgumentOrDefault(ctx, "amount", int.class, 1);
      PlayerSelectorArgumentResolver targets = CommandUtils.getArgumentOrNull(ctx, "targets", PlayerSelectorArgumentResolver.class);
      return this.execute(sender, rarity, fishStr, amount, CommandUtils.parsePlayersArgument((CommandSourceStack)ctx.getSource(), targets));
   }

   private int execute(CommandSender sender, IRarity rarity, String fishStr, int amount, List<Player> targets) throws CommandSyntaxException {
      if (targets.isEmpty()) {
         if (!(sender instanceof Player player)) {
            throw CommandUtils.INVALID_TARGET.create();
         }

         targets = List.of(player);
      }

      IFish initialFish = FishArgument.resolveFish(rarity, fishStr);
      if (initialFish == null) {
         throw FishArgument.INVALID_FISH.create(fishStr);
      } else {
         targets.forEach(target -> {
            IFish fish = initialFish.createCopy();
            fish.init();
            fish.getCatchRewards().forEach(reward -> reward.give(target, target.getLocation()));
            fish.setFisherman(target);
            ItemStack fishItem = fish.give();
            fishItem.setAmount(amount);
            FishUtils.giveItem(fishItem, target);
         });
         EMFMessage message = ConfigMessage.ADMIN_GIVE_PLAYER_FISH.getMessage();
         message.setVariable("{player}", this.getPlayersVariable(targets));
         message.setFishCaught(initialFish.getId());
         message.send(sender);
         return 1;
      }
   }
}
