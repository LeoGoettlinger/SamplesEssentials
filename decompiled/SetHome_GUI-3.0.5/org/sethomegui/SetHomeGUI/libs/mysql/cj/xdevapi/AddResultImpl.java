package org.sethomegui.SetHomeGUI.libs.mysql.cj.xdevapi;

import java.util.List;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.x.StatementExecuteOk;

public class AddResultImpl extends UpdateResult implements AddResult {
   public AddResultImpl(StatementExecuteOk ok) {
      super(ok);
   }

   @Override
   public List<String> getGeneratedIds() {
      return this.ok.getGeneratedIds();
   }
}
