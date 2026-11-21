package com.oheers.fish.commands;

import com.oheers.fish.config.MainConfig;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.EMFListMessage;
import com.oheers.fish.messages.abstracted.EMFMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NonNull;

public class HelpMessage {
   private final String subcommand;
   private final List<HelpMessage.HelpMessageEntry> entries = new ArrayList<>();
   private Predicate<CommandSender> defaultRequirement = sender -> true;

   private HelpMessage(@NonNull String subcommand) {
      this.subcommand = subcommand;
   }

   public static HelpMessage helpMessage() {
      return new HelpMessage("");
   }

   public static HelpMessage helpMessage(@NonNull String subcommand) {
      return new HelpMessage(subcommand);
   }

   public HelpMessage addEntry(@NonNull String name, @NonNull Supplier<EMFMessage> description, @NonNull Predicate<CommandSender> requirement) {
      this.entries.add(new HelpMessage.HelpMessageEntry(name, description, requirement));
      return this;
   }

   public HelpMessage addEntry(@NonNull String name, @NonNull Supplier<EMFMessage> description, @NonNull String permissionRequirement) {
      Predicate<CommandSender> requirement = sender -> sender.hasPermission(permissionRequirement);
      this.entries.add(new HelpMessage.HelpMessageEntry(name, description, requirement));
      return this;
   }

   public HelpMessage addEntry(@NonNull String name, @NonNull Supplier<EMFMessage> description) {
      this.entries.add(new HelpMessage.HelpMessageEntry(name, description, this.defaultRequirement));
      return this;
   }

   public HelpMessage setDefaultRequirement(@NonNull Predicate<CommandSender> requirement) {
      this.defaultRequirement = requirement;
      return this;
   }

   public HelpMessage setDefaultRequirement(@NonNull String permissionRequirement) {
      this.defaultRequirement = sender -> sender.hasPermission(permissionRequirement);
      return this;
   }

   private String correctCommand(@NonNull String name) {
      StringBuilder builder = new StringBuilder("/");
      builder.append(MainConfig.getInstance().getMainCommandName()).append(" ");
      if (!this.subcommand.isEmpty()) {
         builder.append(this.subcommand).append(" ");
      }

      builder.append(name);
      return builder.toString();
   }

   public void send(@NonNull CommandSender sender) {
      EMFListMessage message = ConfigMessage.HELP_GENERAL_TITLE.getMessage().toListMessage();
      this.entries.forEach(entry -> {
         if (entry.requirement().test(sender)) {
            EMFMessage description = entry.description().get();
            if (description != null && !description.isEmpty()) {
               EMFMessage usage = ConfigMessage.HELP_FORMAT.getMessage();
               usage.setVariable("{command}", this.correctCommand(entry.name()));
               usage.setVariable("{description}", description);
               message.appendMessage(usage);
            }
         }
      });
      message.send(sender);
   }

   record HelpMessageEntry(@NonNull String name, @NonNull Supplier<EMFMessage> description, @NonNull Predicate<CommandSender> requirement) {
   }
}
