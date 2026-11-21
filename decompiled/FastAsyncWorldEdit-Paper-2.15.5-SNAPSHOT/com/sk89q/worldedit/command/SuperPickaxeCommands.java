package com.sk89q.worldedit.command;

import com.fastasyncworldedit.core.configuration.Caption;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.command.tool.AreaPickaxe;
import com.sk89q.worldedit.command.tool.RecursivePickaxe;
import com.sk89q.worldedit.command.tool.SinglePickaxe;
import com.sk89q.worldedit.command.util.CommandPermissions;
import com.sk89q.worldedit.command.util.CommandPermissionsConditionGenerator;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import org.enginehub.piston.annotation.Command;
import org.enginehub.piston.annotation.CommandContainer;
import org.enginehub.piston.annotation.param.Arg;

@CommandContainer(
   superTypes = {CommandPermissionsConditionGenerator.Registration.class}
)
public class SuperPickaxeCommands {
   private final WorldEdit we;

   public SuperPickaxeCommands(WorldEdit we) {
      this.we = we;
   }

   @Command(
      name = "single",
      desc = "Enable the single block super pickaxe mode"
   )
   @CommandPermissions({"worldedit.superpickaxe"})
   public void single(Player player, LocalSession session) throws WorldEditException {
      session.setSuperPickaxe(new SinglePickaxe());
      session.enableSuperPickAxe();
      player.print(Caption.of("worldedit.tool.superpickaxe.mode.single"));
   }

   @Command(
      name = "area",
      desc = "Enable the area super pickaxe pickaxe mode"
   )
   @CommandPermissions({"worldedit.superpickaxe.area"})
   public void area(Player player, LocalSession session, @Arg(desc = "The range of the area pickaxe") int range) throws WorldEditException {
      if (range > player.getLimit().MAX_SUPER_PICKAXE_SIZE) {
         player.print(Caption.of("worldedit.tool.superpickaxe.max-range", TextComponent.of(player.getLimit().MAX_SUPER_PICKAXE_SIZE)));
      } else {
         session.setSuperPickaxe(new AreaPickaxe(range));
         session.enableSuperPickAxe();
         player.print(Caption.of("worldedit.tool.superpickaxe.mode.area"));
      }
   }

   @Command(
      name = "recursive",
      aliases = {"recur"},
      desc = "Enable the recursive super pickaxe pickaxe mode"
   )
   @CommandPermissions({"worldedit.superpickaxe.recursive"})
   public void recursive(Player player, LocalSession session, @Arg(desc = "The range of the recursive pickaxe") double range) throws WorldEditException {
      if (range > player.getLimit().MAX_SUPER_PICKAXE_SIZE) {
         player.print(Caption.of("worldedit.tool.superpickaxe.max-range", TextComponent.of(player.getLimit().MAX_SUPER_PICKAXE_SIZE)));
      } else {
         session.setSuperPickaxe(new RecursivePickaxe(range));
         session.enableSuperPickAxe();
         player.print(Caption.of("worldedit.tool.superpickaxe.mode.recursive"));
      }
   }
}
