import com.magmaguy.elitemobs.shaded.luaj.vm2.Globals;
import com.magmaguy.elitemobs.shaded.luaj.vm2.Print;
import com.magmaguy.elitemobs.shaded.luaj.vm2.Prototype;
import com.magmaguy.elitemobs.shaded.luaj.vm2.compiler.DumpState;
import com.magmaguy.elitemobs.shaded.luaj.vm2.lib.jse.JsePlatform;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class luac {
   private static final String version = "Luaj-jse 3.0.1Copyright (C) 2009 luaj.org";
   private static final String usage = "usage: java -cp luaj-jse.jar luac [options] [filenames].\nAvailable options are:\n  -        process stdin\n  -l       list\n  -o name  output to file 'name' (default is \"luac.out\")\n  -p       parse only\n  -s       strip debug information\n  -e       little endian format for numbers\n  -i<n>    number format 'n', (n=0,1 or 4, default=0)\n  -v       show version information\n  -c enc  \tuse the supplied encoding 'enc' for input files\n  --       stop handling options\n";
   private boolean list = false;
   private String output = "luac.out";
   private boolean parseonly = false;
   private boolean stripdebug = false;
   private boolean littleendian = false;
   private int numberformat = 0;
   private boolean versioninfo = false;
   private boolean processing = true;
   private String encoding = null;

   private static void usageExit() {
      System.out
         .println(
            "usage: java -cp luaj-jse.jar luac [options] [filenames].\nAvailable options are:\n  -        process stdin\n  -l       list\n  -o name  output to file 'name' (default is \"luac.out\")\n  -p       parse only\n  -s       strip debug information\n  -e       little endian format for numbers\n  -i<n>    number format 'n', (n=0,1 or 4, default=0)\n  -v       show version information\n  -c enc  \tuse the supplied encoding 'enc' for input files\n  --       stop handling options\n"
         );
      System.exit(-1);
   }

   public static void main(String[] var0) throws IOException {
      new luac(var0);
   }

   private luac(String[] var1) throws IOException {
      try {
         for (int var2 = 0; var2 < var1.length; var2++) {
            if (this.processing && var1[var2].startsWith("-") && var1[var2].length() > 1) {
               switch (var1[var2].charAt(1)) {
                  case '-':
                     if (var1[var2].length() > 2) {
                        usageExit();
                     }

                     this.processing = false;
                     break;
                  case 'c':
                     if (++var2 >= var1.length) {
                        usageExit();
                     }

                     this.encoding = var1[var2];
                     break;
                  case 'e':
                     this.littleendian = true;
                     break;
                  case 'i':
                     if (var1[var2].length() <= 2) {
                        usageExit();
                     }

                     this.numberformat = Integer.parseInt(var1[var2].substring(2));
                     break;
                  case 'l':
                     this.list = true;
                     break;
                  case 'o':
                     if (++var2 >= var1.length) {
                        usageExit();
                     }

                     this.output = var1[var2];
                     break;
                  case 'p':
                     this.parseonly = true;
                     break;
                  case 's':
                     this.stripdebug = true;
                     break;
                  case 'v':
                     this.versioninfo = true;
                     break;
                  default:
                     usageExit();
               }
            }
         }

         if (this.versioninfo) {
            System.out.println("Luaj-jse 3.0.1Copyright (C) 2009 luaj.org");
         }

         FileOutputStream var11 = new FileOutputStream(this.output);

         try {
            Globals var3 = JsePlatform.standardGlobals();
            this.processing = true;

            for (int var4 = 0; var4 < var1.length; var4++) {
               if (!this.processing || !var1[var4].startsWith("-")) {
                  String var5 = var1[var4].substring(0, var1[var4].length() - 4);
                  this.processScript(var3, new FileInputStream(var1[var4]), var5, var11);
               } else if (var1[var4].length() <= 1) {
                  this.processScript(var3, System.in, "=stdin", var11);
               } else {
                  switch (var1[var4].charAt(1)) {
                     case '-':
                        this.processing = false;
                        break;
                     case 'c':
                     case 'o':
                        var4++;
                  }
               }
            }
         } finally {
            var11.close();
         }
      } catch (IOException var10) {
         System.err.println(var10.toString());
         System.exit(-2);
      }
   }

   private void processScript(Globals var1, InputStream var2, String var3, OutputStream var4) throws IOException {
      try {
         var2 = new BufferedInputStream((InputStream)var2);
         Prototype var5 = this.encoding != null
            ? var1.compilePrototype(new InputStreamReader((InputStream)var2, this.encoding), var3)
            : var1.compilePrototype((InputStream)var2, var3);
         if (this.list) {
            Print.printCode(var5);
         }

         if (!this.parseonly) {
            DumpState.dump(var5, var4, this.stripdebug, this.numberformat, this.littleendian);
         }
      } catch (Exception var9) {
         var9.printStackTrace(System.err);
      } finally {
         var2.close();
      }
   }
}
