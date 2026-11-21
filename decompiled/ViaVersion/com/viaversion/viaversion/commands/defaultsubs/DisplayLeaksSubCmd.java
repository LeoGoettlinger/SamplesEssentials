package com.viaversion.viaversion.commands.defaultsubs;

import com.viaversion.viaversion.api.command.ViaCommandSender;
import com.viaversion.viaversion.api.command.ViaSubCommand;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakDetector.Level;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DisplayLeaksSubCmd implements ViaSubCommand {
   @Override
   public String name() {
      return "displayleaks";
   }

   @Override
   public String description() {
      return "Try to hunt memory leaks!";
   }

   @Override
   public String usage() {
      return "displayleaks [level]";
   }

   @Override
   public boolean execute(ViaCommandSender sender, String[] args) {
      if (args.length == 1) {
         try {
            Level level = Level.valueOf(args[0]);
            ResourceLeakDetector.setLevel(level);
            this.sendMessage(sender, "&6Set leak detector level to &2" + level, new Object[0]);
         } catch (IllegalArgumentException var4) {
            this.sendMessage(sender, "&cInvalid level (" + Arrays.toString((Object[])Level.values()) + ")", new Object[0]);
         }
      } else {
         this.sendMessage(sender, "&6Current leak detection level is &2" + ResourceLeakDetector.getLevel(), new Object[0]);
      }

      return true;
   }

   @Override
   public List<String> onTabComplete(ViaCommandSender sender, String[] args) {
      return args.length == 1
         ? Arrays.stream(Level.values()).map(Enum::name).filter(it -> it.startsWith(args[0])).collect(Collectors.toList())
         : ViaSubCommand.super.onTabComplete(sender, args);
   }
}
