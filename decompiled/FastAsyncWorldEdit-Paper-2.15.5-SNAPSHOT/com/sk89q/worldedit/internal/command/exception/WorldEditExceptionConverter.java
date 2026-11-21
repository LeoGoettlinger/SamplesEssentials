package com.sk89q.worldedit.internal.command.exception;

import com.fastasyncworldedit.core.configuration.Caption;
import com.fastasyncworldedit.core.exception.BrushRadiusLimitException;
import com.fastasyncworldedit.core.exception.OutsideWorldBoundsException;
import com.fastasyncworldedit.core.exception.RadiusLimitException;
import com.fastasyncworldedit.core.internal.exception.FaweException;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.sk89q.worldedit.DisallowedItemException;
import com.sk89q.worldedit.EmptyClipboardException;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.InvalidItemException;
import com.sk89q.worldedit.MaxBrushRadiusException;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.MaxRadiusException;
import com.sk89q.worldedit.MissingWorldException;
import com.sk89q.worldedit.UnknownDirectionException;
import com.sk89q.worldedit.UnknownItemException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.command.InsufficientArgumentsException;
import com.sk89q.worldedit.command.tool.InvalidToolBindException;
import com.sk89q.worldedit.extent.clipboard.io.SchematicLoadException;
import com.sk89q.worldedit.internal.expression.ExpressionException;
import com.sk89q.worldedit.regions.RegionOperationException;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.io.file.FileSelectionAbortedException;
import com.sk89q.worldedit.util.io.file.FilenameResolutionException;
import com.sk89q.worldedit.util.io.file.InvalidFilenameException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.enginehub.piston.exception.CommandException;
import org.enginehub.piston.exception.UsageException;

public class WorldEditExceptionConverter extends ExceptionConverterHelper {
   private static final Pattern numberFormat = Pattern.compile("^For input string: \"(.*)\"$");
   private final WorldEdit worldEdit;

   public WorldEditExceptionConverter(WorldEdit worldEdit) {
      Preconditions.checkNotNull(worldEdit);
      this.worldEdit = worldEdit;
   }

   private CommandException newCommandException(String message, Throwable cause) {
      return this.newCommandException(TextComponent.of(String.valueOf(message)), cause);
   }

   private CommandException newCommandException(Component message, Throwable cause) {
      return new CommandException(message, cause, ImmutableList.of());
   }

   @ExceptionMatch
   public void convert(NumberFormatException e) throws CommandException {
      Matcher matcher = numberFormat.matcher(e.getMessage());
      if (matcher.matches()) {
         throw this.newCommandException(Caption.of("worldedit.error.invalid-number.matches", TextComponent.of(matcher.group(1))), e);
      } else {
         throw this.newCommandException(Caption.of("worldedit.error.invalid-number"), e);
      }
   }

   @ExceptionMatch
   public void convert(IncompleteRegionException e) throws CommandException {
      throw this.newCommandException(Caption.of("worldedit.error.incomplete-region"), e);
   }

   @ExceptionMatch
   public void convert(MissingWorldException e) throws CommandException {
      throw this.newCommandException(e.getRichMessage(), e);
   }

   @ExceptionMatch
   public void convert(UnknownItemException e) throws CommandException {
      throw this.newCommandException(Caption.of("worldedit.error.unknown-block", TextComponent.of(e.getID())), e);
   }

   @Deprecated
   @ExceptionMatch
   public void convert(InvalidItemException e) throws CommandException {
      throw this.newCommandException(e.getMessage(), e);
   }

   @ExceptionMatch
   public void convert(DisallowedItemException e) throws CommandException {
      throw this.newCommandException(Caption.of("worldedit.error.disallowed-block", TextComponent.of(e.getID())), e);
   }

   @ExceptionMatch
   public void convert(MaxChangedBlocksException e) throws CommandException {
      throw this.newCommandException(Caption.of("worldedit.error.max-changes", TextComponent.of(e.getBlockLimit())), e);
   }

   @ExceptionMatch
   public void convert(MaxBrushRadiusException e) throws CommandException {
      throw this.newCommandException(Caption.of("worldedit.error.max-brush-radius", TextComponent.of(this.worldEdit.getConfiguration().maxBrushRadius)), e);
   }

   @ExceptionMatch
   public void convert(MaxRadiusException e) throws CommandException {
      throw this.newCommandException(Caption.of("worldedit.error.max-radius", TextComponent.of(this.worldEdit.getConfiguration().maxRadius)), e);
   }

   @ExceptionMatch
   public void convert(BrushRadiusLimitException e) throws CommandException {
      throw this.newCommandException(Caption.of("fawe.error.limit.max-brush-radius", TextComponent.of(e.getMaxRadius())), e);
   }

   @ExceptionMatch
   public void convert(RadiusLimitException e) throws CommandException {
      throw this.newCommandException(Caption.of("fawe.error.limit.max-radius", TextComponent.of(e.getMaxRadius())), e);
   }

   @ExceptionMatch
   public void convert(OutsideWorldBoundsException e) throws CommandException {
      throw this.newCommandException(Caption.of("fawe.cancel.reason.world.limit", TextComponent.of(e.y())), e);
   }

   @ExceptionMatch
   public void convert(UnknownDirectionException e) throws CommandException {
      throw this.newCommandException(e.getRichMessage(), e);
   }

   @ExceptionMatch
   public void convert(InsufficientArgumentsException e) throws CommandException {
      throw this.newCommandException(e.getRichMessage(), e);
   }

   @ExceptionMatch
   public void convert(RegionOperationException e) throws CommandException {
      throw this.newCommandException(e.getRichMessage(), e);
   }

   @ExceptionMatch
   public void convert(ExpressionException e) throws CommandException {
      throw this.newCommandException(e.getMessage(), e);
   }

   @ExceptionMatch
   public void convert(EmptyClipboardException e) throws CommandException {
      throw this.newCommandException(Caption.of("worldedit.error.empty-clipboard"), e);
   }

   @ExceptionMatch
   public void convert(InvalidFilenameException e) throws CommandException {
      throw this.newCommandException(Caption.of("worldedit.error.invalid-filename", TextComponent.of(e.getFilename()), e.getRichMessage()), e);
   }

   @ExceptionMatch
   public void convert(FilenameResolutionException e) throws CommandException {
      throw this.newCommandException(Caption.of("worldedit.error.file-resolution", TextComponent.of(e.getFilename()), e.getRichMessage()), e);
   }

   @ExceptionMatch
   public void convert(InvalidToolBindException e) throws CommandException {
      throw this.newCommandException(Caption.of("worldedit.tool.error.cannot-bind", e.getItemType().getRichName(), e.getRichMessage()), e);
   }

   @ExceptionMatch
   public void convert(FileSelectionAbortedException e) throws CommandException {
      throw this.newCommandException(Caption.of("worldedit.error.file-aborted"), e);
   }

   @ExceptionMatch
   public void convert(SchematicLoadException e) throws CommandException {
      throw this.newCommandException(e.getRichMessage(), e);
   }

   @ExceptionMatch
   public void convert(WorldEditException e) throws CommandException {
      throw this.newCommandException(e.getRichMessage(), e);
   }

   @ExceptionMatch
   public void convert(UsageException e) throws CommandException {
      throw e;
   }

   @ExceptionMatch
   public void convert(FaweException e) throws CommandException {
      throw this.newCommandException(e.getComponent(), e);
   }
}
