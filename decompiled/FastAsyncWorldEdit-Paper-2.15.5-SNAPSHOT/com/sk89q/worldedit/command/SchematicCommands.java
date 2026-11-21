package com.sk89q.worldedit.command;

import com.fastasyncworldedit.core.configuration.Caption;
import com.fastasyncworldedit.core.configuration.Settings;
import com.fastasyncworldedit.core.event.extent.ActorSaveClipboardEvent;
import com.fastasyncworldedit.core.extent.clipboard.MultiClipboardHolder;
import com.fastasyncworldedit.core.extent.clipboard.URIClipboardHolder;
import com.fastasyncworldedit.core.internal.exception.FaweException;
import com.fastasyncworldedit.core.math.transform.MutatingOperationTransformHolder;
import com.fastasyncworldedit.core.util.MainUtil;
import com.fastasyncworldedit.core.util.ReflectionUtils;
import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.command.argument.Arguments;
import com.sk89q.worldedit.command.util.AsyncCommandBuilder;
import com.sk89q.worldedit.command.util.CommandPermissions;
import com.sk89q.worldedit.command.util.CommandPermissionsConditionGenerator;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.extent.clipboard.io.share.ClipboardShareDestination;
import com.sk89q.worldedit.extent.clipboard.io.share.ClipboardShareMetadata;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.util.formatting.component.ErrorFormat;
import com.sk89q.worldedit.util.formatting.component.PaginationBox;
import com.sk89q.worldedit.util.formatting.component.TextComponentProducer;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.util.formatting.text.event.ClickEvent;
import com.sk89q.worldedit.util.formatting.text.event.HoverEvent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import com.sk89q.worldedit.util.io.Closer;
import com.sk89q.worldedit.util.io.file.FilenameException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;
import org.apache.logging.log4j.Logger;
import org.enginehub.piston.annotation.Command;
import org.enginehub.piston.annotation.CommandContainer;
import org.enginehub.piston.annotation.param.Arg;
import org.enginehub.piston.annotation.param.ArgFlag;
import org.enginehub.piston.annotation.param.Switch;
import org.enginehub.piston.exception.StopExecutionException;

@CommandContainer(
   superTypes = {CommandPermissionsConditionGenerator.Registration.class}
)
public class SchematicCommands {
   private static final Logger LOGGER = LogManagerCompat.getLogger();
   private final WorldEdit worldEdit;

   public SchematicCommands(WorldEdit worldEdit) {
      Preconditions.checkNotNull(worldEdit);
      this.worldEdit = worldEdit;
   }

   private static List<File> getFiles(File root, String filter, ClipboardFormat format) {
      File[] files = root.listFiles();
      if (files == null) {
         return null;
      } else {
         if (format != null) {
            files = Arrays.stream(files).filter(format::isFormat).toArray(File[]::new);
         }

         List<File> fileList = new ArrayList<>();

         for (File f : files) {
            if (f.isDirectory()) {
               List<File> subFiles = getFiles(f, filter, format);
               if (subFiles != null) {
                  fileList.addAll(subFiles);
               }
            } else {
               fileList.add(f);
            }
         }

         return fileList;
      }
   }

   @Command(
      name = "loadall",
      desc = "Load multiple clipboards (paste will randomly choose one)"
   )
   @Deprecated
   @CommandPermissions({"worldedit.clipboard.load", "worldedit.schematic.load", "worldedit.schematic.load.web", "worldedit.schematic.load.asset"})
   public void loadall(
      Actor actor,
      LocalSession session,
      @Arg(desc = "Format name.",def = {"fast"}) String formatName,
      @Arg(desc = "File name.") String filename,
      @Switch(name = 'o',desc = "Overwrite/replace existing clipboard(s)") boolean overwrite,
      @Switch(name = 'r',desc = "Apply random rotation (static by default)") boolean randomRotate,
      @Switch(name = 'd',desc = "Random rotation is dynamic, changing each use") boolean dynamicRandom
   ) throws FilenameException {
      ClipboardFormat format = ClipboardFormats.findByAlias(formatName);
      if (format == null) {
         actor.print(Caption.of("fawe.worldedit.clipboard.clipboard.invalid.format", formatName));
      } else {
         try {
            MultiClipboardHolder all = ClipboardFormats.loadAllFromInput(actor, filename, null, true);
            if (all == null) {
               actor.print(Caption.of("fawe.worldedit.schematic.schematic.loaded", filename));
            } else {
               if (randomRotate) {
                  for (ClipboardHolder holder : all) {
                     setRandomRotateTransform(dynamicRandom, holder);
                  }

                  setRandomRotateTransform(dynamicRandom, all);
               }

               if (overwrite) {
                  session.setClipboard(all);
               } else {
                  session.addClipboard(all);
               }

               actor.print(Caption.of("fawe.worldedit.schematic.schematic.loaded", filename));
            }
         } catch (IOException var12) {
            throw new RuntimeException(var12);
         }
      }
   }

   private static void setRandomRotateTransform(boolean dynamicRandom, ClipboardHolder holder) {
      if (dynamicRandom) {
         MutatingOperationTransformHolder<AffineTransform> mutating = new MutatingOperationTransformHolder<>(new AffineTransform(), t -> {
            int rotatex = 90 * ThreadLocalRandom.current().nextInt(4);
            return t.rotateY(rotatex);
         });
         holder.setTransform(mutating);
      } else {
         AffineTransform transform = new AffineTransform();
         int rotate = 90 * ThreadLocalRandom.current().nextInt(4);
         transform = transform.rotateY(rotate);
         holder.setTransform(transform);
      }
   }

