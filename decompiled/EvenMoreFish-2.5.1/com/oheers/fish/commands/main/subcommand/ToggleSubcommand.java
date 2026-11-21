package com.oheers.fish.commands.main.subcommand;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.permissions.UserPerms;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;
import org.evenmorefish.fish.libs.daisylib.command.CommandUtils;
import org.jspecify.annotations.NonNull;

public class ToggleSubcommand {
   private final String name;

   public ToggleSubcommand(@NonNull String name) {
      this.name = name;
   }

   public ArgumentBuilder<CommandSourceStack, ?> get() {
      return ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal(this.name)
                     .requires(stack -> UserPerms.checkTogglePerms(stack.getSender())))
                  .executes(ctx -> {
                     Player player = CommandUtils.requirePlayer(ctx);
                     EvenMoreFish.getInstance().getToggle().performFishToggle(player);
                     return 1;
                  }))
               .then(this.fishing()))
            .then(this.bossbar()))
         .then(this.catchMessage());
   }

   private ArgumentBuilder<CommandSourceStack, ?> fishing() {
      return ((LiteralArgumentBuilder)Commands.literal("fishing").requires(stack -> stack.getSender().hasPermission("emf.toggle.fishing"))).executes(ctx -> {
         Player player = CommandUtils.requirePlayer(ctx);
         EvenMoreFish.getInstance().getToggle().performFishToggle(player);
         return 1;
      });
   }

   private ArgumentBuilder<CommandSourceStack, ?> bossbar() {
      return ((LiteralArgumentBuilder)Commands.literal("bossbar").requires(stack -> stack.getSender().hasPermission("emf.toggle.bossbar"))).executes(ctx -> {
         Player player = CommandUtils.requirePlayer(ctx);
         EvenMoreFish.getInstance().getToggle().performBossBarToggle(player);
         return 1;
      });
   }

   private ArgumentBuilder<CommandSourceStack, ?> catchMessage() {
      return ((LiteralArgumentBuilder)Commands.literal("catchMessage").requires(stack -> stack.getSender().hasPermission("emf.toggle.catchmessage")))
         .executes(ctx -> {
            Player player = CommandUtils.requirePlayer(ctx);
            EvenMoreFish.getInstance().getToggle().performCatchMessageToggle(player);
            return 1;
         });
   }
}
