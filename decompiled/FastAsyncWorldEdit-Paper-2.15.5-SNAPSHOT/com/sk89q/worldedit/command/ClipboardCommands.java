package com.sk89q.worldedit.command;

import com.fastasyncworldedit.core.Fawe;
import com.fastasyncworldedit.core.FaweAPI;
import com.fastasyncworldedit.core.FaweCache;
import com.fastasyncworldedit.core.configuration.Caption;
import com.fastasyncworldedit.core.configuration.Settings;
import com.fastasyncworldedit.core.event.extent.PasteEvent;
import com.fastasyncworldedit.core.extent.clipboard.DiskOptimizedClipboard;
import com.fastasyncworldedit.core.extent.clipboard.MultiClipboardHolder;
import com.fastasyncworldedit.core.extent.clipboard.ReadOnlyClipboard;
import com.fastasyncworldedit.core.extent.clipboard.URIClipboardHolder;
import com.fastasyncworldedit.core.extent.clipboard.WorldCopyClipboard;
import com.fastasyncworldedit.core.internal.io.FastByteArrayOutputStream;
import com.fastasyncworldedit.core.limit.FaweLimit;
import com.fastasyncworldedit.core.math.transform.MutatingOperationTransformHolder;
import com.fastasyncworldedit.core.util.ImgurUtility;
import com.fastasyncworldedit.core.util.MainUtil;
import com.fastasyncworldedit.core.util.MaskTraverser;
import com.fastasyncworldedit.core.util.task.RunnableVal;
import com.google.common.collect.Lists;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.command.util.CommandPermissions;
import com.sk89q.worldedit.command.util.CommandPermissionsConditionGenerator;
import com.sk89q.worldedit.command.util.Logging;
import com.sk89q.worldedit.command.util.annotation.Confirm;
import com.sk89q.worldedit.command.util.annotation.Preload;
import com.sk89q.worldedit.command.util.annotation.SynchronousSettingExpected;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.block.BlockReplace;
import com.sk89q.worldedit.function.mask.InverseSingleBlockTypeMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.MaskIntersection;
import com.sk89q.worldedit.function.mask.Masks;
import com.sk89q.worldedit.function.mask.RegionMask;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.internal.annotation.ClipboardMask;
import com.sk89q.worldedit.internal.annotation.Direction;
import com.sk89q.worldedit.internal.annotation.Selection;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.NullRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionIntersection;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.regions.selector.CuboidRegionSelector;
import com.sk89q.worldedit.regions.selector.ExtendingCuboidRegionSelector;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.event.ClickEvent;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockTypes;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.enginehub.piston.annotation.Command;
import org.enginehub.piston.annotation.CommandContainer;
import org.enginehub.piston.annotation.param.Arg;
import org.enginehub.piston.annotation.param.ArgFlag;
import org.enginehub.piston.annotation.param.Switch;

@CommandContainer(
   superTypes = {CommandPermissionsConditionGenerator.Registration.class}
)
public class ClipboardCommands {
   private void checkRegionBounds(Region region, LocalSession session) throws MaxChangedBlocksException {
      int limit = session.getBlockChangeLimit();
      if (region.getBoundingBox().getVolume() > limit) {
         throw new MaxChangedBlocksException(limit);
      }
   }

