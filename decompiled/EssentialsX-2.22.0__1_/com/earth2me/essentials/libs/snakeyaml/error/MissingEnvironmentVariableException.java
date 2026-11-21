package com.earth2me.essentials.libs.snakeyaml.error;

public class MissingEnvironmentVariableException extends YAMLException {
   public MissingEnvironmentVariableException(String message) {
      super(message);
   }
}
