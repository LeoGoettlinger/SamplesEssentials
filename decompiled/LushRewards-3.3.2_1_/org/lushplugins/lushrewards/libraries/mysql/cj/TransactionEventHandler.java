package org.lushplugins.lushrewards.libraries.mysql.cj;

public interface TransactionEventHandler {
   void transactionBegun();

   void transactionCompleted();
}
