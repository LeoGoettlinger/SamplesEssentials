package org.jdbi.v3.core.statement.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.jdbi.v3.core.argument.NamedArgumentFinder;
import org.jdbi.v3.core.argument.internal.NamedArgumentFinderFactory;
import org.jdbi.v3.core.internal.MemoizingSupplier;
import org.jdbi.v3.core.statement.Binding;
import org.jdbi.v3.core.statement.StatementContext;

public class PreparedBinding extends Binding {
   public final Map<NamedArgumentFinderFactory.PrepareKey, Object> prepareKeys = new HashMap<>();
   public final List<Supplier<NamedArgumentFinder>> backupArgumentFinders = new ArrayList<>();
   public final Supplier<List<NamedArgumentFinder>> realizedBackupArgumentFinders = MemoizingSupplier.of(
      () -> this.backupArgumentFinders.stream().map(Supplier::get).toList()
   );

   public PreparedBinding(StatementContext ctx) {
      super(ctx);
   }

   @Override
   public boolean isEmpty() {
      return super.isEmpty() && this.prepareKeys.isEmpty() && this.backupArgumentFinders.isEmpty();
   }

   @Override
   public void clear() {
      throw new UnsupportedOperationException("can't reset backup argument finders");
   }
}
