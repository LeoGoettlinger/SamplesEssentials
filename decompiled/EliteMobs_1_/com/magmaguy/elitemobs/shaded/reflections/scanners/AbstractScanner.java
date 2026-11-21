package com.magmaguy.elitemobs.shaded.reflections.scanners;

import com.magmaguy.shaded.javassist.bytecode.ClassFile;
import java.util.List;
import java.util.Map.Entry;

@Deprecated
class AbstractScanner implements Scanner {
   protected final Scanner scanner;

   AbstractScanner(Scanner scanner) {
      this.scanner = scanner;
   }

   @Override
   public String index() {
      return this.scanner.index();
   }

   @Override
   public List<Entry<String, String>> scan(ClassFile cls) {
      return this.scanner.scan(cls);
   }
}
