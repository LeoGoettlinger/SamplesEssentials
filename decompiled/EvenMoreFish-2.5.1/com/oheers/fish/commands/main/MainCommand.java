package com.oheers.fish.commands.main;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.oheers.fish.Checks;
import com.oheers.fish.commands.EMFCommand;
import com.oheers.fish.commands.HelpMessage;
import com.oheers.fish.commands.admin.AdminCommand;
import com.oheers.fish.commands.main.subcommand.JournalSubcommand;
import com.oheers.fish.commands.main.subcommand.SellAllSubcommand;
import com.oheers.fish.commands.main.subcommand.ShopSubcommand;
import com.oheers.fish.commands.main.subcommand.ToggleSubcommand;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionManager;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.gui.guis.ApplyBaitsGui;
import com.oheers.fish.gui.guis.MainMenuGui;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.PrefixType;
import com.oheers.fish.messages.abstracted.EMFMessage;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.evenmorefish.fish.libs.daisylib.command.CommandUtils;
import org.jspecify.annotations.NonNull;

public class MainCommand implements EMFCommand {
   private static final HelpMessage HELP_MESSAGE = HelpMessage.helpMessage()
      .addEntry(MainConfig.getInstance().getAdminSubCommandName(), ConfigMessage.HELP_GENERAL_ADMIN::getMessage, "emf.admin")
      .addEntry(MainConfig.getInstance().getHelpSubCommandName(), ConfigMessage.HELP_GENERAL_HELP::getMessage, "emf.help")
      .addEntry(MainConfig.getInstance().getGuiSubCommandName(), ConfigMessage.HELP_GENERAL_GUI::getMessage, "emf.gui")
      .addEntry(MainConfig.getInstance().getTopSubCommandName(), ConfigMessage.HELP_GENERAL_TOP::getMessage, "emf.top")
      .addEntry(MainConfig.getInstance().getSellAllSubCommandName(), ConfigMessage.HELP_GENERAL_SELLALL::getMessage, "emf.sellall")
      .addEntry(MainConfig.getInstance().getApplyBaitsSubCommandName(), ConfigMessage.HELP_GENERAL_APPLYBAITS::getMessage, "emf.applybaits")
      .addEntry(MainConfig.getInstance().getJournalSubCommandName(), ConfigMessage.HELP_GENERAL_JOURNAL::getMessage, "emf.journal")
      .addEntry(MainConfig.getInstance().getNextSubCommandName(), ConfigMessage.HELP_GENERAL_NEXT::getMessage, "emf.next")
      .addEntry(MainConfig.getInstance().getToggleSubCommandName(), ConfigMessage.HELP_GENERAL_TOGGLE::getMessage, "emf.toggle")
      .addEntry(MainConfig.getInstance().getShopSubCommandName(), ConfigMessage.HELP_GENERAL_SHOP::getMessage, "emf.shop");

