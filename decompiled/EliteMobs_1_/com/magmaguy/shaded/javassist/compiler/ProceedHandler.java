package com.magmaguy.shaded.javassist.compiler;

import com.magmaguy.shaded.javassist.bytecode.Bytecode;
import com.magmaguy.shaded.javassist.compiler.ast.ASTList;

public interface ProceedHandler {
   void doit(JvstCodeGen var1, Bytecode var2, ASTList var3) throws CompileError;

   void setReturnType(JvstTypeChecker var1, ASTList var2) throws CompileError;
}
