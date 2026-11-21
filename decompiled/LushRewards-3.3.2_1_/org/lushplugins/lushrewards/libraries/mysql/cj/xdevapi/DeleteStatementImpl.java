package org.lushplugins.lushrewards.libraries.mysql.cj.xdevapi;

import java.util.concurrent.CompletableFuture;
import org.lushplugins.lushrewards.libraries.mysql.cj.MysqlxSession;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.x.XMessage;

public class DeleteStatementImpl extends FilterableStatement<DeleteStatement, Result> implements DeleteStatement {
   DeleteStatementImpl(MysqlxSession mysqlxSession, String schema, String table) {
      super(new TableFilterParams(schema, table, false));
      this.mysqlxSession = mysqlxSession;
   }

   protected Result executeStatement() {
      return this.mysqlxSession.query(this.getMessageBuilder().buildDelete(this.filterParams), new UpdateResultBuilder<>());
   }

   @Override
   protected XMessage getPrepareStatementXMessage() {
      return this.getMessageBuilder().buildPrepareDelete(this.preparedStatementId, this.filterParams);
   }

   protected Result executePreparedStatement() {
      return this.mysqlxSession.query(this.getMessageBuilder().buildPrepareExecute(this.preparedStatementId, this.filterParams), new UpdateResultBuilder<>());
   }

   @Override
   public CompletableFuture<Result> executeAsync() {
      return this.mysqlxSession.queryAsync(this.getMessageBuilder().buildDelete(this.filterParams), new UpdateResultBuilder<>());
   }
}
