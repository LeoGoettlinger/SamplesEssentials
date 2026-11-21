package com.mysql.cj.telemetry;

public enum TelemetrySpanName {
   CANCEL_QUERY("Cancel query"),
   CHANGE_DATABASE("Change database"),
   COMMIT("Commit"),
   CONNECTION_CREATE("Create connection"),
   CONNECTION_RESET("Reset connection"),
   CREATE_DATABASE("Create database"),
   EXPLAIN_QUERY("Explain query"),
   GET_INNODB_STATUS("Get InnoDB status"),
   GET_PROCESS_HOST("Get process host"),
   GET_VARIABLE("Get variable '%s'"),
   LOAD_COLLATIONS("Load collations"),
   LOAD_VARIABLES("Load server variables"),
   PING("Ping"),
   ROLLBACK("Rollback"),
   ROUTINE_EXECUTE("Execute stored routine"),
   ROUTINE_EXECUTE_BATCH("Batch execute stored routine"),
   ROUTINE_PREPARE("Prepare stored routine"),
   SET_CHARSET("Set character set"),
   SET_OPTION_MULTI_STATEMENTS("Set multi-statements '%s'"),
   SET_TRANSACTION_ACCESS_MODE("Set transaction access mode '%s'"),
   SET_TRANSACTION_ISOLATION("Set transaction isolation"),
   SET_VARIABLE("Set variable '%s'"),
   SET_VARIABLES("Set variable(s)"),
   SHOW_WARNINGS("Show warnings"),
   SHUTDOWN("Shutdown"),
   STMT_DEALLOCATE_PREPARED("Deallocate prepared statement"),
   STMT_EXECUTE("Execute statement"),
   STMT_EXECUTE_BATCH("Batch execute statement"),
   STMT_EXECUTE_BATCH_PREPARED("Batch execute prepared statement"),
   STMT_EXECUTE_PREPARED("Execute prepared statement"),
   STMT_FETCH_PREPARED("Fetch rows for prepared statement"),
   STMT_PREPARE("Prepare statement"),
   STMT_RESET_PREPARED("Reset prepared statement"),
   STMT_SEND_LONG_DATA("Send long data for prepared statement"),
   USE_DATABASE("Use database");

   private String name = "";

   private TelemetrySpanName(String name) {
      this.name = name;
   }

   public String getName(Object... args) {
      return args.length > 0 ? String.format(this.name, args) : this.name;
   }
}
