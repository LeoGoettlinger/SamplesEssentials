package ac.grim.grimac.internal.storage.core;

import ac.grim.grimac.api.storage.category.Category;
import ac.grim.grimac.api.storage.kind.Operation;
import ac.grim.grimac.api.storage.kind.ops.EntityOps;
import ac.grim.grimac.api.storage.kind.ops.EventStreamOps;
import ac.grim.grimac.api.storage.kind.ops.KeyValueScopedOps;
import ac.grim.grimac.api.storage.query.Queries;
import ac.grim.grimac.api.storage.query.Query;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;

@ApiStatus.Internal
final class QueryToOperationTranslator {
   private QueryToOperationTranslator() {
   }

   @Nullable
   static Operation<?> translate(Category<?> cat, Query<?> query) {
      if (query instanceof Queries.GetSessionById g) {
         return new EntityOps.GetByIdOp<>(cat, g.sessionId());
      } else if (query instanceof Queries.ListSessionsByPlayer l) {
         return new EntityOps.FindByIndexOp(cat, "by_player_started", l.player(), l.cursor(), l.pageSize());
      } else if (query instanceof Queries.ListViolationsInSession l) {
         return new EventStreamOps.PageOp(cat, "session_id", l.sessionId(), l.cursor(), l.pageSize());
      } else if (query instanceof Queries.GetPlayerIdentity g) {
         return new EntityOps.GetByIdOp<>(cat, g.uuid());
      } else if (query instanceof Queries.GetPlayerIdentityByName g) {
         return new EntityOps.FindByIndexOp(cat, "by_name", g.name(), null, 1);
      } else if (query instanceof Queries.ListPlayersByNamePrefix p) {
         return new EntityOps.PrefixIndexOp(cat, "by_name", p.lowerPrefix(), null, p.limit());
      } else {
         return query instanceof Queries.GetSetting g ? new KeyValueScopedOps.GetOp<>(cat, g.scope(), g.scopeKey(), g.key()) : null;
      }
   }
}
