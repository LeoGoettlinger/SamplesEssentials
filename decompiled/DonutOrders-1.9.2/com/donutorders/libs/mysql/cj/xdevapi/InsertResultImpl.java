package com.donutorders.libs.mysql.cj.xdevapi;

import com.donutorders.libs.mysql.cj.protocol.x.StatementExecuteOk;

public class InsertResultImpl extends UpdateResult implements InsertResult {
   public InsertResultImpl(StatementExecuteOk ok) {
      super(ok);
   }

   @Override
   public Long getAutoIncrementValue() {
      return this.ok.getLastInsertId();
   }
}
