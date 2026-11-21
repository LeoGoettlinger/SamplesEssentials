package org.jdbi.v3.core;

interface TransactionCallback {
   default void afterCommit() {
   }

   default void afterRollback() {
   }
}
