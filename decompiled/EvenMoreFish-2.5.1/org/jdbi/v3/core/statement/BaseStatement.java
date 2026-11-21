package org.jdbi.v3.core.statement;

import java.io.Closeable;
import java.sql.SQLException;
import java.util.Collection;
import org.jdbi.v3.core.CloseException;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.config.Configurable;
import org.jdbi.v3.meta.Beta;

abstract class BaseStatement<This> implements Closeable, Configurable<This> {
   final This typedThis = (This)this;
   private final Handle handle;
   private final StatementContext ctx;

   BaseStatement(Handle handle) {
      this.handle = handle;
      ConfigRegistry config = handle.getConfig().createCopy();
      this.ctx = StatementContext.create(config, handle.getExtensionMethod(), this.getClass());
      if (config.get(SqlStatements.class).isAttachAllStatementsForCleanup()) {
         attachToHandleForCleanup(this.handle, this.ctx);
      }
   }

   public final Handle getHandle() {
      return this.handle;
   }

   @Override
   public ConfigRegistry getConfig() {
      return this.ctx.getConfig();
   }

   public final StatementContext getContext() {
      return this.ctx;
   }

   @Beta
   public final This attachToHandleForCleanup() {
      attachToHandleForCleanup(this.handle, this.ctx);
      return this.typedThis;
   }

   private static void attachToHandleForCleanup(Handle handle, StatementContext context) {
      Cleanable statementCleanable = context::close;
      handle.addCleanable(statementCleanable);
      context.addCleanable(() -> handle.removeCleanable(statementCleanable));
   }

   protected final void cleanUpForException(SQLException e) {
      try {
         this.close();
      } catch (CloseException var3) {
         e.addSuppressed(var3.getCause());
      } catch (Exception var4) {
         e.addSuppressed(var4);
      }
   }

   void addCustomizers(final Collection<StatementCustomizer> customizers) {
      customizers.forEach(this::addCustomizer);
   }

   final void callCustomizers(BaseStatement.StatementCustomizerInvocation invocation) {
      for (StatementCustomizer customizer : this.getCustomizers()) {
         try {
            invocation.call(customizer);
         } catch (SQLException var5) {
            throw new UnableToExecuteStatementException("Exception thrown in statement customization", var5, this.ctx);
         }
      }
   }

   private Collection<StatementCustomizer> getCustomizers() {
      return this.getConfig(SqlStatements.class).getCustomizers();
   }

   @Override
   public void close() {
      this.getContext().close();
   }

   @FunctionalInterface
   interface StatementCustomizerInvocation {
      void call(StatementCustomizer t) throws SQLException;
   }
}
