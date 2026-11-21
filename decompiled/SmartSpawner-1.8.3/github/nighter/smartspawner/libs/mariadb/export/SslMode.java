package github.nighter.smartspawner.libs.mariadb.export;

public enum SslMode {
   DISABLE("disable", new String[]{"DISABLED", "0", "false"}),
   TRUST("trust", new String[]{"REQUIRED"}),
   VERIFY_CA("verify-ca", new String[]{"VERIFY_CA"}),
   VERIFY_FULL("verify-full", new String[]{"VERIFY_IDENTITY", "1", "true"});

   private final String value;
   private final String[] aliases;

   private SslMode(String value, String[] aliases) {
      this.value = value;
      this.aliases = aliases;
   }

   public String getValue() {
      return this.value;
   }

   public static SslMode from(String value) {
      for (SslMode sslMode : values()) {
         if (sslMode.value.equalsIgnoreCase(value) || sslMode.name().equalsIgnoreCase(value)) {
            return sslMode;
         }

         for (String alias : sslMode.aliases) {
            if (alias.equalsIgnoreCase(value)) {
               return sslMode;
            }
         }
      }

      throw new IllegalArgumentException(String.format("Wrong argument value '%s' for SslMode", value));
   }
}
