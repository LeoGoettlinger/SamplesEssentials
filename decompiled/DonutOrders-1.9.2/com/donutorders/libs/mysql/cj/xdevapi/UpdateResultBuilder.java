package com.donutorders.libs.mysql.cj.xdevapi;

import com.donutorders.libs.mysql.cj.exceptions.ExceptionFactory;
import com.donutorders.libs.mysql.cj.exceptions.WrongArgumentException;
import com.donutorders.libs.mysql.cj.protocol.ProtocolEntity;
import com.donutorders.libs.mysql.cj.protocol.ResultBuilder;
import com.donutorders.libs.mysql.cj.protocol.x.Notice;
import com.donutorders.libs.mysql.cj.protocol.x.Ok;
import com.donutorders.libs.mysql.cj.protocol.x.StatementExecuteOk;
import com.donutorders.libs.mysql.cj.protocol.x.StatementExecuteOkBuilder;

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
