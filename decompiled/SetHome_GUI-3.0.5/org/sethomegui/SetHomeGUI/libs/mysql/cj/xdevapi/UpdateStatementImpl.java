package org.sethomegui.SetHomeGUI.libs.mysql.cj.xdevapi;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.MysqlxSession;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.x.XMessage;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.x.XMessageBuilder;

public class UpdateStatementImpl extends FilterableStatement<UpdateStatement, Result> implements UpdateStatement {
   private UpdateParams updateParams = new UpdateParams();

   UpdateStatementImpl(MysqlxSession mysqlxSession, String schema, String table) {
      super(new TableFilterParams(schema, table, false));
      this.mysqlxSession = mysqlxSession;
   }

   protected Result executeStatement() {
      return this.mysqlxSession.query(this.getMessageBuilder().buildRowUpdate(this.filterParams, this.updateParams), new UpdateResultBuilder<>());
   }

   @Override
   protected XMessage getPrepareStatementXMessage() {
      return this.getMessageBuilder().buildPrepareRowUpdate(this.preparedStatementId, this.filterParams, this.updateParams);
   }

   protected Result executePreparedStatement() {
      return this.mysqlxSession.query(this.getMessageBuilder().buildPrepareExecute(this.preparedStatementId, this.filterParams), new UpdateResultBuilder<>());
   }

   @Override
   public CompletableFuture<Result> executeAsync() {
      return this.mysqlxSession
         .queryAsync(
            ((XMessageBuilder)this.mysqlxSession.<XMessage>getMessageBuilder()).buildRowUpdate(this.filterParams, this.updateParams),
            new UpdateResultBuilder<>()
         );
   }

   @Override
   public UpdateStatement set(Map<String, Object> fieldsAndValues) {
      this.resetPrepareState();
      this.updateParams.setUpdates(fieldsAndValues);
      return this;
   }

   @Override
   public UpdateStatement set(String field, Object value) {
      this.resetPrepareState();
      this.updateParams.addUpdate(field, value);
      return this;
   }
}
