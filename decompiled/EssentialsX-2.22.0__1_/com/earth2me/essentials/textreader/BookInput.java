package com.earth2me.essentials.textreader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.lang.ref.SoftReference;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.ess3.api.IEssentials;

public class BookInput implements IText {
   private static final HashMap<String, SoftReference<BookInput>> cache = new HashMap<>();
   private final transient List<String> lines;
   private final transient List<String> chapters;
   private final transient Map<String, Integer> bookmarks;
   private final transient long lastChange;

   public BookInput(final String filename, final boolean createFile, final IEssentials ess) throws IOException {
      File file = null;
      if (file == null || !file.exists()) {
         file = new File(ess.getDataFolder(), filename + ".txt");
      }

      if (!file.exists() && createFile) {
         InputStream input = ess.getResource(filename + ".txt");

         try {
            OutputStream output = new FileOutputStream(file);

            try {
               byte[] buffer = new byte[1024];

               for (int length = input.read(buffer); length > 0; length = input.read(buffer)) {
                  output.write(buffer, 0, length);
               }
            } catch (Throwable var21) {
               try {
                  output.close();
               } catch (Throwable var18) {
                  var21.addSuppressed(var18);
               }

               throw var21;
            }

            output.close();
         } catch (Throwable var22) {
            if (input != null) {
               try {
                  input.close();
               } catch (Throwable var17) {
                  var22.addSuppressed(var17);
               }
            }

            throw var22;
         }

         if (input != null) {
            input.close();
         }

         ess.getLogger().info("File " + filename + ".txt does not exist. Creating one for you.");
      }

      if (!file.exists()) {
         this.lastChange = 0L;
         this.lines = Collections.emptyList();
         this.chapters = Collections.emptyList();
         this.bookmarks = Collections.emptyMap();
         throw new FileNotFoundException("Could not create " + filename + ".txt");
      } else {
         this.lastChange = file.lastModified();
         boolean readFromfile;
         synchronized (cache) {
            SoftReference<BookInput> inputRef = cache.get(file.getName());
            BookInput input;
            if (inputRef != null && (input = inputRef.get()) != null && input.lastChange >= this.lastChange) {
               this.lines = Collections.unmodifiableList(input.getLines());
               this.chapters = Collections.unmodifiableList(input.getChapters());
               this.bookmarks = Collections.unmodifiableMap(input.getBookmarks());
               readFromfile = false;
            } else {
               this.lines = new ArrayList<>();
               this.chapters = new ArrayList<>();
               this.bookmarks = new HashMap<>();
               cache.put(file.getName(), new SoftReference<>(this));
               readFromfile = true;
            }
         }

         if (readFromfile) {
            Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
            BufferedReader bufferedReader = new BufferedReader(reader);

            try {
               for (int lineNumber = 0; bufferedReader.ready(); lineNumber++) {
                  String line = bufferedReader.readLine();
                  if (line == null) {
                     break;
                  }

                  if (line.length() > 0 && line.charAt(0) == '#') {
                     this.bookmarks.put(line.substring(1).toLowerCase(Locale.ENGLISH).replaceAll("&[0-9a-fk]", ""), lineNumber);
                     this.chapters.add(line.substring(1).replace('&', '§').replace("§§", "&"));
                  }

                  this.lines.add(line.replace('&', '§').replace("§§", "&"));
               }
            } finally {
               reader.close();
               bufferedReader.close();
            }
         }
      }
   }

   @Override
   public List<String> getLines() {
      return this.lines;
   }

   @Override
   public List<String> getChapters() {
      return this.chapters;
   }

   @Override
   public Map<String, Integer> getBookmarks() {
      return this.bookmarks;
   }
}
