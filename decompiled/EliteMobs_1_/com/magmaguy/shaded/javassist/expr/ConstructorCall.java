package com.magmaguy.shaded.javassist.expr;

import com.magmaguy.shaded.javassist.CtClass;
import com.magmaguy.shaded.javassist.CtConstructor;
import com.magmaguy.shaded.javassist.CtMethod;
import com.magmaguy.shaded.javassist.NotFoundException;
import com.magmaguy.shaded.javassist.bytecode.CodeIterator;
import com.magmaguy.shaded.javassist.bytecode.MethodInfo;

public class ConstructorCall extends MethodCall {
   protected ConstructorCall(int pos, CodeIterator i, CtClass decl, MethodInfo m) {
      super(pos, i, decl, m);
   }

   @Override
   public String getMethodName() {
      return this.isSuper() ? "super" : "this";
   }

   @Override
   public CtMethod getMethod() throws NotFoundException {
      throw new NotFoundException("this is a constructor call.  Call getConstructor().");
   }

   public CtConstructor getConstructor() throws NotFoundException {
      return this.getCtClass().getConstructor(this.getSignature());
   }

   @Override
   public boolean isSuper() {
      return super.isSuper();
   }
}
