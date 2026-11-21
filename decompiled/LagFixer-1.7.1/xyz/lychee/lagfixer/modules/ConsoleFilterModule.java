package xyz.lychee.lagfixer.modules;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.Filter.Result;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.managers.ModuleManager;
import xyz.lychee.lagfixer.objects.AbstractModule;

public class ConsoleFilterModule extends AbstractModule {
   private static final Pattern ANSI_PATTERN = Pattern.compile("\\u001B\\[[;\\d]*[ -/]*[@-~]");
   private final ConsoleFilterModule.CustomFilter filter = new ConsoleFilterModule.CustomFilter();
   private Path log_path;
   private BufferedWriter writer;
   private PrintWriter print_writer;
   private int logs_limit;
   private boolean filtering;
   private boolean save_filtered;
   private boolean error_filtering;
   private List<Pattern> patterns;

   public ConsoleFilterModule(LagFixer plugin, ModuleManager manager) {
      super(
         plugin,
         manager,
         AbstractModule.Impact.VISUAL_ONLY,
         "ConsoleFilter",
         new String[]{
            "Filters console messages based on predefined rules.",
            "Enhances clarity by selectively displaying essential messages.",
            "Reduces clutter and improves readability in multiplayer servers."
         },
         "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWNjNzg5ZjIzMDc5NGY5MGUzM2M0ZjlhZDAwNjk0YmMyYTJmZjVlOGI5YjM3NWRjMzUzMjQwMWIyODFmM2U1OCJ9fX0="
      );
   }

   private void clearLogs(Path directory) {
      if (this.logs_limit >= 1 && Files.exists(directory)) {
         try (Stream<Path> files = Files.list(directory)) {
            files.filter(p -> p.toString().endsWith(".log.gz")).sorted(Comparator.<Path>comparingLong(p -> {
               try {
                  return Files.getLastModifiedTime(p).toMillis();
               } catch (IOException var2x) {
                  return 0L;
               }
            }).reversed()).skip(this.logs_limit).forEach(path -> {
               try {
                  Files.deleteIfExists(path);
               } catch (IOException var2x) {
               }
            });
         } catch (IOException var7) {
            this.getPlugin().printError(var7);
         }
      }
   }

   public synchronized void write(String text) {
      try {
         this.writer.write(text);
         this.writer.newLine();
         this.writer.flush();
      } catch (IOException var3) {
         this.getPlugin().printError(var3);
      }
   }

   public boolean containsFilter(Logger logger) {
      Iterator<Filter> it = logger.getFilters();

      while (it.hasNext()) {
         Filter filter = it.next();
         if (filter instanceof ConsoleFilterModule.CustomFilter) {
            return true;
         }
      }

      return false;
   }

   @Override
   public void load() throws IOException {
      Path folder = this.getPlugin().getDataFolder().toPath().resolve("logs");
      Files.createDirectories(folder);
      this.log_path = folder.resolve("filtered_logs.txt");
      if (!Files.exists(this.log_path)) {
         Files.createFile(this.log_path);
      }

      this.writer = Files.newBufferedWriter(this.log_path, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
      this.print_writer = new PrintWriter(this.writer);
      this.clearLogs(Paths.get("logs"));
      Logger logger = (Logger)LogManager.getRootLogger();
      if (!this.containsFilter(logger)) {
         logger.addFilter(this.filter);
      }
   }

   @Override
   public boolean loadConfig() {
      this.logs_limit = this.getSection().getInt("logs_limit");
      this.filtering = this.getSection().getBoolean("filter.enabled");
      this.save_filtered = this.getSection().getBoolean("filter.save_filtered");
      this.error_filtering = this.getSection().getBoolean("filter.error_filtering");
      this.patterns = this.getSection().getStringList("filter.patterns").stream().map(Pattern::compile).toList();
      return true;
   }

   @Override
   public void disable() throws IOException {
      this.filtering = false;
      if (this.writer != null) {
         this.writer.close();
         this.print_writer.close();
      }

      if (this.save_filtered && Files.size(this.log_path) > 0L) {
         Path folder = this.getPlugin().getDataFolder().toPath().resolve("logs");
         String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

         long count;
         try (Stream<Path> files = Files.list(folder)) {
            count = files.filter(f -> f.getFileName().toString().startsWith(date)).count();
         }

         Path archivePath = folder.resolve(date + " [" + count + "].log.gz");

         try (
            InputStream fis = Files.newInputStream(this.log_path);
            OutputStream os = Files.newOutputStream(archivePath);
            GZIPOutputStream gos = new GZIPOutputStream(os);
         ) {
            fis.transferTo(gos);
         }

         this.clearLogs(folder);
      }
   }

   private class CustomFilter extends AbstractFilter {
      public Result filter(LogEvent event) {
         if (ConsoleFilterModule.this.filtering && !event.getLoggerName().equals("ErrorFilter") && !event.getLoggerName().equals("LagFixer")) {
            String rawMsg = event.getMessage().getFormattedMessage();
            String cleanMsg = ConsoleFilterModule.ANSI_PATTERN.matcher(rawMsg).replaceAll("");
            if (ConsoleFilterModule.this.error_filtering && event.getMessage().getThrowable() != null) {
               LogManager.getLogger("ErrorFilter").error(cleanMsg);
               ConsoleFilterModule.this.write(cleanMsg);
               event.getMessage().getThrowable().printStackTrace(ConsoleFilterModule.this.print_writer);
               ConsoleFilterModule.this.print_writer.flush();
               return Result.DENY;
            } else {
               for (Pattern pat : ConsoleFilterModule.this.patterns) {
                  if (pat.matcher(cleanMsg).find()) {
                     ConsoleFilterModule.this.write(cleanMsg);
                     return Result.DENY;
                  }
               }

               return Result.NEUTRAL;
            }
         } else {
            return Result.NEUTRAL;
         }
      }
   }
}
