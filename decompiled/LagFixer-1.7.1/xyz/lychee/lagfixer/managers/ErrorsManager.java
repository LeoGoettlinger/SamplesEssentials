package xyz.lychee.lagfixer.managers;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Generated;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.Filter.Result;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.bukkit.Bukkit;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.commands.BenchmarkCommand;
import xyz.lychee.lagfixer.objects.AbstractManager;
import xyz.lychee.lagfixer.objects.ResourceMonitor;
import xyz.lychee.lagfixer.objects.WorldsMonitor;

public class ErrorsManager extends AbstractManager {
   private static ErrorsManager instance;
   private final Gson gson = new Gson();
   private final UUID uuid = UUID.randomUUID();
   private final HashMap<ErrorsManager.ThrowableKey, ErrorsManager.Error> errors = new HashMap<>();
   private final Pattern pattern = Pattern.compile("https://spark\\.lucko\\.me/.{10}");
   private final AbstractFilter filter = new ErrorsManager.CustomFilter();
   private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
   private final ConcurrentLinkedQueue<ErrorsManager.SendTask> sendQueue = new ConcurrentLinkedQueue<>();
   private boolean isSending = false;

   public ErrorsManager(LagFixer plugin) {
      super(plugin);
      instance = this;
   }

   @Override
   public void load() {
      Logger logger = (Logger)LogManager.getRootLogger();
      if (!this.containsFilter(logger)) {
         logger.addFilter(this.filter);
      }

      this.executor.scheduleAtFixedRate(this::processQueue, 1L, 1L, TimeUnit.MINUTES);
      this.getPlugin().getLogger().info(" &8• &rStarted listening console for LagFixer errors!");
   }

   public boolean containsFilter(Logger logger) {
      Iterator<Filter> it = logger.getFilters();

      while (it.hasNext()) {
         Filter filter = it.next();
         if (filter instanceof ErrorsManager.CustomFilter) {
            return true;
         }
      }

      return false;
   }

   @Override
   public void disable() {
      this.executor.shutdownNow();
   }

   private void processQueue() {
      if (!this.isSending && !this.sendQueue.isEmpty()) {
         ErrorsManager.SendTask task = this.sendQueue.poll();
         if (task != null) {
            this.isSending = true;

            try {
               task.send();
            } finally {
               this.isSending = false;
            }
         }
      }
   }

   private void addToQueue(ErrorsManager.SendTask task) {
      for (ErrorsManager.SendTask queuedTask : this.sendQueue) {
         if (queuedTask.equals(task)) {
            return;
         }
      }

      this.sendQueue.offer(task);
   }

   public boolean checkError(Throwable t) {
      if (t == null) {
         return true;
      } else {
         ErrorsManager.ThrowableKey key = new ErrorsManager.ThrowableKey(t);
         List<String> stackTrace = this.filterStackTrace(t);
         if (!stackTrace.isEmpty() && !stackTrace.stream().noneMatch(strx -> strx.contains("lagfixer"))) {
            StringBuilder message = new StringBuilder();
            message.append("\n");
            message.append("\n     &fAn error occurred in lagfixer:");
            message.append("\n        &4&n").append(t.getClass().getSimpleName()).append("&r&8: &4").append(t.getMessage());

            for (String str : stackTrace) {
               message.append("\n        &7^ &c").append(str);
            }

            message.append("\n");
            message.append("\n   &fOur support has been informed about it, it will be fixed soon.");
            message.append("\n   &fMake sure the LagFixer configuration is done correctly.");
            message.append("\n   &fIf you have any doubts, contact support: &nhttps://discord.gg/CFmzJjgZdu&r");
            message.append("\n");
            this.getPlugin().getLogger().warning(message.toString());
            if (this.errors.containsKey(key)) {
               return false;
            } else {
               this.errors.put(key, new ErrorsManager.Error(stackTrace, t));
               if (!this.errors.values().stream().allMatch(ErrorsManager.Error::isReported)) {
                  this.sendStackTraces();
               }

               return false;
            }
         } else {
            return true;
         }
      }
   }

   @Override
   public boolean isEnabled() {
      return this.getPlugin().getConfig().getBoolean("main.errors_reporter");
   }

   public void sendStackTraces() {
      this.addToQueue(new ErrorsManager.ErrorsSendTask());
   }

   public void sendProfiler(String url) {
      this.addToQueue(new ErrorsManager.ProfilerSendTask(url));
   }

   public void sendBenchmark(BenchmarkCommand.Benchmark benchmark) {
      this.addToQueue(new ErrorsManager.BenchmarkSendTask(benchmark));
   }