   @Command(
      name = "/copy",
      aliases = {"/cp"},
      desc = "Copy the selection to the clipboard"
   )
   @CommandPermissions({"worldedit.clipboard.copy"})
   @Preload(Preload.PreloadCheck.PRELOAD)
   @Confirm(Confirm.Processor.REGION)
   public void copy(
      Actor actor,
      LocalSession session,
      EditSession editSession,
      @Selection Region region,
      @Switch(name = 'e',desc = "Also copy entities") boolean copyEntities,
      @Switch(name = 'b',desc = "Also copy biomes") boolean copyBiomes,
      @Switch(name = 'c',desc = "Set the origin of the clipboard to the center of the region, at the region's lowest y-level.") boolean centerClipboard,
      @ArgFlag(name = 'm',desc = "Set the include mask, non-matching blocks become air",def = {""}) Mask mask
   ) throws WorldEditException {
      BlockVector3 min = region.getMinimumPoint();
      BlockVector3 max = region.getMaximumPoint();
      long volume = ((long)max.x() - min.x() + 1L) * ((long)max.y() - min.y() + 1L) * ((long)max.z() - min.z() + 1L);
      FaweLimit limit = actor.getLimit();
      if (volume >= limit.MAX_CHECKS.get()) {
         throw FaweCache.MAX_CHECKS;
      } else {
         session.setClipboard(null);
         Clipboard clipboard = new BlockArrayClipboard(region, actor.getUniqueId());
         clipboard.setOrigin(centerClipboard ? region.getCenter().toBlockPoint().withY(region.getMinimumY()) : session.getPlacementPosition(actor));
         ForwardExtentCopy copy = new ForwardExtentCopy(editSession, region, clipboard, region.getMinimumPoint());
         copy.setCopyingEntities(copyEntities);
         this.createCopy(actor, session, editSession, copyBiomes, mask, clipboard, copy);
         copy.getStatusMessages().forEach(actor::print);
      }
   }

   @Command(
      name = "/lazycopy",
      desc = "Lazily copy the selection to the clipboard"
   )
   @CommandPermissions({"worldedit.clipboard.lazycopy"})
   public void lazyCopy(
      Actor actor,
      LocalSession session,
      EditSession editSession,
      @Selection Region region,
      @Switch(name = 'e',desc = "Skip copy entities") boolean skipEntities,
      @Switch(name = 'b',desc = "Also copy biomes") boolean copyBiomes
   ) throws WorldEditException {
      BlockVector3 min = region.getMinimumPoint();
      BlockVector3 max = region.getMaximumPoint();
      long volume = ((long)max.x() - min.x() + 1L) * ((long)max.y() - min.y() + 1L) * ((long)max.z() - min.z() + 1L);
      FaweLimit limit = actor.getLimit();
      if (volume >= limit.MAX_CHECKS.get()) {
         throw FaweCache.MAX_CHECKS;
      } else {
         session.setClipboard(null);
         ReadOnlyClipboard lazyClipboard = WorldCopyClipboard.of(editSession, region, !skipEntities, copyBiomes);
         lazyClipboard.setOrigin(session.getPlacementPosition(actor));
         session.setClipboard(new ClipboardHolder(lazyClipboard));
         actor.print(Caption.of("fawe.worldedit.copy.command.copy", region.getVolume()));
      }
   }

   @Command(
      name = "/cut",
      desc = "Cut the selection to the clipboard",
      descFooter = "WARNING: Cutting and pasting entities cannot be undone!"
   )
   @CommandPermissions({"worldedit.clipboard.cut"})
   @Logging(Logging.LogMode.REGION)
   @Preload(Preload.PreloadCheck.PRELOAD)
   @Confirm(Confirm.Processor.REGION)
   public void cut(
      Actor actor,
      LocalSession session,
      EditSession editSession,
      @Selection Region region,
      @Arg(desc = "Pattern to leave in place of the selection",def = {"air"}) Pattern leavePattern,
      @Switch(name = 'e',desc = "Also cut entities") boolean copyEntities,
      @Switch(name = 'b',desc = "Also copy biomes, source biomes are unaffected") boolean copyBiomes,
      @ArgFlag(name = 'm',desc = "Set the exclude mask, non-matching blocks become air") Mask mask
   ) throws WorldEditException {
      BlockVector3 min = region.getMinimumPoint();
      BlockVector3 max = region.getMaximumPoint();
      long volume = ((long)max.x() - min.x() + 1L) * ((long)max.y() - min.y() + 1L) * ((long)max.z() - min.z() + 1L);
      FaweLimit limit = actor.getLimit();
      if (volume >= limit.MAX_CHECKS.get()) {
         throw FaweCache.MAX_CHECKS;
      } else if (volume >= limit.MAX_CHANGES.get()) {
         throw FaweCache.MAX_CHANGES;
      } else {
         session.setClipboard(null);
         BlockArrayClipboard clipboard = new BlockArrayClipboard(region, actor.getUniqueId());
         clipboard.setOrigin(session.getPlacementPosition(actor));
         ForwardExtentCopy copy = new ForwardExtentCopy(editSession, region, clipboard, region.getMinimumPoint());
         copy.setSourceFunction(new BlockReplace(editSession, leavePattern));
         copy.setCopyingEntities(copyEntities);
         copy.setRemovingEntities(true);
         this.createCopy(actor, session, editSession, copyBiomes, mask, clipboard, copy);
         if (!actor.hasPermission("fawe.tips")) {
            actor.print(Caption.of("fawe.tips.tip.lazycut"));
         }

         copy.getStatusMessages().forEach(actor::print);
      }
   }

