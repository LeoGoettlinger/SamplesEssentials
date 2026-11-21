package su.nightexpress.nightcore.commands.command;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.NightCorePlugin;
import su.nightexpress.nightcore.NightPlugin;
import su.nightexpress.nightcore.commands.CommandRequirement;
import su.nightexpress.nightcore.commands.NodeUtils;
import su.nightexpress.nightcore.commands.argument.ArgumentReader;
import su.nightexpress.nightcore.commands.context.CommandContext;
import su.nightexpress.nightcore.commands.context.CommandContextBuilder;
import su.nightexpress.nightcore.commands.context.Suggestions;
import su.nightexpress.nightcore.commands.exceptions.CommandSyntaxException;
import su.nightexpress.nightcore.commands.tree.CommandNode;
import su.nightexpress.nightcore.commands.tree.ExecutableNode;
import su.nightexpress.nightcore.core.config.CoreLang;
import su.nightexpress.nightcore.util.CommandUtil;
import su.nightexpress.nightcore.util.text.night.NightMessage;

public abstract class AbstractCommand<N extends ExecutableNode> extends Command implements NightCommand {
   private final NightCorePlugin plugin;
   private final N root;

   @Deprecated
   public AbstractCommand(@NonNull NightPlugin plugin, @NonNull N root, @NonNull List<String> aliases) {
      super(clean(root.getName()), clean(root.getDescription()), clean(root.getUsage()), aliases);
      this.plugin = plugin;
      this.root = root;
      this.setPermission(root.getPermission());
   }

   public AbstractCommand(@NonNull NightCorePlugin plugin, @NonNull N root, @NonNull List<String> aliases) {
      super(clean(root.getName()), clean(root.getDescription()), clean(root.getUsage()), aliases);
      this.plugin = plugin;
      this.root = root;
      this.setPermission(root.getPermission());
   }

   @NonNull
   private static String clean(@NonNull String string) {
      return NightMessage.stripTags(string);
   }

   @NonNull
   @Override
   public NightCorePlugin getPlugin() {
      return this.plugin;
   }

   @NonNull
   public N getRoot() {
      return this.root;
   }

   @Override
   public boolean register() {
      return CommandUtil.register(this, this.plugin.getName());
   }

   @Override
   public boolean unregister() {
      return CommandUtil.unregister(this);
   }

   @Override
   public boolean isRegistered() {
      return super.isRegistered();
   }

   @NonNull
   @Override
   public String getName() {
      return super.getName();
   }

   @Nullable
   @Override
   public String getPermission() {
      return super.getPermission();
   }

   @NonNull
   @Override
   public String getLabel() {
      return super.getLabel();
   }

   @NonNull
   @Override
   public List<String> getAliases() {
      return super.getAliases();
   }

   @NonNull
   @Override
   public String getDescription() {
      return super.getDescription();
   }

   @NonNull
   @Override
   public String getUsage() {
      return super.getUsage();
   }

   @Nullable
   private CommandContext parseNodes(@NonNull CommandNode node, @NonNull ArgumentReader reader, @NonNull CommandContextBuilder builder, boolean forExecution) {
      CommandSender sender = builder.getSender();
      if (!this.testRequirements(sender, node, forExecution)) {
         return null;
      } else {
         try {
            node.parse(reader, builder);
         } catch (CommandSyntaxException var8) {
            if (forExecution) {
               var8.getMessageLocale()
                  .withPrefix(this.plugin.getPrefix())
                  .send(
                     sender,
                     replacer -> replacer.replace("%name%", node.getLocalizedName())
                        .replace("%input%", reader.getCursorArgument())
                        .replace("%value%", String.valueOf(var8.getValue()))
                  );
               return null;
            }
         }

         reader.moveForward();
         if (reader.canMoveForward()) {
            Iterator executable = node.getRelevantNodes(reader).iterator();
            if (executable.hasNext()) {
               CommandNode child = (CommandNode)executable.next();
               return this.parseNodes(child, reader, builder, forExecution);
            }
         } else if (forExecution && node.hasRequiredArguments() && builder.getExecutor() != null) {
            ExecutableNode executable = builder.getExecutor();
            CoreLang.COMMAND_EXECUTION_MISSING_ARGUMENTS
               .withPrefix(this.plugin)
               .send(
                  sender,
                  replacer -> replacer.replace("%command%", NodeUtils.formatLabel(executable, builder)).replace("%description%", executable.getDescription())
               );
            return null;
         }

         return builder.build();
      }
   }

   private boolean testRequirements(@NonNull CommandSender sender, @NonNull CommandNode node, boolean forExecution) {
      if (!node.hasPermission(sender)) {
         if (forExecution) {
            CoreLang.ERROR_NO_PERMISSION.withPrefix(this.plugin).send(sender);
         }

         return false;
      } else {
         for (CommandRequirement requirement : node.getRequirements()) {
            if (!requirement.test(sender)) {
               if (forExecution) {
                  requirement.getMessage().withPrefix(this.plugin).send(sender);
               }

               return false;
            }
         }

         return true;
      }
   }

   private void listSuggestions(@NonNull CommandNode node, @NonNull ArgumentReader reader, @NonNull CommandContext context, @NonNull Suggestions suggestions) {
      if (!reader.isEnd()) {
         reader.moveForward();
         node.suggests(reader, context, suggestions);
         if (reader.canMoveForward()) {
            for (CommandNode child : node.getRelevantNodes(reader)) {
               this.listSuggestions(child, reader, context, suggestions);
            }
         }
      }
   }

   @Nullable
   private CommandContext parse(@NonNull CommandSender sender, @NonNull String label, String[] args, boolean forExecution) {
      ArgumentReader reader = ArgumentReader.forArgumentsWithLabel(label, args);
      CommandContextBuilder builder = new CommandContextBuilder(this.plugin, sender, this.root, reader.getString());
      return this.parseNodes(this.root, reader, builder, forExecution);
   }

   public boolean execute(@NonNull CommandSender sender, @NonNull String label, @NonNull String[] args) {
      CommandContext context = this.parse(sender, label, args, true);
      if (context == null) {
         return false;
      } else {
         ExecutableNode executor = context.getExecutor();
         if (executor == null) {
            return false;
         } else {
            try {
               return executor.run(context);
            } catch (CommandSyntaxException var7) {
               var7.getMessageLocale().withPrefix(this.plugin).send(sender);
               return false;
            }
         }
      }
   }

   @NonNull
   public List<String> tabComplete(@NonNull CommandSender sender, @NonNull String label, @NonNull String[] args) {
      if (args.length == 0) {
         return Collections.emptyList();
      } else {
         CommandContext context = this.parse(sender, label, args, false);
         if (context == null) {
            return Collections.emptyList();
         } else {
            ArgumentReader reader = ArgumentReader.forArgumentsWithLabel(label, args);
            Suggestions suggestions = new Suggestions();
            this.listSuggestions(this.root, reader, context, suggestions);
            return suggestions.getSuggestions();
         }
      }
   }
}
