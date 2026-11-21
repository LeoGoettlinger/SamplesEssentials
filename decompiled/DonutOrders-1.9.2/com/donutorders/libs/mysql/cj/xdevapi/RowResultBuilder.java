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
import java.util.TimeZone;

public class RowResultBuilder implements ResultBuilder<RowResult> {
   private ArrayList<Field> fields = new ArrayList<>();
   private ColumnDefinition metadata;
   private List<com.donutorders.libs.mysql.cj.result.Row> rows = new ArrayList<>();
   private RowResult result;
   TimeZone defaultTimeZone;
   PropertySet pset;
   private StatementExecuteOkBuilder statementExecuteOkBuilder = new StatementExecuteOkBuilder();

   public RowResultBuilder(MysqlxSession sess) {
      this.defaultTimeZone = sess.getServerSession().getDefaultTimeZone();
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

   public RowResult build() {
      if (this.metadata == null) {
         this.metadata = new DefaultColumnDefinition(this.fields.toArray(new Field[0]));
      }

      this.result = new RowResultImpl(
         this.metadata, this.defaultTimeZone, new BufferedRowList(this.rows), () -> this.statementExecuteOkBuilder.build(), this.pset
      );
      return this.result;
   }
}
