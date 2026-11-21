package ch.njol.skript.variables;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Task;
import ch.njol.skript.util.Timespan;
import ch.njol.util.SynchronizedReference;
import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.Callable;
import lib.PatPeter.SQLibrary.Database;
import lib.PatPeter.SQLibrary.DatabaseException;
import lib.PatPeter.SQLibrary.SQLibrary;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

public abstract class SQLStorage extends VariablesStorage {
   public static final int MAX_VARIABLE_NAME_LENGTH = 380;
   public static final int MAX_CLASS_CODENAME_LENGTH = 50;
   public static final int MAX_VALUE_SIZE = 10000;
   private static final String SELECT_ORDER = "name, type, value, rowid";
   private static final String OLD_TABLE_NAME = "variables";
   @Nullable
   private String formattedCreateQuery;
   private final String createTableQuery;
   private String tableName;
   final SynchronizedReference<Database> db = new SynchronizedReference<>(null);
   private boolean monitor = false;
   long monitor_interval;
   private static final String guid = UUID.randomUUID().toString();
   private static final long TRANSACTION_DELAY = 500L;
   @Nullable
   private PreparedStatement writeQuery;
   @Nullable
   private PreparedStatement deleteQuery;
   @Nullable
   private PreparedStatement monitorQuery;
   @Nullable
   PreparedStatement monitorCleanUpQuery;
   long lastRowID = -1L;

   public SQLStorage(String type, String createTableQuery) {
      super(type);
      this.createTableQuery = createTableQuery;
      this.tableName = "variables21";
   }

   public String getTableName() {
      return this.tableName;
   }

   public void setTableName(String tableName) {
      this.tableName = tableName;
   }

   @Nullable
   public abstract Database initialize(SectionNode var1);

   @Nullable
   public String getFormattedCreateQuery() {
      if (this.formattedCreateQuery == null) {
         this.formattedCreateQuery = String.format(this.createTableQuery, this.tableName);
      }

      return this.formattedCreateQuery;
   }

   @Override
   protected boolean load_i(SectionNode n) {
      synchronized (this.db) {
         Plugin plugin = Bukkit.getPluginManager().getPlugin("SQLibrary");
         if (plugin != null && plugin instanceof SQLibrary) {
            Boolean monitor_changes = this.getValue(n, "monitor changes", Boolean.class);
            Timespan monitor_interval = this.getValue(n, "monitor interval", Timespan.class);
            if (monitor_changes != null && monitor_interval != null) {
               this.monitor = monitor_changes;
               this.monitor_interval = monitor_interval.getAs(Timespan.TimePeriod.MILLISECOND);

               Database db;
               try {
                  Database database = this.initialize(n);
                  if (database == null) {
                     return false;
                  }

                  db = database;
                  this.db.set(database);
               } catch (RuntimeException var20) {
                  if (var20 instanceof DatabaseException) {
                     Skript.error(var20.getLocalizedMessage());
                     return false;
                  }

                  throw var20;
               }

               SkriptLogger.setNode(null);
               if (!this.connect(true)) {
                  return false;
               } else {
                  try {
                     boolean hasOldTable = false;
                     boolean hadNewTable = db.isTable(this.getTableName());
                     if (this.getFormattedCreateQuery() == null) {
                        Skript.error(
                           "Could not create the variables table in the database. The query to create the variables table '"
                              + this.tableName
                              + "' in the database '"
                              + this.getUserConfigurationName()
                              + "' is null."
                        );
                        return false;
                     }

                     try {
                        db.query(this.getFormattedCreateQuery());
                     } catch (SQLException var18) {
                        Skript.error(
                           "Could not create the variables table '"
                              + this.tableName
                              + "' in the database '"
                              + this.getUserConfigurationName()
                              + "': "
                              + var18.getLocalizedMessage()
                              + ". Please create the table yourself using the following query: "
                              + String.format(this.createTableQuery, this.tableName).replace(",", ", ").replaceAll("\\s+", " ")
                        );
                        return false;
                     }

                     if (!this.prepareQueries()) {
                        return false;
                     }

                     ResultSet r2 = db.query("SELECT name, type, value, rowid FROM " + this.getTableName());

                     assert r2 != null;

                     try {
                        this.loadVariables(r2);
                     } finally {
                        r2.close();
                     }
                  } catch (SQLException var19) {
                     this.sqlException(var19);
                     return false;
                  }

                  Skript.newThread(new Runnable() {
                     @Override
                     public void run() {
                        while (!SQLStorage.this.closed) {
                           synchronized (SQLStorage.this.db) {
                              try {
                                 Database dbx = SQLStorage.this.db.get();
                                 if (dbx != null) {
                                    dbx.query("SELECT * FROM " + SQLStorage.this.getTableName() + " LIMIT 1");
                                 }
                              } catch (SQLException var5) {
                              }
                           }

                           try {
                              Thread.sleep(10000L);
                           } catch (InterruptedException var4) {
                           }
                        }
                     }
                  }, "Skript database '" + this.getUserConfigurationName() + "' connection keep-alive thread").start();
                  return true;
               }
            } else {
               return false;
            }
         } else {
            Skript.error(
               "You need the plugin SQLibrary in order to use a database with Skript. You can download the latest version from https://dev.bukkit.org/projects/sqlibrary/files/"
            );
            return false;
         }
      }
   }

