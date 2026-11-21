package org.lushplugins.lushrewards.libraries.mysql.cj.xdevapi;

import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.x.StatementExecuteOk;

public class InsertResultImpl extends UpdateResult implements InsertResult {
   public InsertResultImpl(StatementExecuteOk ok) {
      super(ok);
   }

   @Override
   public Long getAutoIncrementValue() {
      return this.ok.getLastInsertId();
   }
}
