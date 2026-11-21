package com.oheers.fish.commands.admin.subcommand;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.oheers.fish.FishUtils;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionManager;
import com.oheers.fish.competition.CompetitionType;
import com.oheers.fish.competition.configs.CompetitionFile;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.abstracted.EMFMessage;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import org.evenmorefish.fish.commands.arguments.CompetitionFileArgument;
import org.evenmorefish.fish.commands.arguments.CompetitionTypeArgument;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class CompetitionSubcommand {
   private final String name;

   public CompetitionSubcommand(@NonNull String name) {
      this.name = name;
   }

   public LiteralArgumentBuilder<CommandSourceStack> get() {
      return (LiteralArgumentBuilder<CommandSourceStack>)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal(this.name)
                  .then(this.start()))
               .then(this.end()))
            .then(this.test()))
         .then(this.extend());
   }

   private ArgumentBuilder<CommandSourceStack, ?> start() {
      return Commands.literal("start").then(((RequiredArgumentBuilder)Commands.argument("file", new CompetitionFileArgument()).executes(ctx -> {
         CompetitionFile file = (CompetitionFile)ctx.getArgument("file", CompetitionFile.class);
         this.start(((CommandSourceStack)ctx.getSource()).getSender(), file, null);
         return 1;
      })).then(Commands.argument("durationSeconds", IntegerArgumentType.integer(1)).executes(ctx -> {
         CompetitionFile file = (CompetitionFile)ctx.getArgument("file", CompetitionFile.class);
         int duration = (Integer)ctx.getArgument("durationSeconds", int.class);
         this.start(((CommandSourceStack)ctx.getSource()).getSender(), file, duration);
         return 1;
      })));
   }

   private void start(@NonNull CommandSender sender, @NonNull CompetitionFile file, @Nullable Integer duration) {
      if (CompetitionManager.getInstance().isCompetitionActive()) {
         ConfigMessage.COMPETITION_ALREADY_RUNNING.getMessage().send(sender);
      } else {
         Competition competition = new Competition(file);
         competition.setAdminStarted(true);
         if (duration != null) {
            competition.setTime(duration.intValue());
         }

         competition.begin();
      }
   }

   private ArgumentBuilder<CommandSourceStack, ?> end() {
      return Commands.literal("end").executes(ctx -> {
         Competition active = CompetitionManager.getInstance().getActiveCompetition();
         if (active == null) {
            ConfigMessage.NO_COMPETITION_RUNNING.getMessage().send(((CommandSourceStack)ctx.getSource()).getSender());
            return 1;
         } else {
            active.end(false);
            return 1;
         }
      });
   }

   private ArgumentBuilder<CommandSourceStack, ?> test() {
      return ((LiteralArgumentBuilder)Commands.literal("test").executes(ctx -> {
         this.test(((CommandSourceStack)ctx.getSource()).getSender(), 1, CompetitionType.DEFAULT);
         return 1;
      })).then(((RequiredArgumentBuilder)Commands.argument("durationMinutes", IntegerArgumentType.integer(1)).executes(ctx -> {
         int duration = (Integer)ctx.getArgument("durationMinutes", int.class);
         this.test(((CommandSourceStack)ctx.getSource()).getSender(), duration, CompetitionType.DEFAULT);
         return 1;
      })).then(Commands.argument("type", new CompetitionTypeArgument()).executes(ctx -> {
         int duration = (Integer)ctx.getArgument("durationMinutes", int.class);
         CompetitionType type = (CompetitionType)ctx.getArgument("type", CompetitionType.class);
         this.test(((CommandSourceStack)ctx.getSource()).getSender(), duration, type);
         return 1;
      })));
   }

   private void test(CommandSender sender, int duration, CompetitionType type) {
      if (CompetitionManager.getInstance().isCompetitionActive()) {
         ConfigMessage.COMPETITION_ALREADY_RUNNING.getMessage().send(sender);
      } else {
         CompetitionFile file = new CompetitionFile("adminTest", type, duration);
         Competition competition = new Competition(file);
         competition.setAdminStarted(true);
         competition.begin();
      }
   }

   private ArgumentBuilder<CommandSourceStack, ?> extend() {
      return Commands.literal("extend").then(Commands.argument("durationSeconds", IntegerArgumentType.integer(1)).executes(ctx -> {
         int duration = (Integer)ctx.getArgument("durationSeconds", int.class);
         Competition active = CompetitionManager.getInstance().getActiveCompetition();
         if (active == null) {
            ConfigMessage.NO_COMPETITION_RUNNING.getMessage().send(((CommandSourceStack)ctx.getSource()).getSender());
            return 1;
         } else {
            active.addTime(duration);
            EMFMessage message = ConfigMessage.COMPETITION_TIME_EXTENDED.getMessage();
            message.setVariable("{duration}", FishUtils.timeFormat(duration));
            message.broadcast();
            return 1;
         }
      }));
   }
}
