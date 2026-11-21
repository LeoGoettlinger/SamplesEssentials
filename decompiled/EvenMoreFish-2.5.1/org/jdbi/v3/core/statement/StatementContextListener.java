package org.jdbi.v3.core.statement;

import org.jdbi.v3.meta.Beta;

@Beta
public interface StatementContextListener {
   default void contextCreated(StatementContext statementContext) {
   }

   default void contextCleaned(StatementContext statementContext) {
   }

   default void cleanableAdded(StatementContext statementContext, Cleanable cleanable) {
   }

   default void cleanableRemoved(StatementContext statementContext, Cleanable cleanable) {
   }
}