   @Override
   protected void allLoaded() {
      Skript.debug("Database " + this.getUserConfigurationName() + " loaded. Queue size = " + this.changesQueue.size());
      Skript.newThread(new Runnable() {
         @Override
         public void run() {
            while (!SQLStorage.this.closed) {
               long lastCommit;
               synchronized (SQLStorage.this.db) {
                  Database db = SQLStorage.this.db.get();

                  try {
                     if (db != null) {
                        db.getConnection().commit();
                     }
                  } catch (SQLException var8) {
                     SQLStorage.this.sqlException(var8);
                  }

                  lastCommit = System.currentTimeMillis();
               }

               try {
                  Thread.sleep(Math.max(0L, lastCommit + 500L - System.currentTimeMillis()));
               } catch (InterruptedException var7) {
               }
            }
         }
      }, "Skript database '" + this.getUserConfigurationName() + "' transaction committing thread").start();
      if (this.monitor) {
         Skript.newThread(
               new Runnable() {
                  @Override
                  public void run() {
                     try {
                        Thread.sleep(SQLStorage.this.monitor_interval);
                     } catch (InterruptedException var10) {
                     }

                     long lastWarning = Long.MIN_VALUE;
                     int WARING_INTERVAL = 10;

                     while (!SQLStorage.this.closed) {
                        long next = System.currentTimeMillis() + SQLStorage.this.monitor_interval;
                        SQLStorage.this.checkDatabase();
                        long now = System.currentTimeMillis();
                        if (next < now && lastWarning + 10000L < now) {
                           Skript.warning(
                              "Cannot load variables from the database fast enough (loading took "
                                 + (now - next + SQLStorage.this.monitor_interval) / 1000.0
                                 + "s, monitor interval = "
                                 + SQLStorage.this.monitor_interval / 1000.0
                                 + "s). Please increase your monitor interval or reduce usage of variables. (this warning will be repeated at most once every 10 seconds)"
                           );
                           lastWarning = now;
                        }

                        while (System.currentTimeMillis() < next) {
                           try {
                              Thread.sleep(next - System.currentTimeMillis());
                           } catch (InterruptedException var9) {
                           }
                        }
                     }
                  }
               },
               "Skript database '" + this.getUserConfigurationName() + "' monitor thread"
            )
            .start();
      }
   }

   @Override
   protected File getFile(String file) {
      if (!file.endsWith(".db")) {
         file = file + ".db";
      }

      return new File(file);
   }

   @Override
   protected boolean connect() {
      return this.connect(false);
   }

   private final boolean connect(boolean first) {
      synchronized (this.db) {
         Database db = this.db.get();
         if (db != null && db.open()) {
            try {
               db.getConnection().setAutoCommit(false);
            } catch (SQLException var6) {
               this.sqlException(var6);
               return false;
            }

            return true;
         } else {
            if (first) {
               Skript.error("Cannot connect to the database '" + this.getUserConfigurationName() + "'! Please make sure that all settings are correct");
            } else {
               Skript.exception("Cannot reconnect to the database '" + this.getUserConfigurationName() + "'!");
            }

            return false;
         }
      }
   }

   private boolean prepareQueries() {
      synchronized (this.db) {
         Database db = this.db.get();

         assert db != null;

         try {
            try {
               if (this.writeQuery != null) {
                  this.writeQuery.close();
               }
            } catch (SQLException var8) {
            }

            this.writeQuery = db.prepare("REPLACE INTO " + this.getTableName() + " (name, type, value, update_guid) VALUES (?, ?, ?, ?)");

            try {
               if (this.deleteQuery != null) {
                  this.deleteQuery.close();
               }
            } catch (SQLException var7) {
            }

            this.deleteQuery = db.prepare("DELETE FROM " + this.getTableName() + " WHERE name = ?");

            try {
               if (this.monitorQuery != null) {
                  this.monitorQuery.close();
               }
            } catch (SQLException var6) {
            }

            this.monitorQuery = db.prepare("SELECT name, type, value, rowid FROM " + this.getTableName() + " WHERE rowid > ? AND update_guid != ?");

            try {
               if (this.monitorCleanUpQuery != null) {
                  this.monitorCleanUpQuery.close();
               }
            } catch (SQLException var5) {
            }

            this.monitorCleanUpQuery = db.prepare("DELETE FROM " + this.getTableName() + " WHERE value IS NULL AND rowid < ?");
         } catch (SQLException var9) {
            Skript.exception(var9, "Could not prepare queries for the database '" + this.getUserConfigurationName() + "': " + var9.getLocalizedMessage());
            return false;
         }

         return true;
      }
   }

   @Override
   protected void disconnect() {
      synchronized (this.db) {
         Database db = this.db.get();
         if (db != null) {
            db.close();
         }
      }
   }

