package me.clip.placeholderapi.commands.impl.local;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.commands.PlaceholderCommand;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.expansion.manager.LocalExpansionManager;
import me.clip.placeholderapi.util.Futures;
import me.clip.placeholderapi.util.Msg;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

public final class CommandExpansionRegister extends PlaceholderCommand {
   public CommandExpansionRegister() {
      super("register");
   }

   @Override
   public void evaluate(@NotNull PlaceholderAPIPlugin plugin, @NotNull CommandSender sender, @NotNull String alias, @NotNull @Unmodifiable List<String> params) {
      if (params.size() < 1) {
         Msg.msg(sender, "&cYou must specify the name of an expansion file.");
      } else {
         LocalExpansionManager manager = plugin.getLocalExpansionManager();
         File file = new File(manager.getExpansionsFolder(), params.get(0));
         if (file.exists() && file.getParentFile().equals(manager.getExpansionsFolder())) {
            Futures.onMainThread(plugin, manager.findExpansionInFile(file), (clazz, exception) -> {
               if (exception != null) {
                  Msg.msg(sender, "&cFailed to find expansion in file: &f" + file);
                  plugin.getLogger().log(Level.WARNING, "failed to find expansion in file: " + file, exception);
               } else if (clazz == null) {
                  Msg.msg(sender, "&cNo expansion class found in file: &f" + file);
               } else {
                  Optional<PlaceholderExpansion> expansion = manager.register(clazz);
                  if (!expansion.isPresent()) {
                     Msg.msg(sender, "&cFailed to register expansion from &f" + params.get(0));
                  } else {
                     Msg.msg(sender, "&aSuccessfully registered expansion: &f" + expansion.get().getName());
                  }
               }
            });
         } else {
            Msg.msg(sender, "&cThe file &f" + file.getName() + "&c doesn't exist!");
         }
      }
   }

   @Override
   public void complete(
      @NotNull PlaceholderAPIPlugin plugin,
      @NotNull CommandSender sender,
      @NotNull String alias,
      @NotNull @Unmodifiable List<String> params,
      @NotNull List<String> suggestions
   ) {
      if (params.size() <= 1) {
         String[] fileNames = plugin.getLocalExpansionManager().getExpansionsFolder().list((dir, name) -> name.endsWith(".jar"));
         if (fileNames != null && fileNames.length != 0) {
            suggestByParameter(Arrays.stream(fileNames), suggestions, params.isEmpty() ? null : params.get(0));
         }
      }
   }
}
