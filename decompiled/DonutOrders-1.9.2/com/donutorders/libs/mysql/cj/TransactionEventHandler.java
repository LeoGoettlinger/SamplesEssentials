package com.donutorders.libs.mysql.cj;

public interface TransactionEventHandler {
   void transactionBegun();

   void transactionCompleted();
}
