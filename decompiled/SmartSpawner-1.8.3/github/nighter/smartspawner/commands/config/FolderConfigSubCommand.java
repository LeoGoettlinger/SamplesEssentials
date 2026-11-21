package github.nighter.smartspawner.commands.config;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.commands.BaseSubCommand;
import github.nighter.smartspawner.config.Config;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class FolderConfigSubCommand extends BaseSubCommand {
   private static final String VALUE_ARGUMENT = "value";
   private final FolderConfigSubCommand.ConfigOption option;

   public FolderConfigSubCommand(SmartSpawner plugin, FolderConfigSubCommand.ConfigOption option) {
      super(plugin);
      this.option = option;
   }

   @Override
   public String getName() {
      return this.option.commandName();
   }

   @Override
   public String getPermission() {
      return this.option.permission();
   }

   @Override
   public String getDescription() {
      return this.option.description();
   }

   @Override
   public LiteralArgumentBuilder<CommandSourceStack> build() {
      LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal(this.getName());
      builder.requires(source -> this.hasPermission(source.getSender()));
      builder.executes(this::execute);
      builder.then(Commands.argument("value", StringArgumentType.word()).suggests(this.createFolderSuggestions()).executes(this::executeSet));
      return builder;
   }

   @Override
   public int execute(CommandContext<CommandSourceStack> context) {
      this.logCommandExecution(context);
      CommandSender sender = ((CommandSourceStack)context.getSource()).getSender();
      String currentValue = this.plugin.getConfig().getString(this.option.configKey(), this.option.defaultValue());
      Map<String, String> placeholders = new HashMap<>();
      placeholders.put("current", currentValue);
      placeholders.put("available", String.join(", ", this.getAvailableFolderNames()));
      this.plugin.getMessageService().sendMessage(sender, this.option.messagePrefix() + ".current", placeholders);
      return 1;
   }

   private int executeSet(CommandContext<CommandSourceStack> context) {
      this.logCommandExecution(context);
      CommandSender sender = ((CommandSourceStack)context.getSource()).getSender();
      String requestedValue = StringArgumentType.getString(context, "value");
      Optional<String> resolvedValue = this.resolveFolderName(requestedValue);
      if (resolvedValue.isEmpty()) {
         Map<String, String> placeholders = new HashMap<>();
         placeholders.put("value", requestedValue);
         placeholders.put("available", String.join(", ", this.getAvailableFolderNames()));
         this.plugin.getMessageService().sendMessage(sender, this.option.messagePrefix() + ".invalid", placeholders);
         return 0;
      } else {
         String oldValue = this.plugin.getConfig().getString(this.option.configKey(), this.option.defaultValue());
         String newValue = resolvedValue.get();
         if (oldValue.equals(newValue)) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("value", newValue);
            this.plugin.getMessageService().sendMessage(sender, this.option.messagePrefix() + ".already_set", placeholders);
            return 1;
         } else {
            try {
               this.plugin.getConfig().set(this.option.configKey(), newValue);
               this.plugin.saveConfig();
               this.reloadAfterChange();
               Map<String, String> placeholders = new HashMap<>();
               placeholders.put("old_value", oldValue);
               placeholders.put("new_value", newValue);
               this.plugin.getMessageService().sendMessage(sender, this.option.messagePrefix() + ".success", placeholders);
               return 1;
            } catch (Exception var8) {
               this.plugin.getLogger().severe("Failed to change " + this.option.displayName() + ": " + var8.getMessage());
               this.plugin.getMessageService().sendMessage(sender, this.option.messagePrefix() + ".error");
               return 0;
            }
         }
      }
   }

   private void reloadAfterChange() {
      this.plugin.getSpawnerItemFactory().clearAllCaches();
      this.plugin.getMessageService().clearKeyExistsCache();
      Config.reload(this.plugin);
      if (this.option == FolderConfigSubCommand.ConfigOption.LANGUAGE) {
         this.plugin.getLanguageManager().reloadLanguages();
      }

      this.plugin.getGuiLayoutConfig().loadLayout();
      this.plugin.getSpawnerMenuUI().loadConfig();
      if (this.plugin.getSpawnerClickManager() != null) {
         this.plugin.getSpawnerClickManager().loadConfig();
      }

      if (this.plugin.getSpawnerExplosionListener() != null) {
         this.plugin.getSpawnerExplosionListener().loadConfig();
      }

      if (this.plugin.getSpawnerGuiViewManager() != null) {
         this.plugin.getSpawnerGuiViewManager().recheckTimerPlaceholders();
      }

      this.plugin.getSpawnerItemFactory().reload();
      this.plugin.getSpawnerManager().reloadAllHolograms();
      this.plugin.reload();
      this.plugin.getMessageService().clearKeyExistsCache();
   }

   private SuggestionProvider<CommandSourceStack> createFolderSuggestions() {
      return (context, builder) -> {
         String input = builder.getRemaining().toLowerCase(Locale.ROOT);

         for (String folderName : this.getAvailableFolderNames()) {
            if (folderName.toLowerCase(Locale.ROOT).startsWith(input)) {
               builder.suggest(folderName, new LiteralMessage(this.option.suggestionTooltip()));
            }
         }

         return builder.buildFuture();
      };
   }

   private Optional<String> resolveFolderName(String requestedValue) {
      for (String folderName : this.getAvailableFolderNames()) {
         if (folderName.equals(requestedValue)) {
            return Optional.of(folderName);
         }
      }

      String normalized = requestedValue.toLowerCase(Locale.ROOT);
      return this.getAvailableFolderNames().stream().filter(folderNamex -> folderNamex.toLowerCase(Locale.ROOT).equals(normalized)).findFirst();
   }

   private List<String> getAvailableFolderNames() {
      File directory = new File(this.plugin.getDataFolder(), this.option.folderName());
      if (!directory.exists() && !directory.mkdirs()) {
         return List.of();
      } else {
         File[] folders = directory.listFiles(File::isDirectory);
         return folders != null && folders.length != 0
            ? Arrays.stream(folders).map(File::getName).sorted(Comparator.comparing(name -> name.toLowerCase(Locale.ROOT))).collect(Collectors.toList())
            : List.of();
      }
   }

   public static enum ConfigOption {
      LANGUAGE(
         "language",
         "language",
         "language",
         "en_US",
         "smartspawner.command.language",
         "Change the active SmartSpawner language",
         "language",
         "language",
         "Language folder"
      ),
      GUI_LAYOUT(
         "gui_layout",
         "gui_layout",
         "gui_layouts",
         "default",
         "smartspawner.command.gui_layout",
         "Change the active SmartSpawner GUI layout",
         "gui_layout",
         "GUI layout",
         "GUI layout folder"
      );

      private final String commandName;
      private final String configKey;
      private final String folderName;
      private final String defaultValue;
      private final String permission;
      private final String description;
      private final String messagePrefix;
      private final String displayName;
      private final String suggestionTooltip;

      private ConfigOption(
         String commandName,
         String configKey,
         String folderName,
         String defaultValue,
         String permission,
         String description,
         String messagePrefix,
         String displayName,
         String suggestionTooltip
      ) {
         this.commandName = commandName;
         this.configKey = configKey;
         this.folderName = folderName;
         this.defaultValue = defaultValue;
         this.permission = permission;
         this.description = description;
         this.messagePrefix = messagePrefix;
         this.displayName = displayName;
         this.suggestionTooltip = suggestionTooltip;
      }

      String commandName() {
         return this.commandName;
      }

      String configKey() {
         return this.configKey;
      }

      String folderName() {
         return this.folderName;
      }

      String defaultValue() {
         return this.defaultValue;
      }

      String permission() {
         return this.permission;
      }

      String description() {
         return this.description;
      }

      String messagePrefix() {
         return this.messagePrefix;
      }

      String displayName() {
         return this.displayName;
      }

      String suggestionTooltip() {
         return this.suggestionTooltip;
      }
   }
}
