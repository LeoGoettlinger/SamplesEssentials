package me.chancesd.pvpmanager.command;

import java.util.ArrayList;
import java.util.List;
import me.chancesd.pvpmanager.manager.PlayerManager;
import me.chancesd.pvpmanager.player.BasePlayer;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.sdutils.command.ArgumentType;
import me.chancesd.pvpmanager.sdutils.command.BaseCommand;
import me.chancesd.pvpmanager.sdutils.command.CommandArgument;
import me.chancesd.pvpmanager.sdutils.utils.ChatUtils;
import me.chancesd.pvpmanager.setting.Lang;
import me.chancesd.pvpmanager.setting.Permissions;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PvPInfo extends BaseCommand {
   private static final String ARG_PLAYER = "player";
   private final PlayerManager ph;

   public PvPInfo(PluginCommand pluginCommand, PlayerManager ph) {
      super(pluginCommand);
      this.ph = ph;
      this.description("Check your or other player info")
         .usage("/pvpinfo [player]")
         .permission(Permissions.COMMAND_PVPINFO.getPermission())
         .argument("player", ArgumentType.PLAYER)
         .requirePermission(Permissions.COMMAND_PVPINFO_OTHERS.getPermission())
         .endArgument();
   }

   @Override
   public void execute(CommandSender sender, String label, List<CommandArgument> args) {
      if (args.isEmpty() && sender instanceof Player player) {
         this.sendInfo(sender, this.ph.get(player));
      } else if (this.hasArgument(args, "player")) {
         CommandArgument targetArg = this.getArgument(args, "player");
         Player targetPlayer = targetArg.getAsPlayer();
         this.sendInfo(sender, this.ph.get(targetPlayer));
      } else {
         ChatUtils.send(sender, "&#FF5555This command requires a target player when used from console.");
      }
   }

   private void sendInfo(CommandSender sender, CombatPlayer target) {
      ChatUtils.send(sender, Lang.getString("PvPInfo_Header").replace("{player}", target.getName()));
      ChatUtils.send(sender, Lang.getString("PvPInfo_UUID") + target.getUUID());
      if (target.isInCombat()) {
         long timeLeft = target.getTagTimeLeft();
         ChatUtils.send(sender, Lang.getString("PvPInfo_Combat") + "&#FF5555⚔ In combat &7(" + timeLeft / 1000L + "s left)");
      } else {
         ChatUtils.send(sender, Lang.getString("PvPInfo_Combat") + "&a✗ Not in combat");
      }

      ChatUtils.send(sender, Lang.getString("PvPInfo_Protection") + this.getProtectionStatus(target));
      ChatUtils.send(sender, Lang.getString("PvPInfo_World") + target.getPlayer().getWorld().getName());
      ChatUtils.send(sender, Lang.getString("PvPInfo_Override") + (target.hasOverride() ? "&a✓ Enabled" : "&c✗ Disabled"));
      if (target.getEnemies().isEmpty()) {
         ChatUtils.send(sender, Lang.getString("PvPInfo_Enemies") + "&cNone");
      } else {
         String enemies = String.join("&7, &f", target.getEnemies().stream().map(BasePlayer::getName).toList());
         ChatUtils.send(sender, Lang.getString("PvPInfo_Enemies") + enemies);
      }

      ChatUtils.send(sender, Lang.getString("PvPInfo_Exemptions") + this.getExemptions(target.getPlayer()));
      ChatUtils.send(sender, Lang.getString("PvPInfo_Footer"));
   }

   private String getProtectionStatus(CombatPlayer target) {
      List<String> protections = new ArrayList<>();
      if (!this.ph.getGlobalStatus()) {
         protections.add("&#CC6666\ud83d\udee1 Global");
      }

      if (!target.getCombatWorld().isCombatAllowed()) {
         protections.add("&#9966CC\ud83d\udee1 World");
      }

      if (target.hasRespawnProtection()) {
         protections.add("&#5599FF\ud83d\udee1 Respawn");
      }

      if (target.isNewbie()) {
         long timeLeft = target.getNewbieTimeLeft();
         if (timeLeft > 0L) {
            protections.add("&#FFAA00\ud83d\udee1 Newbie &7(" + timeLeft / 1000L / 60L + "m left)");
         } else {
            protections.add("&#FFAA00\ud83d\udee1 Newbie");
         }
      }

      if (!target.hasPvPEnabled()) {
         protections.add("&#FF6666\ud83d\udee1 PvP Disabled");
      }

      if (this.ph.getPlugin().getDependencyManager().shouldProtectAFK(target.getPlayer())) {
         protections.add("&#66CCAA\ud83d\udee1 AFK");
      }

      return protections.isEmpty() ? "&c✗ None" : String.join("&7, ", protections);
   }

   public String getExemptions(Player player) {
      List<String> exemptions = new ArrayList<>();
      this.checkExemption(exemptions, "&aCombatTag", Permissions.EXEMPT_COMBAT_TAG, player);
      this.checkExemption(exemptions, "&aCombatLog", Permissions.EXEMPT_COMBAT_LOG, player);
      this.checkExemption(exemptions, "&aDisableActions", Permissions.EXEMPT_DISABLE_ACTIONS, player);
      this.checkExemption(exemptions, "&aKillAbuse", Permissions.EXEMPT_KILL_ABUSE, player);
      this.checkExemption(exemptions, "&aBlockCommands", Permissions.EXEMPT_BLOCK_COMMANDS, player);
      return exemptions.isEmpty() ? "&cNone" : String.join("&7, ", exemptions);
   }

   private void checkExemption(List<String> exemptions, @NotNull String displayName, Permissions perm, Player player) {
      if (perm.hasPerm(player)) {
         exemptions.add(displayName);
      }
   }
}
