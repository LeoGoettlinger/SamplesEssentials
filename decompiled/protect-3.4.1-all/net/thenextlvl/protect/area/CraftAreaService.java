package net.thenextlvl.protect.area;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.regions.Region;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.kyori.adventure.key.Key;
import net.thenextlvl.protect.ProtectPlugin;
import net.thenextlvl.protect.area.event.AreaDeleteEvent;
import net.thenextlvl.protect.area.event.player.PlayerAreaLeaveEvent;
import net.thenextlvl.protect.area.event.player.PlayerAreaTransitionEvent;
import net.thenextlvl.protect.io.AreaAdapter;
import org.bukkit.World;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class CraftAreaService implements AreaService {
   private final Map<Class<? extends Area>, AreaAdapter<?>> adapters = new HashMap<>();
   private final Set<CraftAreaService.RegionWrapper<?>> wrappers = new HashSet<>();
   private final ProtectPlugin plugin;

   public CraftAreaService(ProtectPlugin plugin) {
      this.plugin = plugin;
   }

   @Override
   public <T extends Region> AreaCreator<T> creator(String name, World world, T region) {
      return new CraftAreaCreator<>(this.plugin, name, world, region);
   }

   @Override
   public <T extends Region> AreaCreator<T> creator(RegionizedArea<T> area) {
      return new CraftAreaCreator<>(
         this.plugin,
         area.getName(),
         area.getWorld(),
         area.getRegion(),
         area.getParent().map(Area::getName).orElse(null),
         area.getOwner().orElse(null),
         area.getFlags(),
         area.getMembers(),
         area.getPriority()
      );
   }

   @Override
   public <T extends Region> boolean delete(RegionizedArea<T> area) {
      if (!new AreaDeleteEvent(area).callEvent()) {
         return false;
      } else {
         boolean remove = this.plugin.areaProvider().delete(area);
         if (remove) {
            this.handlePostRemove(area);
         }

         return remove;
      }
   }

   @Override
   public <T extends Region> Optional<Function<AreaCreator<T>, RegionizedArea<T>>> getWrapper(Class<T> type) {
      return this.wrappers
         .stream()
         .filter(wrapper -> wrapper.type().isAssignableFrom(type))
         .map(wrapper -> wrapper)
         .map(CraftAreaService.RegionWrapper::creator)
         .findFirst();
   }

   @Override
   public <T extends Region> boolean unregisterWrapper(Class<T> type) {
      return this.wrappers.removeIf(wrapper -> wrapper.type().isAssignableFrom(type));
   }

   @Override
   public <T extends Region> void registerWrapper(Class<T> type, Function<AreaCreator<T>, RegionizedArea<T>> creator) throws IllegalStateException {
      CraftAreaService.RegionWrapper<T> wrapper = new CraftAreaService.RegionWrapper<>(type, creator);
      Preconditions.checkState(this.wrappers.add(wrapper), "Duplicate wrapper for type: " + type.getName());
   }

   @Override
   public Set<Class<? extends Region>> getRegionWrappers() {
      return this.wrappers.stream().map(CraftAreaService.RegionWrapper::type).collect(Collectors.toUnmodifiableSet());
   }

   @Override
   public Map<Class<? extends Area>, AreaAdapter<?>> getAdapters() {
      return Map.copyOf(this.adapters);
   }

   @Override
   public <T extends Area> void registerAdapter(Class<T> type, AreaAdapter<T> adapter) throws IllegalStateException {
      Preconditions.checkState(!this.adapters.containsKey(type), "Duplicate adapter for type: " + type.getName());
      Preconditions.checkState(
         this.adapters.values().stream().filter(value -> value.key().equals(adapter.key())).findAny().isEmpty(),
         "Duplicate key for adapter: " + adapter.key() + " in " + adapter.getClass().getName()
      );
      this.adapters.put(type, adapter);
   }

   @Override
   public <T extends Area> boolean unregisterAdapter(Class<T> type) {
      return this.adapters.remove(type) != null;
   }

   @Override
   public <T extends Area> AreaAdapter<T> getAdapter(Class<T> type) throws NullPointerException {
      return Objects.requireNonNull((AreaAdapter<T>)this.adapters.get(type), "No adapter for type: " + type.getName());
   }

   @Override
   public <T extends Area> AreaAdapter<T> getAdapter(Key key) throws IllegalArgumentException {
      return (AreaAdapter<T>)this.adapters
         .values()
         .stream()
         .filter(adapter -> adapter.key().equals(key))
         .findAny()
         .orElseThrow(() -> new IllegalArgumentException("No adapter for key: " + key));
   }

   private void handlePostRemove(Area removed) {
      removed.getPlayers().forEach(player -> {
         new PlayerAreaLeaveEvent(player, removed).callEvent();
         Area target = this.plugin.areaProvider().getArea(player);
         if (removed.getPriority() >= target.getPriority()) {
            new PlayerAreaTransitionEvent(player, removed, target).callEvent();
         }
      });
   }

   private record RegionWrapper<T extends Region>(Class<T> type, Function<AreaCreator<T>, RegionizedArea<T>> creator) {
   }
}