   @NonNull
   public LiteralCommandNode<CommandSourceStack> get() {
      return ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal(
                                             MainConfig.getInstance().getMainCommandName()
                                          )
                                          .executes(ctx -> {
                                             CommandSender sender = ((CommandSourceStack)ctx.getSource()).getSender();
                                             if (sender instanceof Player player) {
                                                if (player.hasPermission("emf.gui") && !MainConfig.getInstance().useOldBaseCommandBehavior()) {
                                                   new MainMenuGui(player).open();
                                                   return 1;
                                                } else {
                                                   sendHelpMessage(player);
                                                   return 1;
                                                }
                                             } else {
                                                sendHelpMessage(sender);
                                                return 1;
                                             }
                                          }))
                                       .then(this.admin()))
                                    .then(this.shop()))
                                 .then(this.journal()))
                              .then(this.toggle()))
                           .then(this.next()))
                        .then(this.help()))
                     .then(this.gui()))
                  .then(this.top()))
               .then(this.sellAll()))
            .then(this.applyBaits()))
         .build();
   }

   @NonNull
   protected ArgumentBuilder<CommandSourceStack, ?> admin() {
      return new AdminCommand(MainConfig.getInstance().getAdminSubCommandName()).getAsArgument();
   }

   @NonNull
   protected ArgumentBuilder<CommandSourceStack, ?> shop() {
      return new ShopSubcommand(MainConfig.getInstance().getShopSubCommandName()).get();
   }

   @NonNull
   protected ArgumentBuilder<CommandSourceStack, ?> journal() {
      return new JournalSubcommand(MainConfig.getInstance().getJournalSubCommandName()).get();
   }

   @NonNull
   protected ArgumentBuilder<CommandSourceStack, ?> toggle() {
      return new ToggleSubcommand(MainConfig.getInstance().getToggleSubCommandName()).get();
   }

   @NonNull
   public ArgumentBuilder<CommandSourceStack, ?> next() {
      return ((LiteralArgumentBuilder)Commands.literal(MainConfig.getInstance().getNextSubCommandName())
            .requires(stack -> stack.getSender().hasPermission("emf.next") && CompetitionManager.getInstance().hasTimings()))
         .executes(ctx -> {
            EMFMessage message = CompetitionManager.getInstance().getNextCompetitionMessage();
            message.prependMessage(PrefixType.DEFAULT.getPrefix());
            message.send(((CommandSourceStack)ctx.getSource()).getSender());
            return 1;
         });
   }

   @NonNull
   public ArgumentBuilder<CommandSourceStack, ?> help() {
      return ((LiteralArgumentBuilder)Commands.literal(MainConfig.getInstance().getHelpSubCommandName())
            .requires(stack -> stack.getSender().hasPermission("emf.help")))
         .executes(ctx -> {
            sendHelpMessage(((CommandSourceStack)ctx.getSource()).getSender());
            return 1;
         });
   }

   @NonNull
   public ArgumentBuilder<CommandSourceStack, ?> gui() {
      return ((LiteralArgumentBuilder)Commands.literal(MainConfig.getInstance().getGuiSubCommandName())
            .requires(stack -> stack.getSender().hasPermission("emf.gui")))
         .executes(ctx -> {
            Player player = CommandUtils.requirePlayer(ctx);
            new MainMenuGui(player).open();
            return 1;
         });
   }

   @NonNull
   public ArgumentBuilder<CommandSourceStack, ?> top() {
      return ((LiteralArgumentBuilder)Commands.literal(MainConfig.getInstance().getTopSubCommandName())
            .requires(stack -> stack.getSender().hasPermission("emf.top")))
         .executes(ctx -> {
            CommandSender sender = ((CommandSourceStack)ctx.getSource()).getSender();
            Competition active = CompetitionManager.getInstance().getActiveCompetition();
            if (active == null) {
               ConfigMessage.NO_COMPETITION_RUNNING.getMessage().send(sender);
               return 1;
            } else {
               active.sendLeaderboard(sender);
               return 1;
            }
         });
   }

   @NonNull
   public ArgumentBuilder<CommandSourceStack, ?> sellAll() {
      return new SellAllSubcommand(MainConfig.getInstance().getSellAllSubCommandName()).get();
   }

   @NonNull
   public ArgumentBuilder<CommandSourceStack, ?> applyBaits() {
      return ((LiteralArgumentBuilder)Commands.literal(MainConfig.getInstance().getApplyBaitsSubCommandName())
            .requires(stack -> stack.getSender().hasPermission("emf.applybaits")))
         .executes(ctx -> {
            Player player = CommandUtils.requirePlayer(ctx);
            if (!Checks.canUseRod(player.getInventory().getItemInMainHand())) {
               ConfigMessage.BAIT_INVALID_ROD.getMessage().send(player);
               return 1;
            } else {
               new ApplyBaitsGui(player, null).open();
               return 1;
            }
         });
   }

   public static void sendHelpMessage(CommandSender sender) {
      HELP_MESSAGE.send(sender);
   }
}
