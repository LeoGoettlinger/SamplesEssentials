package com.earth2me.essentials.libs.configurate.yaml.internal.snakeyaml.emitter;

import com.earth2me.essentials.libs.configurate.yaml.internal.snakeyaml.error.YAMLException;

public class EmitterException extends YAMLException {
   private static final long serialVersionUID = -8280070025452995908L;

   public EmitterException(String msg) {
      super(msg);
   }
}
