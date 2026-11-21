package me.chancesd.pvpmanager.sdutils.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import me.chancesd.pvpmanager.sdutils.utils.Log;
import me.chancesd.pvpmanager.sdutils.utils.MCVersion;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

public class Database {
   private static final String MYSQL_URL_TEMPLATE = "jdbc:mysql://%s/%s";
   private static final String SQLITE_URL_TEMPLATE = "jdbc:sqlite:%s";
   private final JavaPlugin plugin;
   private final DatabaseConfigBuilder.DatabaseType databaseType;
   private final Map<String, Table> tableRegister = new HashMap<>();
   private boolean converted;
   private final HikariDataSource connectionPool;
   private static final String SQL_SELECT_ALL = "SELECT * FROM ";
   private static final String SQL_INSERT_INTO = "INSERT INTO ";
   private static final String SQL_WHERE = " WHERE ";
   private static final String SQL_VALUES = " VALUES(";

   protected Database(DatabaseFactory databaseFactory, DatabaseConfigBuilder builder) {
      this.plugin = databaseFactory.getPlugin();
      this.databaseType = builder.getType();
      HikariConfig config = new HikariConfig();
      if (this.databaseType == DatabaseConfigBuilder.DatabaseType.SQLITE) {
         if (MCVersion.isLowerThan(MCVersion.V1_9)) {
            try {
               Class.forName("org.sqlite.JDBC");
               config.setConnectionTestQuery("SELECT 1;");
            } catch (ClassNotFoundException var5) {
               var5.printStackTrace();
            }
         }

         config.setJdbcUrl(String.format("jdbc:sqlite:%s", builder.getFile()));
         if (MCVersion.isAtLeast(MCVersion.V1_9)) {
            config.addDataSourceProperty("journal_mode", "wal");
         }

         config.addDataSourceProperty("synchronous", "normal");
      } else {
         config.setJdbcUrl(String.format("jdbc:mysql://%s/%s", builder.getUrl(), builder.getDatabase()));
         config.setUsername(builder.getUser());
         config.setPassword(builder.getPassword());
      }

      config.setPoolName(this.plugin.getName());
      config.addDataSourceProperty("cachePrepStmts", "true");
      config.addDataSourceProperty("prepStmtCacheSize", "250");
      config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
      this.setLogLevel(Level.WARN);
      this.connectionPool = new HikariDataSource(config);
      this.setLogLevel(Level.INFO);
      if (!this.converted) {
         databaseFactory.doConversion(this);
         this.converted = true;
      }
   }

