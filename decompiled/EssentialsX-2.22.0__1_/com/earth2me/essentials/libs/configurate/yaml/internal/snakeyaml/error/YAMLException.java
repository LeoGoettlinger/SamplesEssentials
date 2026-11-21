package com.earth2me.essentials.libs.configurate.yaml.internal.snakeyaml.error;

public class YAMLException extends RuntimeException {
   private static final long serialVersionUID = -4738336175050337570L;

   public YAMLException(String message) {
      super(message);
   }

   public YAMLException(Throwable cause) {
      super(cause);
   }

   public YAMLException(String message, Throwable cause) {
      super(message, cause);
   }
}
