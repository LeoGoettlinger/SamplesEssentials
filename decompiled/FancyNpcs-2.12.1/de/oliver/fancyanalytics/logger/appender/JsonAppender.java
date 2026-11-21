package de.oliver.fancyanalytics.logger.appender;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import de.oliver.fancyanalytics.logger.ExtendedFancyLogger;
import de.oliver.fancyanalytics.logger.LogEntry;
import de.oliver.fancyanalytics.logger.properties.ThrowableProperty;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class JsonAppender implements Appender {
   private static final ExtendedFancyLogger logger = new ExtendedFancyLogger("JsonAppender");
   private final boolean printToConsole;
   private final boolean printToFile;
   private final String filePath;
   private final Gson gson;

   public JsonAppender(boolean prettyPrint, boolean printToConsole, boolean printToFile, String filePath) {
      this.printToConsole = printToConsole;
      this.printToFile = printToFile;
      this.filePath = filePath;
      GsonBuilder gsonBuilder = new GsonBuilder();
      if (prettyPrint) {
         gsonBuilder.setPrettyPrinting();
      }

      gsonBuilder.registerTypeAdapterFactory(new JsonAppender.ThrowableTypeAdapterFactory());
      this.gson = gsonBuilder.create();
   }

   @Override
   public void append(LogEntry logEntry) {
      String json = this.gson.toJson(logEntry);
      if (this.printToConsole) {
         System.out.println(json);
      }

      if (this.printToFile) {
         try (BufferedWriter writer = new BufferedWriter(new FileWriter(this.filePath, true))) {
            writer.write(json);
            writer.newLine();
         } catch (IOException var8) {
            logger.warn("Failed to write log entry to file.", ThrowableProperty.of(var8));
         }
      }
   }

   @Override
   public void close() {
   }

   static class ThrowableTypeAdapter extends TypeAdapter<Throwable> {
      public void write(JsonWriter out, Throwable throwable) throws IOException {
         if (throwable == null) {
            out.nullValue();
         } else {
            out.beginObject();
            out.name("type").value(throwable.getClass().getName());
            out.name("message").value(throwable.getMessage());
            out.name("stackTrace");
            out.beginArray();

            for (StackTraceElement element : throwable.getStackTrace()) {
               out.beginObject();
               out.name("className").value(element.getClassName());
               out.name("methodName").value(element.getMethodName());
               out.name("fileName").value(element.getFileName());
               out.name("lineNumber").value((long)element.getLineNumber());
               out.endObject();
            }

            out.endArray();
            Throwable cause = throwable.getCause();
            if (cause != null && cause != throwable) {
               out.name("cause");
               this.write(out, cause);
            }

            out.endObject();
         }
      }

      public Throwable read(JsonReader in) throws IOException {
         in.beginObject();
         String message = null;
         String type = null;
         Throwable cause = null;

         while (in.hasNext()) {
            String name = in.nextName();
            switch (name) {
               case "type":
                  type = in.nextString();
                  break;
               case "message":
                  message = in.nextString();
                  break;
               case "cause":
                  cause = this.read(in);
                  break;
               default:
                  in.skipValue();
            }
         }

         in.endObject();
         return new Throwable(message, cause);
      }
   }

   static class ThrowableTypeAdapterFactory implements TypeAdapterFactory {
      @Override
      public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
         return !Throwable.class.isAssignableFrom(type.getRawType()) ? null : new JsonAppender.ThrowableTypeAdapter();
      }
   }
}
