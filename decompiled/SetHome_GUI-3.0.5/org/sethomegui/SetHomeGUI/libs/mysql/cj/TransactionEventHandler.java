package org.sethomegui.SetHomeGUI.libs.mysql.cj;

public interface TransactionEventHandler {
   void transactionBegun();

   void transactionCompleted();
}
