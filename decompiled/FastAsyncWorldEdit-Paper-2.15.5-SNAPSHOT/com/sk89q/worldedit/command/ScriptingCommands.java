package com.sk89q.worldedit.command;

import com.fastasyncworldedit.core.configuration.Caption;
import com.google.common.base.Preconditions;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.command.util.CommandPermissions;
import com.sk89q.worldedit.command.util.CommandPermissionsConditionGenerator;
import com.sk89q.worldedit.command.util.Logging;
import com.sk89q.worldedit.entity.Player;
import java.io.File;
import java.util.List;
import java.util.stream.Stream;
import org.enginehub.piston.annotation.Command;
import org.enginehub.piston.annotation.CommandContainer;
import org.enginehub.piston.annotation.param.Arg;

@CommandContainer(
   superTypes = {CommandPermissionsConditionGenerator.Registration.class}
)
public class ScriptingCommands {
   private final WorldEdit worldEdit;

   public ScriptingCommands(WorldEdit worldEdit) {
      Preconditions.checkNotNull(worldEdit);
      this.worldEdit = worldEdit;
   }

   @Command(
      name = "cs",
      aliases = {"/cs"},
      desc = "Execute a CraftScript"
   )
   @CommandPermissions({"worldedit.scripting.execute"})
   @Logging(Logging.LogMode.ALL)
   public void execute(
      Player player,
      LocalSession session,
      @Arg(desc = "Filename of the CraftScript to load") String filename,
      @Arg(desc = "Arguments to the CraftScript",def = {""},variable = true) List<String> args
   ) throws WorldEditException {
      if (!player.hasPermission("worldedit.scripting.execute." + filename)) {
         player.print(Caption.of("worldedit.execute.script-permissions"));
      } else {
         session.setLastScript(filename);
         File dir = this.worldEdit.getWorkingDirectoryPath(this.worldEdit.getConfiguration().scriptsDir).toFile();
         File f = this.worldEdit.getSafeOpenFile(player, dir, filename, "js", "js");
         this.worldEdit.runScript(player, f, Stream.concat(Stream.of(filename), args.stream()).toArray(String[]::new));
      }
   }

   @Command(
      name = ".s",
      aliases = {"/.s"},
      desc = "Execute last CraftScript"
   )
   @CommandPermissions({"worldedit.scripting.execute"})
   @Logging(Logging.LogMode.ALL)
   public void executeLast(Player player, LocalSession session, @Arg(desc = "Arguments to the CraftScript",def = {""},variable = true) List<String> args) throws WorldEditException {
      String lastScript = session.getLastScript();
      if (!player.hasPermission("worldedit.scripting.execute." + lastScript)) {
         player.print(Caption.of("worldedit.execute.script-permissions"));
      } else if (lastScript == null) {
         player.print(Caption.of("worldedit.executelast.no-script"));
      } else {
         File dir = this.worldEdit.getWorkingDirectoryPath(this.worldEdit.getConfiguration().scriptsDir).toFile();
         File f = this.worldEdit.getSafeOpenFile(player, dir, lastScript, "js", "js");
         this.worldEdit.runScript(player, f, Stream.concat(Stream.of(lastScript), args.stream()).toArray(String[]::new));
      }
   }
}
