package com.oheers.fish.commands.admin.subcommand;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.api.fishing.items.IRarity;
import com.oheers.fish.api.requirement.RequirementContext;
import com.oheers.fish.api.reward.Reward;
import com.oheers.fish.commands.arguments.RarityArgument;
import com.oheers.fish.fishing.items.FishManager;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.EMFSingleMessage;
import com.oheers.fish.messages.PrefixType;
import com.oheers.fish.messages.abstracted.EMFMessage;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.evenmorefish.fish.libs.daisylib.command.CommandUtils;
import org.jspecify.annotations.NonNull;

public class RandomFishSubcommand {
   private final String name;

   public RandomFishSubcommand(@NonNull String name) {
      this.name = name;
   }

   public LiteralArgumentBuilder<CommandSourceStack> get() {
      return (LiteralArgumentBuilder<CommandSourceStack>)Commands.literal(this.name)
         .then(
            ((RequiredArgumentBuilder)Commands.argument("rarity", new RarityArgument()).executes(ctx -> this.execute(ctx, false)))
               .then(
                  ((RequiredArgumentBuilder)Commands.argument("amount", IntegerArgumentType.integer(1)).executes(ctx -> this.execute(ctx, false)))
                     .then(Commands.argument("target", ArgumentTypes.player()).executes(ctx -> this.execute(ctx, true)))
               )
         );
   }

   private int execute(@NonNull CommandContext<CommandSourceStack> ctx, boolean allowConsole) throws CommandSyntaxException {
      CommandSender sender = (CommandSender)(allowConsole ? ((CommandSourceStack)ctx.getSource()).getSender() : CommandUtils.requirePlayer(ctx));
      IRarity rarity = (IRarity)ctx.getArgument("rarity", IRarity.class);
      int amount = CommandUtils.getArgumentOrDefault(ctx, "amount", int.class, 1);

      Player target;
      try {
         target = CommandUtils.parsePlayerArgument(
            (CommandSourceStack)ctx.getSource(), (PlayerSelectorArgumentResolver)ctx.getArgument("target", PlayerSelectorArgumentResolver.class)
         );
      } catch (IllegalArgumentException | CommandSyntaxException var8) {
         target = null;
      }

      return this.execute(sender, rarity, amount, target);
   }

   private int execute(CommandSender sender, IRarity rarity, int amount, Player target) throws CommandSyntaxException {
      if (target == null) {
         if (!(sender instanceof Player player)) {
            throw CommandUtils.INVALID_TARGET.create();
         }

         target = player;
      }

      RequirementContext context = new RequirementContext(target.getWorld(), target.getLocation(), target, null, null, null);
      IFish fish = FishManager.getInstance().getFish(rarity, target.getLocation(), target, 1.0, null, true, null, null, context);
      if (fish == null) {
         EMFSingleMessage message = PrefixType.ADMIN.getPrefix();
         message.appendString("<white>Failed to select fish. See console for information.");
         message.send(sender);
         return 1;
      } else {
         fish.init();

         for (Reward reward : fish.getCatchRewards()) {
            reward.give(target, target.getLocation());
         }

         fish.setFisherman(target);
         ItemStack fishItem = fish.give();
         fishItem.setAmount(amount);
         FishUtils.giveItem(fishItem, target);
         EMFMessage message = ConfigMessage.ADMIN_GIVE_PLAYER_FISH.getMessage();
         message.setPlayer(target);
         message.setFishCaught(fish.getId());
         message.send(sender);
         return 1;
      }
   }
}
