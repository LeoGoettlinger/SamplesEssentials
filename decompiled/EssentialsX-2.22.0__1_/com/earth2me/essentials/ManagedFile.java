package com.earth2me.essentials;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

public class ManagedFile {
   private static final int BUFFERSIZE = 8192;
   private final transient File file;

   public ManagedFile(final String filename, final net.ess3.api.IEssentials ess) {
      this.file = new File(ess.getDataFolder(), filename);
      if (this.file.exists()) {
         try {
            if (checkForVersion(this.file, ess.getDescription().getVersion()) && !this.file.delete()) {
               throw new IOException("Could not delete file " + this.file.toString());
            }
         } catch (IOException var5) {
            Essentials.getWrappedLogger().log(Level.SEVERE, var5.getMessage(), (Throwable)var5);
         }
      }

      if (!this.file.exists()) {
         try {
            copyResourceAscii("/" + filename, this.file);
         } catch (IOException var4) {
            Essentials.getWrappedLogger()
               .log(Level.SEVERE, ess.getAdventureFacet().miniToLegacy(I18n.tlLiteral("itemsCsvNotLoaded", filename)), (Throwable)var4);
         }
      }
   }

   public static void copyResourceAscii(final String resourceName, final File file) throws IOException {
      InputStreamReader reader = new InputStreamReader(ManagedFile.class.getResourceAsStream(resourceName));

      try {
         MessageDigest digest = getDigest();
         DigestOutputStream digestStream = new DigestOutputStream(new FileOutputStream(file), digest);

         try {
            OutputStreamWriter writer = new OutputStreamWriter(digestStream);

            try {
               char[] buffer = new char[8192];

               while (true) {
                  int length = reader.read(buffer);
                  if (length < 0) {
                     writer.write("\n");
                     writer.flush();
                     BigInteger hashInt = new BigInteger(1, digest.digest());
                     digestStream.on(false);
                     digestStream.write(35);
                     digestStream.write(hashInt.toString(16).getBytes());
                     break;
                  }

                  writer.write(buffer, 0, length);
               }
            } catch (Throwable var11) {
               try {
                  writer.close();
               } catch (Throwable var10) {
                  var11.addSuppressed(var10);
               }

               throw var11;
            }

            writer.close();
         } catch (Throwable var12) {
            try {
               digestStream.close();
            } catch (Throwable var9) {
               var12.addSuppressed(var9);
            }

            throw var12;
         }

         digestStream.close();
      } catch (Throwable var13) {
         try {
            reader.close();
         } catch (Throwable var8) {
            var13.addSuppressed(var8);
         }

         throw var13;
      }

      reader.close();
   }

   public static boolean checkForVersion(final File file, final String version) throws IOException {
      if (file.length() < 33L) {
         return false;
      } else {
         BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));

         boolean var23;
         label108: {
            boolean var15;
            label107: {
               try {
                  label120: {
                     byte[] buffer = new byte[(int)file.length()];
                     int position = 0;

                     do {
                        int length = bis.read(buffer, position, Math.min((int)file.length() - position, 8192));
                        if (length < 0) {
                           break;
                        }

                        position += length;
                     } while (position < file.length());

                     ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
                     if (bais.skip(file.length() - 33L) != file.length() - 33L) {
                        var23 = false;
                        break label108;
                     }

                     reader = new BufferedReader(new InputStreamReader(bais));

                     try {
                        String hash = reader.readLine();
                        if (hash == null || !hash.matches("#[a-f0-9]{32}")) {
                           break label120;
                        }

                        hash = hash.substring(1);
                        bais.reset();
                        String versionline = reader.readLine();
                        if (versionline == null || !versionline.matches("#version: .+")) {
                           break label120;
                        }

                        String versioncheck = versionline.substring(10);
                        if (versioncheck.equalsIgnoreCase(version)) {
                           break label120;
                        }

                        bais.reset();
                        MessageDigest digest = getDigest();
                        DigestInputStream digestStream = new DigestInputStream(bais, digest);

                        label99: {
                           try {
                              byte[] bytes = new byte[(int)file.length() - 33];
                              digestStream.read(bytes);
                              BigInteger correct = new BigInteger(hash, 16);
                              BigInteger test = new BigInteger(1, digest.digest());
                              if (correct.equals(test)) {
                                 var15 = true;
                                 break label99;
                              }

                              Essentials.getWrappedLogger()
                                 .warning("File " + file.toString() + " has been modified by user and file version differs, please update the file manually.");
                           } catch (Throwable var19) {
                              try {
                                 digestStream.close();
                              } catch (Throwable var18) {
                                 var19.addSuppressed(var18);
                              }

                              throw var19;
                           }

                           digestStream.close();
                           break label120;
                        }

                        digestStream.close();
                     } catch (Throwable var20) {
                        try {
                           reader.close();
                        } catch (Throwable var17) {
                           var20.addSuppressed(var17);
                        }

                        throw var20;
                     }

                     reader.close();
                     break label107;
                  }

                  reader.close();
               } catch (Throwable var21) {
                  try {
                     bis.close();
                  } catch (Throwable var16) {
                     var21.addSuppressed(var16);
                  }

                  throw var21;
               }

               bis.close();
               return false;
            }

            bis.close();
            return var15;
         }

         bis.close();
         return var23;
      }
   }

   public static MessageDigest getDigest() throws IOException {
      try {
         return MessageDigest.getInstance("MD5");
      } catch (NoSuchAlgorithmException var1) {
         throw new IOException(var1);
      }
   }

   public List<String> getLines() {
      try {
         BufferedReader reader = new BufferedReader(new FileReader(this.file));

         Object var7;
         try {
            List<String> lines = new ArrayList<>();

            while (true) {
               String line = reader.readLine();
               if (line == null) {
                  var7 = lines;
                  break;
               }

               lines.add(line);
            }
         } catch (Throwable var5) {
            try {
               reader.close();
            } catch (Throwable var4) {
               var5.addSuppressed(var4);
            }

            throw var5;
         }

         reader.close();
         return (List<String>)var7;
      } catch (IOException var6) {
         Essentials.getWrappedLogger().log(Level.SEVERE, var6.getMessage(), (Throwable)var6);
         return Collections.emptyList();
      }
   }
}
