package com.earth2me.essentials.textreader;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.FormatUtil;
import com.earth2me.essentials.utils.StringUtil;
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

public class TextInput implements IText {
   private static final HashMap<String, SoftReference<TextInput>> cache = new HashMap<>();
   private final transient List<String> lines;
   private final transient List<String> chapters;
   private final transient Map<String, Integer> bookmarks;
   private final transient long lastChange;

   public TextInput(final CommandSource sender, final String filename, final boolean createFile, final IEssentials ess) throws IOException {
      File file = null;
      if (sender.isPlayer()) {
         User user = ess.getUser(sender.getPlayer());
         file = new File(ess.getDataFolder(), filename + "_" + StringUtil.sanitizeFileName(user.getName()) + ".txt");
         if (!file.exists()) {
            file = new File(ess.getDataFolder(), filename + "_" + StringUtil.sanitizeFileName(user.getGroup()) + ".txt");
         }
      }

      if (file == null || !file.exists()) {
         file = new File(ess.getDataFolder(), filename + ".txt");
      }

      if (file.exists()) {
         this.lastChange = file.lastModified();
         boolean readFromfile;
         synchronized (cache) {
            SoftReference<TextInput> inputRef = cache.get(file.getName());
            TextInput input;
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

                  if (line.length() > 1 && line.charAt(0) == '#') {
                     String[] titles = line.substring(1).trim().replace(" ", "_").split(",");
                     this.chapters.add(FormatUtil.replaceFormat(titles[0]));

                     for (String title : titles) {
                        this.bookmarks.put(FormatUtil.stripEssentialsFormat(title.toLowerCase(Locale.ENGLISH)), lineNumber);
                     }
                  }

                  this.lines.add(FormatUtil.replaceFormat(line));
               }
            } finally {
               reader.close();
               bufferedReader.close();
            }
         }
      } else {
         this.lastChange = 0L;
         this.lines = Collections.emptyList();
         this.chapters = Collections.emptyList();
         this.bookmarks = Collections.emptyMap();
         if (createFile) {
            InputStream input = ess.getResource(filename + ".txt");

            try {
               OutputStream output = new FileOutputStream(file);

               try {
                  byte[] buffer = new byte[1024];

                  for (int length = input.read(buffer); length > 0; length = input.read(buffer)) {
                     output.write(buffer, 0, length);
                  }
               } catch (Throwable var25) {
                  try {
                     output.close();
                  } catch (Throwable var24) {
                     var25.addSuppressed(var24);
                  }

                  throw var25;
               }

               output.close();
            } catch (Throwable var26) {
               if (input != null) {
                  try {
                     input.close();
                  } catch (Throwable var23) {
                     var26.addSuppressed(var23);
                  }
               }

               throw var26;
            }

            if (input != null) {
               input.close();
            }

            throw new FileNotFoundException("File " + filename + ".txt does not exist. Creating one for you.");
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
