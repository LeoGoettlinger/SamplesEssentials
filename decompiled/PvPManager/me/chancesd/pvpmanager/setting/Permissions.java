package me.chancesd.pvpmanager.setting;

import com.google.common.base.Preconditions;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public enum Permissions {
   ADMIN("pvpmanager.admin"),
   COMMAND_ANNOUNCE("pvpmanager.command.announce"),
   COMMAND_NEWBIE("pvpmanager.command.newbie"),
   COMMAND_OVERRIDE("pvpmanager.command.override"),
   COMMAND_PVP("pvpmanager.command.pvp"),
   COMMAND_PVP_OTHERS("pvpmanager.command.pvp.others"),
   COMMAND_PVPGLOBAL("pvpmanager.command.pvpglobal"),
   COMMAND_PVPINFO("pvpmanager.command.pvpinfo"),
   COMMAND_PVPINFO_OTHERS("pvpmanager.command.pvpinfo.others"),
   COMMAND_PVPLIST("pvpmanager.command.pvplist"),
   COMMAND_PVPLIST_OFFLINE("pvpmanager.command.pvplist.offline"),
   COMMAND_PVPSTATUS("pvpmanager.command.pvpstatus"),
   COMMAND_PVPSTATUS_OTHERS("pvpmanager.command.pvpstatus.others"),
   COMMAND_TAG("pvpmanager.command.tag"),
   COMMAND_TAG_OTHERS("pvpmanager.command.tag.others"),
   COMMAND_UNTAG("pvpmanager.command.untag"),
   EXEMPT_BLOCK_COMMANDS("pvpmanager.exempt.blockcommands"),
   EXEMPT_COMBAT_LOG("pvpmanager.exempt.combatlog"),
   EXEMPT_COMBAT_TAG("pvpmanager.exempt.combattag"),
   EXEMPT_DISABLE_ACTIONS("pvpmanager.exempt.disableactions"),
   EXEMPT_KILL_ABUSE("pvpmanager.exempt.killabuse"),
   EXEMPT_PROTECTION_RESPAWN("pvpmanager.exempt.respawnprotection"),
   EXEMPT_PVPTOGGLE_COOLDOWN("pvpmanager.exempt.pvptogglecooldown");

   @NotNull
   private final String permission;

   private Permissions(final String permission) {
      Preconditions.checkNotNull(permission);
      this.permission = permission;
   }

   public boolean hasPerm(Player player) {
      return player.hasPermission(this.permission);
   }

   public boolean hasPerm(CommandSender sender) {
      return sender.hasPermission(this.permission);
   }

   @NotNull
   public String getPermission() {
      return this.permission;
   }

   @Override
   public String toString() {
      return this.getPermission();
   }
}
