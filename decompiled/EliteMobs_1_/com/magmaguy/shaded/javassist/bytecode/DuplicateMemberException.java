package com.magmaguy.shaded.javassist.bytecode;

import com.magmaguy.shaded.javassist.CannotCompileException;

public class DuplicateMemberException extends CannotCompileException {
   private static final long serialVersionUID = 1L;

   public DuplicateMemberException(String msg) {
      super(msg);
   }
}
