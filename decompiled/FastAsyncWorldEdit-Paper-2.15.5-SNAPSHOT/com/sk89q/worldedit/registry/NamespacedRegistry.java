package com.sk89q.worldedit.registry;

import com.fastasyncworldedit.core.registry.RegistryItem;
import com.google.common.base.Preconditions;
import com.sk89q.worldedit.command.util.SuggestionHelper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nullable;

public final class NamespacedRegistry<V extends Keyed> extends Registry<V> {
   private static final String MINECRAFT_NAMESPACE = "minecraft";
   private final Set<String> knownNamespaces = new HashSet<>();
   private final String defaultNamespace;
   private final List<V> values = new ArrayList<>();
   private int lastInternalId = 0;

   @Deprecated
   public NamespacedRegistry(final String name) {
      this(name, "minecraft");
   }

   @Deprecated
   public NamespacedRegistry(final String name, final boolean checkInitialized) {
      this(name, "minecraft", checkInitialized);
   }

   @Deprecated
   public NamespacedRegistry(final String name, final String defaultNamespace) {
      this(name, defaultNamespace, false);
   }

   @Deprecated
   public NamespacedRegistry(final String name, final String defaultNamespace, final boolean checkInitialized) {
      super(name, checkInitialized);
      this.defaultNamespace = defaultNamespace;
   }

   public NamespacedRegistry(final String name, final String id, final String defaultNamespace) {
      this(name, id, defaultNamespace, false);
   }

   public NamespacedRegistry(final String name, final String id, final String defaultNamespace, final boolean checkInitialized) {
      super(name, id, checkInitialized);
      this.defaultNamespace = defaultNamespace;
   }

   @Nullable
   @Override
   public V get(final String key) {
      return super.get(this.orDefaultNamespace(key));
   }

   @Override
   public synchronized V register(final String key, final V value) {
      Objects.requireNonNull(key, "key");
      int i = key.indexOf(58);
      Preconditions.checkState(i > 0, "key is not namespaced");
      if (value instanceof RegistryItem) {
         ((RegistryItem)value).setInternalId(this.lastInternalId++);
      }

      this.values.add(value);
      V registered = super.register(key, value);
      this.knownNamespaces.add(key.substring(0, i));
      return registered;
   }

   public V getByInternalId(int index) {
      try {
         return this.values.get(index);
      } catch (IndexOutOfBoundsException var3) {
         return null;
      }
   }

   public int size() {
      return this.values.size();
   }

   public Set<String> getKnownNamespaces() {
      return Collections.unmodifiableSet(this.knownNamespaces);
   }

   public String getDefaultNamespace() {
      return this.defaultNamespace;
   }

   private String orDefaultNamespace(final String key) {
      return key.indexOf(58) == -1 ? this.defaultNamespace + ":" + key : key;
   }

   public <V1 extends Keyed> Stream<String> getSuggestions(String input) {
      return SuggestionHelper.getNamespacedRegistrySuggestions(this, input);
   }
}
