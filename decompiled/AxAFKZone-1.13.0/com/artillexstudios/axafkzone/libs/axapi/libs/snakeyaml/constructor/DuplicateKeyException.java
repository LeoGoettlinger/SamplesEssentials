package com.artillexstudios.axafkzone.libs.axapi.libs.snakeyaml.constructor;

import com.artillexstudios.axafkzone.libs.axapi.libs.snakeyaml.error.Mark;

public class DuplicateKeyException extends ConstructorException {
   protected DuplicateKeyException(Mark contextMark, Object key, Mark problemMark) {
      super("while constructing a mapping", contextMark, "found duplicate key " + key, problemMark);
   }
}
