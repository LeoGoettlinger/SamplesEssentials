package ac.grim.grimac.internal.storage.core;

import ac.grim.grimac.api.storage.category.Categories;
import ac.grim.grimac.api.storage.category.Category;
import ac.grim.grimac.api.storage.kind.Operation;
import ac.grim.grimac.api.storage.kind.ops.EntityOps;
import ac.grim.grimac.api.storage.kind.ops.EventStreamOps;
import ac.grim.grimac.api.storage.kind.ops.KeyValueScopedOps;
import ac.grim.grimac.api.storage.model.SettingScope;
import ac.grim.grimac.api.storage.query.DeleteCriteria;
import ac.grim.grimac.api.storage.query.Deletes;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;

@ApiStatus.Internal
final class DeleteCriteriaTranslator {
   private DeleteCriteriaTranslator() {
   }

   @Nullable
   static Operation<Void> translate(Category<?> cat, DeleteCriteria criteria) {
      if (criteria instanceof Deletes.ByPlayer bp) {
         if (cat == Categories.VIOLATION) {
            return new EventStreamOps.DeleteByPartitionOp(cat, "player_uuid", bp.uuid());
         }

         if (cat == Categories.PLAYER_IDENTITY) {
            return new EntityOps.DeleteByIdOp<>(cat, bp.uuid());
         }

         if (cat == Categories.SESSION) {
            return new EntityOps.DeleteByIndexOp(cat, "by_player_started", bp.uuid());
         }

         if (cat == Categories.SETTING) {
            return new KeyValueScopedOps.RemoveAllOp<>(cat, SettingScope.PLAYER, bp.uuid().toString());
         }
      }

      if (criteria instanceof Deletes.OlderThan ot && cat == Categories.VIOLATION) {
         long cutoff = System.currentTimeMillis() - ot.maxAgeMs();
         return new EventStreamOps.DeleteOlderThanOp(cat, cutoff);
      } else {
         return null;
      }
   }
}
