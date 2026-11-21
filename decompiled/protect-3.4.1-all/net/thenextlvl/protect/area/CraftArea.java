package net.thenextlvl.protect.area;

import com.google.common.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import net.kyori.adventure.key.Key;
import net.thenextlvl.nbt.tag.CompoundTag;
import net.thenextlvl.nbt.tag.Tag;
import net.thenextlvl.protect.ProtectPlugin;
import net.thenextlvl.protect.area.event.flag.AreaFlagChangeEvent;
import net.thenextlvl.protect.area.event.flag.AreaFlagResetEvent;
import net.thenextlvl.protect.area.event.inheritance.AreaPriorityChangeEvent;
import net.thenextlvl.protect.area.event.member.AreaMemberAddEvent;
import net.thenextlvl.protect.area.event.member.AreaMemberRemoveEvent;
import net.thenextlvl.protect.area.event.member.AreaOwnerChangeEvent;
import net.thenextlvl.protect.flag.Flag;
import net.thenextlvl.protect.io.AreaAdapter;
import org.bukkit.Server;
import org.bukkit.World;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public abstract class CraftArea implements Area {
   protected final ProtectPlugin plugin;
   private final String name;
   private final World world;
   private final Set<UUID> members;
   @Nullable
   private UUID owner;
   private final Map<Flag<?>, Object> flags;
   private int priority;
   private final Map<String, Tag> dataContainer = new LinkedHashMap<>();

   protected CraftArea(ProtectPlugin plugin, String name, World world, Set<UUID> members, @Nullable UUID owner, Map<Flag<?>, Object> flags, int priority) {
      this.plugin = plugin;
      this.name = name;
      this.world = world;
      this.members = new HashSet<>(members);
      this.owner = owner;
      this.flags = new HashMap<>(flags);
      this.priority = priority;
   }

   public CraftArea(ProtectPlugin plugin, World world, String name, CompoundTag tag) {
      this.plugin = plugin;
      this.name = name;
      this.world = world;
      this.members = new HashSet<>();
      this.flags = new HashMap<>();
      this.deserialize(tag);
   }

   @Override
   public Server getServer() {
      return this.plugin.getServer();
   }

   @Override
   public LinkedHashSet<Area> getParents() {
      LinkedHashSet<Area> parents = new LinkedHashSet<>();
      Area parent = this.getParent().orElse(null);

      while (parent != null && parent != this && parents.add(parent)) {
         parent = parent.getParent().orElse(null);
      }

      return parents;
   }

   @Override
   public Map<Flag<?>, Object> getFlags() {
      return Map.copyOf(this.flags);
   }

   @Override
   public Optional<UUID> getOwner() {
      return Optional.ofNullable(this.owner);
   }

   @Override
   public boolean isMember(UUID uuid) {
      return this.members.contains(uuid);
   }

   @Override
   public boolean isPermitted(UUID uuid) {
      return this.owner != null && this.owner.equals(uuid) || this.members.contains(uuid);
   }

   @Override
   public boolean removeMember(UUID uuid) {
      AreaMemberRemoveEvent event = new AreaMemberRemoveEvent(this, uuid);
      return event.callEvent() && this.members.remove(event.getMember());
   }

   @Override
   public boolean addMember(UUID uuid) {
      AreaMemberAddEvent event = new AreaMemberAddEvent(this, uuid);
      return event.callEvent() && this.members.add(event.getMember());
   }

   @Override
   public int getPriority() {
      return this.priority;
   }

   @Override
   public void setMembers(Set<UUID> members) {
      if (!Objects.equals(members, this.members)) {
         for (UUID member : this.members) {
            if (!members.contains(member)) {
               this.removeMember(member);
            }
         }

         for (UUID memberx : members) {
            if (!this.members.contains(memberx)) {
               this.addMember(memberx);
            }
         }
      }
   }

   @Override
   public String getName() {
      return this.name;
   }

   @Override
   public World getWorld() {
      return this.world;
   }

   @Override
   public Set<UUID> getMembers() {
      return Set.copyOf(this.members);
   }

   @Override
   public boolean setOwner(@Nullable UUID owner) {
      if (Objects.equals(owner, this.owner)) {
         return false;
      } else {
         AreaOwnerChangeEvent event = new AreaOwnerChangeEvent(this, owner);
         if (event.callEvent()) {
            this.owner = event.getOwner();
         }

         return !event.isCancelled();
      }
   }

   @Override
   public boolean setPriority(int priority) {
      if (this.priority == priority) {
         return false;
      } else {
         AreaPriorityChangeEvent event = new AreaPriorityChangeEvent(this, priority);
         if (!event.callEvent()) {
            return false;
         } else {
            this.priority = priority;
            return true;
         }
      }
   }

   @Override
   public void setFlags(Map<Flag<?>, Object> flags) {
      if (!Objects.equals(this.flags, flags)) {
         flags.forEach((flag, o) -> this.setFlag((Flag<?>)flag, o));
      }
   }

   @NullUnmarked
   @Override
   public <T> T getFlag(@NonNull Flag<T> flag) {
      T value = (T)this.getFlags().get(flag);
      return value != null ? value : this.getParent().map(area -> area.getFlag(flag)).orElseGet(flag::defaultValue);
   }

   @NullUnmarked
   @Override
   public <T> boolean setFlag(@NonNull Flag<T> flag, T state) {
      if (Objects.equals(this.getFlag(flag), state)) {
         return false;
      } else {
         AreaFlagChangeEvent<T> event = new AreaFlagChangeEvent<>(this, flag, state);
         return event.callEvent() && !Objects.equals(this.flags.put(flag, event.getNewState()), event.getNewState());
      }
   }

   @Override
   public <T> boolean removeFlag(Flag<T> flag) {
      if (!this.flags.containsKey(flag)) {
         return false;
      } else {
         AreaFlagResetEvent<T> event = new AreaFlagResetEvent<>(this, flag);
         if (event.callEvent()) {
            this.flags.remove(flag);
         }

         return !event.isCancelled();
      }
   }

   @Override
   public <T> boolean hasFlag(Flag<T> flag) {
      return this.flags.containsKey(flag);
   }

   public int compareTo(Area area) {
      return Integer.compare(this.getPriority(), area.getPriority());
   }

   public CompoundTag serialize() {
      CompoundTag.Builder tag = CompoundTag.builder();
      if (!this.flags.isEmpty()) {
         tag.put("flags", this.plugin.nbt.serialize(this.flags, (new TypeToken<Map<Flag<?>, Object>>() {}).getType()));
      }

      if (!this.members.isEmpty()) {
         tag.put("members", this.plugin.nbt.serialize(this.members, (new TypeToken<Set<UUID>>() {}).getType()));
      }

      if (!this.dataContainer.isEmpty()) {
         tag.put("data", CompoundTag.of(this.dataContainer));
      }

      if (this.owner != null) {
         tag.put("owner", this.plugin.nbt.serialize(this.owner));
      }

      tag.put("priority", this.priority);
      AreaAdapter<? extends CraftArea> adapter = this.plugin.areaService().getAdapter((Class<? extends CraftArea>)this.getClass());
      tag.put("type", adapter.key().asString());
      return tag.build();
   }

   public void deserialize(CompoundTag tag) {
      this.readFlags(tag).ifPresent(this.flags::putAll);
      this.readMembers(tag).ifPresent(this.members::addAll);
      this.readOwner(tag).ifPresent(owner -> this.owner = owner);
      this.readPriority(tag).ifPresent(priority -> this.priority = priority);
      tag.<CompoundTag>optional("data").ifPresent(data -> data.forEach(this.dataContainer::put));
   }

   private Optional<Map<Flag<?>, Object>> readFlags(CompoundTag tag) {
      return tag.<Tag>optional("flags").map(flags -> {
         Type type = (new TypeToken<Map<Flag<?>, Object>>() {}).getType();
         return this.plugin.nbt.deserialize(flags, type);
      });
   }

   private Optional<Set<UUID>> readMembers(CompoundTag tag) {
      return tag.<Tag>optional("members").map(members -> this.plugin.nbt.deserialize(members, (new TypeToken<Set<UUID>>() {}).getType()));
   }

   private Optional<UUID> readOwner(CompoundTag tag) {
      return tag.<Tag>optional("owner").map(owner -> this.plugin.nbt.deserialize(owner, UUID.class));
   }

   private Optional<Integer> readPriority(CompoundTag tag) {
      return tag.<Tag>optional("priority").map(Tag::getAsInt);
   }

   @Override
   public <T extends Tag> Optional<T> get(Key key) {
      return Optional.ofNullable((T)this.dataContainer.get(key.asString()));
   }

   @Override
   public <T extends Tag> Optional<T> remove(Key key) {
      return Optional.ofNullable((T)this.dataContainer.remove(key.asString()));
   }

   @Override
   public <T extends Tag> T getOrDefault(Key key, T defaultValue) {
      return (T)this.dataContainer.getOrDefault(key.asString(), defaultValue);
   }

   @Override
   public <T extends Tag> void set(Key key, T value) {
      this.dataContainer.put(key.asString(), value);
   }

   @Override
   public Map<Key, Tag> getTags() {
      return this.dataContainer
         .entrySet()
         .stream()
         .map(entry -> Map.entry(Key.key(entry.getKey()), entry.getValue()))
         .collect(Collectors.toUnmodifiableMap(Entry::getKey, Entry::getValue));
   }

   @Override
   public Set<Key> getKeys() {
      return this.dataContainer.keySet().stream().<Key>map(Key::key).collect(Collectors.toUnmodifiableSet());
   }

   @Override
   public Set<Entry<Key, Tag>> entrySet() {
      return this.getTags().entrySet();
   }

   @Override
   public boolean has(Key key) {
      return this.dataContainer.containsKey(key.asString());
   }

   @Override
   public boolean isEmpty() {
      return this.dataContainer.isEmpty();
   }

   @Override
   public void clear() {
      this.dataContainer.clear();
   }

   @Override
   public void copyTo(DataContainer dataContainer) {
      this.copyTo(dataContainer, false);
   }

   @Override
   public void copyTo(DataContainer dataContainer, boolean replace) {
      if (replace) {
         dataContainer.clear();
      }

      this.forEach(dataContainer::set);
   }

   @Override
   public void forEach(BiConsumer<Key, Tag> action) {
      this.entrySet().forEach(entry -> action.accept(entry.getKey(), entry.getValue()));
   }

   @Override
   public String toString() {
      return "CraftArea{name='" + this.name + "', world=" + this.world + "}";
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         CraftArea craftArea = (CraftArea)o;
         return Objects.equals(this.name, craftArea.name) && Objects.equals(this.world, craftArea.world);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.name, this.world);
   }
}
