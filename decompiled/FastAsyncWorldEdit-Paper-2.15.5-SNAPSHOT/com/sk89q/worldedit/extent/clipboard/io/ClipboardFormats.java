package com.sk89q.worldedit.extent.clipboard.io;

import com.fastasyncworldedit.core.configuration.Caption;
import com.fastasyncworldedit.core.configuration.Settings;
import com.fastasyncworldedit.core.extent.clipboard.LazyClipboardHolder;
import com.fastasyncworldedit.core.extent.clipboard.MultiClipboardHolder;
import com.fastasyncworldedit.core.extent.clipboard.URIClipboardHolder;
import com.fastasyncworldedit.core.internal.io.FastByteArrayOutputStream;
import com.fastasyncworldedit.core.util.MainUtil;
import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.io.ByteSource;
import com.sk89q.jnbt.NBTInputStream;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import it.unimi.dsi.fastutil.io.FastBufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;
import javax.annotation.Nullable;
import org.apache.logging.log4j.Logger;

public class ClipboardFormats {
   private static final Map<String, ClipboardFormat> aliasMap = new HashMap<>();
   private static final Multimap<String, ClipboardFormat> fileExtensionMap = Multimaps.newMultimap(new HashMap(), LinkedHashSet::new);
   private static final Multimap<String, ClipboardFormat> explicitFileExtensionMap = Multimaps.newMultimap(new HashMap(), LinkedHashSet::new);
   private static final List<ClipboardFormat> registeredFormats = new ArrayList<>();
   private static final Logger LOGGER = LogManagerCompat.getLogger();
   private static final Set<ClipboardFormat> FAST_SEARCH_BUILTIN_FORMATS = Set.of(
      BuiltInClipboardFormat.PNG,
      BuiltInClipboardFormat.MCEDIT_SCHEMATIC,
      BuiltInClipboardFormat.MINECRAFT_STRUCTURE,
      BuiltInClipboardFormat.SPONGE_V1_SCHEMATIC,
      BuiltInClipboardFormat.FAST_V2,
      BuiltInClipboardFormat.FAST_V3,
      BuiltInClipboardFormat.SPONGE_V2_SCHEMATIC,
      BuiltInClipboardFormat.SPONGE_V3_SCHEMATIC,
      BuiltInClipboardFormat.BROKENENTITY
   );

   public static void registerClipboardFormat(ClipboardFormat format) {
      Preconditions.checkNotNull(format);

      for (String key : format.getAliases()) {
         String lowKey = key.toLowerCase(Locale.ROOT);
         ClipboardFormat old = aliasMap.put(lowKey, format);
         if (old != null) {
            aliasMap.put(lowKey, old);
            WorldEdit.logger.warn(format.getClass().getName() + " cannot override existing alias '" + lowKey + "' used by " + old.getClass().getName());
         }
      }

      for (String ext : format.getFileExtensions()) {
         String lowExt = ext.toLowerCase(Locale.ROOT);
         fileExtensionMap.put(lowExt, format);
      }

      for (String ext : format.getExplicitFileExtensions()) {
         String lowExt = ext.toLowerCase(Locale.ROOT);
         explicitFileExtensionMap.put(lowExt, format);
      }

      registeredFormats.add(format);
   }

   @Nullable
   public static ClipboardFormat findByAlias(String alias) {
      Preconditions.checkNotNull(alias);
      return aliasMap.get(alias.toLowerCase(Locale.ROOT).trim());
   }

   @Nullable
   public static ClipboardFormat findByFile(File file) {
      Preconditions.checkNotNull(file);
      if (file.getName().toLowerCase().endsWith(".png")) {
         return BuiltInClipboardFormat.PNG;
      } else {
         try (
            DataInputStream inputStream = new DataInputStream(new GZIPInputStream(new FastBufferedInputStream(Files.newInputStream(file.toPath()))));
            NBTInputStream nbtInputStream = new NBTInputStream(inputStream);
         ) {
            if (inputStream.readByte() != 10) {
               return findByFileInExternalFormats(file);
            } else {
               int rootNameTagLength = inputStream.readShort() & '\uffff';
               if (rootNameTagLength != 0 && rootNameTagLength != 9) {
                  return findByFileInExternalFormats(file);
               } else {
                  String rootName = new String(inputStream.readNBytes(rootNameTagLength), StandardCharsets.UTF_8);
                  if (rootName.isEmpty()) {
                     while (true) {
                        byte type = inputStream.readByte();
                        if (type == 0) {
                           return findByFileInExternalFormats(file);
                        }

                        String name = nbtInputStream.readNamedTagName(type);
                        if (type == 10 && name.equals("Schematic")) {
                           break;
                        }

                        if (type == 9 && (name.equals("size") || name.equals("palette") || name.equals("blocks") || name.equals("entities"))) {
                           return BuiltInClipboardFormat.MINECRAFT_STRUCTURE;
                        }

                        nbtInputStream.readTagPayloadLazy(type, 0);
                     }
                  }

                  while (true) {
                     byte typex = inputStream.readByte();
                     if (typex == 0) {
                        return findByFileInExternalFormats(file);
                     }

                     String namex = nbtInputStream.readNamedTagName(typex);
                     if (typex == 7 && namex.equals("Blocks") || typex == 8 && namex.equals("Materials")) {
                        return BuiltInClipboardFormat.MCEDIT_SCHEMATIC;
                     }

                     if (typex == 3 && namex.equals("Version")) {
                        int version = inputStream.readInt();

                        return (ClipboardFormat)(switch (version) {
                           case 1 -> BuiltInClipboardFormat.SPONGE_V1_SCHEMATIC;
                           case 2 -> BuiltInClipboardFormat.FAST_V2;
                           case 3 -> BuiltInClipboardFormat.FAST_V3;
                           default -> findByFileInExternalFormats(file);
                        });
                     }

                     nbtInputStream.readTagPayloadLazy(typex, 0);
                  }
               }
            }
         } catch (EOFException | ZipException var13) {
            return findByFileInExternalFormats(file);
         } catch (IOException var14) {
            LOGGER.error("Failed determining clipboard format for file {}", file.getAbsolutePath(), var14);
            return null;
         }
      }
   }

