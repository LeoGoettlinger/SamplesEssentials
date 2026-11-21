package com.oheers.fish.database.strategies;

import com.oheers.fish.database.connection.ConnectionFactory;
import com.oheers.fish.database.connection.H2ConnectionFactory;
import com.oheers.fish.database.connection.SqliteConnectionFactory;
import com.oheers.fish.database.strategies.impl.H2Strategy;
import com.oheers.fish.database.strategies.impl.MySqlStrategy;
import com.oheers.fish.database.strategies.impl.SqliteStrategy;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

public class DatabaseStrategyFactory {
   @Contract("null -> new")
   @NonNull
   public static DatabaseTypeStrategy getStrategy(ConnectionFactory connectionFactory) {
      if (connectionFactory instanceof SqliteConnectionFactory) {
         return new SqliteStrategy();
      } else {
         return (DatabaseTypeStrategy)(connectionFactory instanceof H2ConnectionFactory ? new H2Strategy() : new MySqlStrategy());
      }
   }
}
