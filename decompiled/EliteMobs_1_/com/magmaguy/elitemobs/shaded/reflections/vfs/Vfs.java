package com.magmaguy.elitemobs.shaded.reflections.vfs;

import com.magmaguy.elitemobs.shaded.reflections.Reflections;
import com.magmaguy.elitemobs.shaded.reflections.ReflectionsException;
import com.magmaguy.elitemobs.shaded.reflections.util.ClasspathHelper;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public abstract class Vfs {
   private static List<Vfs.UrlType> defaultUrlTypes = new ArrayList<>(Arrays.asList(Vfs.DefaultUrlTypes.values()));

   public static List<Vfs.UrlType> getDefaultUrlTypes() {
      return defaultUrlTypes;
   }

   public static void setDefaultURLTypes(List<Vfs.UrlType> urlTypes) {
      defaultUrlTypes = urlTypes;
   }

   public static void addDefaultURLTypes(Vfs.UrlType urlType) {
      defaultUrlTypes.add(0, urlType);
   }

   public static Vfs.Dir fromURL(URL url) {
      return fromURL(url, defaultUrlTypes);
   }

   public static Vfs.Dir fromURL(URL url, List<Vfs.UrlType> urlTypes) {
      for (Vfs.UrlType type : urlTypes) {
         try {
            if (type.matches(url)) {
               Vfs.Dir dir = type.createDir(url);
               if (dir != null) {
                  return dir;
               }
            }
         } catch (Throwable var5) {
            if (Reflections.log != null) {
               Reflections.log.warn("could not create Dir using " + type + " from url " + url.toExternalForm() + ". skipping.", var5);
            }
         }
      }

      throw new ReflectionsException(
         "could not create Vfs.Dir from url, no matching UrlType was found ["
            + url.toExternalForm()
            + "]\neither use fromURL(final URL url, final List<UrlType> urlTypes) or use the static setDefaultURLTypes(final List<UrlType> urlTypes) or addDefaultURLTypes(UrlType urlType) with your specialized UrlType."
      );
   }

   public static Vfs.Dir fromURL(URL url, Vfs.UrlType... urlTypes) {
      return fromURL(url, Arrays.asList(urlTypes));
   }

   public static Iterable<Vfs.File> findFiles(Collection<URL> inUrls, String packagePrefix, Predicate<String> nameFilter) {
      Predicate<Vfs.File> fileNamePredicate = file -> {
         String path = file.getRelativePath();
         if (!path.startsWith(packagePrefix)) {
            return false;
         } else {
            String filename = path.substring(path.indexOf(packagePrefix) + packagePrefix.length());
            return !filename.isEmpty() && nameFilter.test(filename.substring(1));
         }
      };
      return findFiles(inUrls, fileNamePredicate);
   }

   public static Iterable<Vfs.File> findFiles(Collection<URL> urls, Predicate<Vfs.File> filePredicate) {
      return () -> urls.stream().flatMap(url -> {
         try {
            return StreamSupport.stream(fromURL(url).getFiles().spliterator(), false);
         } catch (Throwable var2) {
            if (Reflections.log != null) {
               Reflections.log.error("could not findFiles for url. continuing. [" + url + "]", var2);
            }

            return Stream.of();
         }
      }).filter(filePredicate).iterator();
   }

   public static java.io.File getFile(URL url) {
      try {
         String path = url.toURI().getSchemeSpecificPart();
         java.io.File file;
         if ((file = new java.io.File(path)).exists()) {
            return file;
         }
      } catch (URISyntaxException var6) {
      }

      try {
         String path = URLDecoder.decode(url.getPath(), "UTF-8");
         if (path.contains(".jar!")) {
            path = path.substring(0, path.lastIndexOf(".jar!") + ".jar".length());
         }

         java.io.File file;
         if ((file = new java.io.File(path)).exists()) {
            return file;
         }
      } catch (UnsupportedEncodingException var5) {
      }

      try {
         String pathx = url.toExternalForm();
         if (pathx.startsWith("jar:")) {
            pathx = pathx.substring("jar:".length());
         }

         if (pathx.startsWith("wsjar:")) {
            pathx = pathx.substring("wsjar:".length());
         }

         if (pathx.startsWith("file:")) {
            pathx = pathx.substring("file:".length());
         }

         if (pathx.contains(".jar!")) {
            pathx = pathx.substring(0, pathx.indexOf(".jar!") + ".jar".length());
         }

         if (pathx.contains(".war!")) {
            pathx = pathx.substring(0, pathx.indexOf(".war!") + ".war".length());
         }

         java.io.File file;
         if ((file = new java.io.File(pathx)).exists()) {
            return file;
         }

         pathx = pathx.replace("%20", " ");
         if ((file = new java.io.File(pathx)).exists()) {
            return file;
         }
      } catch (Exception var4) {
      }

      return null;
   }

   private static boolean hasJarFileInPath(URL url) {
      return url.toExternalForm().matches(".*\\.jar(!.*|$)");
   }

   private static boolean hasInnerJarFileInPath(URL url) {
      return url.toExternalForm().matches(".+\\.jar!/.+");
   }

   public static enum DefaultUrlTypes implements Vfs.UrlType {
      jarFile {
         @Override
         public boolean matches(URL url) {
            return url.getProtocol().equals("file") && Vfs.hasJarFileInPath(url);
         }

         @Override
         public Vfs.Dir createDir(URL url) throws Exception {
            return new ZipDir(new JarFile(Vfs.getFile(url)));
         }
      },
      jarUrl {
         @Override
         public boolean matches(URL url) {
            return ("jar".equals(url.getProtocol()) || "zip".equals(url.getProtocol()) || "wsjar".equals(url.getProtocol())) && !Vfs.hasInnerJarFileInPath(url);
         }

         @Override
         public Vfs.Dir createDir(URL url) throws Exception {
            try {
               URLConnection urlConnection = url.openConnection();
               if (urlConnection instanceof JarURLConnection) {
                  urlConnection.setUseCaches(false);
                  return new ZipDir(((JarURLConnection)urlConnection).getJarFile());
               }
            } catch (Throwable var3) {
            }

            java.io.File file = Vfs.getFile(url);
            return file != null ? new ZipDir(new JarFile(file)) : null;
         }
      },
      directory {
         @Override
         public boolean matches(URL url) {
            if (url.getProtocol().equals("file") && !Vfs.hasJarFileInPath(url)) {
               java.io.File file = Vfs.getFile(url);
               return file != null && file.isDirectory();
            } else {
               return false;
            }
         }

         @Override
         public Vfs.Dir createDir(URL url) throws Exception {
            return new SystemDir(Vfs.getFile(url));
         }
      },
      jboss_vfs {
         @Override
         public boolean matches(URL url) {
            return url.getProtocol().equals("vfs");
         }

         @Override
         public Vfs.Dir createDir(URL url) throws Exception {
            return JbossDir.createDir(url);
         }
      },
      jboss_vfsfile {
         @Override
         public boolean matches(URL url) throws Exception {
            return "vfszip".equals(url.getProtocol()) || "vfsfile".equals(url.getProtocol());
         }

         @Override
         public Vfs.Dir createDir(URL url) throws Exception {
            return new UrlTypeVFS().createDir(url);
         }
      },
      bundle {
         @Override
         public boolean matches(URL url) throws Exception {
            return url.getProtocol().startsWith("bundle");
         }

         @Override
         public Vfs.Dir createDir(URL url) throws Exception {
            return Vfs.fromURL(
               (URL)ClasspathHelper.contextClassLoader().loadClass("org.eclipse.core.runtime.FileLocator").getMethod("resolve", URL.class).invoke(null, url)
            );
         }
      },
      jarInputStream {
         @Override
         public boolean matches(URL url) throws Exception {
            return url.toExternalForm().contains(".jar");
         }

         @Override
         public Vfs.Dir createDir(URL url) throws Exception {
            return new JarInputDir(url);
         }
      };

      private DefaultUrlTypes() {
      }
   }

   public interface Dir {
      String getPath();

      Iterable<Vfs.File> getFiles();

      default void close() {
      }
   }

   public interface File {
      String getName();

      String getRelativePath();

      InputStream openInputStream() throws IOException;
   }

   public interface UrlType {
      boolean matches(URL var1) throws Exception;

      Vfs.Dir createDir(URL var1) throws Exception;
   }
}
