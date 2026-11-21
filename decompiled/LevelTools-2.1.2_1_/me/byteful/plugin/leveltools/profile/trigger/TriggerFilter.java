package me.byteful.plugin.leveltools.profile.trigger;

import java.util.Collections;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public final class TriggerFilter {
   private final boolean whitelist;
   private final Set<String> entries;

   public TriggerFilter(boolean whitelist, @NotNull Set<String> entries) {
      this.whitelist = whitelist;
      this.entries = Collections.unmodifiableSet(entries);
   }

   @NotNull
   public static TriggerFilter empty() {
      return new TriggerFilter(false, Collections.emptySet());
   }

   public boolean isWhitelist() {
      return this.whitelist;
   }

   public boolean isBlacklist() {
      return !this.whitelist;
   }

   @NotNull
   public Set<String> getEntries() {
      return this.entries;
   }

   public boolean isAllowed(@NotNull String entry) {
      boolean inList = this.entries.contains(entry);
      return this.whitelist == inList;
   }
}
