package org.lushplugins.lushrewards.libraries.mysql.cj.xdevapi;

import java.util.ArrayList;
import java.util.TimeZone;
import org.lushplugins.lushrewards.libraries.mysql.cj.MysqlxSession;
import org.lushplugins.lushrewards.libraries.mysql.cj.conf.PropertySet;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.ColumnDefinition;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.ProtocolEntity;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.ResultBuilder;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.x.Notice;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.x.StatementExecuteOkBuilder;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.x.XProtocol;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.x.XProtocolRowInputStream;
import org.lushplugins.lushrewards.libraries.mysql.cj.result.DefaultColumnDefinition;
import org.lushplugins.lushrewards.libraries.mysql.cj.result.Field;
import org.lushplugins.lushrewards.libraries.mysql.cj.result.RowList;

public class StreamingRowResultBuilder implements ResultBuilder<RowResult> {
   private ArrayList<Field> fields = new ArrayList<>();
   private ColumnDefinition metadata;
   private RowList rowList = null;
   TimeZone defaultTimeZone;
   PropertySet pset;
   XProtocol protocol;
   private StatementExecuteOkBuilder statementExecuteOkBuilder = new StatementExecuteOkBuilder();

   public StreamingRowResultBuilder(MysqlxSession sess) {
      this.defaultTimeZone = sess.getServerSession().getDefaultTimeZone();
      this.pset = sess.getPropertySet();
      this.protocol = sess.getProtocol();
   }

   @Override
   public boolean addProtocolEntity(ProtocolEntity entity) {
      if (entity instanceof Field) {
         this.fields.add((Field)entity);
         return false;
      } else {
         if (entity instanceof Notice) {
            this.statementExecuteOkBuilder.addProtocolEntity(entity);
         }

         if (this.metadata == null) {
            this.metadata = new DefaultColumnDefinition(this.fields.toArray(new Field[0]));
         }

         this.rowList = entity instanceof org.lushplugins.lushrewards.libraries.mysql.cj.result.Row
            ? new XProtocolRowInputStream(
               this.metadata,
               (org.lushplugins.lushrewards.libraries.mysql.cj.result.Row)entity,
               this.protocol,
               n -> this.statementExecuteOkBuilder.addProtocolEntity(n)
            )
            : new XProtocolRowInputStream(this.metadata, this.protocol, n -> this.statementExecuteOkBuilder.addProtocolEntity(n));
         return true;
      }
   }

   public RowResult build() {
      return new RowResultImpl(
         this.metadata, this.defaultTimeZone, this.rowList, () -> this.protocol.readQueryResult(this.statementExecuteOkBuilder), this.pset
      );
   }
}
