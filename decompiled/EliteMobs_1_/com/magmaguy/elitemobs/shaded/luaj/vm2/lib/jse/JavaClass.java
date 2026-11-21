package com.magmaguy.elitemobs.shaded.luaj.vm2.lib.jse;

import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaValue;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

class JavaClass extends JavaInstance implements CoerceJavaToLua.Coercion {
   static final Map classes = Collections.synchronizedMap(new HashMap());
   static final LuaValue NEW = valueOf("new");
   Map fields;
   Map methods;
   Map innerclasses;

   static JavaClass forClass(Class var0) {
      JavaClass var1 = (JavaClass)classes.get(var0);
      if (var1 == null) {
         classes.put(var0, var1 = new JavaClass(var0));
      }

      return var1;
   }

   JavaClass(Class var1) {
      super(var1);
      this.jclass = this;
   }

   public LuaValue coerce(Object var1) {
      return this;
   }

   Field getField(LuaValue var1) {
      if (this.fields == null) {
         HashMap var2 = new HashMap();
         Field[] var3 = ((Class)this.m_instance).getFields();

         for (int var4 = 0; var4 < var3.length; var4++) {
            Field var5 = var3[var4];
            if (Modifier.isPublic(var5.getModifiers())) {
               var2.put(LuaValue.valueOf(var5.getName()), var5);

               try {
                  if (!var5.isAccessible()) {
                     var5.setAccessible(true);
                  }
               } catch (SecurityException var7) {
               }
            }
         }

         this.fields = var2;
      }

      return (Field)this.fields.get(var1);
   }

   LuaValue getMethod(LuaValue var1) {
      if (this.methods == null) {
         HashMap var2 = new HashMap();
         Method[] var3 = ((Class)this.m_instance).getMethods();

         for (int var4 = 0; var4 < var3.length; var4++) {
            Method var5 = var3[var4];
            if (Modifier.isPublic(var5.getModifiers())) {
               String var6 = var5.getName();
               Object var7 = (List)var2.get(var6);
               if (var7 == null) {
                  var2.put(var6, var7 = new ArrayList());
               }

               var7.add(JavaMethod.forMethod(var5));
            }
         }

         HashMap var11 = new HashMap();
         Constructor[] var12 = ((Class)this.m_instance).getConstructors();
         ArrayList var13 = new ArrayList();

         for (int var14 = 0; var14 < var12.length; var14++) {
            if (Modifier.isPublic(var12[var14].getModifiers())) {
               var13.add(JavaConstructor.forConstructor(var12[var14]));
            }
         }

         switch (var13.size()) {
            case 0:
               break;
            case 1:
               var11.put(NEW, var13.get(0));
               break;
            default:
               var11.put(NEW, JavaConstructor.forConstructors(var13.toArray(new JavaConstructor[var13.size()])));
         }

         for (Entry var8 : var2.entrySet()) {
            String var9 = (String)var8.getKey();
            List var10 = (List)var8.getValue();
            var11.put(LuaValue.valueOf(var9), var10.size() == 1 ? var10.get(0) : JavaMethod.forMethods(var10.toArray(new JavaMethod[var10.size()])));
         }

         this.methods = var11;
      }

      return (LuaValue)this.methods.get(var1);
   }

   Class getInnerClass(LuaValue var1) {
      if (this.innerclasses == null) {
         HashMap var2 = new HashMap();
         Class[] var3 = ((Class)this.m_instance).getClasses();

         for (int var4 = 0; var4 < var3.length; var4++) {
            Class var5 = var3[var4];
            String var6 = var5.getName();
            String var7 = var6.substring(Math.max(var6.lastIndexOf(36), var6.lastIndexOf(46)) + 1);
            var2.put(LuaValue.valueOf(var7), var5);
         }

         this.innerclasses = var2;
      }

      return (Class)this.innerclasses.get(var1);
   }

   public LuaValue getConstructor() {
      return this.getMethod(NEW);
   }
}
