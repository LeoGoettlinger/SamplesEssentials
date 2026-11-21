package me.chancesd.pvpmanager.command;

import java.util.Set;
import me.chancesd.pvpmanager.PvPManager;
import me.chancesd.pvpmanager.sdutils.command.BaseCommand;
import me.chancesd.pvpmanager.sdutils.display.chat.ChatMenu;
import me.chancesd.pvpmanager.sdutils.display.chat.NavigationButtons;
import me.chancesd.pvpmanager.sdutils.display.chat.content.StaticContentProvider;
import org.bukkit.command.CommandSender;

public class Help {
   private static final String PLAYER_COMMAND_COLOR = "#4CAF50";
   private static final String STAFF_COMMAND_COLOR = "#FF5722";
   private static final String DESCRIPTION_COLOR = "#9E9E9E";
   private static final String PLAYER_CATEGORY = "player";
   private static final String STAFF_CATEGORY = "staff";
   private static final String PVPMANAGER_COMMAND = "pvpmanager";

   public void helpMenu(CommandSender sender, int page) {
      this.createHelpMenu(sender).show(sender, page);
   }

   private String buildHoverText(String actionText, String description, BaseCommand command) {
      Set<String> permissions = command.getPermissions();
      String usage = command.getUsage() != null && !command.getUsage().isEmpty() ? command.getUsage() : null;
      StringBuilder hoverText = new StringBuilder();
      hoverText.append("#90A4AE").append(actionText);
      hoverText.append("\n#455A64► #CFD8DC").append(description);
      boolean hasUsage = usage != null;
      boolean hasPermissions = !permissions.isEmpty();
      if (hasUsage || hasPermissions) {
         hoverText.append("\n");
      }

      if (hasUsage) {
         hoverText.append("\n#78909C&lUsage: #607D8B").append(usage);
      }

      if (hasPermissions) {
         String perms = String.join(", ", permissions);
         hoverText.append("\n#78909C&lRequires: #546E7A").append(perms);
      }

      return hoverText.toString();
   }

   private String buildExecuteHoverText(String description, BaseCommand command) {
      return this.buildHoverText("Click to execute command", description, command);
   }

   private String buildSyntaxHoverText(String description, BaseCommand command) {
      return this.buildHoverText("Click to see command usage", description, command);
   }

   private String getCommandDescription(BaseCommand command) {
      return command != null && command.getDescription() != null && !command.getDescription().isEmpty() ? command.getDescription() : "No description available";
   }

   private String getCommandUsage(BaseCommand command, String commandName) {
      return command != null && command.getUsage() != null && !command.getUsage().isEmpty() ? command.getUsage() : "/" + commandName;
   }

   private String buildAutoHoverText(BaseCommand command, String description) {
      String usage = this.getCommandUsage(command, "");
      boolean needsParameters = usage.contains("<");
      return needsParameters ? this.buildSyntaxHoverText(description, command) : this.buildExecuteHoverText(description, command);
   }

   private void addCommandIfAvailable(StaticContentProvider content, CommandSender sender, String commandName, String category) {
      PvPManager plugin = PvPManager.getInstance();
      BaseCommand command = plugin.getRegisteredCommand(commandName);
      if (command != null && command.hasPermission(sender)) {
         String description = this.getCommandDescription(command);
         String usage = this.getCommandUsage(command, commandName);
         String hoverText = this.buildAutoHoverText(command, description);
         String color = "player".equals(category) ? "#4CAF50" : "#FF5722";
         String displayText = String.format("  %s%s %s&8- &7%s", color, usage, "#9E9E9E", description);
         content.addLine(displayText, commandName, hoverText);
      }
   }

   private void addPmrSubcommandIfAvailable(StaticContentProvider content, CommandSender sender, String subcommandName) {
      PvPManager plugin = PvPManager.getInstance();
      BaseCommand pmrCommand = plugin.getRegisteredCommand("pvpmanager");
      if (pmrCommand != null) {
         BaseCommand subcommand = null;

         for (BaseCommand sub : pmrCommand.getSubCommands()) {
            if (sub.getAliases().contains(subcommandName)) {
               subcommand = sub;
               break;
            }
         }

         if (subcommand != null && subcommand.hasPermission(sender)) {
            String description = subcommand.getDescription();
            if (description == null || description.isEmpty()) {
               description = "No description available";
            }

            String usage = "/pmr " + subcommandName;
            String hoverText = this.buildAutoHoverText(subcommand, description);
            String displayText = String.format("  %s%s %s&8- &7%s", "#FF5722", usage, "#9E9E9E", description);
            content.addLine(displayText, usage, hoverText);
         }
      }
   }

   private ChatMenu createHelpMenu(CommandSender sender) {
      StaticContentProvider contentProvider = new StaticContentProvider();
      contentProvider.addLine("&a&l► Player Commands:", null, null);
      this.addCommandIfAvailable(contentProvider, sender, "pvp", "player");
      this.addCommandIfAvailable(contentProvider, sender, "pvpinfo", "player");
      this.addCommandIfAvailable(contentProvider, sender, "pvpstatus", "player");
      this.addCommandIfAvailable(contentProvider, sender, "pvplist", "player");
      this.addCommandIfAvailable(contentProvider, sender, "newbie", "player");
      contentProvider.addLine("", null, null);
      contentProvider.addLine("#FF5722&l► Staff Commands:", null, null);
      this.addCommandIfAvailable(contentProvider, sender, "pvpoverride", "staff");
      this.addCommandIfAvailable(contentProvider, sender, "pvpglobal", "staff");
      this.addCommandIfAvailable(contentProvider, sender, "pvptag", "staff");
      this.addCommandIfAvailable(contentProvider, sender, "untag", "staff");
      this.addCommandIfAvailable(contentProvider, sender, "announce", "staff");
      PvPManager plugin = PvPManager.getInstance();
      BaseCommand pmrCommand = plugin.getRegisteredCommand("pvpmanager");
      if (pmrCommand != null && pmrCommand.hasPermission(sender)) {
         this.addPmrSubcommandIfAvailable(contentProvider, sender, "worlds");
         this.addPmrSubcommandIfAvailable(contentProvider, sender, "reload");
         this.addPmrSubcommandIfAvailable(contentProvider, sender, "cleanup");
         this.addPmrSubcommandIfAvailable(contentProvider, sender, "update");
         this.addPmrSubcommandIfAvailable(contentProvider, sender, "convert");
         this.addPmrSubcommandIfAvailable(contentProvider, sender, "debug");
         this.addPmrSubcommandIfAvailable(contentProvider, sender, "locale");
      }

      String pluginVersion = plugin.getDescription().getVersion();
      return ChatMenu.builder()
         .header("#607D8B&l╔══════ §4§lPvP§8§lManager #f7e758&lv" + pluginVersion + " #9E9E9E({page}/{total}) #607D8B&l══════╗")
         .footer("#607D8B&l╚══════════════════════════════╝")
         .linesPerPage(15)
         .contentProvider(contentProvider)
         .navigation(NavigationButtons.builder().navigationPrefix("/pmr").build())
         .build();
   }
}
