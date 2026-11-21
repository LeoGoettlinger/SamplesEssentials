package me.chancesd.pvpmanager.command;

import java.util.List;
import me.chancesd.pvpmanager.manager.PlayerManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.player.world.CombatWorld;
import me.chancesd.pvpmanager.sdutils.command.ArgumentType;
import me.chancesd.pvpmanager.sdutils.command.BaseCommand;
import me.chancesd.pvpmanager.sdutils.command.CommandArgument;
import me.chancesd.pvpmanager.setting.Conf;
import me.chancesd.pvpmanager.setting.Lang;
import me.chancesd.pvpmanager.setting.Permissions;
import me.chancesd.pvpmanager.utils.CombatUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

public class PvP extends BaseCommand {
   private final PlayerManager ph;

   public PvP(PluginCommand pluginCommand, PlayerManager playerHandler) {
      super(pluginCommand);
      this.ph = playerHandler;
      this.permission(new String[]{Permissions.COMMAND_PVP.getPermission()})
         .argument("player", ArgumentType.PLAYER_OR_ALL)
         .requirePermission(Permissions.COMMAND_PVP_OTHERS.getPermission())
         .endArgument()
         .argument("status", ArgumentType.BOOLEAN)
         .requirePermission(Permissions.COMMAND_PVP_OTHERS.getPermission())
         .tabComplete("ON", "OFF")
         .endArgument()
         .description("Toggle PvP for yourself or others");
   }

   @Override
   public void execute(CommandSender sender, String label, List<CommandArgument> args) {
      if (args.isEmpty()) {
         if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players.");
         } else {
            CombatPlayer pvpPlayer = this.ph.get(player);
            this.togglePvP(pvpPlayer, !pvpPlayer.hasPvPEnabled());
         }
      } else {
         CommandArgument playerArg = this.getArgument(args, "player");
         Player player = playerArg.getAsPlayer();
         if (args.size() == 1) {
            if (playerArg.isWildcard()) {
               this.togglePvPAll(sender, false, true);
            } else {
               this.togglePvPAdmin(sender, player, false, true);
            }
         } else {
            boolean state = this.getArgument(args, "status").getAsBoolean();
            if (playerArg.isWildcard()) {
               this.togglePvPAll(sender, state, false);
            } else {
               this.togglePvPAdmin(sender, player, state, false);
            }
         }
      }
   }

   private void togglePvP(CombatPlayer player, boolean state) {
      if (this.hasToggleCooldownPassed(player)) {
         if (player.hasPvPEnabled() == state) {
            player.message(state ? Lang.PVP_TOGGLE_ALREADY_ENABLED : Lang.PVP_TOGGLE_ALREADY_DISABLED, new Object[0]);
         } else if (!state && this.ph.getPlugin().getDependencyManager().shouldDisableProtection(player.getPlayer())) {
            player.message(Lang.ERROR_PVP_TOGGLE_FORCE_PVP, new Object[0]);
         } else {
            CombatWorld combatWorld = player.getCombatWorld();
            if (state && combatWorld.isPvPForced() == CombatWorld.WorldOptionState.OFF) {
               player.message(Lang.ERROR_PVP_TOGGLE_NO_PVP, new Object[0]);
            } else if (!state && combatWorld.isPvPForced() == CombatWorld.WorldOptionState.ON) {
               player.message(Lang.ERROR_PVP_TOGGLE_FORCE_PVP, new Object[0]);
            } else {
               player.setPvP(state);
            }
         }
      }
   }

   private void togglePvPAdmin(CommandSender sender, Player player, boolean state, boolean toggle) {
      CombatPlayer specifiedPlayer = this.ph.get(player);
      specifiedPlayer.setPvP(toggle ? !specifiedPlayer.hasPvPEnabled() : state);
      String stateMessage = specifiedPlayer.hasPvPEnabled() ? Lang.ENABLED.msg() : Lang.DISABLED.msg();
      sender.sendMessage(Lang.PVP_TOGGLE_ADMIN_CHANGED.msg(player.getName(), stateMessage));
   }

   private void togglePvPAll(CommandSender sender, boolean state, boolean toggle) {
      for (Player player : Bukkit.getOnlinePlayers()) {
         this.togglePvPAdmin(sender, player, state, toggle);
      }
   }

   public final boolean hasToggleCooldownPassed(CombatPlayer player) {
      if (!CombatUtils.hasTimePassed(player.getToggleTime(), Conf.TOGGLE_COOLDOWN.asInt()) && !player.hasPerm(Permissions.EXEMPT_PVPTOGGLE_COOLDOWN)) {
         long timeLeft = CombatUtils.getTimeLeftMs(player.getToggleTime(), Conf.TOGGLE_COOLDOWN.asInt() * 1000L);
         player.message(Lang.ERROR_PVP_COOLDOWN.msgTime(timeLeft));
         return false;
      } else {
         return true;
      }
   }
}
