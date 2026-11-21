package ch.njol.skript;

import ch.njol.skript.doc.Documentation;
import ch.njol.skript.test.runner.TestMode;
import ch.njol.util.StringUtils;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class SkriptCommandTabCompleter implements TabCompleter {
   @Nullable
   public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
      ArrayList<String> options = new ArrayList<>();
      if (!command.getName().equalsIgnoreCase("skript")) {
         return null;
      } else {
         if (args[0].equalsIgnoreCase("update") && args.length == 2) {
            options.add("check");
            options.add("changes");
         } else if (args[0].matches("(?i)(reload|disable|enable|test)") && args.length >= 2) {
            boolean useTestDirectory = args[0].equalsIgnoreCase("test") && TestMode.DEV_MODE;
            File scripts = useTestDirectory ? TestMode.TEST_DIR.toFile() : Skript.getInstance().getScriptsFolder();
            String scriptsPathString = scripts.toPath().toString();
            int scriptsPathLength = scriptsPathString.length();
            String scriptArg = StringUtils.join(args, " ", 1, args.length);
            String fs = File.separator;
            boolean enable = args[0].equalsIgnoreCase("enable");

            try (Stream<Path> files = Files.walk(scripts.toPath())) {
               files.map(Path::toFile).forEach(file -> {
                  if ((enable ? ScriptLoader.getDisabledScriptsFilter() : ScriptLoader.getLoadedScriptsFilter()).accept(file)) {
                     if (!file.isHidden()) {
                        String fileString = file.toString().substring(scriptsPathLength);
                        if (!fileString.isEmpty()) {
                           if (file.isDirectory()) {
                              fileString = fileString + fs;
                           } else if (file.getParentFile().toPath().toString().equals(scriptsPathString)) {
                              fileString = fileString.substring(1);
                              if (fileString.isEmpty()) {
                                 return;
                              }
                           }

                           if (scriptArg.length() <= 0 || file.getName().startsWith(scriptArg) || fileString.startsWith(scriptArg)) {
                              if (args.length > 2 && fileString.length() >= scriptArg.length()) {
                                 fileString = fileString.substring(scriptArg.lastIndexOf(" ") + 1);
                              }

                              if (!fileString.isEmpty()) {
                                 options.add(fileString);
                              }
                           }
                        }
                     }
                  }
               });
            } catch (Exception var18) {
               Skript.exception(var18, "An error occurred while trying to update the list of disabled scripts!");
            }

            if (args.length == 2) {
               options.add("all");
               if (args[0].equalsIgnoreCase("reload")) {
                  options.add("config");
                  options.add("aliases");
                  options.add("scripts");
               }
            }
         } else if (args.length == 1) {
            options.add("help");
            options.add("reload");
            options.add("enable");
            options.add("disable");
            options.add("update");
            options.add("list");
            options.add("show");
            options.add("info");
            if (Documentation.getDocsTemplateDirectory().exists()) {
               options.add("gen-docs");
            }

            if (TestMode.DEV_MODE) {
               options.add("test");
            }
         }

         return options;
      }
   }
}
