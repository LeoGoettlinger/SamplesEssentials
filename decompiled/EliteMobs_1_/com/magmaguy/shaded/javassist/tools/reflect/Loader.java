package com.magmaguy.shaded.javassist.tools.reflect;

import com.magmaguy.shaded.javassist.CannotCompileException;
import com.magmaguy.shaded.javassist.ClassPool;
import com.magmaguy.shaded.javassist.NotFoundException;

public class Loader extends com.magmaguy.shaded.javassist.Loader {
   protected Reflection reflection;

   public static void main(String[] args) throws Throwable {
      Loader cl = new Loader();
      cl.run(args);
   }

   public Loader() throws CannotCompileException, NotFoundException {
      this.delegateLoadingOf("com.magmaguy.shaded.javassist.tools.reflect.Loader");
      this.reflection = new Reflection();
      ClassPool pool = ClassPool.getDefault();
      this.addTranslator(pool, this.reflection);
   }

   public boolean makeReflective(String clazz, String metaobject, String metaclass) throws CannotCompileException, NotFoundException {
      return this.reflection.makeReflective(clazz, metaobject, metaclass);
   }
}
