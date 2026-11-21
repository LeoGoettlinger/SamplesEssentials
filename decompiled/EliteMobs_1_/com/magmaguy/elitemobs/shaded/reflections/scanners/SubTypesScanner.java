package com.magmaguy.elitemobs.shaded.reflections.scanners;

import com.magmaguy.shaded.javassist.bytecode.ClassFile;
import java.util.List;
import java.util.Map.Entry;

@Deprecated
public class SubTypesScanner extends AbstractScanner {
   @Deprecated
   public SubTypesScanner() {
      super(Scanners.SubTypes);
   }

   @Deprecated
   public SubTypesScanner(boolean excludeObjectClass) {
      super(excludeObjectClass ? Scanners.SubTypes : Scanners.SubTypes.filterResultsBy(s -> true));
   }

   @Override
   public List<Entry<String, String>> scan(ClassFile cls) {
      return this.scanner.scan(cls);
   }
}
