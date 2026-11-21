package me.byteful.plugin.leveltools.libs.xseries.reflection.jvm;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import me.byteful.plugin.leveltools.libs.xseries.reflection.ReflectiveHandle;
import me.byteful.plugin.leveltools.libs.xseries.reflection.XReflection;
import me.byteful.plugin.leveltools.libs.xseries.reflection.jvm.classes.ClassHandle;
import me.byteful.plugin.leveltools.libs.xseries.reflection.jvm.objects.ReflectedObject;
import me.byteful.plugin.leveltools.libs.xseries.reflection.jvm.objects.ReflectedObjectHandle;
import me.byteful.plugin.leveltools.libs.xseries.reflection.minecraft.MinecraftMapping;
import org.intellij.lang.annotations.Language;
import org.intellij.lang.annotations.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Obsolete;

public class EnumMemberHandle extends NamedMemberHandle {
   public EnumMemberHandle(ClassHandle clazz) {
      super(clazz);
   }

   public EnumMemberHandle map(MinecraftMapping mapping, @Pattern("\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*") String name) {
      super.map(mapping, name);
      return this;
   }

   public EnumMemberHandle named(@Pattern("\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*") String... names) {
      super.named(names);
      return this;
   }

   @Obsolete
   @Override
   public MemberHandle signature(@Language(value = "Java",suffix = ";") String declaration) {
      throw new UnsupportedOperationException();
   }

   @NotNull
   @Obsolete
   public MethodHandle unreflect() {
      return super.unreflect();
   }

   @Obsolete
   @Nullable
   public MethodHandle reflectOrNull() {
      return super.reflectOrNull();
   }

   @Obsolete
   @NotNull
   @Override
   public ReflectiveHandle<ReflectedObject> jvm() {
      return new ReflectedObjectHandle(() -> ReflectedObject.of(this.reflectJvm()));
   }

   @Obsolete
   @Override
   public MethodHandle reflect() throws ReflectiveOperationException {
      Field jvm = this.reflectJvm();
      return this.clazz.getNamespace().getLookup().unreflectGetter(jvm);
   }

   @Nullable
   public Object getEnumConstant() {
      try {
         return this.reflectJvm().get(null);
      } catch (ReflectiveOperationException var2) {
         throw XReflection.throwCheckedException(var2);
      }
   }

   public Field reflectJvm() throws ReflectiveOperationException {
      if (this.names.isEmpty()) {
         throw new IllegalStateException("No enum names specified");
      } else {
         NoSuchFieldException errors = null;
         Field field = null;
         Class<?> clazz = this.clazz.reflect();
         if (!clazz.isEnum()) {
            throw new IllegalStateException("Class is not an enum: " + this.clazz + " -> " + clazz);
         } else {
            for (String name : this.names) {
               if (field != null) {
                  break;
               }

               try {
                  field = clazz.getDeclaredField(name);
                  if (!field.isEnumConstant()) {
                     throw new NoSuchFieldException("Field named '" + name + "' was found but it's not an enum constant " + this);
                  }
               } catch (NoSuchFieldException var7) {
                  field = null;
                  if (errors == null) {
                     errors = new NoSuchFieldException("None of the enums were found for " + this);
                  }

                  errors.addSuppressed(var7);
               }
            }

            if (field == null) {
               throw (NoSuchFieldException)XReflection.relativizeSuppressedExceptions(errors);
            } else {
               return this.handleAccessible(field);
            }
         }
      }
   }

   @Obsolete
   public EnumMemberHandle copy() {
      throw new UnsupportedOperationException();
   }
}
