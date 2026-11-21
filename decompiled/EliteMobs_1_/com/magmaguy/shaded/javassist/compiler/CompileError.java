package com.magmaguy.shaded.javassist.compiler;

import com.magmaguy.shaded.javassist.CannotCompileException;
import com.magmaguy.shaded.javassist.NotFoundException;

public class CompileError extends Exception {
   private static final long serialVersionUID = 1L;
   private Lex lex;
   private String reason;

   public CompileError(String s, Lex l) {
      this.reason = s;
      this.lex = l;
   }

   public CompileError(String s) {
      this.reason = s;
      this.lex = null;
   }

   public CompileError(CannotCompileException e) {
      this(e.getReason());
   }

   public CompileError(NotFoundException e) {
      this("cannot find " + e.getMessage());
   }

   public Lex getLex() {
      return this.lex;
   }

   @Override
   public String getMessage() {
      return this.reason;
   }

   @Override
   public String toString() {
      return "compile error: " + this.reason;
   }
}
