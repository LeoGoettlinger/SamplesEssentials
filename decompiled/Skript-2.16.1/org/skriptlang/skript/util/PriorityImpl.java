package org.skriptlang.skript.util;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

class PriorityImpl implements Priority {
   private final Set<Priority> after;
   private final Set<Priority> before;

   PriorityImpl() {
      this.after = ImmutableSet.of();
      this.before = ImmutableSet.of();
   }

   PriorityImpl(Priority priority, boolean isBefore) {
      Set<Priority> after = new HashSet<>();
      Set<Priority> before = new HashSet<>();
      if (isBefore) {
         before.add(priority);
      } else {
         after.add(priority);
      }

      after.addAll(priority.after());
      before.addAll(priority.before());
      this.after = ImmutableSet.copyOf(after);
      this.before = ImmutableSet.copyOf(before);
   }

   public int compareTo(@NotNull Priority other) {
      if (this == other) {
         return 0;
      } else {
         Collection<Priority> ourBefore = this.before();
         Collection<Priority> otherAfter = other.after();
         if (!ourBefore.contains(other) && !otherAfter.contains(this)) {
            Collection<Priority> ourAfter = this.after();
            Collection<Priority> otherBefore = other.before();
            if (!ourAfter.contains(other) && !otherBefore.contains(this)) {
               if (ourBefore.stream().anyMatch(otherAfter::contains)) {
                  return -1;
               } else if (ourAfter.stream().anyMatch(otherBefore::contains)) {
                  return 1;
               } else {
                  return other instanceof PriorityImpl ? 0 : other.compareTo(this) * -1;
               }
            } else {
               return 1;
            }
         } else {
            return -1;
         }
      }
   }

   @Override
   public Collection<Priority> after() {
      return this.after;
   }

   @Override
   public Collection<Priority> before() {
      return this.before;
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.after, this.before);
   }

   @Override
   public boolean equals(Object obj) {
      return obj instanceof Priority priority ? this.compareTo(priority) == 0 : false;
   }

   @Override
   public String toString() {
      return MoreObjects.toStringHelper(this).add("after", this.after).add("before", this.before).toString();
   }
}
