package org.sethomegui.SetHomeGUI.libs.mysql.cj.xdevapi;

public class AddResultBuilder extends UpdateResultBuilder<AddResult> {
   public AddResult build() {
      return new AddResultImpl(this.statementExecuteOkBuilder.build());
   }
}
