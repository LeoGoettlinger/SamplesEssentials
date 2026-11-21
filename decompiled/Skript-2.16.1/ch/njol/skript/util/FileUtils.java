package ch.njol.skript.util;

import ch.njol.skript.SkriptConfig;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.invoke.StringConcatFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.zip.GZIPOutputStream;
import org.skriptlang.skript.lang.converter.Converter;

public abstract class FileUtils {
   private static final SimpleDateFormat backupFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

   private FileUtils() {
   }

   public static String getBackupSuffix() {
      synchronized (backupFormat) {
         return StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(backupFormat.format(System.currentTimeMillis()));
      }
   }

   public static void backupPurge(File varFile, int toKeep) throws IOException, IllegalArgumentException {
      if (toKeep < 0) {
         throw new IllegalArgumentException("Called with invalid input, 'toKeep' can not be less than 0");
      } else {
         File backupDir = new File(varFile.getParentFile(), "backups" + File.separator);
         if (backupDir.exists() && backupDir.isDirectory()) {
            ArrayList<File> files = new ArrayList<>(Arrays.asList(backupDir.listFiles()));
            if (files != null && files.size() > toKeep) {
               if (toKeep > 0) {
                  files.sort(Comparator.comparingLong(File::lastModified));
               }

               int numberToRemove = files.size() - toKeep;

               for (int i = 0; i < numberToRemove; i++) {
                  files.get(i).delete();
               }
            }
         } else {
            throw new IOException("Backup directory not found");
         }
      }
   }

   public static File backup(File file) throws IOException {
      Path source = file.toPath();
      Path backupFolder = source.getParent().resolve("backups");
      if (!Files.exists(backupFolder)) {
         Files.createDirectories(backupFolder);
      }

      String originalName = file.getName();
      int dotIdx = originalName.lastIndexOf(46);
      String name;
      String ext;
      if (dotIdx != -1) {
         name = originalName.substring(0, dotIdx);
         ext = originalName.substring(dotIdx);
      } else {
         name = originalName;
         ext = "";
      }

      String newFileName = name + "_" + getBackupSuffix() + ext;
      boolean compress = SkriptConfig.compressBackups.value();
      if (compress) {
         newFileName = newFileName + ".gz";
      }

      Path backup = backupFolder.resolve(newFileName);
      if (Files.exists(backup)) {
         throw new IOException("Backup file " + backup.getFileName() + " already exists");
      } else {
         if (compress) {
            class GZIPOutputStreamWithLevel extends GZIPOutputStream {
               public GZIPOutputStreamWithLevel(OutputStream out, int level) throws IOException {
                  super(out);
                  this.def.setLevel(level);
               }
            }

            try (
               OutputStream os = Files.newOutputStream(backup);
               GZIPOutputStreamWithLevel gzipOs = new GZIPOutputStreamWithLevel(os, 9);
            ) {
               Files.copy(source, gzipOs);
            }
         } else {
            copy(source, backup);
         }

         return backup.toFile();
      }
   }

   public static File move(File from, File to, boolean replace) throws IOException {
      if (!replace && to.exists()) {
         throw new IOException("Can't rename " + from.getName() + " to " + to.getName() + ": The target file already exists");
      } else {
         if (replace) {
            Files.move(from.toPath(), to.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
         } else {
            Files.move(from.toPath(), to.toPath(), StandardCopyOption.ATOMIC_MOVE);
         }

         return to;
      }
   }

   public static void copy(File from, File to) throws IOException {
      copy(from.toPath(), to.toPath());
   }

   public static void copy(Path from, Path to) throws IOException {
      Files.copy(from, to, StandardCopyOption.COPY_ATTRIBUTES);
   }

   public static Collection<File> renameAll(File directory, Converter<String, String> renamer) throws IOException {
      Collection<File> changed = new ArrayList<>();

      for (File f : directory.listFiles()) {
         if (f.isDirectory()) {
            changed.addAll(renameAll(f, renamer));
         } else {
            String name = f.getName();
            if (name != null) {
               String newName = renamer.convert(name);
               if (newName != null) {
                  File newFile = new File(f.getParent(), newName);
                  move(f, newFile, false);
                  changed.add(newFile);
               }
            }
         }
      }

      return changed;
   }

   public static void save(InputStream in, File file) throws IOException {
      file.getParentFile().mkdirs();

      try (FileOutputStream out = new FileOutputStream(file)) {
         byte[] buffer = new byte[16384];

         int read;
         while ((read = in.read(buffer)) > 0) {
            out.write(buffer, 0, read);
         }
      }
   }
}
