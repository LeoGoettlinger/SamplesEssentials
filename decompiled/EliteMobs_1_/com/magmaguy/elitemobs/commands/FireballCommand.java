package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import java.util.List;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;

public class FireballCommand extends AdvancedCommand {
   public FireballCommand() {
      super(List.of("fireball"));
      this.setUsage("/em fireball");
      this.setPermission("elitemobs.fireball");
      this.setSenderType(SenderType.PLAYER);
      this.setDescription("Shoots a fireball, to test terrain protections and explosion regeneration.");
   }

   @Override
   public void execute(CommandData commandData) {
      Fireball fireball = (Fireball)commandData.getPlayerSender().getWorld().spawnEntity(commandData.getPlayerSender().getLocation(), EntityType.FIREBALL);
      fireball.setDirection(commandData.getPlayerSender().getLocation().getDirection().normalize());
      fireball.setShooter(commandData.getPlayerSender());
      fireball.setYield(3.0F);
      EntityTracker.registerProjectileEntity(fireball);
   }
}
