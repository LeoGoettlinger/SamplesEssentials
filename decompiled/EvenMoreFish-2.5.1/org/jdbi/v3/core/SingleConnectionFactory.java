package org.jdbi.v3.core;

import java.sql.Connection;

class SingleConnectionFactory implements ConnectionFactory {
   private final Connection connection;

   SingleConnectionFactory(Connection connection) {
      this.connection = connection;
   }

   @Override
   public Connection openConnection() {
      return this.connection;
   }

   @Override
   public void closeConnection(Connection toClose) {
   }
}
