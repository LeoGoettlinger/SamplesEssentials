package org.sethomegui.SetHomeGUI.libs.mysql.cj.xdevapi;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.MysqlxSession;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.conf.PropertySet;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.ColumnDefinition;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.ProtocolEntity;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.ResultBuilder;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.x.FetchDoneEntity;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.x.FetchDoneMoreResults;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.x.Notice;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.x.StatementExecuteOk;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.x.StatementExecuteOkBuilder;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.result.BufferedRowList;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.result.DefaultColumnDefinition;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.result.Field;

public class SqlResultBuilder implements ResultBuilder<SqlResult> {
   private ArrayList<Field> fields = new ArrayList<>();
   private ColumnDefinition metadata;
   private List<org.sethomegui.SetHomeGUI.libs.mysql.cj.result.Row> rows = new ArrayList<>();
   TimeZone defaultTimeZone;
   PropertySet pset;
   boolean isRowResult = false;
   List<SqlSingleResult> resultSets = new ArrayList<>();
   private ProtocolEntity prevEntity = null;
   private StatementExecuteOkBuilder statementExecuteOkBuilder = new StatementExecuteOkBuilder();

   public SqlResultBuilder(TimeZone defaultTimeZone, PropertySet pset) {
      this.defaultTimeZone = defaultTimeZone;
      this.pset = pset;
   }

   public SqlResultBuilder(MysqlxSession sess) {
      this.defaultTimeZone = sess.getServerSession().getDefaultTimeZone();
      this.pset = sess.getPropertySet();
   }

   @Override
   public boolean addProtocolEntity(ProtocolEntity entity) {
      if (entity instanceof Field) {
         this.fields.add((Field)entity);
         if (!this.isRowResult) {
            this.isRowResult = true;
         }

         this.prevEntity = entity;
         return false;
      } else if (entity instanceof Notice) {
         this.statementExecuteOkBuilder.addProtocolEntity(entity);
         return false;
      } else {
         if (this.isRowResult && this.metadata == null) {
            this.metadata = new DefaultColumnDefinition(this.fields.toArray(new Field[0]));
         }

         if (entity instanceof org.sethomegui.SetHomeGUI.libs.mysql.cj.result.Row) {
            this.rows.add(((org.sethomegui.SetHomeGUI.libs.mysql.cj.result.Row)entity).setMetadata(this.metadata));
         } else if (entity instanceof FetchDoneMoreResults) {
            this.resultSets
               .add(
                  new SqlSingleResult(
                     this.metadata, this.defaultTimeZone, new BufferedRowList(this.rows), () -> this.statementExecuteOkBuilder.build(), this.pset
                  )
               );
            this.fields = new ArrayList<>();
            this.metadata = null;
            this.rows = new ArrayList<>();
            this.statementExecuteOkBuilder = new StatementExecuteOkBuilder();
         } else if (entity instanceof FetchDoneEntity) {
            if (!(this.prevEntity instanceof FetchDoneMoreResults)) {
               this.resultSets
                  .add(
                     new SqlSingleResult(
                        this.metadata, this.defaultTimeZone, new BufferedRowList(this.rows), () -> this.statementExecuteOkBuilder.build(), this.pset
                     )
                  );
            }
         } else if (entity instanceof StatementExecuteOk) {
            return true;
         }

         this.prevEntity = entity;
         return false;
      }
   }

   public SqlResult build() {
      return (SqlResult)(this.isRowResult
         ? new SqlMultiResult(() -> this.resultSets.size() > 0 ? this.resultSets.remove(0) : null)
         : new SqlUpdateResult(this.statementExecuteOkBuilder.build()));
   }
}
