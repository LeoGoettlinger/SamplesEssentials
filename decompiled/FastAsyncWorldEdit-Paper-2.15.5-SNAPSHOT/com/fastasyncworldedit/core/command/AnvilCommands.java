package com.fastasyncworldedit.core.command;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.command.util.CommandPermissions;
import com.sk89q.worldedit.command.util.CommandPermissionsConditionGenerator;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.internal.annotation.Selection;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.biome.BiomeType;
import java.io.IOException;
import org.enginehub.piston.annotation.Command;
import org.enginehub.piston.annotation.CommandContainer;
import org.enginehub.piston.annotation.param.Arg;
import org.enginehub.piston.annotation.param.Switch;

@Deprecated
@CommandContainer(
   superTypes = {CommandPermissionsConditionGenerator.Registration.class}
)
public class AnvilCommands {
   public AnvilCommands(WorldEdit worldEdit) {
      Preconditions.checkNotNull(worldEdit);
   }

   @Command(
      name = "replaceall",
      aliases = {"rea", "repall"},
      desc = "Replace all blocks in the selection with another"
   )
   @CommandPermissions({"worldedit.anvil.replaceall"})
   public void replaceAll(
      Player player,
      String folder,
      @Arg(name = "from",desc = "String",def = {""}) String fromPattern,
      String toPatternStr,
      @Switch(name = 'd',desc = "Disable wildcard data matching") boolean useData
   ) throws WorldEditException {
   }

   @Command(
      name = "remapall",
      descFooter = "Remap the world between MCPE/PC values",
      desc = "Remap the world between MCPE/PC values"
   )
   @CommandPermissions({"worldedit.anvil.remapall"})
   public void remapall(Player player, String folder) throws WorldEditException {
   }

   @Command(
      name = "deleteallunvisited",
      aliases = {"delunvisited"},
      desc = "Delete all chunks which haven't been occupied",
      descFooter = "occupied for `age-ticks` (20t = 1s) and \nHave not been accessed since `file-duration` (ms) after creation and\nHave not been used in the past `chunk-inactivity` (ms)The auto-save interval is the recommended value for `file-duration` and `chunk-inactivity`"
   )
   @CommandPermissions({"worldedit.anvil.deleteallunvisited"})
   public void deleteAllUnvisited(Player player, String folder, int inhabitedTicks, @Arg(desc = "int",def = {"60000"}) int fileDurationMillis) throws WorldEditException {
   }

   @Command(
      name = "deleteallunclaimed",
      aliases = {"delallunclaimed"},
      desc = "Delete all chunks which haven't been occupied",
      descFooter = "Supports: WorldGuard, PlotSquared, GriefPrevention:\nDelete all chunks which aren't claimed AND haven't been occupied for `age-ticks` (20t = 1s) and \nHave not been accessed since `file-duration` (ms) after creation and\nHave not been used in the past `chunk-inactivity` (ms)The auto-save interval is the recommended value for `file-duration` and `chunk-inactivity`"
   )
   @CommandPermissions({"worldedit.anvil.deleteallunclaimed"})
   public void deleteAllUnclaimed(
      Player player,
      int inhabitedTicks,
      @Arg(desc = "int",def = {"60000"}) int fileDurationMillis,
      @Switch(name = 'd',desc = "The flag will debug the task") boolean debug
   ) throws WorldEditException {
   }

   @Command(
      name = "deleteunclaimed",
      desc = "Delete all chunks which haven't been occupied",
      descFooter = "(Supports: WorldGuard, PlotSquared, GriefPrevention):\nIs not claimed\nHas not been occupied for `age-ticks` (20t = 1s) and \nHave not been accessed since `file-duration` (ms) after creation and\nHave not been used in the past `chunk-inactivity` (ms)The auto-save interval is the recommended value for `file-duration` and `chunk-inactivity`"
   )
   @CommandPermissions({"worldedit.anvil.deleteunclaimed"})
   public void deleteUnclaimed(
      Player player,
      EditSession editSession,
      @Selection Region selection,
      int inhabitedTicks,
      @Arg(desc = "int",def = {"60000"}) int fileDurationMillis,
      @Switch(name = 'd',desc = "The flag will debug the task") boolean debug
   ) throws WorldEditException {
   }

   @Command(
      name = "deletealloldregions",
      aliases = {"deloldreg"},
      desc = "Delete regions which haven't been accessed in a certain amount of time",
      descFooter = "You can use seconds (s), minutes (m), hours (h), days (d), weeks (w), years (y)\n(months are not a unit of time) e.g., 8h5m12s\n"
   )
   @CommandPermissions({"worldedit.anvil.deletealloldregions"})
   public void deleteAllOldRegions(Player player, String folder, String time) throws WorldEditException {
   }

   @Command(
      name = "trimallplots",
      desc = "Trim chunks in a Plot World",
      descFooter = "Unclaimed chunks will be deleted\nUnmodified chunks will be deleted\n"
   )
   @CommandPermissions({"worldedit.anvil.trimallplots"})
   public void trimAllPlots(Player player, @Switch(name = 'v',desc = "Delete unvisited chunks") boolean deleteUnvisited) throws WorldEditException {
   }

