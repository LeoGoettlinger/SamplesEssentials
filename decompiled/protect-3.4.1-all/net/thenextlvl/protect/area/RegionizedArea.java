package net.thenextlvl.protect.area;

import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
import net.thenextlvl.protect.exception.CircularInheritanceException;
import net.thenextlvl.protect.schematic.SchematicHolder;
import org.bukkit.Location;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

public interface RegionizedArea<T extends Region> extends Area, SchematicHolder {
   @Contract(
      value = " -> new",
      pure = true
   )
   Location getLocation();

   @Contract(
      value = " -> new",
      pure = true
   )
   RegionSelector getRegionSelector();

   @Contract(
      pure = true
   )
   T getRegion();

   @Contract(
      mutates = "this"
   )
   boolean setParent(@Nullable Area var1) throws CircularInheritanceException;

   @Contract(
      mutates = "this"
   )
   boolean setRegion(T var1);

   @Contract(
      pure = true
   )
   boolean isTooBig();
}
