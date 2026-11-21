package net.thenextlvl.protect.area;

import com.sk89q.worldedit.regions.Region;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.thenextlvl.protect.exception.CircularInheritanceException;
import net.thenextlvl.protect.exception.UnsupportedRegionException;
import net.thenextlvl.protect.flag.Flag;
import org.bukkit.World;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.Nullable;

public interface AreaCreator<T extends Region> {
   @Contract(
      pure = true
   )
   String name();

   @Contract(
      pure = true
   )
   @Nullable
   UUID owner();

   @Contract(
      value = " -> new",
      pure = true
   )
   AreaCreator<T> copy();

   @Contract(
      mutates = "this"
   )
   AreaCreator<T> flags(Map<Flag<?>, Object> var1);

   @Contract(
      mutates = "this"
   )
   AreaCreator<T> members(Set<UUID> var1);

   @Contract(
      mutates = "this"
   )
   AreaCreator<T> name(String var1);

   @Contract(
      mutates = "this"
   )
   AreaCreator<T> owner(@Nullable UUID var1);

   @Contract(
      mutates = "this"
   )
   AreaCreator<T> parent(@Nullable String var1);

   @Contract(
      mutates = "this"
   )
   AreaCreator<T> parent(@Nullable Area var1);

   @Contract(
      mutates = "this"
   )
   AreaCreator<T> priority(int var1);

   @Contract(
      value = "_ -> new",
      pure = true
   )
   <V extends Region> AreaCreator<V> region(V var1);

   @Contract(
      mutates = "this"
   )
   AreaCreator<T> world(World var1);

   @Contract(
      pure = true
   )
   @Unmodifiable
   Map<Flag<?>, Object> flags();

   @Contract(
      value = "-> new",
      mutates = "io"
   )
   RegionizedArea<T> create() throws UnsupportedRegionException, CircularInheritanceException, IOException;

   @Contract(
      pure = true
   )
   @Unmodifiable
   Set<UUID> members();

   @Contract(
      pure = true
   )
   @Nullable
   String parent();

   @Contract(
      pure = true
   )
   T region();

   @Contract(
      pure = true
   )
   World world();

   @Contract(
      pure = true
   )
   int priority();
}
