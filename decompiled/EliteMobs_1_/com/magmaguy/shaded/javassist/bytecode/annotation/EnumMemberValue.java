package com.magmaguy.shaded.javassist.bytecode.annotation;

import com.magmaguy.shaded.javassist.ClassPool;
import com.magmaguy.shaded.javassist.bytecode.ConstPool;
import com.magmaguy.shaded.javassist.bytecode.Descriptor;
import java.io.IOException;
import java.lang.reflect.Method;

public class EnumMemberValue extends MemberValue {
   int typeIndex;
   int valueIndex;

   public EnumMemberValue(int type, int value, ConstPool cp) {
      super('e', cp);
      this.typeIndex = type;
      this.valueIndex = value;
   }

   public EnumMemberValue(ConstPool cp) {
      super('e', cp);
      this.typeIndex = this.valueIndex = 0;
   }

   @Override
   Object getValue(ClassLoader cl, ClassPool cp, Method m) throws ClassNotFoundException {
      try {
         return this.getType(cl).getField(this.getValue()).get(null);
      } catch (NoSuchFieldException var5) {
         throw new ClassNotFoundException(this.getType() + "." + this.getValue());
      } catch (IllegalAccessException var6) {
         throw new ClassNotFoundException(this.getType() + "." + this.getValue());
      }
   }

   @Override
   Class<?> getType(ClassLoader cl) throws ClassNotFoundException {
      return loadClass(cl, this.getType());
   }

   public String getType() {
      return Descriptor.toClassName(this.cp.getUtf8Info(this.typeIndex));
   }

   public void setType(String typename) {
      this.typeIndex = this.cp.addUtf8Info(Descriptor.of(typename));
   }

   public String getValue() {
      return this.cp.getUtf8Info(this.valueIndex);
   }

   public void setValue(String name) {
      this.valueIndex = this.cp.addUtf8Info(name);
   }

   @Override
   public String toString() {
      return this.getType() + "." + this.getValue();
   }

   @Override
   public void write(AnnotationsWriter writer) throws IOException {
      writer.enumConstValue(this.cp.getUtf8Info(this.typeIndex), this.getValue());
   }

   @Override
   public void accept(MemberValueVisitor visitor) {
      visitor.visitEnumMemberValue(this);
   }
}