   private JsonObject createJson() {
      UpdaterManager updater = UpdaterManager.getInstance();
      SupportManager support = SupportManager.getInstance();
      ResourceMonitor resourceMonitor = support.getResourceMonitor();
      WorldsMonitor worldsMonitor = support.getWorldsMonitor();
      JsonObject jo = new JsonObject();
      jo.addProperty("bukkit", Bukkit.getName() + " " + Bukkit.getServer().getBukkitVersion());
      jo.addProperty("version", this.getPlugin().getDescription().getVersion());
      jo.addProperty("uuid", this.uuid.toString());
      jo.addProperty("entities", worldsMonitor.getEntities());
      jo.addProperty("creatures", worldsMonitor.getCreatures());
      jo.addProperty("items", worldsMonitor.getItems());
      jo.addProperty("projectiles", worldsMonitor.getProjectiles());
      jo.addProperty("vehicles", worldsMonitor.getVehicles());
      jo.addProperty("players", Bukkit.getOnlinePlayers().size());
      jo.addProperty("maxplayers", Bukkit.getMaxPlayers());
      jo.addProperty("cpuprocess", resourceMonitor.getCpuProcess());
      jo.addProperty("cpusystem", resourceMonitor.getCpuSystem());
      jo.addProperty("ramused", resourceMonitor.getRamUsed());
      jo.addProperty("ramtotal", resourceMonitor.getRamTotal());
      jo.addProperty("ramfree", resourceMonitor.getRamFree());
      jo.addProperty("tps", resourceMonitor.getTps());
      jo.addProperty("mspt", resourceMonitor.getMspt());
      jo.addProperty("current_version", updater.getCurrentVersion());
      jo.addProperty("latest_version", updater.getLatestVersion());
      jo.addProperty("difference_version", updater.getDifference());
      return jo;
   }

