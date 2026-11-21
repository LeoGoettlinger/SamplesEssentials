package com.magmaguy.elitemobs.shaded.reflections.scanners;

import com.magmaguy.elitemobs.shaded.reflections.util.JavassistHelper;
import com.magmaguy.shaded.javassist.bytecode.ClassFile;
import com.magmaguy.shaded.javassist.bytecode.CodeAttribute;
import com.magmaguy.shaded.javassist.bytecode.LocalVariableAttribute;
import com.magmaguy.shaded.javassist.bytecode.MethodInfo;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MethodParameterNamesScanner implements Scanner {
   @Override
   public List<Entry<String, String>> scan(ClassFile classFile) {
      List<Entry<String, String>> entries = new ArrayList<>();

      for (MethodInfo method : classFile.getMethods()) {
         String key = JavassistHelper.methodName(classFile, method);
         String value = this.getString(method);
         if (!value.isEmpty()) {
            entries.add(this.entry(key, value));
         }
      }

      return entries;
   }

   private String getString(MethodInfo method) {
      CodeAttribute codeAttribute = method.getCodeAttribute();
      LocalVariableAttribute table = codeAttribute != null ? (LocalVariableAttribute)codeAttribute.getAttribute("LocalVariableTable") : null;
      int length = JavassistHelper.getParameters(method).size();
      if (length > 0) {
         int shift = Modifier.isStatic(method.getAccessFlags()) ? 0 : 1;
         return IntStream.range(shift, length + shift)
            .mapToObj(i -> method.getConstPool().getUtf8Info(table.nameIndex(i)))
            .filter(name -> !name.startsWith("this$"))
            .collect(Collectors.joining(", "));
      } else {
         return "";
      }
   }
}
