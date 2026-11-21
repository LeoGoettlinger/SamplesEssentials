package com.magmaguy.elitemobs.magmacore.nightbreak;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.PluginDescriptionFile;

final class DownloadedPluginJarValidator {
   private static final long MAX_ARCHIVE_BYTES = 268435456L;
   private static final int MAX_ENTRIES = 100000;
   private static final long MAX_ENTRY_BYTES = 134217728L;
   private static final long MAX_TOTAL_EXPANDED_BYTES = 1073741824L;
   private static final int MAX_DESCRIPTOR_BYTES = 1048576;
   private static final double MAX_COMPRESSION_RATIO = 1000.0;
   private static final Set<String> RSPM_DESCRIPTORS = Set.of("plugin.yml", "bungee.yml", "extension.yml", "velocity-plugin.json");

   private DownloadedPluginJarValidator() {
   }

   static DownloadedPluginJarValidator.ValidationResult validate(File file, Collection<String> expectedPluginNames, String expectedVersion) {
      if (file != null && file.isFile()) {
         if (file.length() > 268435456L) {
            return DownloadedPluginJarValidator.ValidationResult.failure("The downloaded update exceeds the maximum JAR size.");
         } else {
            Set<String> normalizedExpectedNames = new HashSet<>();
            if (expectedPluginNames != null) {
               for (String expectedName : expectedPluginNames) {
                  if (expectedName != null && !expectedName.isBlank()) {
                     normalizedExpectedNames.add(normalizePluginName(expectedName));
                  }
               }
            }

            if (normalizedExpectedNames.isEmpty()) {
               return DownloadedPluginJarValidator.ValidationResult.failure("No expected plugin identity was available for validation.");
            } else {
               try {
                  DownloadedPluginJarValidator.ValidationResult universalFailure;
                  try (JarFile jar = new JarFile(file, true)) {
                     JarEntry descriptorEntry = null;
                     int descriptorCount = 0;
                     int entryCount = 0;
                     long totalExpandedBytes = 0L;
                     Set<String> normalizedEntryNames = new HashSet<>();
                     Map<String, JarEntry> universalDescriptors = new HashMap<>();
                     Enumeration<JarEntry> entries = jar.entries();
                     byte[] buffer = new byte[8192];

                     while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        if (++entryCount > 100000) {
                           return DownloadedPluginJarValidator.ValidationResult.failure("The downloaded update contains too many JAR entries.");
                        }

                        String normalizedEntryName = entry.getName().toLowerCase(Locale.ROOT);
                        if (!normalizedEntryNames.add(normalizedEntryName)) {
                           return DownloadedPluginJarValidator.ValidationResult.failure("The downloaded update contains a duplicate JAR path.");
                        }

                        if ("plugin.yml".equals(entry.getName())) {
                           descriptorEntry = entry;
                           descriptorCount++;
                        }

                        if (RSPM_DESCRIPTORS.contains(entry.getName())) {
                           universalDescriptors.put(entry.getName(), entry);
                        }

                        if (!entry.isDirectory()) {
                           long declaredSize = entry.getSize();
                           if (declaredSize > 134217728L) {
                              return DownloadedPluginJarValidator.ValidationResult.failure("The downloaded update contains an oversized JAR entry.");
                           }

                           long compressedSize = entry.getCompressedSize();
                           if (declaredSize > 0L && compressedSize >= 0L && (double)declaredSize / Math.max(1L, compressedSize) > 1000.0) {
                              return DownloadedPluginJarValidator.ValidationResult.failure(
                                 "The downloaded update contains a suspiciously compressed JAR entry."
                              );
                           }

                           CRC32 crc = new CRC32();
                           long bytesRead = 0L;

                           int read;
                           try (InputStream input = jar.getInputStream(entry)) {
                              while ((read = input.read(buffer)) != -1) {
                                 if (bytesRead + read > 134217728L || totalExpandedBytes + read > 1073741824L) {
                                    return DownloadedPluginJarValidator.ValidationResult.failure("The downloaded update exceeds expanded JAR size limits.");
                                 }

                                 crc.update(buffer, 0, read);
                                 bytesRead += read;
                                 totalExpandedBytes += read;
                              }
                           }

                           if (entry.getSize() >= 0L && entry.getSize() != bytesRead || entry.getCrc() >= 0L && entry.getCrc() != crc.getValue()) {
                              return DownloadedPluginJarValidator.ValidationResult.failure("The downloaded update contains a damaged JAR entry.");
                           }
                        }
                     }

                     if (descriptorCount != 1 || descriptorEntry == null) {
                        return DownloadedPluginJarValidator.ValidationResult.failure("The downloaded JAR must contain exactly one root plugin.yml.");
                     }

                     PluginDescriptionFile descriptor;
                     try (InputStream input = jar.getInputStream(descriptorEntry)) {
                        descriptor = new PluginDescriptionFile(input);
                     } catch (RuntimeException | InvalidDescriptionException var32) {
                        return DownloadedPluginJarValidator.ValidationResult.failure("The downloaded plugin.yml is invalid: " + var32.getMessage());
                     }

                     String pluginName = descriptor.getName().trim();
                     if (!normalizedExpectedNames.contains(normalizePluginName(pluginName))) {
                        return DownloadedPluginJarValidator.ValidationResult.failure(
                           "Downloaded plugin identity '" + pluginName + "' does not match the requested plugin."
                        );
                     }

                     String pluginVersion = descriptor.getVersion().trim();
                     if (pluginVersion.isBlank() || !normalizeArtifactVersion(pluginVersion).equals(normalizeArtifactVersion(expectedVersion))) {
                        return DownloadedPluginJarValidator.ValidationResult.failure(
                           "Downloaded plugin version '" + pluginVersion + "' does not match expected version '" + expectedVersion + "'."
                        );
                     }

                     String mainClass = descriptor.getMain().trim();
                     if (mainClass.isBlank()) {
                        return DownloadedPluginJarValidator.ValidationResult.failure("The downloaded plugin descriptor has no main class.");
                     }

                     String mainClassEntry = mainClass.replace('.', '/') + ".class";
                     JarEntry mainEntry = jar.getJarEntry(mainClassEntry);
                     if (mainEntry == null || mainEntry.isDirectory()) {
                        return DownloadedPluginJarValidator.ValidationResult.failure("The downloaded plugin is missing its declared main class.");
                     }

                     try (InputStream input = jar.getInputStream(mainEntry)) {
                        DownloadedPluginJarValidator.ValidationResult bytecodeFailure = validateClassMagic(input, "plugin");
                        if (bytecodeFailure != null) {
                           return bytecodeFailure;
                        }
                     }

                     if ("resourcepackmanager".equals(normalizePluginName(pluginName))) {
                        universalFailure = validateRspmUniversalDescriptors(jar, universalDescriptors, expectedVersion);
                        if (universalFailure != null) {
                           return universalFailure;
                        }
                     }

                     universalFailure = new DownloadedPluginJarValidator.ValidationResult(true, null, pluginName, pluginVersion, mainClass);
                  }

                  return universalFailure;
               } catch (RuntimeException | IOException var35) {
                  String message = var35.getMessage();
                  if (message == null || message.isBlank()) {
                     message = var35.getClass().getSimpleName();
                  }

                  return DownloadedPluginJarValidator.ValidationResult.failure("The downloaded update is not a valid plugin JAR: " + message);
               }
            }
         }
      } else {
         return DownloadedPluginJarValidator.ValidationResult.failure("The downloaded update is not a regular file.");
      }
   }

   private static DownloadedPluginJarValidator.ValidationResult validateRspmUniversalDescriptors(
      JarFile jar, Map<String, JarEntry> descriptors, String expectedVersion
   ) throws IOException {
      if (!descriptors.keySet().equals(RSPM_DESCRIPTORS)) {
         return DownloadedPluginJarValidator.ValidationResult.failure("ResourcePackManager update is missing a universal platform descriptor.");
      } else {
         DownloadedPluginJarValidator.DescriptorValues bungee = yamlDescriptor(jar, descriptors.get("bungee.yml"));
         DownloadedPluginJarValidator.DescriptorValues geyser = yamlDescriptor(jar, descriptors.get("extension.yml"));
         DownloadedPluginJarValidator.DescriptorValues velocity = jsonDescriptor(jar, descriptors.get("velocity-plugin.json"));
         if (bungee == null || geyser == null || velocity == null) {
            return DownloadedPluginJarValidator.ValidationResult.failure("ResourcePackManager has an unreadable universal platform descriptor.");
         } else if ("resourcepackmanager".equals(normalizePluginName(bungee.name()))
            && "resourcepackmanagergeyserbridge".equals(normalizePluginName(geyser.name()))
            && "resourcepackmanager".equals(normalizePluginName(velocity.name()))) {
            String normalizedExpected = normalizeArtifactVersion(expectedVersion);
            if (normalizedExpected.equals(normalizeArtifactVersion(bungee.version()))
               && normalizedExpected.equals(normalizeArtifactVersion(geyser.version()))
               && normalizedExpected.equals(normalizeArtifactVersion(velocity.version()))) {
               for (DownloadedPluginJarValidator.DescriptorValues descriptor : Set.of(bungee, geyser, velocity)) {
                  if (descriptor.mainClass() != null && !descriptor.mainClass().isBlank()) {
                     JarEntry entry = jar.getJarEntry(descriptor.mainClass().replace('.', '/') + ".class");
                     if (entry != null && !entry.isDirectory()) {
                        try (InputStream input = jar.getInputStream(entry)) {
                           DownloadedPluginJarValidator.ValidationResult bytecodeFailure = validateClassMagic(input, "universal platform");
                           if (bytecodeFailure != null) {
                              return bytecodeFailure;
                           }
                           continue;
                        }
                     }

                     return DownloadedPluginJarValidator.ValidationResult.failure("ResourcePackManager universal descriptor entrypoint is missing.");
                  }

                  return DownloadedPluginJarValidator.ValidationResult.failure("ResourcePackManager universal descriptor has no entrypoint.");
               }

               return null;
            } else {
               return DownloadedPluginJarValidator.ValidationResult.failure("ResourcePackManager universal platform versions disagree.");
            }
         } else {
            return DownloadedPluginJarValidator.ValidationResult.failure("ResourcePackManager universal platform identities disagree.");
         }
      }
   }

   private static DownloadedPluginJarValidator.DescriptorValues yamlDescriptor(JarFile jar, JarEntry entry) throws IOException {
      String source = readDescriptor(jar, entry);
      String name = yamlField(source, "name");
      String version = yamlField(source, "version");
      String main = yamlField(source, "main");
      return name != null && version != null && main != null ? new DownloadedPluginJarValidator.DescriptorValues(name, version, main) : null;
   }

   private static DownloadedPluginJarValidator.DescriptorValues jsonDescriptor(JarFile jar, JarEntry entry) throws IOException {
      String source = readDescriptor(jar, entry);
      String name = jsonField(source, "name");
      String version = jsonField(source, "version");
      String main = jsonField(source, "main");
      return name != null && version != null && main != null ? new DownloadedPluginJarValidator.DescriptorValues(name, version, main) : null;
   }

   private static String readDescriptor(JarFile jar, JarEntry entry) throws IOException {
      if (entry != null && !entry.isDirectory()) {
         if (entry.getSize() > 1048576L) {
            throw new IOException("Platform descriptor exceeds the size limit.");
         } else {
            String var4;
            try (InputStream input = jar.getInputStream(entry)) {
               byte[] bytes = input.readNBytes(1048577);
               if (bytes.length > 1048576) {
                  throw new IOException("Platform descriptor exceeds the size limit.");
               }

               var4 = new String(bytes, StandardCharsets.UTF_8);
            }

            return var4;
         }
      } else {
         throw new IOException("Platform descriptor is missing.");
      }
   }

   private static String yamlField(String source, String field) {
      Matcher matcher = Pattern.compile("(?m)^\\s*" + Pattern.quote(field) + "\\s*:\\s*['\\\"]?([^'\\\"#\\r\\n]+)").matcher(source);
      return matcher.find() ? matcher.group(1).trim() : null;
   }

   private static String jsonField(String source, String field) {
      Matcher matcher = Pattern.compile("\\\"" + Pattern.quote(field) + "\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"").matcher(source);
      return matcher.find() ? matcher.group(1).trim() : null;
   }

   private static DownloadedPluginJarValidator.ValidationResult validateClassMagic(InputStream input, String label) throws IOException {
      byte[] magic = input.readNBytes(4);
      return magic.length == 4 && magic[0] == -54 && magic[1] == -2 && magic[2] == -70 && magic[3] == -66
         ? null
         : DownloadedPluginJarValidator.ValidationResult.failure("The downloaded " + label + " main class is not valid JVM bytecode.");
   }

   private static String normalizePluginName(String value) {
      return value.trim().replace(' ', '_').toLowerCase(Locale.ROOT);
   }

   private static String normalizeArtifactVersion(String value) {
      if (value == null) {
         return "";
      } else {
         String normalized = value.trim();
         if (normalized.startsWith("v") || normalized.startsWith("V")) {
            normalized = normalized.substring(1);
         }

         return normalized;
      }
   }

   private record DescriptorValues(String name, String version, String mainClass) {
   }

   record ValidationResult(boolean valid, String detail, String pluginName, String pluginVersion, String mainClass) {
      private static DownloadedPluginJarValidator.ValidationResult failure(String detail) {
         return new DownloadedPluginJarValidator.ValidationResult(false, detail, null, null, null);
      }
   }
}
