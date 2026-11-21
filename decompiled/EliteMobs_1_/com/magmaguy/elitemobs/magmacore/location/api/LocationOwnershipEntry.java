package com.magmaguy.elitemobs.magmacore.location.api;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import org.bukkit.Location;

public final class LocationOwnershipEntry implements Predicate<Location> {
   private final String namespace;
   private final Predicate<Location> ownsFn;
   private final Function<Location, Set<String>> kindsFn;
   private final Predicate<Location> protectionFn;

   public LocationOwnershipEntry(String namespace, Predicate<Location> ownsFn, Function<Location, Set<String>> kindsFn, Predicate<Location> protectionFn) {
      this.namespace = Objects.requireNonNull(namespace, "namespace");
      this.ownsFn = Objects.requireNonNull(ownsFn, "ownsFn");
      this.kindsFn = kindsFn != null ? kindsFn : loc -> Collections.emptySet();
      this.protectionFn = protectionFn != null ? protectionFn : loc -> false;
   }

   public String getNamespace() {
      return this.namespace;
   }

   public Set<String> kindsAt(Location loc) {
      Set<String> result = this.kindsFn.apply(loc);
      return result == null ? Collections.emptySet() : result;
   }

   public boolean isProtectedAt(Location loc) {
      return this.protectionFn.test(loc);
   }

   public boolean test(Location loc) {
      return this.ownsFn.test(loc);
   }
}