   @Override
   protected boolean save(String name, @Nullable String type, @Nullable byte[] value) {
      synchronized (this.db) {
         if (name.length() > 380) {
            Skript.error(
               "The name of the variable {"
                  + name
                  + "} is too long to be saved in a database (length: "
                  + name.length()
                  + ", maximum allowed: 380)! It will be truncated and won't bet available under the same name again when loaded."
            );
         }

         if (value != null && value.length > 10000) {
            Skript.error(
               "The variable {"
                  + name
                  + "} cannot be saved in the database as its value's size ("
                  + value.length
                  + ") exceeds the maximum allowed size of 10000! An attempt to save the variable will be made nonetheless."
            );
         }

         try {
            if (type == null) {
               assert value == null;

               PreparedStatement deleteQuery = this.deleteQuery;

               assert deleteQuery != null;

               deleteQuery.setString(1, name);
               deleteQuery.executeUpdate();
            } else {
               int i = 1;
               PreparedStatement writeQuery = this.writeQuery;

               assert writeQuery != null;

               writeQuery.setString(i++, name);
               writeQuery.setString(i++, type);
               writeQuery.setBytes(i++, value);
               writeQuery.setString(i++, guid);
               writeQuery.executeUpdate();
            }
         } catch (SQLException var8) {
            this.sqlException(var8);
            return false;
         }

         return true;
      }
   }

   @Override
   public void close() {
      synchronized (this.db) {
         super.close();
         Database db = this.db.get();
         if (db != null) {
            try {
               db.getConnection().commit();
            } catch (SQLException var5) {
               this.sqlException(var5);
            }

            db.close();
            this.db.set(null);
         }
      }
   }

   protected void checkDatabase() {
      try {
         ResultSet r = null;

         final long lastRowID;
         try {
            synchronized (this.db) {
               if (this.closed || this.db.get() == null) {
                  return;
               }

               lastRowID = this.lastRowID;
               PreparedStatement monitorQuery = this.monitorQuery;

               assert monitorQuery != null;

               monitorQuery.setLong(1, lastRowID);
               monitorQuery.setString(2, guid);
               monitorQuery.execute();
               r = monitorQuery.getResultSet();

               assert r != null;
            }

            if (!this.closed) {
               this.loadVariables(r);
            }
         } finally {
            if (r != null) {
               r.close();
            }
         }

         if (!this.closed) {
            new Task(Skript.getInstance(), (long)Math.ceil(2.0 * this.monitor_interval / 50.0) + 100L, true) {
               @Override
               public void run() {
                  try {
                     synchronized (SQLStorage.this.db) {
                        if (SQLStorage.this.closed || SQLStorage.this.db.get() == null) {
                           return;
                        }

                        PreparedStatement monitorCleanUpQuery = SQLStorage.this.monitorCleanUpQuery;

                        assert monitorCleanUpQuery != null;

                        monitorCleanUpQuery.setLong(1, lastRowID);
                        monitorCleanUpQuery.executeUpdate();
                     }
                  } catch (SQLException var5) {
                     SQLStorage.this.sqlException(var5);
                  }
               }
            };
         }
      } catch (SQLException var13) {
         this.sqlException(var13);
      }
   }

   private void loadVariables(final ResultSet r) throws SQLException {
      SQLException e = Task.callSync(
         new Callable<SQLException>() {
            @Nullable
            public SQLException call() throws Exception {
               try {
                  while (r.next()) {
                     int i = 1;
                     String name = r.getString(i++);
                     if (name == null) {
                        Skript.error("Variable with NULL name found in the database '" + SQLStorage.this.getUserConfigurationName() + "', ignoring it");
                     } else {
                        String type = r.getString(i++);
                        byte[] value = r.getBytes(i++);
                        SQLStorage.this.lastRowID = r.getLong(i++);
                        if (value == null) {
                           Variables.variableLoaded(name, null, SQLStorage.this);
                        } else {
                           ClassInfo<?> c = Classes.getClassInfoNoError(type);
                           if (c != null && c.getSerializer() != null) {
                              Object d = Classes.deserialize(c, value);
                              if (d == null) {
                                 Skript.error(
                                    "Cannot load the variable {"
                                       + name
                                       + "} from the database '"
                                       + SQLStorage.this.getUserConfigurationName()
                                       + "', because it cannot be loaded as "
                                       + c.getName().withIndefiniteArticle()
                                 );
                              } else {
                                 Variables.variableLoaded(name, d, SQLStorage.this);
                              }
                           } else {
                              Skript.error(
                                 "Cannot load the variable {"
                                    + name
                                    + "} from the database '"
                                    + SQLStorage.this.getUserConfigurationName()
                                    + "', because the type '"
                                    + type
                                    + "' cannot be recognised or cannot be stored in variables"
                              );
                           }
                        }
                     }
                  }

                  return null;
               } catch (SQLException var8) {
                  return var8;
               }
            }
         }
      );
      if (e != null) {
         throw e;
      }
   }

   void sqlException(SQLException e) {
      Skript.error("database error: " + e.getLocalizedMessage());
      if (Skript.testing()) {
         e.printStackTrace();
      }

      this.prepareQueries();
   }
}