   private void createCopy(
      final Actor actor,
      final LocalSession session,
      final EditSession editSession,
      final boolean copyBiomes,
      final Mask mask,
      final Clipboard clipboard,
      final ForwardExtentCopy copy
   ) {
      copy.setCopyingBiomes(copyBiomes);
      Mask sourceMask = editSession.getSourceMask();
      Region[] regions = editSession.getAllowedRegions();
      Region allowedRegion;
      if (regions != null && regions.length != 0) {
         allowedRegion = new RegionIntersection(regions);
      } else {
         allowedRegion = new NullRegion();
      }

      Mask firstSourceMask = mask != null ? mask : sourceMask;
      Mask finalMask = MaskIntersection.of(firstSourceMask, new RegionMask(allowedRegion)).optimize();
      if (finalMask != Masks.alwaysTrue()) {
         copy.setSourceMask(finalMask);
      }

      if (sourceMask != null) {
         editSession.setSourceMask(null);
         new MaskTraverser(sourceMask).reset(editSession);
         editSession.setSourceMask(null);
      }

      try {
         Operations.completeLegacy(copy);
      } catch (Exception var15) {
         if (!(clipboard instanceof DiskOptimizedClipboard doc)) {
            if (!(clipboard instanceof BlockArrayClipboard) || !(((BlockArrayClipboard)clipboard).getParent() instanceof DiskOptimizedClipboard)) {
               throw var15;
            }

            doc = (DiskOptimizedClipboard)((BlockArrayClipboard)clipboard).getParent();
         }

         Fawe.instance().submitUUIDKeyQueuedTask(actor.getUniqueId(), () -> {
            clipboard.close();
            doc.getFile().delete();
         });
         throw var15;
      }

      clipboard.flush();
      session.setClipboard(new ClipboardHolder(clipboard));
   }

