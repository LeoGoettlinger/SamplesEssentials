package com.magmaguy.shaded.javassist.tools.reflect;

import com.magmaguy.shaded.javassist.CannotCompileException;

public class CannotReflectException extends CannotCompileException {
   private static final long serialVersionUID = 1L;

   public CannotReflectException(String msg) {
      super(msg);
   }
}
