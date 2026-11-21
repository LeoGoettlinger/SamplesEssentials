package com.sk89q.worldedit.registry;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.WorldEdit;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;

public class Registry<V extends Keyed> implements Iterable<V>, Keyed {
   public static final Registry<Registry<?>> REGISTRY = new Registry<>("registry", "registry");
   private final Map<String, V> map = new HashMap<>();
   private final String name;
   private final String id;
   private final boolean checkInitialized;

   private static String nameToId(String name) {
      return name.toLowerCase(Locale.ROOT).replace(' ', '_');
   }

   @Deprecated
   public Registry(final String name) {
      this(name, false);
   }

   @Deprecated
   public Registry(final String name, final boolean checkInitialized) {
      this(name, nameToId(name), checkInitialized);
   }

   public Registry(final String name, final String id) {
      this(name, id, false);
   }

   public Registry(final String name, final String id, final boolean checkInitialized) {
      this.name = name;
      this.id = id;
      this.checkInitialized = checkInitialized;
   }

   public String getName() {
      return this.name;
   }

   @Override
   public String id() {
      return this.id;
   }

   public Map<String, V> getMap() {
      return this.map;
   }

   @Nullable
   public V get(final String key) {
      Preconditions.checkState(key.equals(key.toLowerCase(Locale.ROOT)), "key must be lowercase: %s", key);
      if (this.checkInitialized) {
         Preconditions.checkState(WorldEdit.getInstance().getPlatformManager().isInitialized(), "WorldEdit is not initialized yet.");
      }

      return this.map.get(key);
   }

   public V register(final String key, final V value) {
      Objects.requireNonNull(key, "key");
      Objects.requireNonNull(value, "value");
      Preconditions.checkState(key.equals(key.toLowerCase(Locale.ROOT)), "key must be lowercase: %s", key);
      Preconditions.checkState(!this.map.containsKey(key), "key '%s' already has an associated %s", key, this.name);
      this.map.put(key, value);
      return value;
   }

   public void clear() {
      this.map.clear();
   }

   public Set<String> keySet() {
      return Collections.unmodifiableSet(this.map.keySet());
   }

   public Collection<V> values() {
      return Collections.unmodifiableCollection(this.map.values());
   }

   @Override
   public Iterator<V> iterator() {
      return this.map.values().iterator();
   }
}
