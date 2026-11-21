package com.oheers.fish.commands.admin.subcommand;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.oheers.fish.FishUtils;
import com.oheers.fish.commands.EMFCommand;
import com.oheers.fish.commands.arguments.CustomRodArgument;
import com.oheers.fish.fishing.rods.CustomRod;
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

public class CustomRodSubcommand implements EMFCommand {
   private final String name;

   public CustomRodSubcommand(@NonNull String name) {
      this.name = name;
   }

   public LiteralArgumentBuilder<CommandSourceStack> get() {
      return (LiteralArgumentBuilder<CommandSourceStack>)Commands.literal("custom-rod")
         .then(
            ((RequiredArgumentBuilder)Commands.argument("rod", new CustomRodArgument()).executes(ctx -> this.execute(ctx, false)))
               .then(Commands.argument("targets", ArgumentTypes.players()).executes(ctx -> this.execute(ctx, true)))
         );
   }

   private int execute(@NonNull CommandContext<CommandSourceStack> ctx, boolean allowConsole) throws CommandSyntaxException {
      CommandSender sender = (CommandSender)(allowConsole ? ((CommandSourceStack)ctx.getSource()).getSender() : CommandUtils.requirePlayer(ctx));
      CustomRod rod = (CustomRod)ctx.getArgument("rod", CustomRod.class);
      PlayerSelectorArgumentResolver targets = CommandUtils.getArgumentOrNull(ctx, "targets", PlayerSelectorArgumentResolver.class);
      return this.execute(sender, rod, CommandUtils.parsePlayersArgument((CommandSourceStack)ctx.getSource(), targets));
   }

   private int execute(@NonNull CommandSender sender, @NonNull CustomRod rod, @NonNull List<Player> targets) throws CommandSyntaxException {
      if (targets.isEmpty()) {
         if (!(sender instanceof Player player)) {
            throw CommandUtils.INVALID_TARGET.create();
         }

         targets = List.of(player);
      }

      ItemStack rodItem = rod.create();

      for (Player player : targets) {
         FishUtils.giveItems(List.of(rodItem), player);
      }

      EMFMessage giveMessage = ConfigMessage.ADMIN_CUSTOM_ROD_GIVEN.getMessage();
      giveMessage.setVariable("{player}", this.getPlayersVariable(targets));
      giveMessage.send(sender);
      return 1;
   }
}
