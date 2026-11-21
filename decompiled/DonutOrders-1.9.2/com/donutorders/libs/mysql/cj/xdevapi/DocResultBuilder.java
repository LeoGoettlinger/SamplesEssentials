package com.donutorders.libs.mysql.cj.xdevapi;

import com.donutorders.libs.mysql.cj.MysqlxSession;
import com.donutorders.libs.mysql.cj.conf.PropertySet;
import com.donutorders.libs.mysql.cj.exceptions.ExceptionFactory;
import com.donutorders.libs.mysql.cj.exceptions.WrongArgumentException;
import com.donutorders.libs.mysql.cj.protocol.ColumnDefinition;
import com.donutorders.libs.mysql.cj.protocol.ProtocolEntity;
import com.donutorders.libs.mysql.cj.protocol.ResultBuilder;
import com.donutorders.libs.mysql.cj.protocol.x.FetchDoneEntity;
import com.donutorders.libs.mysql.cj.protocol.x.Notice;
import com.donutorders.libs.mysql.cj.protocol.x.StatementExecuteOk;
import com.donutorders.libs.mysql.cj.protocol.x.StatementExecuteOkBuilder;
import com.donutorders.libs.mysql.cj.result.BufferedRowList;
import com.donutorders.libs.mysql.cj.result.DefaultColumnDefinition;
import com.donutorders.libs.mysql.cj.result.Field;
import java.util.ArrayList;
import java.util.List;

public class DocResultBuilder implements ResultBuilder<DocResult> {
   private ArrayList<Field> fields = new ArrayList<>();
   private ColumnDefinition metadata;
   private List<com.donutorders.libs.mysql.cj.result.Row> rows = new ArrayList<>();
   private DocResult result;
   PropertySet pset;
   private StatementExecuteOkBuilder statementExecuteOkBuilder = new StatementExecuteOkBuilder();

   public DocResultBuilder(MysqlxSession sess) {
      this.pset = sess.getPropertySet();
   }

   @Override
   public boolean addProtocolEntity(ProtocolEntity entity) {
      if (entity instanceof Field) {
         this.fields.add((Field)entity);
         return false;
      } else if (entity instanceof com.donutorders.libs.mysql.cj.result.Row) {
         if (this.metadata == null) {
            this.metadata = new DefaultColumnDefinition(this.fields.toArray(new Field[0]));
         }

         this.rows.add(((com.donutorders.libs.mysql.cj.result.Row)entity).setMetadata(this.metadata));
         return false;
      } else if (entity instanceof Notice) {
         this.statementExecuteOkBuilder.addProtocolEntity(entity);
         return false;
      } else if (entity instanceof FetchDoneEntity) {
         return false;
      } else if (entity instanceof StatementExecuteOk) {
         return true;
      } else {
         throw (WrongArgumentException)ExceptionFactory.createException(WrongArgumentException.class, "Unexpected protocol entity " + entity);
      }
   }

   public DocResult build() {
      this.result = new DocResultImpl(new BufferedRowList(this.rows), () -> this.statementExecuteOkBuilder.build(), this.pset);
      return this.result;
   }
}