   @Command(
      name = "download",
      aliases = {"/download"},
      desc = "Downloads your clipboard through the configured web interface"
   )
   @Deprecated(
      forRemoval = true,
      since = "2.11.0"
   )
   @CommandPermissions({"worldedit.clipboard.download"})
   public void download(final Actor actor, final LocalSession session, @Arg(name = "format",desc = "String",def = {"fast"}) final String formatName) throws WorldEditException {
      ClipboardFormat format = ClipboardFormats.findByAlias(formatName);
      if (format == null) {
         actor.print(Caption.of("fawe.worldedit.clipboard.clipboard.invalid.format", formatName));
      } else {
         actor.print(Caption.of("fawe.web.generating.link", formatName));
         ClipboardHolder holder = session.getClipboard();
         URL url;
         if (holder instanceof MultiClipboardHolder multi) {
            final Set<File> files = new HashSet<>();
            Set<URI> invalid = new HashSet<>();

            for (ClipboardHolder cur : multi.getHolders()) {
               if (cur instanceof URIClipboardHolder uriHolder) {
                  URI uri = uriHolder.getUri();
                  File file = new File(uri.getPath());
                  if (file.exists() && file.isFile()) {
                     files.add(file.getAbsoluteFile());
                  } else if (!uri.getPath().isEmpty()) {
                     invalid.add(uri);
                  }
               }
            }

            LocalConfiguration config = WorldEdit.getInstance().getConfiguration();
            final File working = WorldEdit.getInstance().getWorkingDirectoryFile(config.saveDir).getAbsoluteFile();
            url = MainUtil.upload(null, null, "zip", new RunnableVal<OutputStream>() {
               public void run(OutputStream out) {
                  try {
                     try (ZipOutputStream zos = new ZipOutputStream(out)) {
                        for (File file : files) {
                           String fileName = file.getName();
                           if (MainUtil.isInSubDirectory(working, file)) {
                              fileName = working.toURI().relativize(file.toURI()).getPath();
                           }

                           ZipEntry ze = new ZipEntry(fileName);
                           zos.putNextEntry(ze);
                           Files.copy(file.toPath(), zos);
                           zos.closeEntry();
                        }
                     }
                  } catch (IOException var9) {
                     throw new RuntimeException(var9);
                  }
               }
            });
         } else {
            Clipboard clipboard = holder.getClipboard();
            Transform transform = holder.getTransform();
            Clipboard target;
            if (!transform.isIdentity()) {
               target = clipboard.transform(MutatingOperationTransformHolder.transform(transform));
            } else {
               target = clipboard;
            }

            if (format == BuiltInClipboardFormat.PNG) {
               try {
                  FastByteArrayOutputStream baos = new FastByteArrayOutputStream(32767);
                  ClipboardWriter writer = format.getWriter(baos);
                  writer.write(target);
                  baos.flush();
                  url = ImgurUtility.uploadImage(baos.toByteArray());
               } catch (IOException var15) {
                  var15.printStackTrace();
                  url = null;
               }
            } else {
               if (Settings.settings().WEB.URL.isEmpty()) {
                  actor.print(Caption.of("fawe.error.setting.disable", "web.url"));
                  return;
               }

               url = FaweAPI.upload(target, format);
            }
         }

         if (url == null) {
            actor.print(Caption.of("fawe.web.generating.link.failed"));
         } else {
            String urlText = url.toString();
            actor.print(Caption.of("fawe.web.download.link", urlText).clickEvent(ClickEvent.openUrl(urlText)));
         }
      }
   }

   @Command(
      name = "/place",
      desc = "Place the clipboard's contents without applying transformations (e.g. rotate)"
   )
   @CommandPermissions({"worldedit.clipboard.place"})
   @SynchronousSettingExpected
   @Logging(Logging.LogMode.PLACEMENT)
   public void place(
      Actor actor,
      World world,
      LocalSession session,
      final EditSession editSession,
      @Switch(name = 'a',desc = "Skip air blocks") boolean ignoreAirBlocks,
      @Switch(name = 'o',desc = "Paste at the original position") boolean atOrigin,
      @Switch(name = 's',desc = "Select the region after pasting") boolean selectPasted,
      @Switch(name = 'n',desc = "No paste, select only. (Implies -s)") boolean onlySelect,
      @Switch(name = 'e',desc = "Paste entities if available") boolean pasteEntities,
      @Switch(name = 'b',desc = "Paste biomes if available") boolean pasteBiomes,
      @Switch(name = 'x',desc = "Remove existing entities in the affected region") boolean removeEntities
   ) throws WorldEditException {
      ClipboardHolder holder = session.getClipboard();
      Clipboard clipboard = holder.getClipboard();
      BlockVector3 origin = clipboard.getOrigin();
      BlockVector3 to = atOrigin ? origin : session.getPlacementPosition(actor);
      this.checkPaste(actor, editSession, to, holder, clipboard);
      if (!onlySelect) {
         clipboard.paste(editSession, to, !ignoreAirBlocks, pasteEntities, pasteBiomes);
      }

      Region region = clipboard.getRegion().clone();
      if (selectPasted || onlySelect || removeEntities) {
         BlockVector3 clipboardOffset = clipboard.getRegion().getMinimumPoint().subtract(clipboard.getOrigin());
         Transform transform = MutatingOperationTransformHolder.transform(holder.getTransform());
         BlockVector3 realTo = to.add(transform.apply(clipboardOffset.toVector3()).toBlockPoint());
         BlockVector3 max = realTo.add(transform.apply(region.getMaximumPoint().subtract(region.getMinimumPoint()).toVector3()).toBlockPoint());
         if (removeEntities) {
            editSession.removeEntities(new CuboidRegion(realTo, max));
         }

         if (selectPasted || onlySelect) {
            RegionSelector selector = new CuboidRegionSelector(world, realTo, max);
            session.setRegionSelector(world, selector);
            selector.learnChanges();
            selector.explainRegionAdjust(actor, session);
         }
      }

      if (onlySelect) {
         actor.print(Caption.of("worldedit.paste.selected"));
      } else {
         actor.print(Caption.of("fawe.worldedit.paste.command.paste", to));
      }

      if (!actor.hasPermission("fawe.tips")) {
         actor.print(Caption.of("fawe.tips.tip.copypaste"));
      }
   }

