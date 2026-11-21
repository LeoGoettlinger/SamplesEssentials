package com.magmaguy.elitemobs.shaded.luaj.vm2.lib.jse;

import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaValue;
import com.magmaguy.elitemobs.shaded.luaj.vm2.lib.BaseLib;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class JseBaseLib extends BaseLib {
   public LuaValue call(LuaValue var1, LuaValue var2) {
      super.call(var1, var2);
      var2.checkglobals().STDIN = System.in;
      return var2;
   }

   public InputStream findResource(String var1) {
      File var2 = new File(var1);
      if (!var2.exists()) {
         return super.findResource(var1);
      } else {
         try {
            return new FileInputStream(var2);
         } catch (IOException var4) {
            return null;
         }
      }
   }
}
