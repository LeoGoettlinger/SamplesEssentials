package com.magmaguy.elitemobs.shaded.reflections.scanners;

import com.magmaguy.elitemobs.shaded.reflections.ReflectionsException;
import com.magmaguy.elitemobs.shaded.reflections.util.ClasspathHelper;
import com.magmaguy.elitemobs.shaded.reflections.util.JavassistHelper;
import com.magmaguy.shaded.javassist.CannotCompileException;
import com.magmaguy.shaded.javassist.ClassPool;
import com.magmaguy.shaded.javassist.CtBehavior;
import com.magmaguy.shaded.javassist.CtClass;
import com.magmaguy.shaded.javassist.LoaderClassPath;
import com.magmaguy.shaded.javassist.NotFoundException;
import com.magmaguy.shaded.javassist.bytecode.ClassFile;
import com.magmaguy.shaded.javassist.bytecode.MethodInfo;
import com.magmaguy.shaded.javassist.expr.ConstructorCall;
import com.magmaguy.shaded.javassist.expr.ExprEditor;
import com.magmaguy.shaded.javassist.expr.FieldAccess;
import com.magmaguy.shaded.javassist.expr.MethodCall;
import com.magmaguy.shaded.javassist.expr.NewExpr;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Predicate;
import javax.annotation.Nonnull;

public class MemberUsageScanner implements Scanner {
   private Predicate<String> resultFilter = s -> true;
   private final ClassLoader[] classLoaders;
   private volatile ClassPool classPool;

   public MemberUsageScanner() {
      this(ClasspathHelper.classLoaders());
   }

   public MemberUsageScanner(@Nonnull ClassLoader[] classLoaders) {
      this.classLoaders = classLoaders;
   }

   @Override
   public List<Entry<String, String>> scan(ClassFile classFile) {
      List<Entry<String, String>> entries = new ArrayList<>();
      CtClass ctClass = null;

      try {
         ctClass = this.getClassPool().get(classFile.getName());

         for (CtBehavior member : ctClass.getDeclaredConstructors()) {
            this.scanMember(member, entries);
         }

         for (CtBehavior member : ctClass.getDeclaredMethods()) {
            this.scanMember(member, entries);
         }
      } catch (Exception var11) {
         throw new ReflectionsException("Could not scan method usage for " + classFile.getName(), var11);
      } finally {
         if (ctClass != null) {
            ctClass.detach();
         }
      }

      return entries;
   }

   public Scanner filterResultsBy(Predicate<String> filter) {
      this.resultFilter = filter;
      return this;
   }

   private void scanMember(CtBehavior member, final List<Entry<String, String>> entries) throws CannotCompileException {
      final String key = member.getDeclaringClass().getName() + "." + member.getMethodInfo().getName() + "(" + parameterNames(member.getMethodInfo()) + ")";
      member.instrument(
         new ExprEditor() {
            @Override
            public void edit(NewExpr e) {
               try {
                  MemberUsageScanner.this.add(
                     entries,
                     e.getConstructor().getDeclaringClass().getName()
                        + ".<init>("
                        + MemberUsageScanner.parameterNames(e.getConstructor().getMethodInfo())
                        + ")",
                     key + " #" + e.getLineNumber()
                  );
               } catch (NotFoundException var3) {
                  throw new ReflectionsException("Could not find new instance usage in " + key, var3);
               }
            }

            @Override
            public void edit(MethodCall m) {
               try {
                  MemberUsageScanner.this.add(
                     entries,
                     m.getMethod().getDeclaringClass().getName()
                        + "."
                        + m.getMethodName()
                        + "("
                        + MemberUsageScanner.parameterNames(m.getMethod().getMethodInfo())
                        + ")",
                     key + " #" + m.getLineNumber()
                  );
               } catch (NotFoundException var3) {
                  throw new ReflectionsException("Could not find member " + m.getClassName() + " in " + key, var3);
               }
            }

            @Override
            public void edit(ConstructorCall c) {
               try {
                  MemberUsageScanner.this.add(
                     entries,
                     c.getConstructor().getDeclaringClass().getName()
                        + ".<init>("
                        + MemberUsageScanner.parameterNames(c.getConstructor().getMethodInfo())
                        + ")",
                     key + " #" + c.getLineNumber()
                  );
               } catch (NotFoundException var3) {
                  throw new ReflectionsException("Could not find member " + c.getClassName() + " in " + key, var3);
               }
            }

            @Override
            public void edit(FieldAccess f) {
               try {
                  MemberUsageScanner.this.add(entries, f.getField().getDeclaringClass().getName() + "." + f.getFieldName(), key + " #" + f.getLineNumber());
               } catch (NotFoundException var3) {
                  throw new ReflectionsException("Could not find member " + f.getFieldName() + " in " + key, var3);
               }
            }
         }
      );
   }

   private void add(List<Entry<String, String>> entries, String key, String value) {
      if (this.resultFilter.test(key)) {
         entries.add(this.entry(key, value));
      }
   }

   public static String parameterNames(MethodInfo info) {
      return String.join(", ", JavassistHelper.getParameters(info));
   }

   private ClassPool getClassPool() {
      if (this.classPool == null) {
         synchronized (this) {
            if (this.classPool == null) {
               this.classPool = new ClassPool();

               for (ClassLoader classLoader : this.classLoaders) {
                  this.classPool.appendClassPath(new LoaderClassPath(classLoader));
               }
            }
         }
      }

      return this.classPool;
   }
}
