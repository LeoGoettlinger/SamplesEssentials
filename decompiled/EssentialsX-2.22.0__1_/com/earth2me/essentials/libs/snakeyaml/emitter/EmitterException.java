package com.earth2me.essentials.libs.snakeyaml.emitter;

import com.earth2me.essentials.libs.snakeyaml.error.YAMLException;

public class EmitterException extends YAMLException {
   private static final long serialVersionUID = -8280070025452995908L;

   public EmitterException(String msg) {
      super(msg);
   }
}
