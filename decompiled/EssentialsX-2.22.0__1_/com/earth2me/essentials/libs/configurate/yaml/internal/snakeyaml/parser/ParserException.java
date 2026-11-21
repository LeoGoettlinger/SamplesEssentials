package com.earth2me.essentials.libs.configurate.yaml.internal.snakeyaml.parser;

import com.earth2me.essentials.libs.configurate.yaml.internal.snakeyaml.error.Mark;
import com.earth2me.essentials.libs.configurate.yaml.internal.snakeyaml.error.MarkedYAMLException;

public class ParserException extends MarkedYAMLException {
   private static final long serialVersionUID = -2349253802798398038L;

   public ParserException(String context, Mark contextMark, String problem, Mark problemMark) {
      super(context, contextMark, problem, problemMark, null, null);
   }
}
