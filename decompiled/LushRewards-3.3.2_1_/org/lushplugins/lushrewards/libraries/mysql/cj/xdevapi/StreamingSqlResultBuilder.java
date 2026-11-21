package org.lushplugins.lushrewards.libraries.mysql.cj.xdevapi;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import org.lushplugins.lushrewards.libraries.mysql.cj.MysqlxSession;
import org.lushplugins.lushrewards.libraries.mysql.cj.conf.PropertySet;
import org.lushplugins.lushrewards.libraries.mysql.cj.exceptions.CJCommunicationsException;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.ColumnDefinition;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.ProtocolEntity;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.ResultBuilder;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.x.Notice;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.x.StatementExecuteOk;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.x.StatementExecuteOkBuilder;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.x.XProtocol;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.x.XProtocolRowInputStream;
import org.lushplugins.lushrewards.libraries.mysql.cj.result.Field;

public class StreamingSqlResultBuilder implements ResultBuilder<SqlResult> {
   TimeZone defaultTimeZone;
   PropertySet pset;
   XProtocol protocol;
   StatementExecuteOkBuilder statementExecuteOkBuilder = new StatementExecuteOkBuilder();
   boolean isRowResult = false;
   ProtocolEntity lastEntity = null;
   List<SqlSingleResult> resultSets = new ArrayList<>();
   private SqlResult result;

   public StreamingSqlResultBuilder(MysqlxSession sess) {
      this.defaultTimeZone = sess.getServerSession().getDefaultTimeZone();
      this.pset = sess.getPropertySet();
      this.protocol = sess.getProtocol();
   }

   @Override
   public boolean addProtocolEntity(ProtocolEntity entity) {
      if (entity instanceof Notice) {
         this.statementExecuteOkBuilder.addProtocolEntity(entity);
      } else {
         this.lastEntity = entity;
      }

      AtomicBoolean readLastResult = new AtomicBoolean(false);
      Supplier<ProtocolEntity> okReader = () -> {
         if (readLastResult.get()) {
            throw new CJCommunicationsException("Invalid state attempting to read ok packet");
         } else if (this.protocol.hasMoreResults()) {
            StatementExecuteOk res = this.statementExecuteOkBuilder.build();
            this.statementExecuteOkBuilder = new StatementExecuteOkBuilder();
            return res;
         } else {
            readLastResult.set(true);
            return this.protocol.readQueryResult(this.statementExecuteOkBuilder);
         }
      };
      Supplier<SqlResult> resultStream = () -> {
         if (readLastResult.get()) {
            return null;
         } else if ((this.lastEntity == null || !(this.lastEntity instanceof Field)) && !this.protocol.isSqlResultPending()) {
            readLastResult.set(true);
            SqlResultBuilder rb = new SqlResultBuilder(this.defaultTimeZone, this.pset);
            rb.addProtocolEntity(entity);
            return this.protocol.readQueryResult(rb);
         } else {
            ColumnDefinition cd;
            if (this.lastEntity != null && this.lastEntity instanceof Field) {
               cd = this.protocol.readMetadata((Field)this.lastEntity, n -> this.statementExecuteOkBuilder.addProtocolEntity(n));
               this.lastEntity = null;
            } else {
               cd = this.protocol.readMetadata(this.statementExecuteOkBuilder::addProtocolEntity);
            }

            return new SqlSingleResult(
               cd,
               this.protocol.getServerSession().getDefaultTimeZone(),
               new XProtocolRowInputStream(cd, this.protocol, n -> this.statementExecuteOkBuilder.addProtocolEntity(n)),
               okReader,
               this.pset
            );
         }
      };
      this.result = new SqlMultiResult(resultStream);
      return true;
   }

   public SqlResult build() {
      return this.result;
   }
}
