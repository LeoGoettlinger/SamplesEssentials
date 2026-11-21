package com.oheers.fish.commands.main.subcommand;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.oheers.fish.api.fishing.items.IRarity;
import com.oheers.fish.commands.arguments.RarityArgument;
import com.oheers.fish.database.DatabaseUtil;
import com.oheers.fish.gui.guis.FishJournalGui;
import com.oheers.fish.messages.ConfigMessage;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;
import org.evenmorefish.fish.libs.daisylib.command.CommandUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class JournalSubcommand {
   private final String name;

   public JournalSubcommand(@NonNull String name) {
      this.name = name;
   }

   public ArgumentBuilder<CommandSourceStack, ?> get() {
      return ((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal(this.name).requires(stack -> stack.getSender().hasPermission("emf.journal")))
            .executes(ctx -> {
               Player player = CommandUtils.requirePlayer(ctx);
               this.execute(player, null);
               return 1;
            }))
         .then(this.withRarity());
   }

   private ArgumentBuilder<CommandSourceStack, ?> withRarity() {
      return Commands.argument("rarity", new RarityArgument()).executes(ctx -> {
         Player player = CommandUtils.requirePlayer(ctx);
         IRarity rarity = (IRarity)ctx.getArgument("rarity", IRarity.class);
         this.execute(player, rarity);
         return 1;
      });
   }

   private void execute(@NonNull Player player, @Nullable IRarity rarity) {
      if (!DatabaseUtil.isDatabaseOnline()) {
         ConfigMessage.JOURNAL_DISABLED.getMessage().send(player);
      } else {
         FishJournalGui.openAsync(player, rarity);
      }
   }
}
