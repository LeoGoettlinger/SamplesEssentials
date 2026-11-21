package github.nighter.smartspawner.libs.mariadb.util.constants;

public enum MetaExportedKeys {
   UseInformationSchema,
   UseShowCreate,
   Auto;

   public static MetaExportedKeys from(String value) {
      for (MetaExportedKeys val : values()) {
         if (val.name().equalsIgnoreCase(value)) {
            return val;
         }
      }

      throw new IllegalArgumentException(String.format("Wrong argument value '%s' for MetaExportedKeys", value));
   }
}
