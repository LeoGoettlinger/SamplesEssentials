package de.theredend2000.advancedhunt.lib.cloud.parser.flag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@API(
   status = API.Status.STABLE
)
public final class FlagContext {
   public static final Object FLAG_PRESENCE_VALUE = new Object();
   private final Map<String, List> flagValues = new HashMap<>();

   private FlagContext() {
   }

   @NonNull
   public static FlagContext create() {
      return new FlagContext();
   }

   public void addPresenceFlag(@NonNull final CommandFlag<?> flag) {
      ((List)this.flagValues.computeIfAbsent(flag.name(), $ -> new ArrayList())).add(FLAG_PRESENCE_VALUE);
   }

   public <T> void addValueFlag(@NonNull final CommandFlag<T> flag, @NonNull final T value) {
      ((List)this.flagValues.computeIfAbsent(flag.name(), $ -> new ArrayList())).add(value);
   }

   @API(
      status = API.Status.STABLE
   )
   public <T> int count(@NonNull final CommandFlag<T> flag) {
      return this.getAll(flag).size();
   }

   @API(
      status = API.Status.STABLE
   )
   public int count(@NonNull final String flag) {
      return this.getAll(flag).size();
   }

   public boolean isPresent(@NonNull final String flag) {
      List value = this.flagValues.get(flag);
      return value != null && !value.isEmpty();
   }

   @API(
      status = API.Status.STABLE
   )
   public boolean isPresent(@NonNull final CommandFlag<Void> flag) {
      return this.isPresent(flag.name());
   }

   @API(
      status = API.Status.STABLE
   )
   @NonNull
   public <T> Optional<T> getValue(@NonNull final String name) {
      List value = this.flagValues.get(name);
      return value != null && !value.isEmpty() ? Optional.of((T)value.get(0)) : Optional.empty();
   }

   @API(
      status = API.Status.STABLE
   )
   @NonNull
   public <T> Optional<T> getValue(@NonNull final CommandFlag<T> flag) {
      return this.getValue(flag.name());
   }

   @Nullable
   public <T> T getValue(@NonNull final String name, @Nullable final T defaultValue) {
      return this.<T>getValue(name).orElse(defaultValue);
   }

   @API(
      status = API.Status.STABLE
   )
   @Nullable
   public <T> T getValue(@NonNull final CommandFlag<T> name, @Nullable final T defaultValue) {
      return this.getValue(name).orElse(defaultValue);
   }

   @API(
      status = API.Status.STABLE
   )
   public boolean hasFlag(@NonNull final String name) {
      return this.getValue(name).isPresent();
   }

   @API(
      status = API.Status.STABLE
   )
   public boolean hasFlag(@NonNull final CommandFlag<?> flag) {
      return this.getValue(flag).isPresent();
   }

   @API(
      status = API.Status.STABLE
   )
   public boolean contains(@NonNull final String name) {
      return this.hasFlag(name);
   }

   @API(
      status = API.Status.STABLE
   )
   public boolean contains(@NonNull final CommandFlag<?> flag) {
      return this.hasFlag(flag);
   }

   @API(
      status = API.Status.STABLE
   )
   @Nullable
   public <T> T get(@NonNull final String name) {
      return this.<T>getValue(name).orElse(null);
   }

   @API(
      status = API.Status.STABLE
   )
   @Nullable
   public <T> T get(@NonNull final CommandFlag<T> flag) {
      return this.getValue(flag).orElse(null);
   }

   @API(
      status = API.Status.STABLE
   )
   @NonNull
   public <T> Collection<T> getAll(@NonNull final CommandFlag<T> flag) {
      List values = this.flagValues.get(flag.name());
      return values != null ? Collections.unmodifiableList(values) : Collections.emptyList();
   }

   @API(
      status = API.Status.STABLE
   )
   @NonNull
   public <T> Collection<T> getAll(@NonNull final String flag) {
      List values = this.flagValues.get(flag);
      return values != null ? Collections.unmodifiableList(values) : Collections.emptyList();
   }
}
