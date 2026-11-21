package com.donutorders.libs.mysql.cj.xdevapi;

import com.donutorders.libs.mysql.cj.MysqlxSession;
import com.donutorders.libs.mysql.cj.conf.PropertySet;
import com.donutorders.libs.mysql.cj.protocol.ColumnDefinition;
import com.donutorders.libs.mysql.cj.protocol.ProtocolEntity;
import com.donutorders.libs.mysql.cj.protocol.ResultBuilder;
import com.donutorders.libs.mysql.cj.protocol.x.Notice;
import com.donutorders.libs.mysql.cj.protocol.x.StatementExecuteOkBuilder;
import com.donutorders.libs.mysql.cj.protocol.x.XProtocol;
import com.donutorders.libs.mysql.cj.protocol.x.XProtocolRowInputStream;
import com.donutorders.libs.mysql.cj.result.DefaultColumnDefinition;
import com.donutorders.libs.mysql.cj.result.Field;
import com.donutorders.libs.mysql.cj.result.RowList;
import java.util.ArrayList;

public class StreamingDocResultBuilder implements ResultBuilder<DocResult> {
   private ArrayList<Field> fields = new ArrayList<>();
   private ColumnDefinition metadata;
   private RowList rowList = null;
   PropertySet pset;
   XProtocol protocol;
   private StatementExecuteOkBuilder statementExecuteOkBuilder = new StatementExecuteOkBuilder();

   public StreamingDocResultBuilder(MysqlxSession sess) {
      this.pset = sess.getPropertySet();
      this.protocol = sess.getProtocol();
   }

   @Override
   public boolean addProtocolEntity(ProtocolEntity entity) {
      if (entity instanceof Field) {
         this.fields.add((Field)entity);
         return false;
      } else if (entity instanceof Notice) {
         this.statementExecuteOkBuilder.addProtocolEntity(entity);
         return false;
      } else {
         if (this.metadata == null) {
            this.metadata = new DefaultColumnDefinition(this.fields.toArray(new Field[0]));
         }

         this.rowList = entity instanceof com.donutorders.libs.mysql.cj.result.Row
            ? new XProtocolRowInputStream(
               this.metadata, (com.donutorders.libs.mysql.cj.result.Row)entity, this.protocol, n -> this.statementExecuteOkBuilder.addProtocolEntity(n)
            )
            : new XProtocolRowInputStream(this.metadata, this.protocol, n -> this.statementExecuteOkBuilder.addProtocolEntity(n));
         return true;
      }
   }

   public DocResult build() {
      return new DocResultImpl(this.rowList, () -> this.protocol.readQueryResult(this.statementExecuteOkBuilder), this.pset);
   }
}