   @Command(
      name = "clear",
      desc = "Clear your clipboard"
   )
   @CommandPermissions({"worldedit.clipboard.clear", "worldedit.schematic.clear"})
   public void clear(Actor actor, LocalSession session) throws WorldEditException {
      session.setClipboard(null);
      actor.print(Caption.of("fawe.worldedit.clipboard.clipboard.cleared"));
   }

   @Command(
      name = "unload",
      desc = "Remove a clipboard from your multi-clipboard"
   )
   @CommandPermissions({"worldedit.clipboard.clear", "worldedit.schematic.clear"})
   public void unload(Actor actor, LocalSession session, @Arg(desc = "File name, requires extension.") String fileName) throws WorldEditException {
      URI uri;
      if (!fileName.startsWith("file:/") && !fileName.startsWith("http://") && !fileName.startsWith("https://")) {
         LocalConfiguration config = this.worldEdit.getConfiguration();
         File working = this.worldEdit.getWorkingDirectoryPath(config.saveDir).toFile();
         File root = Settings.settings().PATHS.PER_PLAYER_SCHEMATICS ? new File(working, actor.getUniqueId().toString()) : working;
         uri = new File(root, fileName).toURI();
      } else {
         uri = URI.create(fileName);
      }

      if (session.getClipboard() instanceof URIClipboardHolder identifiable && identifiable.contains(uri)) {
         if (identifiable instanceof MultiClipboardHolder multi) {
            multi.remove(uri);
            if (multi.getHolders().isEmpty()) {
               session.setClipboard(null);
            }
         } else {
            session.setClipboard(null);
         }

         actor.print(Caption.of("fawe.worldedit.clipboard.clipboard.cleared"));
      } else {
         actor.print(Caption.of("fawe.worldedit.clipboard.clipboard.uri.not.found", fileName));
      }
   }

   @Command(
      name = "move",
      aliases = {"m"},
      desc = "Move your loaded schematic"
   )
   @CommandPermissions({"worldedit.schematic.move", "worldedit.schematic.move.other"})
   public void move(Actor actor, LocalSession session, @Arg(desc = "Directory.") String directory) throws WorldEditException, IOException {
      LocalConfiguration config = this.worldEdit.getConfiguration();
      File working = this.worldEdit.getWorkingDirectoryPath(config.saveDir).toFile();
      File dir = Settings.settings().PATHS.PER_PLAYER_SCHEMATICS ? new File(working, actor.getUniqueId().toString()) : working;
      File destDir = new File(dir, directory);
      if (!MainUtil.isInSubDirectory(working, destDir)) {
         actor.print(Caption.of("worldedit.schematic.directory-does-not-exist", TextComponent.of(String.valueOf(destDir))));
      } else if (Settings.settings().PATHS.PER_PLAYER_SCHEMATICS
         && !MainUtil.isInSubDirectory(dir, destDir)
         && !actor.hasPermission("worldedit.schematic.move.other")) {
         actor.print(Caption.of("fawe.error.no-perm", "worldedit.schematic.move.other"));
      } else {
         ClipboardHolder clipboard = session.getClipboard();
         List<File> sources = this.getFiles(clipboard);
         if (sources.isEmpty()) {
            actor.print(Caption.of("fawe.worldedit.schematic.schematic.none"));
         } else if (!destDir.exists() && !destDir.mkdirs()) {
            actor.print(Caption.of("worldedit.schematic.file-perm-fail", TextComponent.of(String.valueOf(destDir))));
         } else {
            for (File source : sources) {
               File destFile = new File(destDir, source.getName());
               if (destFile.exists()) {
                  actor.print(Caption.of("fawe.worldedit.schematic.schematic.move.exists", destFile));
               } else if (Settings.settings().PATHS.PER_PLAYER_SCHEMATICS
                  && (!MainUtil.isInSubDirectory(dir, destFile) || !MainUtil.isInSubDirectory(dir, source))
                  && !actor.hasPermission("worldedit.schematic.delete.other")) {
                  actor.print(
                     Caption.of("fawe.worldedit.schematic.schematic.move.failed", destFile, Caption.of("fawe.error.no-perm", "worldedit.schematic.move.other"))
                  );
               } else {
                  try {
                     File cached = new File(source.getParentFile(), "." + source.getName() + ".cached");
                     Files.move(source.toPath(), destFile.toPath());
                     if (cached.exists()) {
                        Files.move(cached.toPath(), destFile.toPath());
                     }

                     actor.print(Caption.of("fawe.worldedit.schematic.schematic.move.success", source, destFile));
                  } catch (IOException var14) {
                     throw new RuntimeException(var14);
                  }
               }
            }
         }
      }
   }

   private List<File> getFiles(ClipboardHolder clipboard) {
      Collection<URI> uris = Collections.emptyList();
      if (clipboard instanceof URIClipboardHolder) {
         uris = ((URIClipboardHolder)clipboard).getURIs();
      }

      List<File> files = new ArrayList<>();

      for (URI uri : uris) {
         File file = new File(uri);
         if (file.exists()) {
            files.add(file);
         }
      }

      return files;
   }

