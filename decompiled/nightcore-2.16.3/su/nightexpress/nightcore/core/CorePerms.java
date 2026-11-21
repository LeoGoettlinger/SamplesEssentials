package su.nightexpress.nightcore.core;

import su.nightexpress.nightcore.util.wrapper.UniPermission;

public class CorePerms {
   public static final String PREFIX = "nightcore.";
   public static final String PREFIX_COMMAND = "nightcore.command.";
   public static final UniPermission PLUGIN = new UniPermission("nightcore.*");
   public static final UniPermission COMMAND = new UniPermission("nightcore.command.*");
   public static final UniPermission COMMAND_RELOAD = new UniPermission("nightcore.command.reload");
   public static final UniPermission COMMAND_CHECK_PERM = new UniPermission("nightcore.command.checkperm");
   public static final UniPermission COMMAND_DUMP_ITEM = new UniPermission("nightcore.command.dumpitem");
   public static final UniPermission COMMAND_ECONOMY_BRIDGE = new UniPermission("nightcore.command.economybridge");
   @Deprecated
   public static final UniPermission COMMAND_FLAGS = new UniPermission("nightcore.command.flags");

   static {
      PLUGIN.addChildren(COMMAND);
      COMMAND.addChildren(COMMAND_RELOAD, COMMAND_CHECK_PERM, COMMAND_DUMP_ITEM, COMMAND_ECONOMY_BRIDGE);
   }
}
