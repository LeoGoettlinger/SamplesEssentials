package me.byteful.plugin.leveltools.libs.xseries.reflection.jvm;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import me.byteful.plugin.leveltools.libs.xseries.reflection.jvm.classes.ClassHandle;
import me.byteful.plugin.leveltools.libs.xseries.reflection.minecraft.MinecraftMapping;
import org.intellij.lang.annotations.Pattern;
import org.jetbrains.annotations.NotNull;

public abstract class NamedMemberHandle extends MemberHandle implements NameableReflectiveHandle {
   protected final Set<String> names = new HashSet<>(5);

   @NotNull
   @Override
   public Set<String> getPossibleNames() {
      return this.names;
   }

   protected NamedMemberHandle(ClassHandle clazz) {
      super(clazz);
   }

   public NamedMemberHandle map(MinecraftMapping mapping, @Pattern("\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*") String name) {
      this.names.add(name);
      return this;
   }

   public NamedMemberHandle named(@Pattern("\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*") String... names) {
      this.names.addAll(Arrays.asList(names));
      return this;
   }

   public abstract NamedMemberHandle copy();
}