   @Command(
      name = "load",
      desc = "Load a schematic into your clipboard"
   )
   @CommandPermissions({"worldedit.clipboard.load", "worldedit.schematic.load", "worldedit.schematic.load.asset", "worldedit.schematic.load.web", "worldedit.schematic.load.other"})
   public void load(
      Actor actor,
      LocalSession session,
      @Arg(desc = "File name.") String filename,
      @Arg(desc = "Format name.",def = {""}) String formatName,
      @Switch(name = 'r',desc = "Apply random rotation to the clipboard") boolean randomRotate,
      @Switch(name = 'd',desc = "Random rotation is dynamic, changing each use") boolean dynamicRandom
   ) throws FilenameException {
      LocalConfiguration config = this.worldEdit.getConfiguration();
      boolean noExplicitFormat = formatName == null;
      if (noExplicitFormat) {
         formatName = "fast";
      }

      try (Closer closer = Closer.create()) {
         if (formatName.startsWith("url:")) {
            String t = filename;
            filename = formatName;
            formatName = t;
         }

         ClipboardFormat format;
         InputStream in;
         URI uri;
         if (filename.startsWith("url:")) {
            if (!actor.hasPermission("worldedit.schematic.load.web")) {
               actor.print(Caption.of("fawe.error.no-perm", "worldedit.schematic.load.web"));
               return;
            }

            UUID uuid = UUID.fromString(filename.substring(4));
            URL webUrl = new URL(Settings.settings().WEB.URL);
            if ((format = ClipboardFormats.findByAlias(formatName)) == null) {
               actor.print(Caption.of("worldedit.schematic.unknown-format", TextComponent.of(formatName)));
               return;
            }

            URL url = new URL(webUrl, "uploads/" + uuid + "." + format.getPrimaryFileExtension());
            Path temp = Files.createTempFile("faweremoteschem", null);
            File tempFile = temp.toFile();
            closer.register(() -> Files.deleteIfExists(temp));
            InputStream urlIn = new BufferedInputStream(url.openStream());

            try (OutputStream tempOut = new BufferedOutputStream(new FileOutputStream(tempFile))) {
               urlIn.transferTo(tempOut);
            } catch (Throwable var26) {
               try {
                  urlIn.close();
               } catch (Throwable var23) {
                  var26.addSuppressed(var23);
               }

               throw var26;
            }

            urlIn.close();
            if (noExplicitFormat && (format = ClipboardFormats.findByFile(tempFile)) == null) {
               actor.print(Caption.of("fawe.worldedit.schematic.schematic.load-failure", TextComponent.of(filename)));
               return;
            }

            in = new FileInputStream(tempFile);
            uri = temp.toUri();
         } else {
            File saveDir = this.worldEdit.getWorkingDirectoryPath(config.saveDir).toFile();
            File dir = Settings.settings().PATHS.PER_PLAYER_SCHEMATICS ? new File(saveDir, actor.getUniqueId().toString()) : saveDir;
            File file;
            if (filename.startsWith("#")) {
               format = noExplicitFormat ? null : ClipboardFormats.findByAlias(formatName);
               String[] extensions;
               if (format != null) {
                  extensions = format.getFileExtensions().toArray(new String[0]);
               } else {
                  extensions = ClipboardFormats.getFileExtensionArray();
               }

               file = actor.openFileOpenDialog(extensions);
               if (file == null || !file.exists()) {
                  actor.print(Caption.of("worldedit.schematic.load.does-not-exist", TextComponent.of(filename)));
                  return;
               }
            } else {
               if (Settings.settings().PATHS.PER_PLAYER_SCHEMATICS
                  && !actor.hasPermission("worldedit.schematic.load.other")
                  && Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}").matcher(filename).find()) {
                  actor.print(Caption.of("fawe.error.no-perm", "worldedit.schematic.load.other"));
                  return;
               }

               if (!noExplicitFormat) {
                  format = ClipboardFormats.findByAlias(formatName);
               } else if (filename.matches(".*\\.\\w.*")) {
                  format = ClipboardFormats.findByExplicitExtension(filename.substring(filename.lastIndexOf(46) + 1));
               } else {
                  format = null;
               }

               file = MainUtil.resolve(dir, filename, format, false);
            }

            if ((file == null || !file.exists()) && !filename.contains("../")) {
               dir = this.worldEdit.getWorkingDirectoryPath(config.saveDir).toFile();
               file = MainUtil.resolve(dir, filename, format, false);
            }

            if (file == null || !file.exists() || !MainUtil.isInSubDirectory(saveDir, file)) {
               actor.printError(
                  TextComponent.of(
                     "Schematic "
                        + filename
                        + " does not exist! ("
                        + (file != null && file.exists())
                        + "|"
                        + file
                        + "|"
                        + (file != null && !MainUtil.isInSubDirectory(saveDir, file))
                        + ")"
                  )
               );
               return;
            }

            if (format == null) {
               format = ClipboardFormats.findByFile(file);
               if (format == null) {
                  if (noExplicitFormat) {
                     actor.print(Caption.of("fawe.worldedit.schematic.schematic.load-failure", TextComponent.of(file.getName())));
                  } else {
                     actor.print(Caption.of("worldedit.schematic.unknown-format", TextComponent.of(formatName)));
                  }

                  return;
               }
            }

            in = new FileInputStream(file);
            uri = file.toURI();
         }

         closer.register(in);
         format.hold(actor, uri, in);
         if (randomRotate) {
            setRandomRotateTransform(dynamicRandom, session.getClipboard());
         }

         actor.print(Caption.of("fawe.worldedit.schematic.schematic.loaded", filename));
         return;
      } catch (IllegalArgumentException var28) {
         actor.print(Caption.of("worldedit.schematic.unknown-filename", TextComponent.of(filename)));
      } catch (EOFException var29) {
         actor.print(
            Caption.of("fawe.worldedit.schematic.schematic.load-failure", TextComponent.of(var29.getMessage() != null ? var29.getMessage() : "EOFException"))
         );
         LOGGER.error("Error loading a schematic", var29);
      } catch (IOException var30) {
         actor.print(Caption.of("worldedit.schematic.file-not-exist", TextComponent.of(Objects.toString(var30.getMessage()))));
         LOGGER.warn("Failed to load a saved clipboard", var30);
      } catch (Exception var31) {
         actor.print(Caption.of("fawe.worldedit.schematic.schematic.load-failure", TextComponent.of(var31.getMessage())));
         LOGGER.error("Error loading a schematic", var31);
      }
   }

   @Command(
      name = "save",
      desc = "Save your clipboard into a schematic file"
   )
   @CommandPermissions({"worldedit.clipboard.save", "worldedit.schematic.save", "worldedit.schematic.save.other", "worldedit.schematic.save.global"})
   public void save(
      Actor actor,
      LocalSession session,
      @Arg(desc = "File name.") String filename,
      @Arg(desc = "Format name.",def = {"fast"}) ClipboardFormat format,
      @Switch(name = 'f',desc = "Overwrite an existing file.") boolean allowOverwrite,
      @Switch(name = 'g',desc = "Bypasses per-player-schematic folders") boolean global
   ) throws WorldEditException {
      if (global && !actor.hasPermission("worldedit.schematic.save.global")) {
         actor.print(Caption.of("fawe.error.no-perm", "worldedit.schematic.save.global"));
      } else if (this.worldEdit.getPlatformManager().queryCapability(Capability.GAME_HOOKS).getDataVersion() == -1) {
         actor.print(TranslatableComponent.of("worldedit.schematic.unsupported-minecraft-version"));
      } else {
         LocalConfiguration config = this.worldEdit.getConfiguration();
         File dir = this.worldEdit.getWorkingDirectoryPath(config.saveDir).toFile();
         if (!global && Settings.settings().PATHS.PER_PLAYER_SCHEMATICS) {
            dir = new File(dir, actor.getUniqueId().toString());
         }

         boolean other = false;
         if (filename.contains("../")) {
            other = true;
            if (!actor.hasPermission("worldedit.schematic.save.other")) {
               actor.print(Caption.of("fawe.error.no-perm", "worldedit.schematic.save.other"));
               return;
            }

            if (filename.startsWith("../")) {
               dir = this.worldEdit.getWorkingDirectoryPath(config.saveDir).toFile();
               filename = filename.substring(3);
            }
         }

         File f = this.worldEdit.getSafeSaveFile(actor, dir, filename, format.getPrimaryFileExtension());
         int i = f.getName().lastIndexOf(46);
         if (i == -1 && f.getName().isEmpty() || i == 0) {
            File directory = f.getParentFile();
            int fileNumber = directory.exists() ? MainUtil.getMaxFileId(directory) : 0;
            String extension = i == 0 ? f.getName().substring(i + 1) : format.getPrimaryFileExtension();
            String name = String.format("%s.%s", fileNumber, extension);
            f = new File(directory, name);
            filename = filename + name;
         }

         boolean overwrite = f.exists();
         if (overwrite) {
            if (!actor.hasPermission("worldedit.schematic.delete")) {
               throw new StopExecutionException(Caption.of("worldedit.schematic.already-exists"));
            }

            if (other && !actor.hasPermission("worldedit.schematic.delete.other")) {
               actor.print(Caption.of("fawe.error.no-perm", "worldedit.schematic.delete.other"));
               return;
            }

            if (!allowOverwrite) {
               actor.print(Caption.of("worldedit.schematic.save.already-exists"));
               return;
            }
         }

         File parent = f.getParentFile();
         if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new StopExecutionException(Caption.of("worldedit.schematic.save.failed-directory"));
         } else {
            ClipboardHolder holder = session.getClipboard();
            SchematicCommands.SchematicSaveTask task = new SchematicCommands.SchematicSaveTask(actor, f, dir, format, holder, overwrite);
            AsyncCommandBuilder.wrap(task, actor)
               .registerWithSupervisor(this.worldEdit.getSupervisor(), "Saving schematic " + filename)
               .setDelayMessage(Caption.of("worldedit.schematic.save.saving"))
               .onSuccess(
                  overwrite ? Caption.of("fawe.worldedit.schematic.schematic.overwritten") : Caption.of("fawe.worldedit.schematic.schematic.saved", filename),
                  null
               )
               .onFailure(
                  Caption.of("worldedit.schematic.failed-to-save"), this.worldEdit.getPlatformManager().getPlatformCommandManager().getExceptionConverter()
               )
               .buildAndExec(this.worldEdit.getExecutorService());
         }
      }
   }

   @Command(
      name = "share",
      desc = "Share your clipboard as a schematic online"
   )
   @CommandPermissions({"worldedit.clipboard.share", "worldedit.schematic.share"})
   public void share(
      Actor actor,
      LocalSession session,
      @Arg(desc = "Schematic name. Defaults to name-millis",def = {""}) String schematicName,
      @Arg(desc = "Share location",def = {"arkitektonika"}) ClipboardShareDestination destination,
      @Arg(desc = "Format name",def = {"fast"}) ClipboardFormat format
   ) throws WorldEditException {
      if (this.worldEdit.getPlatformManager().queryCapability(Capability.GAME_HOOKS).getDataVersion() == -1) {
         actor.printError(TranslatableComponent.of("worldedit.schematic.unsupported-minecraft-version"));
      } else {
         if (format == null) {
            format = destination.getDefaultFormat();
         }

         if (!destination.supportsFormat(format)) {
            actor.printError(
               Caption.of("worldedit.schematic.share.unsupported-format", TextComponent.of(destination.getName()), TextComponent.of(format.getName()))
            );
         } else {
            ClipboardHolder holder = session.getClipboard();
            SchematicCommands.SchematicShareTask task = new SchematicCommands.SchematicShareTask(actor, holder, destination, format, schematicName);
            AsyncCommandBuilder.wrap(task, actor)
               .registerWithSupervisor(this.worldEdit.getSupervisor(), "Sharing schematic")
               .setDelayMessage(TranslatableComponent.of("worldedit.schematic.save.saving"))
               .setWorkingMessage(TranslatableComponent.of("worldedit.schematic.save.still-saving"))
               .onSuccess("Shared", consumer -> consumer.accept(actor))
               .onFailure("Failed to share schematic", this.worldEdit.getPlatformManager().getPlatformCommandManager().getExceptionConverter())
               .buildAndExec(this.worldEdit.getExecutorService());
         }
      }
   }

   @Command(
      name = "delete",
      aliases = {"d"},
      desc = "Delete a saved schematic"
   )
   @CommandPermissions({"worldedit.schematic.delete"})
   public void delete(Actor actor, LocalSession session, @Arg(desc = "File name.") String filename) throws WorldEditException, IOException {
      LocalConfiguration config = this.worldEdit.getConfiguration();
      File working = this.worldEdit.getWorkingDirectoryPath(config.saveDir).toFile();
      File dir = Settings.settings().PATHS.PER_PLAYER_SCHEMATICS ? new File(working, actor.getUniqueId().toString()) : working;
      List<File> files = new ArrayList<>();
      if (filename.equalsIgnoreCase("*")) {
         files.addAll(this.getFiles(session.getClipboard()));
      } else {
         File f = MainUtil.resolveRelative(new File(dir, filename));
         files.add(f);
      }

      if (files.isEmpty()) {
         actor.print(Caption.of("worldedit.schematic.delete.does-not-exist", TextComponent.of(filename)));
      } else {
         for (File f : files) {
            if (!MainUtil.isInSubDirectory(working, f) || !f.exists()) {
               actor.print(Caption.of("worldedit.schematic.delete.does-not-exist", TextComponent.of(filename)));
            } else if (Settings.settings().PATHS.PER_PLAYER_SCHEMATICS
               && !MainUtil.isInSubDirectory(dir, f)
               && !actor.hasPermission("worldedit.schematic.delete.other")) {
               actor.print(Caption.of("fawe.error.no-perm", "worldedit.schematic.delete.other"));
            } else if (!this.deleteFile(f)) {
               actor.print(Caption.of("worldedit.schematic.delete.failed", TextComponent.of(filename)));
            } else {
               actor.print(Caption.of("worldedit.schematic.delete.deleted", filename));
            }
         }
      }
   }

   private boolean deleteFile(File file) {
      if (file.delete()) {
         new File(file.getParentFile(), "." + file.getName() + ".cached").delete();
         return true;
      } else {
         return false;
      }
   }

   @Command(
      name = "formats",
      aliases = {"listformats", "f"},
      desc = "List available formats"
   )
   @CommandPermissions(
      value = {"worldedit.schematic.formats"},
      queued = false
   )
   public void formats(Actor actor) {
      actor.print(Caption.of("worldedit.schematic.formats.title"));
      boolean first = true;

      for (ClipboardFormat format : ClipboardFormats.getAll()) {
         StringBuilder builder = new StringBuilder();
         builder.append(format.getName()).append(": ");

         for (String lookupName : format.getAliases()) {
            if (!first) {
               builder.append(", ");
            }

            builder.append(lookupName);
            first = false;
         }

         first = true;
         actor.print(TextComponent.of(builder.toString()));
      }
   }

   @Command(
      name = "list",
      aliases = {"all", "ls"},
      desc = "List saved schematics",
      descFooter = "Note: Format is not fully verified until loading."
   )
   @CommandPermissions(
      value = {"worldedit.schematic.list"},
      queued = false
   )
   public void list(
      Actor actor,
      LocalSession session,
      @ArgFlag(name = 'p',desc = "Page to view.",def = {"1"}) int page,
      @Switch(name = 'd',desc = "Sort by date, oldest first") boolean oldFirst,
      @Switch(name = 'n',desc = "Sort by date, newest first") boolean newFirst,
      @ArgFlag(name = 'f',desc = "Restricts by format.",def = {""}) String formatName,
      @Arg(name = "filter",desc = "Filter for schematics",def = {"all"}) String filter,
      Arguments arguments
   ) throws WorldEditException {
      if (oldFirst && newFirst) {
         throw new StopExecutionException(Caption.of("worldedit.schematic.sorting-old-new"));
      } else {
         String pageCommand = "/" + arguments.get();
         LocalConfiguration config = this.worldEdit.getConfiguration();
         File dir = this.worldEdit.getWorkingDirectoryPath(config.saveDir).toFile();
         String schemCmd = "//schematic";
         String loadSingle = schemCmd + " load";
         String loadMulti = schemCmd + " loadall";
         String unload = schemCmd + " unload";
         String delete = schemCmd + " delete";
         String list = schemCmd + " list";
         String showCmd = schemCmd + " show";
         List<String> args = filter.isEmpty() ? Collections.emptyList() : Arrays.asList(filter.split(" "));
         URIClipboardHolder multi = ReflectionUtils.as(URIClipboardHolder.class, session.getExistingClipboard());
         boolean hasShow = false;
         boolean playerFolder = Settings.settings().PATHS.PER_PLAYER_SCHEMATICS;
         UUID uuid = playerFolder ? actor.getUniqueId() : null;
         List<File> files = UtilityCommands.getFiles(dir, actor, args, formatName, playerFolder, oldFirst, newFirst);
         List<Entry<URI, String>> entries = UtilityCommands.filesToEntry(dir, files, uuid);
         Function<URI, Boolean> isLoaded = multi == null ? f -> false : multi::contains;
         List<Component> components = UtilityCommands.entryToComponent(
            dir,
            entries,
            isLoaded,
            (name, path, type, loaded) -> {
               TextComponentProducer msg = new TextComponentProducer();
               msg.append(Caption.of("worldedit.schematic.dash.symbol"));
               if (loaded) {
                  msg.append(
                     ((TranslatableComponent)Caption.of("worldedit.schematic.minus.symbol")
                           .clickEvent(ClickEvent.of(ClickEvent.Action.SUGGEST_COMMAND, unload + " " + path)))
                        .hoverEvent(HoverEvent.of(HoverEvent.Action.SHOW_TEXT, Caption.of("worldedit.schematic.unload")))
                  );
               } else {
                  msg.append(
                     ((TranslatableComponent)Caption.of("worldedit.schematic.plus.symbol")
                           .clickEvent(ClickEvent.of(ClickEvent.Action.SUGGEST_COMMAND, loadMulti + " " + path)))
                        .hoverEvent(HoverEvent.of(HoverEvent.Action.SHOW_TEXT, Caption.of("worldedit.schematic.clipboard")))
                  );
               }

               if (type != UtilityCommands.URIType.DIRECTORY) {
                  msg.append(
                     ((TranslatableComponent)Caption.of("worldedit.schematic.x.symbol")
                           .clickEvent(ClickEvent.of(ClickEvent.Action.SUGGEST_COMMAND, delete + " " + path)))
                        .hoverEvent(HoverEvent.of(HoverEvent.Action.SHOW_TEXT, Caption.of("worldedit.schematic.delete")))
                  );
               }

               TextComponent msgElem = TextComponent.of(name);
               if (type != UtilityCommands.URIType.DIRECTORY) {
                  msgElem = (TextComponent)msgElem.clickEvent(ClickEvent.of(ClickEvent.Action.SUGGEST_COMMAND, loadSingle + " " + path));
                  msgElem = (TextComponent)msgElem.hoverEvent(HoverEvent.of(HoverEvent.Action.SHOW_TEXT, Caption.of("worldedit.schematic.load")));
               } else {
                  msgElem = (TextComponent)msgElem.clickEvent(ClickEvent.of(ClickEvent.Action.SUGGEST_COMMAND, list + " " + path));
                  msgElem = (TextComponent)msgElem.hoverEvent(HoverEvent.of(HoverEvent.Action.SHOW_TEXT, Caption.of("worldedit.schematic.list")));
               }

               msg.append(msgElem);
               if (type == UtilityCommands.URIType.FILE) {
                  long filesize = 0L;

                  try {
                     filesize = Files.size(Paths.get(dir.getAbsolutePath() + File.separator + (playerFolder ? uuid.toString() + File.separator : "") + path));
                  } catch (IOException var17x) {
                     var17x.printStackTrace();
                  }

                  TextComponent sizeElem = TextComponent.of(String.format(" (%.1f kb)", filesize / 1000.0), TextColor.GRAY);
                  msg.append(sizeElem);
               }

               return msg.create();
            }
         );
         long totalBytes = 0L;
         File parentDir = new File(dir.getAbsolutePath() + (playerFolder ? File.separator + uuid.toString() : ""));

         try {
            List<File> toAddUp = getFiles(parentDir, null, null);
            if (toAddUp != null && toAddUp.size() != 0) {
               for (File schem : toAddUp) {
                  if (schem.getName().endsWith(".schem") || schem.getName().endsWith(".schematic")) {
                     totalBytes += Files.size(Paths.get(schem.getAbsolutePath()));
                  }
               }
            }
         } catch (IOException var34) {
            var34.printStackTrace();
         }

         String headerBytesElem = String.format("%.1fkb", totalBytes / 1000.0);
         if (Settings.settings().PATHS.PER_PLAYER_SCHEMATICS && actor.getLimit().SCHEM_FILE_SIZE_LIMIT > -1) {
            headerBytesElem = headerBytesElem + String.format(" / %dkb", actor.getLimit().SCHEM_FILE_SIZE_LIMIT);
         }

         if (Settings.settings().PATHS.PER_PLAYER_SCHEMATICS) {
            String fullHeader = "| My Schematics: " + headerBytesElem + " |";
            PaginationBox paginationBox = PaginationBox.fromComponents(fullHeader, pageCommand, components);
            actor.print(paginationBox.create(page));
         } else {
            String fullHeader = "| Schematics: " + headerBytesElem + " |";
            PaginationBox paginationBox = PaginationBox.fromComponents(fullHeader, pageCommand, components);
            actor.print(paginationBox.create(page));
         }
      }
   }

   private static class SchematicListTask implements Callable<Component> {
      private final String prefix;
      private final int sortType;
      private final int page;
      private final File rootDir;
      private final String pageCommand;
      private final String filter;
      private final String formatName;

      SchematicListTask(String prefix, int sortType, int page, String pageCommand, String filter, String formatName) {
         this.prefix = prefix;
         this.sortType = sortType;
         this.page = page;
         this.rootDir = WorldEdit.getInstance().getWorkingDirectoryFile(prefix);
         this.pageCommand = pageCommand;
         this.filter = filter;
         this.formatName = formatName;
      }

      public Component call() throws Exception {
         ClipboardFormat format = ClipboardFormats.findByAlias(this.formatName);
         List<File> fileList = SchematicCommands.getFiles(this.rootDir, this.filter, format);
         if (fileList != null && !fileList.isEmpty()) {
            File[] files = new File[fileList.size()];
            fileList.toArray(files);
            Arrays.sort(files, (f1, f2) -> {
               int res;
               if (this.sortType == 0) {
                  int p = f1.getParent().compareTo(f2.getParent());
                  if (p == 0) {
                     res = f1.getName().compareTo(f2.getName());
                  } else {
                     res = p;
                  }
               } else {
                  res = Long.compare(f1.lastModified(), f2.lastModified());
                  if (this.sortType == 1) {
                     res = -res;
                  }
               }

               return res;
            });
            PaginationBox paginationBox = new SchematicCommands.SchematicPaginationBox(this.prefix, files, this.pageCommand);
            return paginationBox.create(this.page);
         } else {
            return ErrorFormat.wrap("No schematics found.");
         }
      }
   }

   private static class SchematicLoadTask implements Callable<ClipboardHolder> {
      private final Actor actor;
      private final File file;
      private final ClipboardFormat format;

      SchematicLoadTask(Actor actor, File file, ClipboardFormat format) {
         this.actor = actor;
         this.file = file;
         this.format = format;
      }

      public ClipboardHolder call() throws Exception {
         ClipboardHolder var6;
         try (Closer closer = Closer.create()) {
            FileInputStream fis = closer.register(new FileInputStream(this.file));
            BufferedInputStream bis = closer.register(new BufferedInputStream(fis));
            ClipboardReader reader = closer.register(this.format.getReader(bis));
            Clipboard clipboard = reader.read();
            SchematicCommands.LOGGER.info(this.actor.getName() + " loaded " + this.file.getCanonicalPath());
            var6 = new ClipboardHolder(clipboard);
         }

         return var6;
      }
   }

   private abstract static class SchematicOutputTask<T> implements Callable<T> {
      protected final Actor actor;
      protected final ClipboardFormat format;
      protected final ClipboardHolder holder;

      SchematicOutputTask(Actor actor, ClipboardFormat format, ClipboardHolder holder) {
         this.actor = actor;
         this.format = format;
         this.holder = holder;
      }

      protected void writeToOutputStream(OutputStream outputStream) throws IOException, WorldEditException {
         Clipboard clipboard = this.holder.getClipboard();
         Transform transform = MutatingOperationTransformHolder.transform(this.holder.getTransform());
         Clipboard target = clipboard.transform(transform);

         try (Closer closer = Closer.create()) {
            OutputStream stream = closer.register(outputStream);
            BufferedOutputStream bos = closer.register(new BufferedOutputStream(stream));
            ClipboardWriter writer = closer.register(this.format.getWriter(bos));
            writer.write(target);
         }
      }
   }

   private static class SchematicPaginationBox extends PaginationBox {
      private final String prefix;
      private final File[] files;

      SchematicPaginationBox(String rootDir, File[] files, String pageCommand) {
         super("worldedit.schematic.available", pageCommand);
         this.prefix = rootDir == null ? "" : rootDir;
         this.files = files;
      }

      @Override
      public Component getComponent(int number) {
         Preconditions.checkArgument(number < this.files.length && number >= 0);
         File file = this.files[number];
         Multimap<String, ClipboardFormat> exts = ClipboardFormats.getFileExtensionMap();
         String format = exts.get(com.google.common.io.Files.getFileExtension(file.getName()))
            .stream()
            .findFirst()
            .map(ClipboardFormat::getName)
            .orElse("Unknown");
         boolean inRoot = file.getParentFile().getName().equals(this.prefix);
         String path = inRoot ? file.getName() : file.getPath().split(Pattern.quote(this.prefix + File.separator))[1];
         return TextComponent.builder()
            .content("")
            .append(
               ((TranslatableComponent)TranslatableComponent.of("worldedit.schematic.load.symbol")
                     .clickEvent(ClickEvent.of(ClickEvent.Action.RUN_COMMAND, "/schem load \"" + path + "\"")))
                  .hoverEvent(HoverEvent.of(HoverEvent.Action.SHOW_TEXT, TranslatableComponent.of("worldedit.schematic.click-to-load")))
            )
            .append(TextComponent.space())
            .append(TextComponent.of(path).hoverEvent(HoverEvent.of(HoverEvent.Action.SHOW_TEXT, TextComponent.of(format))))
            .build();
      }

      @Override
      public int getComponentsSize() {
         return this.files.length;
      }
   }

   private static class SchematicSaveTask extends SchematicCommands.SchematicOutputTask<Void> {
      private final Actor actor;
      private File file;
      private final boolean overwrite;
      private final File rootDir;

      SchematicSaveTask(Actor actor, File file, File rootDir, ClipboardFormat format, ClipboardHolder holder, boolean overwrite) {
         super(actor, format, holder);
         this.actor = actor;
         this.file = file;
         this.overwrite = overwrite;
         this.rootDir = rootDir;
      }

      public Void call() throws Exception {
         Clipboard clipboard = this.holder.getClipboard();
         Transform transform = MutatingOperationTransformHolder.transform(this.holder.getTransform());
         boolean checkFilesize = Settings.settings().PATHS.PER_PLAYER_SCHEMATICS && this.actor.getLimit().SCHEM_FILE_SIZE_LIMIT > -1;
         double directorysizeKb = 0.0;
         String curFilepath = this.file.getAbsolutePath();
         String SCHEMATIC_NAME = this.file.getName();
         double oldKbOverwritten = 0.0;
         int numFiles = -1;
         if (checkFilesize) {
            List<File> toAddUp = SchematicCommands.getFiles(this.rootDir, null, null);
            if (toAddUp != null && toAddUp.size() != 0) {
               for (File child : toAddUp) {
                  if (child.getName().endsWith(".schem") || child.getName().endsWith(".schematic")) {
                     directorysizeKb += Files.size(Paths.get(child.getAbsolutePath())) / 1000.0;
                     numFiles++;
                  }
               }
            }

            if (this.overwrite) {
               oldKbOverwritten = Files.size(Paths.get(this.file.getAbsolutePath())) / 1000.0;
               int iter = 1;

               while (new File(curFilepath + "." + iter + "." + this.format.getPrimaryFileExtension()).exists()) {
                  iter++;
               }

               this.file = new File(curFilepath + "." + iter + "." + this.format.getPrimaryFileExtension());
            }
         }

         if (Settings.settings().PATHS.PER_PLAYER_SCHEMATICS && this.actor.getLimit().SCHEM_FILE_NUM_LIMIT > -1) {
            if (numFiles == -1) {
               numFiles = 0;
               List<File> toAddUpx = SchematicCommands.getFiles(this.rootDir, null, null);
               if (toAddUpx != null && toAddUpx.size() != 0) {
                  for (File childx : toAddUpx) {
                     if (childx.getName().endsWith(".schem") || childx.getName().endsWith(".schematic")) {
                        numFiles++;
                     }
                  }
               }
            }

            int limit = this.actor.getLimit().SCHEM_FILE_NUM_LIMIT;
            if (numFiles >= limit) {
               SchematicCommands.LOGGER.info(this.actor.getName() + " failed to save " + this.file.getCanonicalPath() + " - too many schematics!");
               throw new FaweException(Caption.of("fawe.error.schematic.over.limit", numFiles, limit));
            }
         }

         Clipboard target;
         if (transform.isIdentity()) {
            target = clipboard;
         } else {
            target = clipboard.transform(transform);
         }

         try (Closer closer = Closer.create()) {
            FileOutputStream fos = closer.register(new FileOutputStream(this.file));
            BufferedOutputStream bos = closer.register(new BufferedOutputStream(fos));
            ClipboardWriter writer = closer.register(this.format.getWriter(bos));
            URI uri = null;
            if (this.holder instanceof URIClipboardHolder) {
               uri = ((URIClipboardHolder)this.holder).getURI(clipboard);
            }

            if (new ActorSaveClipboardEvent(this.actor, clipboard, uri, this.file.toURI()).call()) {
               writer.write(target);
               closer.close();
               double filesizeKb = Files.size(Paths.get(this.file.getAbsolutePath())) / 1000.0;
               this.actor.print(Caption.of("fawe.worldedit.schematic.schematic.size", SCHEMATIC_NAME, String.format("%.1f", filesizeKb)));
               if (checkFilesize) {
                  double curKb = filesizeKb + directorysizeKb;
                  int allocatedKb = this.actor.getLimit().SCHEM_FILE_SIZE_LIMIT;
                  if (this.overwrite) {
                     curKb -= oldKbOverwritten;
                  }

                  if (curKb > allocatedKb) {
                     this.file.delete();
                     SchematicCommands.LOGGER.info(this.actor.getName() + " failed to save " + SCHEMATIC_NAME + " - not enough space!");
                     throw new FaweException(
                        Caption.of("fawe.error.schematic.over.disk.limit", String.format("%.1f", curKb), String.format("%dkb", allocatedKb))
                     );
                  }

                  if (this.overwrite) {
                     new File(curFilepath).delete();
                     this.file.renameTo(new File(curFilepath));
                  } else {
                     numFiles++;
                  }

                  this.actor.print(Caption.of("fawe.worldedit.schematic.schematic.disk.space", String.format("%.1f", allocatedKb - curKb)));
               }

               if (Settings.settings().PATHS.PER_PLAYER_SCHEMATICS && this.actor.getLimit().SCHEM_FILE_NUM_LIMIT > -1) {
                  this.actor.print(Caption.of("fawe.worldedit.schematic.schematic.slots.free", this.actor.getLimit().SCHEM_FILE_NUM_LIMIT - numFiles));
               }

               SchematicCommands.LOGGER.info(this.actor.getName() + " saved " + this.file.getCanonicalPath());
            } else {
               this.actor.print(Caption.of("fawe.cancel.reason.manual"));
            }
         }

         return null;
      }
   }

   private static class SchematicShareTask extends SchematicCommands.SchematicOutputTask<Consumer<Actor>> {
      private final Actor actor;
      private final String name;
      private final ClipboardShareDestination destination;

      SchematicShareTask(Actor actor, ClipboardHolder holder, ClipboardShareDestination destination, ClipboardFormat format, String name) {
         super(actor, format, holder);
         this.actor = actor;
         this.name = name;
         this.destination = destination;
      }

      public Consumer<Actor> call() throws Exception {
         ClipboardShareMetadata metadata = new ClipboardShareMetadata(
            this.format, this.actor.getName(), this.name == null ? this.actor.getName() + "-" + System.currentTimeMillis() : this.name
         );
         return this.destination.share(metadata, this::writeToOutputStream);
      }
   }
}
