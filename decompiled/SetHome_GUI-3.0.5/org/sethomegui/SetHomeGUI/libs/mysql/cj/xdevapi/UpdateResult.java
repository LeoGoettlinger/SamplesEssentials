package org.sethomegui.SetHomeGUI.libs.mysql.cj.xdevapi;

import java.util.Iterator;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.x.StatementExecuteOk;

public class UpdateResult implements Result {
   protected StatementExecuteOk ok;

   public UpdateResult(StatementExecuteOk ok) {
      this.ok = ok;
   }

   @Override
   public long getAffectedItemsCount() {
      return this.ok.getAffectedItemsCount();
   }

   @Override
   public int getWarningsCount() {
      return this.ok.getWarningsCount();
   }

   @Override
   public Iterator<Warning> getWarnings() {
      return this.ok.getWarnings();
   }
}
