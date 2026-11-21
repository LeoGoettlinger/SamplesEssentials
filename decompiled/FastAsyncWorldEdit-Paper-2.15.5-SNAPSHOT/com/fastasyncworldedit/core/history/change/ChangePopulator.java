package com.fastasyncworldedit.core.history.change;

import com.sk89q.worldedit.history.change.Change;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public interface ChangePopulator<C extends Change> {
   static <C extends Change> ChangePopulator<C> empty() {
      class Empty implements ChangePopulator<C> {
         @NotNull
         @Override
         public C create() {
            throw new UnsupportedOperationException("empty");
         }

         @Nullable
         @Override
         public C populate(@NotNull final C change) {
            return null;
         }

         @Nullable
         @Override
         public C updateOrCreate(@Nullable final Change change) {
            return null;
         }

         @Override
         public boolean accepts(final Change change) {
            return false;
         }
      }

      return new Empty();
   }

   @NotNull
   default C update(@Nullable Change before) {
      return (C)(this.accepts(before) ? before : this.create());
   }

   @NotNull
   C create();

   @Nullable
   default C updateOrCreate(@Nullable Change change) {
      C u = this.update(change);
      return this.populate(u);
   }

   @Nullable
   C populate(@NotNull C change);

   @Contract("null->false")
   boolean accepts(Change change);
}
