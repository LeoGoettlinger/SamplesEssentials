package com.magmaguy.elitemobs.shaded.reflections.vfs;

import com.magmaguy.elitemobs.shaded.reflections.Reflections;
import com.magmaguy.elitemobs.shaded.reflections.ReflectionsException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.function.Predicate;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlTypeVFS implements Vfs.UrlType {
   public static final String[] REPLACE_EXTENSION = new String[]{".ear/", ".jar/", ".war/", ".sar/", ".har/", ".par/"};
   final String VFSZIP = "vfszip";
   final String VFSFILE = "vfsfile";

   @Override
   public boolean matches(URL url) {
      return "vfszip".equals(url.getProtocol()) || "vfsfile".equals(url.getProtocol());
   }

   @Override
   public Vfs.Dir createDir(URL url) {
      try {
         URL adaptedUrl = this.adaptURL(url);
         return new ZipDir(new JarFile(adaptedUrl.getFile()));
      } catch (Exception var5) {
         try {
            return new ZipDir(new JarFile(url.getFile()));
         } catch (IOException var4) {
            if (Reflections.log != null) {
               Reflections.log.warn("Could not get URL", (Throwable)var5);
            }

            return null;
         }
      }
   }

   public URL adaptURL(URL url) throws MalformedURLException {
      if ("vfszip".equals(url.getProtocol())) {
         return this.replaceZipSeparators(url.getPath(), file -> file.exists() && file.isFile());
      } else {
         return "vfsfile".equals(url.getProtocol()) ? new URL(url.toString().replace("vfsfile", "file")) : url;
      }
   }

   URL replaceZipSeparators(String path, Predicate<File> acceptFile) throws MalformedURLException {
      int pos = 0;

      while (pos != -1) {
         pos = this.findFirstMatchOfDeployableExtention(path, pos);
         if (pos > 0) {
            File file = new File(path.substring(0, pos - 1));
            if (acceptFile.test(file)) {
               return this.replaceZipSeparatorStartingFrom(path, pos);
            }
         }
      }

      throw new ReflectionsException("Unable to identify the real zip file in path '" + path + "'.");
   }

   int findFirstMatchOfDeployableExtention(String path, int pos) {
      Pattern p = Pattern.compile("\\.[ejprw]ar/");
      Matcher m = p.matcher(path);
      return m.find(pos) ? m.end() : -1;
   }

   URL replaceZipSeparatorStartingFrom(String path, int pos) throws MalformedURLException {
      String zipFile = path.substring(0, pos - 1);
      String zipPath = path.substring(pos);
      int numSubs = 1;
      String[] prefix = REPLACE_EXTENSION;
      int i = prefix.length;

      for (int var8 = 0; var8 < i; var8++) {
         for (String ext = prefix[var8]; zipPath.contains(ext); numSubs++) {
            zipPath = zipPath.replace(ext, ext.substring(0, 4) + "!");
         }
      }

      String prefixx = "";

      for (int ix = 0; ix < numSubs; ix++) {
         prefixx = prefixx + "zip:";
      }

      return zipPath.trim().length() == 0 ? new URL(prefixx + "/" + zipFile) : new URL(prefixx + "/" + zipFile + "!" + zipPath);
   }
}
