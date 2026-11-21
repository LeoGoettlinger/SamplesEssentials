package net.thenextlvl.protect.area;

import com.sk89q.worldedit.regions.Region;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import net.kyori.adventure.key.Key;
import net.thenextlvl.protect.io.AreaAdapter;
import org.bukkit.World;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;

public interface AreaService {
   @Contract(
      value = "_, _, _ -> new",
      pure = true
   )
   <T extends Region> AreaCreator<T> creator(String var1, World var2, T var3);

   @Contract(
      value = "_ -> new",
      pure = true
   )
   <T extends Region> AreaCreator<T> creator(RegionizedArea<T> var1);

   @Contract(
      mutates = "io"
   )
   <T extends Region> boolean delete(RegionizedArea<T> var1);

   @Contract(
      pure = true
   )
   <T extends Region> Optional<Function<AreaCreator<T>, RegionizedArea<T>>> getWrapper(Class<T> var1);

   @Contract(
      mutates = "this"
   )
   <T extends Region> boolean unregisterWrapper(Class<T> var1);

   @Contract(
      mutates = "this"
   )
   <T extends Region> void registerWrapper(Class<T> var1, Function<AreaCreator<T>, RegionizedArea<T>> var2) throws IllegalStateException;

   @Contract(
      pure = true
   )
   @Unmodifiable
   Set<Class<? extends Region>> getRegionWrappers();

   @Contract(
      pure = true
   )
   @Unmodifiable
   Map<Class<? extends Area>, AreaAdapter<?>> getAdapters();

   @Contract(
      mutates = "this"
   )
   <T extends Area> void registerAdapter(Class<T> var1, AreaAdapter<T> var2) throws IllegalStateException;

   @Contract(
      mutates = "this"
   )
   <T extends Area> boolean unregisterAdapter(Class<T> var1);

   @Contract(
      pure = true
   )
   <T extends Area> AreaAdapter<T> getAdapter(Class<T> var1) throws NullPointerException;

   @Contract(
      pure = true
   )
   <T extends Area> AreaAdapter<T> getAdapter(Key var1) throws IllegalArgumentException;
}
