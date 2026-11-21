package org.jdbi.v3.core.argument;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import org.jdbi.v3.core.statement.StatementContext;

@FunctionalInterface
public interface NamedArgumentFinder {
   Optional<Argument> find(String name, StatementContext ctx);

   default Collection<String> getNames() {
      return Collections.emptySet();
   }
}
