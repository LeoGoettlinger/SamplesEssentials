package me.chancesd.pvpmanager.command;

import com.google.common.base.Strings;
import java.util.List;
import me.chancesd.pvpmanager.PvPManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.player.world.CombatWorld;
import me.chancesd.pvpmanager.sdutils.command.ArgumentType;
import me.chancesd.pvpmanager.sdutils.command.BaseCommand;
import me.chancesd.pvpmanager.sdutils.command.CommandArgument;
import me.chancesd.pvpmanager.sdutils.utils.Log;
import me.chancesd.pvpmanager.setting.Lang;
import me.chancesd.pvpmanager.setting.Permissions;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ComponentBuilder.FormatRetention;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.hover.content.Content;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Worlds extends BaseCommand {
   private final PvPManager plugin;

   public Worlds(PvPManager plugin) {
      this.plugin = plugin;
      this.description("Control panel to manage world PvP")
         .usage("/pmr worlds [list|set] [world] [option] [value]")
         .permission(Permissions.ADMIN.getPermission());
      this.subCommand("list", new Worlds.WorldsListCommand());
      this.subCommand("set", new Worlds.WorldsSetCommand());
   }

   @Override
   public void execute(CommandSender sender, String label, List<CommandArgument> args) {
      if (args.isEmpty()) {
         if (sender instanceof Player player) {
            this.createWorldMenu(player);
         } else {
            sender.sendMessage("§cConsole must use '/pmr worlds list' to see world information.");
         }
      }
   }

   public void createWorldMenu(Player player) {
      TextComponent menuHeader = new TextComponent("" + ChatColor.GREEN + ChatColor.BOLD + "World Combat Manager\n");
      player.spigot().sendMessage(menuHeader);

      for (CombatWorld combatWorld : this.plugin.getWorldManager().getWorlds()) {
         TextComponent worldItem = new TextComponent(
            "" + ChatColor.DARK_AQUA + ChatColor.BOLD + "World: " + ChatColor.GOLD + combatWorld.getName() + ChatColor.AQUA + " \n  "
         );
         World world = Bukkit.getWorld(combatWorld.getName());
         if (world == null) {
            Log.warning("Null world in world list!");
         } else {
            List<CombatPlayer> pvPlayerList = world.getPlayers().stream().map(p -> this.plugin.getPlayerManager().get(p)).toList();
            worldItem.addExtra(this.createInfoText("Players", pvPlayerList.size()));
            worldItem.addExtra(this.createSeparator());
            worldItem.addExtra(this.createInfoText("In Combat", pvPlayerList.stream().filter(CombatPlayer::isInCombat).count()));
            worldItem.addExtra(this.createSeparator());
            worldItem.addExtra(this.createInfoText("PvP On", pvPlayerList.stream().filter(CombatPlayer::hasPvPEnabled).count()));
            worldItem.addExtra(this.createSeparator());
            worldItem.addExtra(this.createInfoText("PvP Off", pvPlayerList.stream().filter(pvPlayer -> !pvPlayer.hasPvPEnabled()).count()));
            player.spigot().sendMessage(worldItem);
            player.sendMessage(ChatColor.DARK_AQUA + "  Options");
            String pvpHoverText = ""
               + ChatColor.BLUE
               + ChatColor.BOLD
               + "Enables or disables PvP in "
               + ChatColor.YELLOW
               + combatWorld.getName()
               + "\n\n"
               + ChatColor.GREEN
               + "On - Enables PvP\n"
               + ChatColor.RED
               + "Off - Disables PvP";
            BaseComponent[] pvpEnabled = this.createFeature(
               String.format("/pmr worlds set %s PvP", combatWorld.getName()),
               pvpHoverText,
               "PvP",
               CombatWorld.WorldOptionState.fromBoolean(combatWorld.isCombatAllowed()),
               false
            );
            String forcepvpHoverText = ""
               + ChatColor.BLUE
               + ChatColor.BOLD
               + "Forces a PvP state in "
               + ChatColor.YELLOW
               + combatWorld.getName()
               + "\n\n"
               + ChatColor.GREEN
               + "On - Forces PvP to enabled and blocks players from turning it off\n"
               + ChatColor.RED
               + "Off - Forces PvP to disabled and blocks players from turning it on\n"
               + ChatColor.DARK_PURPLE
               + "Neutral - Does nothing, just leaves the player with the PvP state they already had";
            BaseComponent[] forcePvP = this.createFeature(
               String.format("/pmr worlds set %s ForcePvP", combatWorld.getName()), forcepvpHoverText, "Force PvP", combatWorld.isPvPForced(), true
            );
            player.spigot().sendMessage(pvpEnabled);
            player.spigot().sendMessage(forcePvP);
         }
      }
   }

   private BaseComponent[] createFeature(String command, String hoverText, String displayName, CombatWorld.WorldOptionState state, boolean thirdState) {
      ComponentBuilder builder = new ComponentBuilder();
      builder.append(ChatColor.DARK_AQUA + "    - " + ChatColor.GRAY + displayName).color(ChatColor.GRAY);
      builder.event(new HoverEvent(Action.SHOW_TEXT, new Content[]{new Text(hoverText)}));
      builder.append(ChatColor.DARK_GRAY + Strings.repeat(".", (int)(75.0 - displayName.length() * 2.86)) + " ");
      BaseComponent[] onButton = this.createToggleButton(command, "On", ChatColor.GREEN, state, CombatWorld.WorldOptionState.ON);
      BaseComponent[] offButton = this.createToggleButton(command, "Off", ChatColor.RED, state, CombatWorld.WorldOptionState.OFF);
      builder.append(onButton);
      builder.append(" ", FormatRetention.NONE);
      builder.append(offButton);
      if (thirdState) {
         BaseComponent[] neutralButton = this.createToggleButton(command, "Neutral", ChatColor.DARK_PURPLE, state, CombatWorld.WorldOptionState.NONE);
         builder.append(" ", FormatRetention.NONE);
         builder.append(neutralButton);
      }

      return builder.create();
   }

   private BaseComponent[] createToggleButton(
      String command, String text, ChatColor color, CombatWorld.WorldOptionState state, CombatWorld.WorldOptionState selectedState
   ) {
      String hoverText = "";
      String finalCommand = "";
      BaseComponent[] stateComp;
      if (state == selectedState) {
         stateComp = new ComponentBuilder(text).color(color).create();
         hoverText = ChatColor.DARK_RED + "Click to unselect this option";
         finalCommand = command + " " + CombatWorld.WorldOptionState.getOpposite(selectedState) + " --menu";
      } else {
         stateComp = new ComponentBuilder(text).color(ChatColor.DARK_GRAY).create();
         hoverText = ChatColor.DARK_GREEN + "Click to select this option";
         finalCommand = command + " " + selectedState + " --menu";
      }

      ComponentBuilder builder = new ComponentBuilder();
      builder.event(new ClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND, finalCommand));
      builder.event(new HoverEvent(Action.SHOW_TEXT, new Content[]{new Text(hoverText)}));
      builder.append("[").color(ChatColor.DARK_GRAY).append(stateComp).append("]").color(ChatColor.DARK_GRAY);
      return builder.create();
   }

   private TextComponent createInfoText(String text, long value) {
      return new TextComponent(ChatColor.DARK_AQUA + text + ": " + ChatColor.YELLOW + value);
   }

   private TextComponent createSeparator() {
      return new TextComponent(ChatColor.DARK_GRAY + " | ");
   }

   private class WorldsListCommand extends BaseCommand {
      public WorldsListCommand() {
         this.description("List all worlds and their PvP settings").usage("/pmr worlds list").permission(Permissions.ADMIN.getPermission());
      }

      @Override
      public void execute(CommandSender sender, String label, List<CommandArgument> args) {
         sender.sendMessage("§6§l--- World PvP Settings ---");

         for (CombatWorld combatWorld : Worlds.this.plugin.getWorldManager().getWorlds()) {
            String pvpStatus = combatWorld.isCombatAllowed() ? "§a✓ Enabled" : "§c✗ Disabled";

            String forceStatus = switch (combatWorld.isPvPForced()) {
               case ON -> "§a[Force ON]";
               case OFF -> "§c[Force OFF]";
               case NONE -> "§7[Normal]";
            };
            sender.sendMessage(String.format("§e%s: §fPvP %s %s", combatWorld.getName(), pvpStatus, forceStatus));
         }
      }
   }

   private class WorldsSetCommand extends BaseCommand {
      public WorldsSetCommand() {
         this.description("Set world PvP options")
            .usage("/pmr worlds set <world> <option> <value> [--menu]")
            .permission(Permissions.ADMIN.getPermission())
            .argument("world", ArgumentType.WORLD)
            .required()
            .endArgument()
            .argument("option", ArgumentType.STRING)
            .required()
            .tabComplete("PvP", "ForcePvP")
            .endArgument()
            .argument("value", ArgumentType.STRING)
            .required()
            .tabComplete("ON", "OFF", "NONE")
            .endArgument()
            .argument("refreshMenu", ArgumentType.STRING)
            .tabComplete("--menu")
            .endArgument();
      }

      @Override
      public void execute(CommandSender sender, String label, List<CommandArgument> args) {
         if (args.size() < 3) {
            sender.sendMessage("§cUsage: /pmr worlds set <world> <option> <value>");
            sender.sendMessage("§cOptions: PvP, ForcePvP");
            sender.sendMessage("§cValues: ON, OFF (and NONE for ForcePvP)");
         } else {
            String worldName = this.getArgument(args, "world").getAsWorld().getName();
            String option = this.getArgument(args, "option").getValue();
            String value = this.getArgument(args, "value").getValue();
            CombatWorld combatWorld = Worlds.this.plugin.getWorldManager().getWorld(worldName);
            if (combatWorld == null) {
               sender.sendMessage(Lang.PREFIX + " §cThere is no world with that name: " + worldName);
            } else {
               if (option.equalsIgnoreCase("PvP")) {
                  boolean pvp = value.equalsIgnoreCase("ON");
                  combatWorld.setCombatAllowed(pvp);
                  Worlds.this.plugin.getWorldManager().saveWorldData(combatWorld);
                  sender.sendMessage(String.format("%s §aPvP in world '%s' set to: §e%s", Lang.PREFIX, worldName, pvp ? "ENABLED" : "DISABLED"));
               } else if (option.equalsIgnoreCase("ForcePvP")) {
                  try {
                     CombatWorld.WorldOptionState state = CombatWorld.WorldOptionState.valueOf(value.toUpperCase());
                     combatWorld.setForcePVP(state);
                     Worlds.this.plugin.getWorldManager().saveWorldData(combatWorld);
                     sender.sendMessage(String.format("%s §aForce PvP in world '%s' set to: §e%s", Lang.PREFIX, worldName, state.name()));
                  } catch (IllegalArgumentException var9) {
                     sender.sendMessage("§cInvalid ForcePvP value. Use: ON, OFF, or NONE");
                  }
               } else {
                  sender.sendMessage("§cInvalid option. Use 'PvP' or 'ForcePvP'");
               }

               if (sender instanceof Player player && this.hasArgument(args, "refreshMenu")) {
                  Worlds.this.createWorldMenu(player);
               }
            }
         }
      }
   }
}
