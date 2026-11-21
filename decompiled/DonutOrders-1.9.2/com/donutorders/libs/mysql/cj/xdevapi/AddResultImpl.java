package com.donutorders.libs.mysql.cj.xdevapi;

import com.donutorders.libs.mysql.cj.protocol.x.StatementExecuteOk;
import java.util.List;

public class AddResultImpl extends UpdateResult implements AddResult {
   public AddResultImpl(StatementExecuteOk ok) {
      super(ok);
   }

   @Override
   public List<String> getGeneratedIds() {
      return this.ok.getGeneratedIds();
   }
}