   public <T> void doQuery(String sql, Consumer<T> consumer, String field, Class<T> type) {
      CompletableFuture.runAsync(() -> {
         try (
            Connection connection = this.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql);
            ResultSet result = ps.executeQuery();
         ) {
            if (result.next()) {
               T value = result.getObject(field, type);
               consumer.accept(value);
            }
         } catch (SQLException var16) {
            this.log("Failed to do query", var16);
         }
      });
   }

   public void registerTable(Table table) {
      try (
         Connection connection = this.getConnection();
         PreparedStatement ps = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + table.getName() + table.getUsage());
      ) {
         ps.executeUpdate();
         this.tableRegister.put(table.getName(), table);
      } catch (SQLException var10) {
         this.log("Failed to register table", var10);
      }
   }

   public void deleteTable(String table) {
      try (
         Connection connection = this.getConnection();
         PreparedStatement ps = connection.prepareStatement("DROP TABLE " + table);
      ) {
         ps.executeUpdate();
         this.tableRegister.remove(table);
      } catch (SQLException var10) {
         this.log("Failed to delete table", var10);
      }
   }

   public void renameTable(String oldName, String newName) {
      try (
         Connection connection = this.getConnection();
         PreparedStatement ps = connection.prepareStatement("RENAME " + oldName + " TO " + newName);
      ) {
         ps.executeUpdate();
         Table table = this.tableRegister.get(oldName);
         if (table != null) {
            this.tableRegister.remove(oldName);
            this.tableRegister.put(newName, table);
         }
      } catch (SQLException var11) {
         this.log("Failed to rename table", var11);
      }
   }

   public boolean tableExists(String table) {
      try {
         boolean var5;
         try (Connection connection = this.getConnection()) {
            DatabaseMetaData metadata = connection.getMetaData();

            try (ResultSet result = metadata.getTables(null, null, table, null)) {
               var5 = result.next();
            }
         }

         return var5;
      } catch (SQLException var11) {
         this.log("Failed to check if table exists", var11);
         return false;
      }
   }

   public boolean columnExists(String table, String column) {
      try {
         boolean var6;
         try (Connection connection = this.getConnection()) {
            DatabaseMetaData metadata = connection.getMetaData();

            try (ResultSet result = metadata.getColumns(null, null, table, column)) {
               var6 = result.next();
            }
         }

         return var6;
      } catch (SQLException var12) {
         this.log("Failed to check database if column exists", var12);
         return false;
      }
   }

   public void insertDefault(Table table, Object... values) {
      try (Connection connection = this.getConnection()) {
         StringBuilder valueCount = this.getValueParameteres(Arrays.asList(values));

         try (PreparedStatement ps = connection.prepareStatement("INSERT INTO " + table.getName() + " VALUES(" + valueCount + ");")) {
            for (int i = 0; i < values.length; i++) {
               ps.setObject(i + 1, values[i]);
            }

            ps.executeUpdate();
         }
      } catch (SQLException var12) {
         this.log("Failed to insert data to database", var12);
      }
   }

   public boolean insertColumns(Table table, Collection<String> columns, Collection<Object> values) {
      try {
         boolean var18;
         try (Connection connection = this.getConnection()) {
            StringBuilder valueCount = this.getValueParameteres(values);
            StringBuilder columnList = new StringBuilder("(");
            int index = 0;

            for (String col : columns) {
               columnList.append(col);
               if (++index < columns.size()) {
                  columnList.append(",");
               }
            }

            columnList.append(")");

            try (PreparedStatement ps = connection.prepareStatement("INSERT INTO " + table.getName() + columnList + " VALUES(" + valueCount + ");")) {
               int i = 0;

               for (Object object : values) {
                  ps.setObject(++i, object);
               }

               ps.executeUpdate();
            }

            var18 = true;
         }

         return var18;
      } catch (SQLException var16) {
         this.log("Failed to insert data to database", var16);
         return false;
      }
   }

   public void insertColumnsBatch(Table table, Collection<String> columns, Collection<Collection<Object>> values) {
      try (Connection connection = this.getConnection()) {
         Collection<Object> collection = values.stream().findFirst().orElse(Collections.emptyList());
         StringBuilder valueCount = this.getValueParameteres(collection);
         StringBuilder columnList = new StringBuilder("(");
         int index = 0;

         for (String col : columns) {
            columnList.append(col);
            if (++index < columns.size()) {
               columnList.append(",");
            }
         }

         columnList.append(")");

         try (PreparedStatement ps = connection.prepareStatement("INSERT INTO " + table.getName() + columnList + " VALUES(" + valueCount + ");")) {
            int inserts = 0;

            for (Collection<Object> object : values) {
               int i = 0;

               for (Object value : object) {
                  ps.setObject(++i, value);
               }

               ps.addBatch();
               inserts++;
               if (inserts % 1000 == 0 || inserts == values.size()) {
                  ps.executeBatch();
               }
            }
         }
      } catch (SQLException var20) {
         this.log("Failed to insert batch data to database", var20);
      }
   }

   public void updateValuesBatch(Table table, String index, Collection<String> columns, Map<Object, Collection<Object>> indexToValues) {
      try (Connection connection = this.getConnection()) {
         StringBuilder updateString = new StringBuilder();
         int i = 0;

         for (String col : columns) {
            updateString.append(col + "=?");
            if (++i < columns.size()) {
               updateString.append(",");
            }
         }

         try (PreparedStatement ps = connection.prepareStatement("UPDATE " + table.getName() + " SET " + updateString + " WHERE " + index + "=?")) {
            int updates = 0;

            for (Entry<Object, Collection<Object>> entry : indexToValues.entrySet()) {
               int paramIndex = 1;

               for (Object value : entry.getValue()) {
                  ps.setObject(paramIndex++, value);
               }

               ps.setObject(paramIndex, entry.getKey());
               ps.addBatch();
               updates++;
               if (updates % 1000 == 0 || updates == indexToValues.size()) {
                  ps.executeBatch();
               }
            }
         }
      } catch (SQLException var19) {
         this.log("Failed to batch update database", var19);
      }
   }

   public Object getValue(Table table, String index, String column, Object value) {
      try (
         Connection connection = this.getConnection();
         PreparedStatement ps = connection.prepareStatement("SELECT * FROM " + table.getName() + " WHERE " + index + "=?");
      ) {
         ps.setObject(1, value);

         try (ResultSet result = ps.executeQuery()) {
            return result.next() ? result.getObject(column) : null;
         }
      } catch (SQLException var16) {
         this.log("Failed to get value from database", var16);
         return null;
      }
   }

   public Object getValue(Table table, String sql, String column, Object... args) {
      try (
         Connection connection = this.getConnection();
         PreparedStatement ps = connection.prepareStatement(sql);
      ) {
         for (int i = 0; i < args.length; i++) {
            Object object = args[i];
            ps.setObject(i + 1, object);
         }

         try (ResultSet result = ps.executeQuery()) {
            return result.next() ? result.getObject(column) : null;
         }
      } catch (SQLException var16) {
         this.log("Failed to get value from database", var16);
         return null;
      }
   }

   public Map<String, Object> getRow(Table table, String index, Object value) {
      try (
         Connection connection = this.getConnection();
         PreparedStatement ps = connection.prepareStatement("SELECT * FROM " + table.getName() + " WHERE " + index + "=?");
      ) {
         ps.setObject(1, value);

         try (ResultSet result = ps.executeQuery()) {
            if (result.next()) {
               ResultSetMetaData metaData = result.getMetaData();
               Map<String, Object> row = new HashMap<>();

               for (int i = 1; i <= metaData.getColumnCount(); i++) {
                  row.put(metaData.getColumnName(i), result.getObject(i));
               }

               return row;
            } else {
               return Collections.emptyMap();
            }
         }
      } catch (SQLException var16) {
         this.log("Failed to get data from database", var16);
         return Collections.emptyMap();
      }
   }

   public List<Map<String, Object>> getAllRows(Table table) {
      try {
         Object var16;
         try (
            Connection connection = this.getConnection();
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM " + table.getName() + ";");
            ResultSet result = ps.executeQuery();
         ) {
            List<Map<String, Object>> rows = new ArrayList<>();

            while (result.next()) {
               ResultSetMetaData metaData = result.getMetaData();
               Map<String, Object> row = new HashMap<>();

               for (int i = 1; i <= metaData.getColumnCount(); i++) {
                  row.put(metaData.getColumnName(i), result.getObject(i));
               }

               rows.add(row);
            }

            var16 = rows;
         }

         return (List<Map<String, Object>>)var16;
      } catch (SQLException var15) {
         this.log("Failed to get data from database", var15);
         return Collections.emptyList();
      }
   }

   public boolean contains(Table table, String index, Object value) {
      try {
         boolean var7;
         try (
            Connection connection = this.getConnection();
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM " + table.getName() + " WHERE " + index + "=?");
         ) {
            ps.setObject(1, value);

            try (ResultSet result = ps.executeQuery()) {
               var7 = result.next();
            }
         }

         return var7;
      } catch (SQLException var15) {
         this.log("Failed to check database", var15);
         return false;
      }
   }

   public void update(Table table, String index, String toUpdate, Object indexValue, Object updateValue, String extra) {
      try (Connection connection = this.getConnection()) {
         String update = extra.isEmpty() ? "?" : updateValue + extra;

         try (PreparedStatement ps = connection.prepareStatement("UPDATE " + table.getName() + " SET " + toUpdate + "=" + update + " WHERE " + index + "=?;")) {
            if (extra.isEmpty()) {
               ps.setObject(1, updateValue);
               ps.setObject(2, indexValue);
            } else {
               ps.setObject(1, indexValue);
            }

            ps.executeUpdate();
         }
      } catch (SQLException var16) {
         this.log("Failed to update database", var16);
      }
   }

   public int update(Table table, String sql, Object... args) {
      try {
         int var13;
         try (
            Connection connection = this.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql);
         ) {
            for (int i = 0; i < args.length; i++) {
               Object object = args[i];
               ps.setObject(i + 1, object);
            }

            var13 = ps.executeUpdate();
         }

         return var13;
      } catch (SQLException var12) {
         this.log("Failed to get value from database", var12);
         return 0;
      }
   }

   public void update(Table table, String index, String toUpdate, Object indexValue, Object updateValue) {
      this.update(table, index, toUpdate, indexValue, updateValue, "");
   }

   public boolean updateValues(Table table, String index, Object indexValue, Collection<String> columns, Collection<Object> values) {
      try {
         boolean var19;
         try (Connection connection = this.getConnection()) {
            StringBuilder updateString = new StringBuilder();
            int i = 0;

            for (String col : columns) {
               updateString.append(col + "=?");
               if (++i < columns.size()) {
                  updateString.append(",");
               }
            }

            try (PreparedStatement ps = connection.prepareStatement("UPDATE " + table.getName() + " SET " + updateString + " WHERE " + index + "=?;")) {
               i = 0;

               for (Object object : values) {
                  ps.setObject(++i, object);
               }

               ps.setObject(values.size() + 1, indexValue);
               ps.executeUpdate();
            }

            var19 = true;
         }

         return var19;
      } catch (SQLException var16) {
         this.log("Failed to update database", var16);
         return false;
      }
   }

   public void remove(Table table, String index, Object value) {
      try (
         Connection connection = this.getConnection();
         PreparedStatement ps = connection.prepareStatement("DELETE FROM " + table.getName() + " WHERE " + index + "=?;");
      ) {
         ps.setObject(1, value);
         ps.executeUpdate();
      } catch (SQLException var12) {
         this.log("Failed to remove from database", var12);
      }
   }

   public int executeCountQuery(Table table, String where, Object... args) {
      try (
         Connection connection = this.getConnection();
         PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM " + table.getName() + " " + where);
      ) {
         for (int i = 0; i < args.length; i++) {
            ps.setObject(i + 1, args[i]);
         }

         try (ResultSet result = ps.executeQuery()) {
            return result.next() ? result.getInt(1) : 0;
         }
      } catch (SQLException var15) {
         this.log("Failed to execute count query: " + where, var15);
         return 0;
      }
   }

   @Nullable
   public Table getTable(String tableName) {
      return this.tableRegister.get(tableName);
   }

   public Connection getConnection() throws SQLException {
      return this.connectionPool.getConnection();
   }

   public void close() {
      this.setLogLevel(Level.WARN);
      this.connectionPool.close();
      this.setLogLevel(Level.INFO);
   }

   public DatabaseConfigBuilder.DatabaseType getDatabaseType() {
      return this.databaseType;
   }

   private void setLogLevel(Level level) {
      LoggerContext ctx = (LoggerContext)LogManager.getContext(false);
      ctx.getConfiguration().getLoggerConfig("com.zaxxer.hikari.HikariDataSource").setLevel(level);
   }

   private StringBuilder getValueParameteres(Collection<Object> values) {
      StringBuilder valueParams = new StringBuilder();

      for (int i = 0; i < values.size(); i++) {
         valueParams.append("?");
         if (i < values.size() - 1) {
            valueParams.append(",");
         }
      }

      return valueParams;
   }

   private void log(String message, Throwable t) {
      Log.severe(message, t);
   }

   public JavaPlugin getPlugin() {
      return this.plugin;
   }
}
