package net.thenextlvl.protect.service;

import net.kyori.adventure.util.TriState;
import net.thenextlvl.protect.ProtectPlugin;
import net.thenextlvl.protect.area.Area;
import net.thenextlvl.protect.flag.Flag;
import net.thenextlvl.protect.flag.ProtectionFlag;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class CraftProtectionService implements ProtectionService {
   private final ProtectPlugin plugin;

   public CraftProtectionService(ProtectPlugin plugin) {
      this.plugin = plugin;
   }

   @Override
   public boolean canEdit(@Nullable Entity entity, Area area) {
      return this.canPlace(entity, area) && this.canDestroy(entity, area);
   }

   @Override
   public boolean canEdit(@Nullable Entity entity, Location location) {
      return this.canEdit(entity, this.plugin.areaProvider().getArea(location));
   }

   @Override
   public boolean canPlace(@Nullable Entity entity, Area area) {
      return this.canPerformAction(entity, area, this.plugin.flags.place, "protect.bypass.place");
   }

   @Override
   public boolean canPlace(@Nullable Entity entity, Location location) {
      return this.canPlace(entity, this.plugin.areaProvider().getArea(location));
   }

   @Override
   public boolean canDestroy(@Nullable Entity entity, Area area) {
      return this.canPerformAction(entity, area, this.plugin.flags.destroy, "protect.bypass.destroy");
   }

   @Override
   public boolean canDestroy(@Nullable Entity entity, Location location) {
      return this.canDestroy(entity, this.plugin.areaProvider().getArea(location));
   }

   @Override
   public boolean canInteract(@Nullable Entity entity, Location location) {
      return this.canPerformAction(entity, this.plugin.areaProvider().getArea(location), this.plugin.flags.interact, "protect.bypass.interact");
   }

   @Override
   public boolean canInteract(@Nullable Entity entity, Entity interacted) {
      ProtectionFlag<Boolean> flag = interacted.getType().equals(EntityType.ARMOR_STAND)
         ? this.plugin.flags.armorStandManipulate
         : this.plugin.flags.entityInteract;
      return this.canPerformAction(entity, this.plugin.areaProvider().getArea(interacted), flag, "protect.bypass.entity-interact");
   }

   @Override
   public boolean canInteractPhysical(@Nullable Entity entity, Location location) {
      return this.canPerformAction(entity, this.plugin.areaProvider().getArea(location), this.plugin.flags.physicalInteract, "protect.bypass.physical-interact");
   }

   @Override
   public boolean canAttack(Entity attacker, Entity victim) {
      ProtectionFlag<Boolean> flag = attacker.getType().equals(EntityType.PLAYER) && victim.getType().equals(EntityType.PLAYER)
         ? this.plugin.flags.playerAttackPlayer
         : (
            attacker.getType().equals(EntityType.PLAYER)
               ? this.plugin.flags.playerAttackEntity
               : (victim.getType().equals(EntityType.PLAYER) ? this.plugin.flags.entityAttackPlayer : this.plugin.flags.entityAttackEntity)
         );
      Area first = this.plugin.areaProvider().getArea(attacker);
      Area second = this.plugin.areaProvider().getArea(victim);
      boolean areasCanInteract = first.canInteract(second);
      boolean canAttackFromFirst = this.canPerformAction(attacker, first, flag, null);
      if (!canAttackFromFirst || !areasCanInteract && !this.canPerformAction(attacker, second, flag, null)) {
         if (areasCanInteract && this.canPerformAction(attacker, second, flag, null)) {
            return true;
         } else {
            return !attacker.hasPermission("protect.bypass.attack") ? false : attacker instanceof Player player && player.getGameMode().isInvulnerable();
         }
      } else {
         return true;
      }
   }

   @Override
   public boolean canKnockback(Entity source, Entity target) {
      if (source.equals(target)) {
         return true;
      } else {
         ProtectionFlag<Boolean> flag = this.plugin.flags.knockback;
         Area first = this.plugin.areaProvider().getArea(source);
         Area second = this.plugin.areaProvider().getArea(target);
         if (!first.getFlag(flag) && !first.isPermitted(source.getUniqueId()) || !second.getFlag(flag) && !second.isPermitted(source.getUniqueId())) {
            if ((!first.canInteract(second) || !first.getFlag(flag) && !first.isPermitted(source.getUniqueId()))
               && !second.getFlag(flag)
               && !second.isPermitted(source.getUniqueId())) {
               return !source.hasPermission("protect.bypass.knockback") ? false : source instanceof Player player && player.getGameMode().isInvulnerable();
            } else {
               return true;
            }
         } else {
            return true;
         }
      }
   }

   @Override
   public boolean canShear(@Nullable Entity entity, Entity sheared) {
      return this.canPerformAction(entity, this.plugin.areaProvider().getArea(sheared), this.plugin.flags.entityShear, "protect.bypass.entity-shear");
   }

   @Override
   public boolean canTrample(@Nullable Entity entity, Location location) {
      return this.canPerformAction(entity, this.plugin.areaProvider().getArea(location), this.plugin.flags.cropTrample, "protect.bypass.trample");
   }

   @Override
   public boolean canEnter(@Nullable Entity entity, Area area) {
      return this.canPerformAction(entity, area, this.plugin.flags.areaEnter, "protect.bypass.enter");
   }

   @Override
   public boolean canLeave(@Nullable Entity entity, Area area) {
      return this.canPerformAction(entity, area, this.plugin.flags.areaLeave, "protect.bypass.leave");
   }

   @Override
   public boolean canPerformAction(@Nullable Entity entity, Area area, Flag<Boolean> flag, @Nullable String permission) {
      if (area.getFlag(flag)) {
         return true;
      } else if (entity == null) {
         return false;
      } else if (area.isPermitted(entity.getUniqueId())) {
         return true;
      } else {
         String areaFlagPermission = "protect.area." + area.getName() + "." + flag.key().asString();
         if (entity.permissionValue(areaFlagPermission).equals(TriState.TRUE)) {
            return true;
         } else {
            return permission != null && entity.hasPermission(permission)
               ? !(entity instanceof Player player && !player.getGameMode().isInvulnerable())
               : false;
         }
      }
   }
}
