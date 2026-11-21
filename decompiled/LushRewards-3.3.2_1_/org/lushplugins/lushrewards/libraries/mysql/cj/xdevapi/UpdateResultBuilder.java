package org.lushplugins.lushrewards.libraries.mysql.cj.xdevapi;

import org.lushplugins.lushrewards.libraries.mysql.cj.exceptions.ExceptionFactory;
import org.lushplugins.lushrewards.libraries.mysql.cj.exceptions.WrongArgumentException;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.ProtocolEntity;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.ResultBuilder;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.x.Notice;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.x.Ok;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.x.StatementExecuteOk;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.x.StatementExecuteOkBuilder;

public class UpdateResultBuilder<T extends Result> implements ResultBuilder<T> {
   protected StatementExecuteOkBuilder statementExecuteOkBuilder = new StatementExecuteOkBuilder();

   @Override
   public boolean addProtocolEntity(ProtocolEntity entity) {
      if (entity instanceof Notice) {
         this.statementExecuteOkBuilder.addProtocolEntity(entity);
         return false;
      } else if (entity instanceof StatementExecuteOk) {
         return true;
      } else if (entity instanceof Ok) {
         return true;
      } else {
         throw (WrongArgumentException)ExceptionFactory.createException(WrongArgumentException.class, "Unexpected protocol entity " + entity);
      }
   }

   public T build() {
      return (T)(new UpdateResult(this.statementExecuteOkBuilder.build()));
   }
}
