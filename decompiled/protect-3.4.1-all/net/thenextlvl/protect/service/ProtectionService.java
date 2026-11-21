package net.thenextlvl.protect.service;

import net.thenextlvl.protect.area.Area;
import net.thenextlvl.protect.flag.Flag;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

public interface ProtectionService {
   @Contract(
      pure = true
   )
   boolean canEdit(@Nullable Entity var1, Area var2);

   @Contract(
      pure = true
   )
   boolean canEdit(@Nullable Entity var1, Location var2);

   @Contract(
      pure = true
   )
   boolean canPlace(@Nullable Entity var1, Area var2);

   @Contract(
      pure = true
   )
   boolean canPlace(@Nullable Entity var1, Location var2);

   @Contract(
      pure = true
   )
   boolean canDestroy(@Nullable Entity var1, Area var2);

   @Contract(
      pure = true
   )
   boolean canDestroy(@Nullable Entity var1, Location var2);

   @Contract(
      pure = true
   )
   boolean canInteract(@Nullable Entity var1, Location var2);

   @Contract(
      pure = true
   )
   boolean canInteract(@Nullable Entity var1, Entity var2);

   @Contract(
      pure = true
   )
   boolean canInteractPhysical(@Nullable Entity var1, Location var2);

   @Contract(
      pure = true
   )
   boolean canAttack(Entity var1, Entity var2);

   @Contract(
      pure = true
   )
   boolean canKnockback(Entity var1, Entity var2);

   @Contract(
      pure = true
   )
   boolean canShear(@Nullable Entity var1, Entity var2);

   @Contract(
      pure = true
   )
   boolean canTrample(@Nullable Entity var1, Location var2);

   @Contract(
      pure = true
   )
   boolean canEnter(@Nullable Entity var1, Area var2);

   @Contract(
      pure = true
   )
   boolean canLeave(@Nullable Entity var1, Area var2);

   @Contract(
      pure = true
   )
   boolean canPerformAction(@Nullable Entity var1, Area var2, Flag<Boolean> var3, @Nullable String var4);
}