   @Command(
      name = "/paste",
      aliases = {"/p", "/pa"},
      desc = "Paste the clipboard's contents"
   )
   @CommandPermissions({"worldedit.clipboard.paste"})
   @SynchronousSettingExpected
   @Logging(Logging.LogMode.PLACEMENT)
   public void paste(
      Actor actor,
      World world,
      LocalSession session,
      EditSession editSession,
      @Switch(name = 'a',desc = "Skip air blocks") boolean ignoreAirBlocks,
      @Switch(name = 'o',desc = "Paste at the original position") boolean atOrigin,
      @Switch(name = 's',desc = "Select the region after pasting") boolean selectPasted,
      @Switch(name = 'n',desc = "No paste, select only. (Implies -s)") boolean onlySelect,
      @Switch(name = 'e',desc = "Paste entities if available") boolean pasteEntities,
      @Switch(name = 'b',desc = "Paste biomes if available") boolean pasteBiomes,
      @ClipboardMask @ArgFlag(name = 'm',desc = "Only paste blocks matching this mask") Mask sourceMask,
      @Switch(name = 'x',desc = "Remove existing entities in the affected region") boolean removeEntities,
      @Switch(name = 'v',desc = "Don't paste structure void blocks and keep the target block state") boolean ignoreStructureVoidBlocks
   ) throws WorldEditException {
      ClipboardHolder holder = session.getClipboard();
      if (holder.getTransform().isIdentity() && sourceMask == null && !ignoreStructureVoidBlocks) {
         this.place(actor, world, session, editSession, ignoreAirBlocks, atOrigin, selectPasted, onlySelect, pasteEntities, pasteBiomes, removeEntities);
      } else {
         Clipboard clipboard = holder.getClipboard();
         Region region = clipboard.getRegion();
         List<Component> messages = Lists.newArrayList();
         BlockVector3 to = atOrigin ? clipboard.getOrigin() : session.getPlacementPosition(actor);
         this.checkPaste(actor, editSession, to, holder, clipboard);
         if (!onlySelect) {
            Mask finalSourceMask = ignoreStructureVoidBlocks
               ? MaskIntersection.of(sourceMask, new InverseSingleBlockTypeMask(clipboard, BlockTypes.STRUCTURE_VOID))
               : sourceMask;
            Operation operation = holder.createPaste(editSession)
               .to(to)
               .ignoreAirBlocks(ignoreAirBlocks)
               .copyBiomes(pasteBiomes)
               .copyEntities(pasteEntities)
               .maskSource(finalSourceMask)
               .build();
            Operations.completeLegacy(operation);
            messages.addAll(Lists.newArrayList(operation.getStatusMessages()));
         }

         if (selectPasted || onlySelect || removeEntities) {
            BlockVector3 clipboardOffset = clipboard.getRegion().getMinimumPoint().subtract(clipboard.getOrigin());
            Transform transform = MutatingOperationTransformHolder.transform(holder.getTransform());
            Vector3 realTo = to.toVector3().add(transform.apply(clipboardOffset.toVector3()));
            Vector3 max = realTo.add(transform.apply(region.getMaximumPoint().subtract(region.getMinimumPoint()).toVector3()));
            if (removeEntities) {
               editSession.removeEntities(new CuboidRegion(realTo.toBlockPoint(), max.toBlockPoint()));
            }

            if (selectPasted || onlySelect) {
               CuboidRegionSelector selector;
               if (session.getRegionSelector(world) instanceof ExtendingCuboidRegionSelector) {
                  selector = new ExtendingCuboidRegionSelector(world, realTo.toBlockPoint(), max.toBlockPoint());
               } else {
                  selector = new CuboidRegionSelector(world, realTo.toBlockPoint(), max.toBlockPoint());
               }

               session.setRegionSelector(world, selector);
               selector.learnChanges();
               selector.explainRegionAdjust(actor, session);
            }
         }

         if (onlySelect) {
            actor.print(Caption.of("worldedit.paste.selected"));
         } else {
            actor.print(Caption.of("worldedit.paste.pasted", TextComponent.of(to.toString())));
         }

         messages.forEach(actor::print);
      }
   }

