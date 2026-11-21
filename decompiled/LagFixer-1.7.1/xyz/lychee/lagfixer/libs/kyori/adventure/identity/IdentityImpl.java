package xyz.lychee.lagfixer.libs.kyori.adventure.identity;

import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lychee.lagfixer.libs.kyori.adventure.internal.Internals;
import xyz.lychee.lagfixer.libs.kyori.examination.Examinable;

final class IdentityImpl implements Examinable, Identity {
   private final UUID uuid;

   IdentityImpl(final UUID uuid) {
      this.uuid = uuid;
   }

   @NotNull
   @Override
   public UUID uuid() {
      return this.uuid;
   }

   @Override
   public String toString() {
      return Internals.toString(this);
   }

   @Override
   public boolean equals(@Nullable final Object other) {
      if (this == other) {
         return true;
      } else if (!(other instanceof Identity)) {
         return false;
      } else {
         Identity that = (Identity)other;
         return this.uuid.equals(that.uuid());
      }
   }

   @Override
   public int hashCode() {
      return this.uuid.hashCode();
   }
}
