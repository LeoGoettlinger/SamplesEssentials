package com.magmaguy.elitemobs.magmacore.nightbreak;

public record NightbreakLogoutMessages(String commandDescription, String permissionDenied, String success, String reconnect, String failure) {
   public NightbreakLogoutMessages(String commandDescription, String permissionDenied, String success, String reconnect, String failure) {
      commandDescription = valueOrDefault(commandDescription, "Remove the registered Nightbreak account token");
      permissionDenied = valueOrDefault(permissionDenied, "&cYou don't have permission to use this command.");
      success = valueOrDefault(success, "&aThis server is no longer connected to a Nightbreak account.");
      reconnect = valueOrDefault(reconnect, "&7Use &a/nightbreaklogin <token> &7to connect it again.");
      failure = valueOrDefault(failure, "&cThe shared Nightbreak account token could not be removed.");
      this.commandDescription = commandDescription;
      this.permissionDenied = permissionDenied;
      this.success = success;
      this.reconnect = reconnect;
      this.failure = failure;
   }

   public static NightbreakLogoutMessages defaults() {
      return new NightbreakLogoutMessages(null, null, null, null, null);
   }

   private static String valueOrDefault(String value, String fallback) {
      return value == null ? fallback : value;
   }
}
