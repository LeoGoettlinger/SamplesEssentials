import com.magmaguy.elitemobs.shaded.luaj.vm2.Globals;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaString;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaTable;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaValue;
import com.magmaguy.elitemobs.shaded.luaj.vm2.Print;
import com.magmaguy.elitemobs.shaded.luaj.vm2.Varargs;
import com.magmaguy.elitemobs.shaded.luaj.vm2.lib.jse.JsePlatform;
import com.magmaguy.elitemobs.shaded.luaj.vm2.luajc.LuaJC;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;

public class lua {
   private static final String version = "Luaj-jse 3.0.1 Copyright (c) 2012 Luaj.org.org";
   private static final String usage = "usage: java -cp luaj-jse.jar lua [options] [script [args]].\nAvailable options are:\n  -e stat  execute string 'stat'\n  -l name  require library 'name'\n  -i       enter interactive mode after executing 'script'\n  -v       show version information\n  -b      \tuse luajc bytecode-to-bytecode compiler (requires bcel on class path)\n  -n      \tnodebug - do not load debug library by default\n  -p      \tprint the prototype\n  -c enc  \tuse the supplied encoding 'enc' for input files\n  --       stop handling options\n  -        execute stdin and stop handling options";
   private static Globals globals;
   private static boolean print = false;
   private static String encoding = null;

   private static void usageExit() {
      System.out
         .println(
            "usage: java -cp luaj-jse.jar lua [options] [script [args]].\nAvailable options are:\n  -e stat  execute string 'stat'\n  -l name  require library 'name'\n  -i       enter interactive mode after executing 'script'\n  -v       show version information\n  -b      \tuse luajc bytecode-to-bytecode compiler (requires bcel on class path)\n  -n      \tnodebug - do not load debug library by default\n  -p      \tprint the prototype\n  -c enc  \tuse the supplied encoding 'enc' for input files\n  --       stop handling options\n  -        execute stdin and stop handling options"
         );
      System.exit(-1);
   }

   public static void main(String[] var0) throws IOException {
      boolean var1 = var0.length == 0;
      boolean var2 = false;
      boolean var3 = true;
      boolean var4 = false;
      boolean var5 = false;
      Vector var6 = null;

      try {
         for (int var7 = 0; var7 < var0.length && var3 && var0[var7].startsWith("-") && var0[var7].length() > 1; var7++) {
            switch (var0[var7].charAt(1)) {
               case '-':
                  if (var0[var7].length() > 2) {
                     usageExit();
                  }

                  var3 = false;
                  break;
               case 'b':
                  var5 = true;
                  break;
               case 'c':
                  if (++var7 >= var0.length) {
                     usageExit();
                  }

                  encoding = var0[var7];
                  break;
               case 'e':
                  if (++var7 >= var0.length) {
                     usageExit();
                  }
                  break;
               case 'i':
                  var1 = true;
                  break;
               case 'l':
                  if (++var7 >= var0.length) {
                     usageExit();
                  }

                  var6 = var6 != null ? var6 : new Vector();
                  var6.addElement(var0[var7]);
                  break;
               case 'n':
                  var4 = true;
                  break;
               case 'p':
                  print = true;
                  break;
               case 'v':
                  var2 = true;
                  break;
               default:
                  usageExit();
            }
         }

         if (var2) {
            System.out.println("Luaj-jse 3.0.1 Copyright (c) 2012 Luaj.org.org");
         }

         globals = var4 ? JsePlatform.standardGlobals() : JsePlatform.debugGlobals();
         if (var5) {
            LuaJC.install(globals);
         }

         int var11 = 0;

         for (int var8 = var6 != null ? var6.size() : 0; var11 < var8; var11++) {
            loadLibrary((String)var6.elementAt(var11));
         }

         var3 = true;

         for (int var12 = 0; var12 < var0.length; var12++) {
            if (!var3 || !var0[var12].startsWith("-")) {
               processScript(new FileInputStream(var0[var12]), var0[var12], var0, var12);
               break;
            }

            if ("-".equals(var0[var12])) {
               processScript(System.in, "=stdin", var0, var12);
               break;
            }

            switch (var0[var12].charAt(1)) {
               case '-':
                  var3 = false;
                  break;
               case 'c':
               case 'l':
                  var12++;
                  break;
               case 'e':
                  processScript(new ByteArrayInputStream(var0[++var12].getBytes()), "string", var0, var12);
            }
         }

         if (var1) {
            interactiveMode();
         }
      } catch (IOException var9) {
         System.err.println(var9.toString());
         System.exit(-2);
      }
   }

   private static void loadLibrary(String var0) throws IOException {
      LuaString var1 = LuaValue.valueOf(var0);

      try {
         globals.get("require").call(var1);
      } catch (Exception var5) {
         try {
            LuaValue var3 = (LuaValue)Class.forName(var0).newInstance();
            var3.call(var1, globals);
         } catch (Exception var4) {
            throw new IOException("loadLibrary(" + var0 + ") failed: " + var5 + "," + var4);
         }
      }
   }

   private static void processScript(InputStream var0, String var1, String[] var2, int var3) throws IOException {
      try {
         LuaValue var4;
         try {
            var0 = new BufferedInputStream((InputStream)var0);
            var4 = encoding != null
               ? globals.load(new InputStreamReader((InputStream)var0, encoding), var1)
               : globals.load((InputStream)var0, var1, "bt", globals);
         } finally {
            var0.close();
         }

         if (print && var4.isclosure()) {
            Print.print(var4.checkclosure().p);
         }

         Varargs var5 = setGlobalArg(var1, var2, var3, globals);
         var4.invoke(var5);
      } catch (Exception var9) {
         var9.printStackTrace(System.err);
      }
   }

   private static Varargs setGlobalArg(String var0, String[] var1, int var2, LuaValue var3) {
      if (var1 == null) {
         return LuaValue.NONE;
      } else {
         LuaTable var4 = LuaValue.tableOf();

         for (int var5 = 0; var5 < var1.length; var5++) {
            var4.set(var5 - var2, LuaValue.valueOf(var1[var5]));
         }

         var4.set(0, LuaValue.valueOf(var0));
         var4.set(-1, LuaValue.valueOf("luaj"));
         var3.set("arg", var4);
         return var4.unpack();
      }
   }

   private static void interactiveMode() throws IOException {
      BufferedReader var0 = new BufferedReader(new InputStreamReader(System.in));

      while (true) {
         System.out.print("> ");
         System.out.flush();
         String var1 = var0.readLine();
         if (var1 == null) {
            return;
         }

         processScript(new ByteArrayInputStream(var1.getBytes()), "=stdin", null, 0);
      }
   }
}
