package com.earth2me.essentials.libs.configurate.yaml.internal.snakeyaml.constructor;

import com.earth2me.essentials.libs.configurate.yaml.internal.snakeyaml.error.Mark;

public class DuplicateKeyException extends ConstructorException {
   protected DuplicateKeyException(Mark contextMark, Object key, Mark problemMark) {
      super("while constructing a mapping", contextMark, "found duplicate key " + key, problemMark);
   }
}
