package org.jdbi.v3.sqlobject;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.internal.OnDemandExtensions;
import org.jdbi.v3.core.spi.JdbiPlugin;
import org.jdbi.v3.sqlobject.statement.internal.SqlObjectStatementConfiguration;

public class SqlObjectPlugin extends JdbiPlugin.Singleton {
   @Override
   public void customizeJdbi(Jdbi db) {
      GeneratorSqlObjectFactory generatorSqlObjectFactory = new GeneratorSqlObjectFactory();
      db.registerExtension(generatorSqlObjectFactory);
      db.getConfig(OnDemandExtensions.class).setFactory(generatorSqlObjectFactory);
      db.registerExtension(new SqlObjectFactory());
      db.getConfig(SqlObjectStatementConfiguration.class);
   }
}
