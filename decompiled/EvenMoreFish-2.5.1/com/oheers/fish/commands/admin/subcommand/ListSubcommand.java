package com.oheers.fish.commands.admin.subcommand;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.api.fishing.items.IRarity;
import com.oheers.fish.commands.EMFCommand;
import com.oheers.fish.commands.arguments.RarityArgument;
import com.oheers.fish.fishing.items.FishManager;
import com.oheers.fish.messages.EMFSingleMessage;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent.Builder;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEvent.Action;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NonNull;

public class ListSubcommand implements EMFCommand {
   private final String name;

   public ListSubcommand(@NonNull String name) {
      this.name = name;
   }

   public LiteralArgumentBuilder<CommandSourceStack> get() {
      return (LiteralArgumentBuilder<CommandSourceStack>)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal(
                           this.name
                        )
                        .then(this.rarities()))
                     .then(this.fish()))
                  .then(this.rewardTypes()))
               .then(this.requirementTypes()))
            .then(this.itemAddons()))
         .then(this.competitionTypes());
   }

   private ArgumentBuilder<CommandSourceStack, ?> rarities() {
      return Commands.literal("rarities").executes(ctx -> {
         this.showRarities(((CommandSourceStack)ctx.getSource()).getSender());
         return 1;
      });
   }

   private ArgumentBuilder<CommandSourceStack, ?> fish() {
      return Commands.literal("fish").then(Commands.argument("rarity", new RarityArgument()).executes(ctx -> {
         IRarity rarity = (IRarity)ctx.getArgument("rarity", IRarity.class);
         this.showFish(((CommandSourceStack)ctx.getSource()).getSender(), rarity);
         return 1;
      }));
   }

   private ArgumentBuilder<CommandSourceStack, ?> rewardTypes() {
      return Commands.literal("rewardTypes").executes(ctx -> {
         this.listRewardTypes(((CommandSourceStack)ctx.getSource()).getSender());
         return 1;
      });
   }

   private ArgumentBuilder<CommandSourceStack, ?> requirementTypes() {
      return Commands.literal("requirementTypes").executes(ctx -> {
         this.listRequirementTypes(((CommandSourceStack)ctx.getSource()).getSender());
         return 1;
      });
   }

   private ArgumentBuilder<CommandSourceStack, ?> itemAddons() {
      return Commands.literal("itemAddons").executes(ctx -> {
         this.listItemAddons(((CommandSourceStack)ctx.getSource()).getSender());
         return 1;
      });
   }

   private ArgumentBuilder<CommandSourceStack, ?> competitionTypes() {
      return Commands.literal("competitionTypes").executes(ctx -> {
         this.listCompetitionTypes(((CommandSourceStack)ctx.getSource()).getSender());
         return 1;
      });
   }

   private void showRarities(@NonNull CommandSender sender) {
      Builder builder = Component.text();

      for (IRarity rarity : FishManager.getInstance().getRarityMap().values()) {
         Builder rarityBuilder = Component.text();
         EMFSingleMessage message = EMFSingleMessage.fromString("<gray>[</gray>{rarity}<gray>]</gray>");
         message.setVariable("{rarity}", rarity.getDisplayName());
         rarityBuilder.append(message.getComponentMessage());
         rarityBuilder.hoverEvent(
            HoverEvent.hoverEvent(Action.SHOW_TEXT, EMFSingleMessage.fromString("Click to view " + rarity.getId() + " fish.").getComponentMessage())
         );
         rarityBuilder.clickEvent(ClickEvent.runCommand("/emf admin list fish " + rarity.getId()));
         builder.append(rarityBuilder);
      }

      sender.sendMessage(builder.build());
   }

   private void showFish(@NonNull CommandSender sender, @NonNull IRarity rarity) {
      Builder builder = Component.text();
      builder.append(rarity.getDisplayName());
      builder.append(Component.space());

      for (IFish fish : rarity.getOriginalFishList()) {
         Builder fishBuilder = Component.text();
         EMFSingleMessage message = EMFSingleMessage.fromString("<gray>[</gray>{fish}<gray>]</gray>");
         message.setVariable("{fish}", fish.getDisplayName());
         fishBuilder.append(message.getComponentMessage());
         fishBuilder.hoverEvent(HoverEvent.hoverEvent(Action.SHOW_TEXT, Component.text("Click to receive fish")));
         fishBuilder.clickEvent(ClickEvent.runCommand("/emf admin fish " + rarity.getId() + " " + fish.getId().replace(" ", "_")));
         builder.append(fishBuilder);
      }

      sender.sendMessage(builder.build());
   }
}