   @Command(
      name = "deletebiomechunks",
      desc = "Delete chunks matching a specific biome"
   )
   @CommandPermissions({"worldedit.anvil.trimallair"})
   public void deleteBiome(Player player, String folder, BiomeType biome, @Switch(name = 'u',desc = "The flag will run the task unsafe") boolean unsafe) {
   }

   @Command(
      name = "trimallair",
      desc = "Trim all air in the world"
   )
   @CommandPermissions({"worldedit.anvil.trimallair"})
   public void trimAllAir(Player player, String folder, @Switch(name = 'u',desc = "The flag will run the task unsafe") boolean unsafe) throws WorldEditException {
   }

   @Command(
      name = "debugfixroads",
      desc = "debug - do not use"
   )
   @CommandPermissions({"worldedit.anvil.debugfixroads"})
   public void debugfixroads(Player player, String folder) throws WorldEditException {
   }

   @Command(
      name = "replaceallpattern",
      aliases = {"reap", "repallpat"},
      desc = "Replace all blocks in the selection with another"
   )
   @CommandPermissions({"worldedit.anvil.replaceall"})
   public void replaceAllPattern(
      Player player,
      String folder,
      @Arg(desc = "String",def = {""}) String from,
      Pattern toPattern,
      @Switch(name = 'd',desc = "The flag specifies the data to use") boolean useData,
      @Switch(name = 'm',desc = "The flag specifies the map to use") boolean useMap
   ) throws WorldEditException {
   }

   @Command(
      name = "countall",
      desc = "Count all blocks in a world"
   )
   @CommandPermissions({"worldedit.anvil.countall"})
   public void countAll(
      Player player, EditSession editSession, String folder, String argStr, @Switch(name = 'd',desc = "The flag specifies the data to use") boolean useData
   ) throws WorldEditException {
   }

   @Command(
      name = "clear",
      aliases = {"unset"},
      desc = "Clear the chunks in a selection (delete without defrag)"
   )
   @CommandPermissions({"worldedit.anvil.clear"})
   public void unset(Player player, EditSession editSession, @Selection Region selection) throws WorldEditException {
   }

   @Command(
      name = "count",
      desc = "Count blocks in a selection"
   )
   @CommandPermissions({"worldedit.anvil.count"})
   public void count(
      Player player,
      EditSession editSession,
      @Selection Region selection,
      String argStr,
      @Switch(name = 'd',desc = "The flag specifies the data to use") boolean useData
   ) throws WorldEditException {
   }

   @Command(
      name = "distr",
      desc = "Replace all blocks in the selection with another"
   )
   @CommandPermissions({"worldedit.anvil.distr"})
   public void distr(
      Player player, EditSession editSession, @Selection Region selection, @Switch(name = 'd',desc = "The flag specifies the data to use") boolean useData
   ) throws WorldEditException {
   }

   @Command(
      name = "replace",
      aliases = {"r"},
      desc = "Replace all blocks in the selection with another"
   )
   @CommandPermissions({"worldedit.anvil.replace"})
   public void replace(
      Player player,
      EditSession editSession,
      @Selection Region selection,
      @Arg(desc = "String",def = {""}) String from,
      String toStr,
      @Switch(name = 'd',desc = "The flag specifies the data to use") boolean useData
   ) throws WorldEditException {
   }

   @Command(
      name = "replacepattern",
      aliases = {"preplace", "rp"},
      desc = "Replace all blocks in the selection with a pattern"
   )
   @CommandPermissions({"worldedit.anvil.replace"})
   public void replacePattern(
      Player player,
      EditSession editSession,
      @Selection Region selection,
      @Arg(desc = "String",def = {""}) String from,
      final Pattern toPattern,
      @Switch(name = 'd',desc = "The flag specifies the data to use") boolean useData,
      @Switch(name = 'm',desc = "The flag specifies the map to use") boolean useMap
   ) throws WorldEditException {
   }

   @Command(
      name = "set",
      desc = "Set all blocks in the selection with a pattern"
   )
   @CommandPermissions({"worldedit.anvil.set"})
   public void set(Player player, EditSession editSession, @Selection Region selection, final Pattern toPattern) throws WorldEditException {
   }

   @Command(
      name = "removelayers",
      desc = "Removes matching chunk layers",
      descFooter = "Only if a chunk matches the provided id"
   )
   @CommandPermissions({"worldedit.anvil.removelayer"})
   public void removeLayers(Player player, EditSession editSession, @Selection Region selection, int id) throws WorldEditException {
   }

   @Command(
      name = "copy",
      desc = "Lazily copy chunks to your anvil clipboard"
   )
   @CommandPermissions({"worldedit.anvil.copychunks"})
   public void copy(Player player, LocalSession session, EditSession editSession, @Selection Region selection) throws WorldEditException {
   }

   @Command(
      name = "paste",
      desc = "Paste chunks from your anvil clipboard",
      descFooter = "Paste the chunks from your anvil clipboard."
   )
   @CommandPermissions({"worldedit.anvil.pastechunks"})
   public void paste(Player player, LocalSession session, EditSession editSession, @Switch(name = 'c',desc = "Align to chunks") boolean alignChunk) throws WorldEditException, IOException {
   }
}