   private void checkPaste(Actor player, EditSession editSession, BlockVector3 to, ClipboardHolder holder, Clipboard clipboard) {
      URI uri = null;
      if (holder instanceof URIClipboardHolder) {
         uri = ((URIClipboardHolder)holder).getURI(clipboard);
      }

      PasteEvent event = new PasteEvent(player, clipboard, uri, editSession, to);
      WorldEdit.getInstance().getEventBus().post(event);
      if (event.isCancelled()) {
         throw FaweCache.MANUAL;
      }
   }

   @Command(
      name = "/rotate",
      desc = "Rotate the contents of the clipboard",
      descFooter = "Non-destructively rotate the contents of the clipboard.\nAngles are provided in degrees and a positive angle will result in a clockwise rotation. Multiple rotations can be stacked. Interpolation is not performed so angles should be a multiple of 90 degrees.\n"
   )
   @CommandPermissions({"worldedit.clipboard.rotate"})
   public void rotate(
      Actor actor,
      LocalSession session,
      @Arg(desc = "Amount to rotate on the y-axis") double rotateY,
      @Arg(desc = "Amount to rotate on the x-axis",def = {"0"}) double rotateX,
      @Arg(desc = "Amount to rotate on the z-axis",def = {"0"}) double rotateZ
   ) throws WorldEditException {
      ClipboardHolder holder = session.getClipboard();
      AffineTransform transform = new AffineTransform();
      transform = transform.rotateY(-rotateY);
      transform = transform.rotateX(-rotateX);
      transform = transform.rotateZ(-rotateZ);
      holder.setTransform(transform.combine(holder.getTransform()));
      actor.print(Caption.of("worldedit.rotate.rotated"));
   }

   @Command(
      name = "/flip",
      aliases = {"/mirror"},
      desc = "Flip the contents of the clipboard across the origin"
   )
   @CommandPermissions({"worldedit.clipboard.flip"})
   public void flip(
      Actor actor, LocalSession session, @Direction @Arg(desc = "The direction to flip, defaults to look direction.",def = {"me"}) BlockVector3 direction
   ) throws WorldEditException {
      ClipboardHolder holder = session.getClipboard();
      AffineTransform transform = new AffineTransform();
      transform = transform.scale(direction.abs().multiply(-2).add(1, 1, 1).toVector3());
      holder.setTransform(transform.combine(holder.getTransform()));
      actor.print(Caption.of("worldedit.flip.flipped"));
   }

   @Command(
      name = "clearclipboard",
      aliases = {"/clearclipboard", "/cc", "/clearclip"},
      desc = "Clear your clipboard"
   )
   @CommandPermissions({"worldedit.clipboard.clear"})
   public void clearClipboard(Actor actor, LocalSession session) throws WorldEditException {
      ClipboardHolder holder = session.getExistingClipboard();
      if (holder != null) {
         for (Clipboard clipboard : holder.getClipboards()) {
            if (!(clipboard instanceof DiskOptimizedClipboard doc)) {
               if (!(clipboard instanceof BlockArrayClipboard) || !(((BlockArrayClipboard)clipboard).getParent() instanceof DiskOptimizedClipboard)) {
                  continue;
               }

               doc = (DiskOptimizedClipboard)((BlockArrayClipboard)clipboard).getParent();
            }

            doc.close();
            doc.getFile().delete();
         }

         session.setClipboard(null);
         actor.print(Caption.of("worldedit.clearclipboard.cleared"));
      }
   }
}
