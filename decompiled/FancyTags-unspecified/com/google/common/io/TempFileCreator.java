package com.google.common.io;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.base.StandardSystemProperty;
import com.google.common.collect.ImmutableList;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryFlag;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserPrincipal;
import java.util.EnumSet;
import java.util.Set;

@ElementTypesAreNonnullByDefault
@J2ktIncompatible
@GwtIncompatible
abstract class TempFileCreator {
   static final TempFileCreator INSTANCE = pickSecureCreator();

   abstract File createTempDir();

   abstract File createTempFile(String prefix) throws IOException;

   private static TempFileCreator pickSecureCreator() {
      try {
         Class.forName("java.nio.file.Path");
         return new TempFileCreator.JavaNioCreator();
      } catch (ClassNotFoundException var5) {
         try {
            int version = (Integer)Class.forName("android.os.Build$VERSION").getField("SDK_INT").get(null);
            int jellyBean = (Integer)Class.forName("android.os.Build$VERSION_CODES").getField("JELLY_BEAN").get(null);
            if (version < jellyBean) {
               return new TempFileCreator.ThrowingCreator();
            }
         } catch (NoSuchFieldException var2) {
            return new TempFileCreator.ThrowingCreator();
         } catch (ClassNotFoundException var3) {
            return new TempFileCreator.ThrowingCreator();
         } catch (IllegalAccessException var4) {
            return new TempFileCreator.ThrowingCreator();
         }

         return new TempFileCreator.JavaIoCreator();
      }
   }

   private TempFileCreator() {
   }

   private static final class JavaIoCreator extends TempFileCreator {
      private static final int TEMP_DIR_ATTEMPTS = 10000;

      private JavaIoCreator() {
      }

      @Override
      File createTempDir() {
         File baseDir = new File(StandardSystemProperty.JAVA_IO_TMPDIR.value());
         String baseName = System.currentTimeMillis() + "-";

         for (int counter = 0; counter < 10000; counter++) {
            File tempDir = new File(baseDir, baseName + counter);
            if (tempDir.mkdir()) {
               return tempDir;
            }
         }

         throw new IllegalStateException("Failed to create directory within 10000 attempts (tried " + baseName + "0 to " + baseName + 9999 + ')');
      }

      @Override
      File createTempFile(String prefix) throws IOException {
         return File.createTempFile(prefix, null, null);
      }
   }

   @IgnoreJRERequirement
   private static final class JavaNioCreator extends TempFileCreator {
      private static final TempFileCreator.JavaNioCreator.PermissionSupplier filePermissions;
      private static final TempFileCreator.JavaNioCreator.PermissionSupplier directoryPermissions;

      private JavaNioCreator() {
      }

      @Override
      File createTempDir() {
         try {
            return java.nio.file.Files.createTempDirectory(Paths.get(StandardSystemProperty.JAVA_IO_TMPDIR.value()), null, directoryPermissions.get()).toFile();
         } catch (IOException var2) {
            throw new IllegalStateException("Failed to create directory", var2);
         }
      }

      @Override
      File createTempFile(String prefix) throws IOException {
         return java.nio.file.Files.createTempFile(Paths.get(StandardSystemProperty.JAVA_IO_TMPDIR.value()), prefix, null, filePermissions.get()).toFile();
      }

      private static TempFileCreator.JavaNioCreator.PermissionSupplier userPermissions() {
         try {
            UserPrincipal user = FileSystems.getDefault().getUserPrincipalLookupService().lookupPrincipalByName(StandardSystemProperty.USER_NAME.value());
            final ImmutableList<AclEntry> acl = ImmutableList.of(
               AclEntry.newBuilder()
                  .setType(AclEntryType.ALLOW)
                  .setPrincipal(user)
                  .setPermissions(EnumSet.allOf(AclEntryPermission.class))
                  .setFlags(AclEntryFlag.DIRECTORY_INHERIT, AclEntryFlag.FILE_INHERIT)
                  .build()
            );
            FileAttribute<ImmutableList<AclEntry>> attribute = new FileAttribute<ImmutableList<AclEntry>>() {
               @Override
               public String name() {
                  return "acl:acl";
               }

               public ImmutableList<AclEntry> value() {
                  return acl;
               }
            };
            return () -> attribute;
         } catch (IOException var3) {
            return () -> {
               throw new IOException("Could not find user", var3);
            };
         }
      }

      static {
         Set<String> views = FileSystems.getDefault().supportedFileAttributeViews();
         if (views.contains("posix")) {
            filePermissions = () -> PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------"));
            directoryPermissions = () -> PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwx------"));
         } else if (views.contains("acl")) {
            filePermissions = directoryPermissions = userPermissions();
         } else {
            filePermissions = directoryPermissions = () -> {
               throw new IOException("unrecognized FileSystem type " + FileSystems.getDefault());
            };
         }
      }

      @IgnoreJRERequirement
      private interface PermissionSupplier {
         FileAttribute<?> get() throws IOException;
      }
   }

   private static final class ThrowingCreator extends TempFileCreator {
      private static final String MESSAGE = "Guava cannot securely create temporary files or directories under SDK versions before Jelly Bean. You can create one yourself, either in the insecure default directory or in a more secure directory, such as context.getCacheDir(). For more information, see the Javadoc for Files.createTempDir().";

      private ThrowingCreator() {
      }

      @Override
      File createTempDir() {
         throw new IllegalStateException(
            "Guava cannot securely create temporary files or directories under SDK versions before Jelly Bean. You can create one yourself, either in the insecure default directory or in a more secure directory, such as context.getCacheDir(). For more information, see the Javadoc for Files.createTempDir()."
         );
      }

      @Override
      File createTempFile(String prefix) throws IOException {
         throw new IOException(
            "Guava cannot securely create temporary files or directories under SDK versions before Jelly Bean. You can create one yourself, either in the insecure default directory or in a more secure directory, such as context.getCacheDir(). For more information, see the Javadoc for Files.createTempDir()."
         );
      }
   }
}