   private void connect(String params, JsonObject jsonObject) {
      HttpRequest request = HttpRequest.newBuilder()
         .uri(URI.create("https://api.sakuramc.pl" + params))
         .header("Content-Type", "application/json; charset=UTF-8")
         .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:90.0) Gecko/20100101 Firefox/90.0")
         .header("Accept", "application/json")
         .POST(BodyPublishers.ofString(jsonObject.toString()))
         .build();
      SupportManager.getInstance().getClient().sendAsync(request, BodyHandlers.ofByteArray());
   }

   private List<String> filterStackTrace(Throwable ex) {
      StackTraceElement[] stackTrace = ex.getStackTrace();
      List<String> list = new ArrayList<>();
      int maxLines = 10;
      int index = 0;

      for (StackTraceElement element : stackTrace) {
         if (index >= maxLines) {
            int tracesLeft = stackTrace.length - index;
            list.add("and " + tracesLeft + " traces more…");
            return list;
         }

         String trace = this.formatStackTraceElement(element);
         if (trace != null) {
            list.add(trace);
            index++;
         }
      }

      return list;
   }

   private String formatStackTraceElement(StackTraceElement element) {
      return element.getFileName() != null && element.getLineNumber() >= 0
         ? String.format("%s$%s(%d)", element.getClassName(), element.getMethodName(), element.getLineNumber())
         : null;
   }

   @Generated
   public Gson getGson() {
      return this.gson;
   }

   @Generated
   public UUID getUuid() {
      return this.uuid;
   }

   @Generated
   public HashMap<ErrorsManager.ThrowableKey, ErrorsManager.Error> getErrors() {
      return this.errors;
   }

   @Generated
   public Pattern getPattern() {
      return this.pattern;
   }

   @Generated
   public AbstractFilter getFilter() {
      return this.filter;
   }

   @Generated
   public ScheduledExecutorService getExecutor() {
      return this.executor;
   }

   @Generated
   public ConcurrentLinkedQueue<ErrorsManager.SendTask> getSendQueue() {
      return this.sendQueue;
   }

   @Generated
   public boolean isSending() {
      return this.isSending;
   }

   @Generated
   public static ErrorsManager getInstance() {
      return instance;
   }

   private class BenchmarkSendTask implements ErrorsManager.SendTask {
      private final BenchmarkCommand.Benchmark benchmark;

      public BenchmarkSendTask(BenchmarkCommand.Benchmark benchmark) {
         this.benchmark = benchmark;
      }

      @Override
      public void send() {
         JsonObject jsonObject = ErrorsManager.this.createJson();
         jsonObject.add("benchmark", ErrorsManager.this.gson.toJsonTree(this.benchmark));
         ErrorsManager.this.connect("/benchmarks?plugin=" + ErrorsManager.this.getPlugin().getName(), jsonObject);
      }

      @Override
      public boolean equals(Object obj) {
         return obj instanceof ErrorsManager.BenchmarkSendTask;
      }
   }

   private class CustomFilter extends AbstractFilter {
      public Result filter(LogEvent event) {
         if (event.getLoggerName().equals(ErrorsManager.this.getPlugin().getLogger().getName())) {
            return Result.NEUTRAL;
         } else if (event.getThrown() != null) {
            return ErrorsManager.this.checkError(event.getThrown()) ? Result.NEUTRAL : Result.DENY;
         } else {
            Matcher matcher = ErrorsManager.this.pattern.matcher(event.getMessage().getFormattedMessage());
            if (matcher.find()) {
               ErrorsManager.this.sendProfiler(matcher.group());
               ErrorsManager.this.getPlugin()
                  .getLogger()
                  .info("&7Spark profiler has been sent to our support to improve LagFixer optimizations and investigate what loads the server the most.");
            }

            return Result.NEUTRAL;
         }
      }
   }

   public class Error {
      private final String message;
      private final String stackTrace;
      private final String fullStackTrace;
      private transient boolean reported;

      public Error(List<String> stackTrace, Throwable ex) {
         this.message = ex.getClass().getSimpleName() + ": " + ex.getMessage();
         this.stackTrace = String.join("\n", stackTrace);
         this.fullStackTrace = ExceptionUtils.getStackTrace(ex);
         this.reported = false;
      }

      public void handle(JsonArray arr) {
         arr.add(ErrorsManager.this.gson.toJsonTree(this, ErrorsManager.Error.class));
         this.reported = true;
      }

      @Generated
      public String getMessage() {
         return this.message;
      }

      @Generated
      public String getStackTrace() {
         return this.stackTrace;
      }

      @Generated
      public String getFullStackTrace() {
         return this.fullStackTrace;
      }

      @Generated
      public boolean isReported() {
         return this.reported;
      }

      @Generated
      public void setReported(boolean reported) {
         this.reported = reported;
      }

      @Generated
      @Override
      public boolean equals(Object o) {
         if (o == this) {
            return true;
         } else if (!(o instanceof ErrorsManager.Error other)) {
            return false;
         } else if (!other.canEqual(this)) {
            return false;
         } else {
            Object this$message = this.getMessage();
            Object other$message = other.getMessage();
            if (this$message == null ? other$message == null : this$message.equals(other$message)) {
               Object this$stackTrace = this.getStackTrace();
               Object other$stackTrace = other.getStackTrace();
               if (this$stackTrace == null ? other$stackTrace == null : this$stackTrace.equals(other$stackTrace)) {
                  Object this$fullStackTrace = this.getFullStackTrace();
                  Object other$fullStackTrace = other.getFullStackTrace();
                  return this$fullStackTrace == null ? other$fullStackTrace == null : this$fullStackTrace.equals(other$fullStackTrace);
               } else {
                  return false;
               }
            } else {
               return false;
            }
         }
      }

      @Generated
      protected boolean canEqual(Object other) {
         return other instanceof ErrorsManager.Error;
      }

      @Generated
      @Override
      public int hashCode() {
         int PRIME = 59;
         int result = 1;
         Object $message = this.getMessage();
         result = result * 59 + ($message == null ? 43 : $message.hashCode());
         Object $stackTrace = this.getStackTrace();
         result = result * 59 + ($stackTrace == null ? 43 : $stackTrace.hashCode());
         Object $fullStackTrace = this.getFullStackTrace();
         return result * 59 + ($fullStackTrace == null ? 43 : $fullStackTrace.hashCode());
      }

      @Generated
      @Override
      public String toString() {
         return "ErrorsManager.Error(message="
            + this.getMessage()
            + ", stackTrace="
            + this.getStackTrace()
            + ", fullStackTrace="
            + this.getFullStackTrace()
            + ", reported="
            + this.isReported()
            + ")";
      }
   }

   private class ErrorsSendTask implements ErrorsManager.SendTask {
      @Override
      public void send() {
         JsonObject jsonObject = ErrorsManager.this.createJson();
         JsonArray errorsArray = new JsonArray();

         for (ErrorsManager.Error error : ErrorsManager.this.errors.values()) {
            if (!error.isReported()) {
               error.handle(errorsArray);
            }
         }

         jsonObject.add("errors", errorsArray);
         ErrorsManager.this.connect("/errors?plugin=" + ErrorsManager.this.getPlugin().getName(), jsonObject);
      }

      @Override
      public boolean equals(Object obj) {
         return obj instanceof ErrorsManager.ErrorsSendTask;
      }
   }

   private class ProfilerSendTask implements ErrorsManager.SendTask {
      private final String url;

      public ProfilerSendTask(String url) {
         this.url = url;
      }

      @Override
      public void send() {
         JsonObject jsonObject = ErrorsManager.this.createJson();
         jsonObject.addProperty("profiler", this.url);
         ErrorsManager.this.connect("/profilers?plugin=" + ErrorsManager.this.getPlugin().getName(), jsonObject);
      }

      @Override
      public boolean equals(Object obj) {
         return obj instanceof ErrorsManager.ProfilerSendTask;
      }
   }

   private interface SendTask {
      void send();

      @Override
      boolean equals(Object var1);
   }

   public static final class ThrowableKey {
      private final Class<? extends Throwable> type;
      private final String message;
      private final ErrorsManager.ThrowableKey causeKey;

      public ThrowableKey(Throwable t) {
         this.type = (Class<? extends Throwable>)t.getClass();
         this.message = t.getMessage();
         this.causeKey = t.getCause() == null ? null : new ErrorsManager.ThrowableKey(t.getCause());
      }

      @Override
      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else {
            return !(o instanceof ErrorsManager.ThrowableKey that)
               ? false
               : Objects.equals(this.type, that.type) && Objects.equals(this.message, that.message) && Objects.equals(this.causeKey, that.causeKey);
         }
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.type, this.message, this.causeKey);
      }

      @Override
      public String toString() {
         return this.type.getSimpleName() + ": " + this.message + (this.causeKey != null ? " <- " + this.causeKey : "");
      }
   }
}
