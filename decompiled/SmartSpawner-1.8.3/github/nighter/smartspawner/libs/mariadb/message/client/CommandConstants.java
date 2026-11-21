package github.nighter.smartspawner.libs.mariadb.message.client;

public final class CommandConstants {
   public static final byte COM_QUIT = 1;
   public static final byte COM_INIT_DB = 2;
   public static final byte COM_QUERY = 3;
   public static final byte COM_PING = 14;
   public static final byte COM_STMT_PREPARE = 22;
   public static final byte COM_STMT_EXECUTE = 23;
   public static final byte COM_STMT_SEND_LONG_DATA = 24;
   public static final byte COM_STMT_CLOSE = 25;
   public static final byte COM_RESET_CONNECTION = 31;
   public static final byte COM_STMT_BULK_EXECUTE = -6;
   public static final byte CURSOR_TYPE_NO_CURSOR = 0;
   public static final byte PARAMETER_TYPE_FLAG = 1;
   public static final byte PARAMETER_NULL = 1;
   public static final byte PARAMETER_NOT_NULL = 0;

   private CommandConstants() {
   }
}
