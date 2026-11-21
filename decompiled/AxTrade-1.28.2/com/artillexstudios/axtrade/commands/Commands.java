package com.artillexstudios.axtrade.commands;

import com.artillexstudios.axtrade.commands.subcommands.Accept;
import com.artillexstudios.axtrade.commands.subcommands.Deny;
import com.artillexstudios.axtrade.commands.subcommands.Force;
import com.artillexstudios.axtrade.commands.subcommands.Preview;
import com.artillexstudios.axtrade.commands.subcommands.Reload;
import com.artillexstudios.axtrade.commands.subcommands.Request;
import com.artillexstudios.axtrade.commands.subcommands.Toggle;
import com.artillexstudios.axtrade.libs.lamp.annotation.DefaultFor;
import com.artillexstudios.axtrade.libs.lamp.annotation.Optional;
import com.artillexstudios.axtrade.libs.lamp.annotation.Subcommand;
import com.artillexstudios.axtrade.libs.lamp.bukkit.annotation.CommandPermission;
import com.artillexstudios.axtrade.libs.lamp.orphan.OrphanCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandPermission("axtrade.trade")
public class Commands implements OrphanCommand {
   @DefaultFor({"~"})
   public void trade(Player sender, @Optional Player other) {
      Request.INSTANCE.execute(sender, other);
   }

   @Subcommand({"accept"})
   public void accept(Player sender, Player other) {
      Accept.INSTANCE.execute(sender, other);
   }

   @Subcommand({"deny"})
   public void deny(Player sender, Player other) {
      Deny.INSTANCE.execute(sender, other);
   }

   @CommandPermission("axtrade.toggle")
   @Subcommand({"toggle"})
   public void toggle(CommandSender sender, @Optional @CommandPermission("axtrade.toggle.other") Player other) {
      Toggle.INSTANCE.execute(sender, other);
   }

   @Subcommand({"reload"})
   @CommandPermission("axtrade.admin")
   public void reload(CommandSender sender) {
      Reload.INSTANCE.execute(sender);
   }

   @Subcommand({"force"})
   @CommandPermission("axtrade.admin")
   public void force(Player sender, Player other) {
      Force.INSTANCE.execute(sender, other);
   }

   @Subcommand({"preview"})
   @CommandPermission("axtrade.admin")
   public void preview(Player sender) {
      Preview.INSTANCE.execute(sender);
   }
}
