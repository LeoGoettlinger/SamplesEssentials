package com.magmaguy.elitemobs.config.commands;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.commands.premade.CheckTierConfig;
import com.magmaguy.elitemobs.config.commands.premade.CheckTierOthersConfig;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.configuration.file.FileConfiguration;

public class CommandsConfig {
   private static final ArrayList<CommandsConfigFields> commandsConfigFields = new ArrayList<>(
      new ArrayList<>(List.of(new CheckTierConfig(), new CheckTierOthersConfig()))
   );

   private CommandsConfig() {
   }

   public static void initializeConfigs() {
      for (CommandsConfigFields commandsConfigFields : CommandsConfig.commandsConfigFields) {
         initializeConfiguration(commandsConfigFields);
      }
   }

   private static FileConfiguration initializeConfiguration(CommandsConfigFields commandsConfigFields) {
      File file = ConfigurationEngine.fileCreator("commands", commandsConfigFields.getFileName());
      FileConfiguration fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);
      commandsConfigFields.generateConfigDefaults(file, fileConfiguration);
      ConfigurationEngine.fileSaverOnlyDefaults(fileConfiguration, file);
      return fileConfiguration;
   }
}
