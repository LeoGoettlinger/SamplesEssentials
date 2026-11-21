package net.thenextlvl.protect.area;

import com.sk89q.worldedit.regions.Region;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.thenextlvl.protect.ProtectPlugin;
import net.thenextlvl.protect.area.event.AreaCreateEvent;
import net.thenextlvl.protect.exception.CircularInheritanceException;
import net.thenextlvl.protect.exception.UnsupportedRegionException;
import net.thenextlvl.protect.flag.Flag;
import org.bukkit.World;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class CraftAreaCreator<T extends Region> implements AreaCreator<T> {
   private final ProtectPlugin plugin;
   private String name;
   private World world;
   private final T region;
   @Nullable
   private String parent = null;
   @Nullable
   private UUID owner = null;
   private Map<Flag<?>, Object> flags = new HashMap<>();
   private Set<UUID> members = new HashSet<>();
   private int priority = 0;

   public CraftAreaCreator(
      ProtectPlugin plugin,
      String name,
      World world,
      T region,
      @Nullable String parent,
      @Nullable UUID owner,
      Map<Flag<?>, Object> flags,
      Set<UUID> members,
      int priority
   ) {
      this.plugin = plugin;
      this.name = name;
      this.world = world;
      this.region = (T)region.clone();
      this.parent = parent;
      this.owner = owner;
      this.flags = new HashMap<>(flags);
      this.members = new HashSet<>(members);
      this.priority = priority;
   }

   public CraftAreaCreator(ProtectPlugin plugin, String name, World world, T region) {
      this.plugin = plugin;
      this.name = name;
      this.world = world;
      this.region = (T)region.clone();
   }

   @Override
   public String name() {
      return this.name;
   }

   @Nullable
   @Override
   public UUID owner() {
      return this.owner;
   }

   @Override
   public AreaCreator<T> copy() {
      return new CraftAreaCreator<>(this.plugin, this.name, this.world, this.region, this.parent, this.owner, this.flags, this.members, this.priority);
   }

   @Override
   public <V extends Region> AreaCreator<V> region(V region) {
      return new CraftAreaCreator<>(this.plugin, this.name, this.world, region, this.parent, this.owner, this.flags, this.members, this.priority);
   }

   @Override
   public AreaCreator<T> world(World world) {
      this.world = world;
      return this;
   }

   @Override
   public Map<Flag<?>, Object> flags() {
      return Map.copyOf(this.flags);
   }

   @Override
   public AreaCreator<T> flags(Map<Flag<?>, Object> flags) {
      this.flags = new HashMap<>(flags);
      return this;
   }

   @Override
   public AreaCreator<T> members(Set<UUID> members) {
      this.members = new HashSet<>(members);
      return this;
   }

   @Override
   public AreaCreator<T> name(String name) {
      this.name = name;
      return this;
   }

   @Override
   public AreaCreator<T> owner(@Nullable UUID owner) {
      this.owner = owner;
      return this;
   }

   @Override
   public AreaCreator<T> parent(@Nullable String parent) {
      this.parent = parent;
      return this;
   }

   @Override
   public AreaCreator<T> parent(@Nullable Area area) {
      return this.parent(area == null ? null : area.getName());
   }

   @Override
   public AreaCreator<T> priority(int priority) {
      this.priority = priority;
      return this;
   }

   @Override
   public RegionizedArea<T> create() throws UnsupportedRegionException, CircularInheritanceException, IOException {
      RegionizedArea<T> area = this.plugin
         .areaService()
         .<T>getWrapper(this.region().getClass())
         .orElseThrow(() -> new UnsupportedRegionException(this.region().getClass()))
         .apply(this);
      this.plugin.areaProvider().persist(area);
      new AreaCreateEvent(area).callEvent();
      return area;
   }

   @Override
   public Set<UUID> members() {
      return this.members;
   }

   @Nullable
   @Override
   public String parent() {
      return this.parent;
   }

   @Override
   public T region() {
      return this.region;
   }

   @Override
   public World world() {
      return this.world;
   }

   @Override
   public int priority() {
      return this.priority;
   }
}
