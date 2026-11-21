package com.fastasyncworldedit.core.command;

import com.fastasyncworldedit.core.Fawe;
import com.fastasyncworldedit.core.IFawe;
import com.fastasyncworldedit.core.util.StringMan;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import java.io.File;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.enginehub.piston.annotation.Command;
import org.enginehub.piston.exception.StopExecutionException;

public class ListFilters {
   @Command(
      name = "all",
      desc = "List both public and private schematics"
   )
   public ListFilters.Filter all() {
      return new ListFilters.Filter() {
         @Override
         public boolean listPublic() {
            return true;
         }
      };
   }

   @Command(
      name = "local",
      aliases = {"me", "mine", "private"},
      desc = "List your personal schematics"
   )
   public ListFilters.Filter local() {
      return new ListFilters.Filter();
   }

   @Command(
      name = "global",
      aliases = {"public"},
      desc = "List public schematics"
   )
   public ListFilters.Filter global() {
      return new ListFilters.Filter() {
         @Override
         public boolean listPrivate() {
            return false;
         }

         @Override
         public boolean listPublic() {
            return true;
         }
      };
   }

   @Command(
      name = "*",
      desc = "wildcard"
   )
   public ListFilters.Filter wildcard(Actor actor, File root, String arg) {
      arg = arg.replace("/", File.separator);
      final String argLower = arg.toLowerCase(Locale.ROOT);
      if (arg.endsWith(File.separator)) {
         final String finalArg = arg;
         return new ListFilters.Filter() {
            @Override
            public File getPath(File root) {
               File newRoot = new File(root, finalArg);
               if (newRoot.exists()) {
                  return newRoot;
               } else {
                  String firstArg = finalArg.substring(0, finalArg.length() - File.separator.length());
                  if (firstArg.length() > 3 && firstArg.length() <= 16) {
                     UUID fromName = Fawe.<IFawe>platform().getUUID(finalArg);
                     if (fromName != null) {
                        newRoot = new File(root, finalArg);
                        if (newRoot.exists()) {
                           return newRoot;
                        }
                     }
                  }

                  throw new StopExecutionException(TextComponent.of("Cannot find path: " + finalArg));
               }
            }
         };
      } else if (StringMan.containsAny(arg, "\\^$.|?+(){}<>~$!%^&*+-/")) {
         Pattern pattern;
         try {
            pattern = Pattern.compile(argLower);
         } catch (PatternSyntaxException var7) {
            pattern = Pattern.compile(Pattern.quote(argLower));
         }

         final Pattern finalPattern = pattern;
         return new ListFilters.Filter() {
            @Override
            public boolean applies(File file) {
               String path = file.getPath().toLowerCase(Locale.ROOT);
               return finalPattern.matcher(path).find();
            }
         };
      } else {
         return new ListFilters.Filter() {
            @Override
            public boolean applies(File file) {
               return StringMan.containsIgnoreCase(file.getPath(), argLower);
            }
         };
      }
   }

   public class Filter {
      public boolean listPrivate() {
         return true;
      }

      public boolean listPublic() {
         return false;
      }

      public File getPath(File root) {
         return null;
      }

      public boolean applies(File file) {
         return true;
      }
   }
}