   private static ClipboardFormat findByFileInExternalFormats(File file) {
      if (registeredFormats.size() == FAST_SEARCH_BUILTIN_FORMATS.size()) {
         return null;
      } else {
         for (ClipboardFormat format : registeredFormats) {
            if (!FAST_SEARCH_BUILTIN_FORMATS.contains(format) && format.isFormat(file)) {
               return format;
            }
         }

         return null;
      }
   }

   public static Multimap<String, ClipboardFormat> getFileExtensionMap() {
      return Multimaps.unmodifiableMultimap(fileExtensionMap);
   }

   public static Collection<ClipboardFormat> getAll() {
      return Collections.unmodifiableCollection(registeredFormats);
   }

   public static String[] getFileExtensionArray() {
      return fileExtensionMap.keySet().toArray(new String[fileExtensionMap.keySet().size()]);
   }

   public static Multimap<String, ClipboardFormat> getExplicitFileExtensionMap() {
      return Multimaps.unmodifiableMultimap(explicitFileExtensionMap);
   }

   private ClipboardFormats() {
   }

   @Nullable
   @Deprecated(
      forRemoval = true,
      since = "2.11.1"
   )
   public static ClipboardFormat findByExtension(String extension) {
      Preconditions.checkNotNull(extension);

      for (Entry<String, ClipboardFormat> entry : getFileExtensionMap().entries()) {
         if (entry.getKey().equalsIgnoreCase(extension)) {
            return entry.getValue();
         }
      }

      return null;
   }

   @Nullable
   public static ClipboardFormat findByExplicitExtension(String extension) {
      Preconditions.checkNotNull(extension);

      for (Entry<String, ClipboardFormat> entry : getExplicitFileExtensionMap().entries()) {
         if (entry.getKey().equalsIgnoreCase(extension)) {
            return entry.getValue();
         }
      }

      return null;
   }

   public static MultiClipboardHolder loadAllFromInput(Actor player, String input, ClipboardFormat format, boolean message) throws IOException {
      Preconditions.checkNotNull(player);
      Preconditions.checkNotNull(input);
      WorldEdit worldEdit = WorldEdit.getInstance();
      LocalConfiguration config = worldEdit.getConfiguration();
      if (input.startsWith("url:")) {
         if (!player.hasPermission("worldedit.schematic.load.web")) {
            if (message) {
               player.print(Caption.of("fawe.error.no-perm", "worldedit.schematic.load.web"));
            }

            return null;
         }

         URL base = new URL(Settings.settings().WEB.URL);
         input = new URL(base, "uploads/" + input.substring(4) + "." + format.getPrimaryFileExtension()).toString();
      }

      if (input.startsWith("http")) {
         return null;
      } else if (Settings.settings().PATHS.PER_PLAYER_SCHEMATICS
         && Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}").matcher(input).find()
         && !player.hasPermission("worldedit.schematic.load.other")) {
         player.print(Caption.of("fawe.error.no-perm", "worldedit.schematic.load.other"));
         return null;
      } else {
         File working = worldEdit.getWorkingDirectoryPath(config.saveDir).toFile();
         File dir = Settings.settings().PATHS.PER_PLAYER_SCHEMATICS ? new File(working, player.getUniqueId().toString()) : working;
         File f;
         if (input.startsWith("#")) {
            String[] extensions;
            if (format != null) {
               extensions = format.getFileExtensions().toArray(new String[0]);
            } else {
               extensions = getFileExtensionArray();
            }

            f = player.openFileOpenDialog(extensions);
            if (f == null || !f.exists()) {
               if (message) {
                  player.print(Caption.of("worldedit.schematic.load.does-not-exist", TextComponent.of(input)));
               }

               return null;
            }
         } else {
            if (Settings.settings().PATHS.PER_PLAYER_SCHEMATICS
               && Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}").matcher(input).find()
               && !player.hasPermission("worldedit.schematic.load.other")) {
               if (message) {
                  player.print(Caption.of("fawe.error.no-perm", "worldedit.schematic.load.other"));
               }

               return null;
            }

            if (format == null && input.matches(".*\\.[\\w].*")) {
               String extension = input.substring(input.lastIndexOf(46) + 1);
               format = findByExplicitExtension(extension);
            }

            f = MainUtil.resolve(dir, input, format, true);
         }

