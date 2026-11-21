package de.theredend2000.advancedhunt.lib.xseries.reflection.jvm;

import de.theredend2000.advancedhunt.lib.xseries.reflection.minecraft.MinecraftMapping;
import org.intellij.lang.annotations.Pattern;

public interface NameableReflectiveHandle extends NamedReflectiveHandle {
   NameableReflectiveHandle map(MinecraftMapping var1, @Pattern("\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*") String var2);

   NameableReflectiveHandle named(@Pattern("\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*") String... var1);
}
