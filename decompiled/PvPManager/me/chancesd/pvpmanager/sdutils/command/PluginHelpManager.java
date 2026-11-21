package me.chancesd.pvpmanager.sdutils.command;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import me.chancesd.pvpmanager.sdutils.display.chat.ChatMenu;
import me.chancesd.pvpmanager.sdutils.display.chat.NavigationButtons;
import me.chancesd.pvpmanager.sdutils.display.chat.content.StaticContentProvider;
import me.chancesd.pvpmanager.sdutils.plugin.SDPlugin;
import me.chancesd.pvpmanager.sdutils.utils.ChatUtils;
import org.bukkit.command.CommandSender;

public class PluginHelpManager {
   private static final String DEFAULT_PLUGIN_COLOR_CODE = "4";
   private final SDPlugin plugin;
   private final String pluginName;
   private final String pluginVersion;

   public PluginHelpManager(SDPlugin plugin) {
      this.plugin = plugin;
      this.pluginName = plugin.getName();
      this.pluginVersion = plugin.getDescription().getVersion();
   }

   public ChatMenu generatePluginHelp(CommandSender sender, String navigationCommand) {
      return this.generatePluginHelp(sender, navigationCommand, 15);
   }

   public ChatMenu generatePluginHelp(CommandSender sender, String navigationCommand, int linesPerPage) {
      StaticContentProvider contentProvider = new StaticContentProvider();
      Collection<BaseCommand> allCommands = this.plugin.getRegisteredCommands();
      List<BaseCommand> availableCommands = allCommands.stream().filter(cmd -> cmd.hasPermission(sender)).collect(Collectors.toList());
      if (availableCommands.isEmpty()) {
         contentProvider.addLine("&7No commands available.", null, null);
      } else {
         contentProvider.addLine("#4CAF50&l► Available Commands:", null, null);

         for (BaseCommand command : availableCommands) {
            this.addCommandToContent(contentProvider, command);
         }
      }

      String header = String.format(
         "#607D8B&l╔══════ §%s§l%s #f7e758&lv%s #9E9E9E(Page {page}/{total}) #607D8B&l══════╗", this.getPluginColorCode(), this.pluginName, this.pluginVersion
      );
      return ChatMenu.builder()
         .header(header)
         .footer("#607D8B&l╚══════════════════════════════╝")
         .linesPerPage(linesPerPage)
         .contentProvider(contentProvider)
         .navigation(
            NavigationButtons.builder().navigationPrefix(navigationCommand).previousText("&7[&c« Previous Page&7]").nextText("&7[&cNext Page »&7]").build()
         )
         .build();
   }

   private void addCommandToContent(StaticContentProvider contentProvider, BaseCommand command) {
      List<String> aliases = command.getAliases();
      String commandName = aliases.isEmpty() ? "unknown" : aliases.get(0);
      String usage = command.getUsage();
      String description = command.getDescription();
      String displayText;
      if (usage != null && !usage.isEmpty()) {
         displayText = String.format("  #4CAF50%s #9E9E9E- &f%s", usage, description);
      } else {
         displayText = String.format("  #4CAF50/%s #9E9E9E- &f%s", commandName, description);
      }

      String clickCommand;
      if (usage != null && !usage.isEmpty()) {
         clickCommand = usage.split(" ")[0];
      } else {
         clickCommand = "/" + commandName;
      }

      String hoverText = this.buildHoverText(command, description);
      contentProvider.addLine(displayText, clickCommand, hoverText);
   }

   private String buildHoverText(BaseCommand command, String description) {
      StringBuilder hover = new StringBuilder();
      String usage = command.getUsage();
      boolean needsParameters = usage != null && usage.contains("<") || usage != null && usage.contains("[");
      if (needsParameters) {
         hover.append("&7Click to see command usage");
      } else {
         hover.append("&7Click to execute command");
      }

      if (description != null && !description.isEmpty()) {
         hover.append("\n&7&o").append(description);
      }

      Set<String> permissions = command.getPermissions();
      if (!permissions.isEmpty()) {
         hover.append("\n&#992222&lRequires: &#c98d81").append(String.join(", ", permissions));
      }

      return ChatUtils.colorize(hover.toString());
   }

   private String getPluginColorCode() {
      return "4";
   }

   public ChatMenu generateDefaultPluginHelp(CommandSender sender) {
      String defaultNavCommand = "/" + this.pluginName.toLowerCase() + " help";
      return this.generatePluginHelp(sender, defaultNavCommand);
   }
}