         if ((f == null || !f.exists()) && !input.contains("../")) {
            f = MainUtil.resolve(dir, input, null, true);
         }

         if (f != null && f.exists() && MainUtil.isInSubDirectory(working, f)) {
            if (format == null && f.isFile()) {
               format = findByFile(f);
               if (format == null) {
                  player.print(Caption.of("fawe.worldedit.clipboard.clipboard.invalid.format", f.getName()));
                  return null;
               }
            }

            if (!f.exists()) {
               if (message) {
                  player.print(Caption.of("fawe.error.schematic.not.found", input));
               }

               return null;
            } else if (!f.isDirectory()) {
               ByteSource source = com.google.common.io.Files.asByteSource(f);
               URI uri = f.toURI();
               return new MultiClipboardHolder(uri, new LazyClipboardHolder(f.toURI(), source, format, null));
            } else {
               URIClipboardHolder[] clipboards = loadAllFromDirectory(f);
               if (clipboards.length < 1) {
                  if (message) {
                     player.print(Caption.of("fawe.error.schematic.not.found", input));
                  }

                  return null;
               } else {
                  return new MultiClipboardHolder(f.toURI(), clipboards);
               }
            }
         } else {
            if (message) {
               player.printError(
                  "Schematic "
                     + input
                     + " does not exist! ("
                     + (f != null && f.exists())
                     + "|"
                     + f
                     + "|"
                     + (f != null && !MainUtil.isInSubDirectory(working, f))
                     + ")"
               );
            }

            return null;
         }
      }
   }

   public static URIClipboardHolder[] loadAllFromDirectory(File dir) {
      HashSet<String> extensions = new HashSet<>(Arrays.asList(getFileExtensionArray()));
      File[] files = dir.listFiles(pathname -> {
         String input = pathname.getName();
         String extension = input.substring(input.lastIndexOf(46) + 1);
         return extensions.contains(extension.toLowerCase(Locale.ENGLISH));
      });
      LazyClipboardHolder[] clipboards = new LazyClipboardHolder[files.length];

      for (int i = 0; i < files.length; i++) {
         File file = files[i];
         ByteSource source = com.google.common.io.Files.asByteSource(file);
         ClipboardFormat format = findByFile(file);
         clipboards[i] = new LazyClipboardHolder(file.toURI(), source, format, null);
      }

      return clipboards;
   }

   public static MultiClipboardHolder loadAllFromUrl(URL url) throws IOException {
      List<LazyClipboardHolder> clipboards = new ArrayList<>();

      try (
         ReadableByteChannel rbc = Channels.newChannel(url.openStream());
         InputStream in = Channels.newInputStream(rbc);
      ) {
         try (ZipInputStream zip = new ZipInputStream(in)) {
            byte[] buffer = new byte[8192];

            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
               String filename = entry.getName();
               ClipboardFormat format = findByExtension(filename);
               if (format != null) {
                  FastByteArrayOutputStream out = new FastByteArrayOutputStream();

                  int len;
                  while ((len = zip.read(buffer)) > 0) {
                     out.write(buffer, 0, len);
                  }

                  byte[] array = out.toByteArray();
                  ByteSource source = ByteSource.wrap(array);
                  LazyClipboardHolder clipboard = new LazyClipboardHolder(url.toURI(), source, format, null);
                  clipboards.add(clipboard);
               }
            }
         } catch (URISyntaxException var19) {
            var19.printStackTrace();
         }
      }

      LazyClipboardHolder[] arr = clipboards.toArray(new LazyClipboardHolder[0]);

      try {
         MultiClipboardHolder multi = new MultiClipboardHolder(url.toURI());

         for (LazyClipboardHolder h : arr) {
            multi.add((URIClipboardHolder)h);
         }

         return multi;
      } catch (URISyntaxException var17) {
         throw new RuntimeException(var17);
      }
   }

   static {
      for (BuiltInClipboardFormat format : BuiltInClipboardFormat.values()) {
         registerClipboardFormat(format);
      }
   }
}
