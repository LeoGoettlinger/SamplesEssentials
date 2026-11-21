package org.sethomegui.SetHomeGUI.libs.mysql.cj.telemetry;

public enum TelemetryAttribute {
   DB_NAME("db.name"),
   DB_OPERATION("db.operation"),
   DB_STATEMENT("db.statement"),
   DB_CONNECTION_STRING("db.connection_string"),
   DB_SYSTEM("db.system"),
   DB_USER("db.user"),
   NETWORK_PEER_ADDRESS("network.peer.address"),
   NETWORK_PEER_PORT("network.peer.port"),
   NETWORK_TRANSPORT("network.transport"),
   SERVER_ADDRESS("server.address"),
   SERVER_PORT("server.port"),
   THREAD_ID("thread.id"),
   THREAD_NAME("thread.name");

   private String key = null;
   public static final String DB_SYSTEM_DEFAULT = "mysql";
   public static final String NETWORK_TRANSPORT_TCP = "tcp";
   public static final String NETWORK_TRANSPORT_UNIX = "unix";
   public static final String NETWORK_TRANSPORT_PIPE = "pipe";
   public static final String STATEMENT_SUFFIX = " (...)";
   public static final String OPERATION_BATCH = "(SQL batch)";
   public static final String OPERATION_COMMIT = "COMMIT";
   public static final String OPERATION_CREATE = "CREATE";
   public static final String OPERATION_EXPLAIN = "EXPLAIN";
   public static final String OPERATION_INIT_DB = "INIT_DB";
   public static final String OPERATION_KILL = "KILL";
   public static final String OPERATION_PING = "PING";
   public static final String OPERATION_ROLLBACK = "ROLLBACK";
   public static final String OPERATION_SELECT = "SELECT";
   public static final String OPERATION_SET = "SET";
   public static final String OPERATION_SHOW = "SHOW";
   public static final String OPERATION_SHUTDOWN = "SHUTDOWN";
   public static final String OPERATION_USE = "USE";

   private TelemetryAttribute(String key) {
      this.key = key;
   }

   public String getKey() {
      return this.key;
   }
}
