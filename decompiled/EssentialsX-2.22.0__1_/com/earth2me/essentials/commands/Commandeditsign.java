package com.earth2me.essentials.commands;

import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.FormatUtil;
import com.earth2me.essentials.utils.NumberUtil;
import com.earth2me.essentials.utils.VersionUtil;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import net.ess3.api.TranslatableException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.HangingSign;
import org.bukkit.block.data.type.WallHangingSign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.block.sign.Side;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.util.Vector;

public class Commandeditsign extends EssentialsCommand {
   public Commandeditsign() {
      super("editsign");
   }

   @Override
   protected void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
      if (args.length != 0 && (args.length <= 1 || NumberUtil.isInt(args[1]))) {
         Block target = user.getTargetBlock(5);
         if (!(target.getState() instanceof Sign)) {
            throw new TranslatableException("editsignCommandTarget");
         } else {
            Commandeditsign.ModifiableSign sign = this.wrapSign((Sign)target.getState(), user);

            try {
               if (args[0].equalsIgnoreCase("set") && args.length > 2) {
                  String[] existingLines = sign.getLines();
                  int line = Integer.parseInt(args[1]) - 1;
                  String text = FormatUtil.formatString(user, "essentials.editsign", getFinalArg(args, 2)).trim();
                  if (ChatColor.stripColor(text).length() > 15 && !user.isAuthorized("essentials.editsign.unlimited")) {
                     throw new TranslatableException("editsignCommandLimit");
                  }

                  existingLines[line] = text;
                  if (this.callSignEvent(sign, user, existingLines)) {
                     return;
                  }

                  user.sendTl("editsignCommandSetSuccess", line + 1, text);
               } else if (args[0].equalsIgnoreCase("clear")) {
                  if (args.length == 1) {
                     String[] existingLinesx = sign.getLines();

                     for (int i = 0; i < 4; i++) {
                        existingLinesx[i] = "";
                     }

                     if (this.callSignEvent(sign, user, existingLinesx)) {
                        return;
                     }

                     user.sendTl("editsignCommandClear");
                  } else {
                     String[] existingLinesx = sign.getLines();
                     int linex = Integer.parseInt(args[1]) - 1;
                     existingLinesx[linex] = "";
                     if (this.callSignEvent(sign, user, existingLinesx)) {
                        return;
                     }

                     user.sendTl("editsignCommandClearLine", linex + 1);
                  }
               } else if (args[0].equalsIgnoreCase("copy")) {
                  int linex = args.length == 1 ? -1 : Integer.parseInt(args[1]) - 1;
                  if (linex == -1) {
                     for (int i = 0; i < 4; i++) {
                        user.getSignCopy().set(i, FormatUtil.unformatString(user, "essentials.editsign", sign.getLine(i)));
                     }

                     user.sendTl("editsignCopy", commandLabel);
                  } else {
                     user.getSignCopy().set(linex, FormatUtil.unformatString(user, "essentials.editsign", sign.getLine(linex)));
                     user.sendTl("editsignCopyLine", linex + 1, commandLabel);
                  }
               } else {
                  if (!args[0].equalsIgnoreCase("paste")) {
                     throw new NotEnoughArgumentsException();
                  }

                  int linex = args.length == 1 ? -1 : Integer.parseInt(args[1]) - 1;
                  String[] existingLinesx = sign.getLines();
                  if (linex == -1) {
                     for (int i = 0; i < 4; i++) {
                        existingLinesx[i] = FormatUtil.formatString(user, "essentials.editsign", user.getSignCopy().get(i));
                     }

                     if (this.callSignEvent(sign, user, existingLinesx)) {
                        return;
                     }

                     user.sendTl("editsignPaste", commandLabel);
                  } else {
                     existingLinesx[linex] = FormatUtil.formatString(user, "essentials.editsign", user.getSignCopy().get(linex));
                     if (this.callSignEvent(sign, user, existingLinesx)) {
                        return;
                     }

                     user.sendTl("editsignPasteLine", linex + 1, commandLabel);
                  }
               }
            } catch (IndexOutOfBoundsException var10) {
               throw new TranslatableException(var10, "editsignCommandNoLine");
            }
         }
      } else {
         throw new NotEnoughArgumentsException();
      }
   }

   private boolean callSignEvent(final Commandeditsign.ModifiableSign sign, final User user, final String[] lines) {
      SignChangeEvent event;
      if (VersionUtil.getServerBukkitVersion().isHigherThanOrEqualTo(VersionUtil.v1_20_1_R01)) {
         if (sign.isWaxed() && !user.isAuthorized("essentials.editsign.waxed.exempt")) {
            return true;
         }

         event = new SignChangeEvent(sign.getBlock(), user.getBase(), lines, sign.isFront() ? Side.FRONT : Side.BACK);
      } else {
         event = new SignChangeEvent(sign.getBlock(), user.getBase(), lines);
      }

      Bukkit.getServer().getPluginManager().callEvent(event);
      if (event.isCancelled()) {
         if (this.ess.getSettings().isDebug()) {
            this.ess.getLogger().info("SignChangeEvent canceled for /editsign execution by " + user.getName());
         }

         return true;
      } else {
         for (int i = 0; i < 4; i++) {
            sign.setLine(i, lines[i]);
         }

         sign.update();
         return false;
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final User user, final String commandLabel, final String[] args) {
      if (args.length == 1) {
         return Lists.newArrayList(new String[]{"set", "clear", "copy", "paste"});
      } else if (args.length == 2) {
         return Lists.newArrayList(new String[]{"1", "2", "3", "4"});
      } else if (args.length == 3 && args[0].equalsIgnoreCase("set") && NumberUtil.isPositiveInt(args[1])) {
         int line = Integer.parseInt(args[1]);
         Block target = user.getTargetBlock(5);
         if (target.getState() instanceof Sign && line <= 4) {
            Commandeditsign.ModifiableSign sign = this.wrapSign((Sign)target.getState(), user);
            return Lists.newArrayList(new String[]{FormatUtil.unformatString(user, "essentials.editsign", sign.getLine(line - 1))});
         } else {
            return Collections.emptyList();
         }
      } else {
         return Collections.emptyList();
      }
   }

   private Commandeditsign.ModifiableSign wrapSign(final Sign sign, final User user) {
      if (VersionUtil.getServerBukkitVersion().isHigherThanOrEqualTo(VersionUtil.v1_20_1_R01)) {
         Vector eyeLocLessSign = user.getBase().getEyeLocation().toVector().subtract(sign.getLocation().add(0.5, 0.5, 0.5).toVector());
         BlockData signBlockData = sign.getBlockData();
         BlockFace signDirection;
         if (signBlockData instanceof org.bukkit.block.data.type.Sign) {
            signDirection = ((org.bukkit.block.data.type.Sign)signBlockData).getRotation();
         } else if (signBlockData instanceof WallSign) {
            signDirection = ((WallSign)signBlockData).getFacing();
         } else if (signBlockData instanceof HangingSign) {
            signDirection = ((HangingSign)signBlockData).getRotation();
         } else {
            if (!(signBlockData instanceof WallHangingSign)) {
               throw new IllegalStateException("Unknown block data for sign: " + signBlockData.getClass());
            }

            signDirection = ((WallHangingSign)signBlockData).getFacing();
         }

         final Side side = eyeLocLessSign.dot(signDirection.getDirection()) > 0.0 ? Side.FRONT : Side.BACK;
         return new Commandeditsign.ModifiableSign(sign) {
            @Override
            String[] getLines() {
               return this.sign.getSide(side).getLines();
            }

            @Override
            String getLine(int line) {
               return this.sign.getSide(side).getLine(line);
            }

            @Override
            void setLine(int line, String text) {
               this.sign.getSide(side).setLine(line, text);
            }

            @Override
            boolean isFront() {
               return side == Side.FRONT;
            }

            @Override
            boolean isWaxed() {
               return this.sign.isWaxed();
            }
         };
      } else {
         return new Commandeditsign.ModifiableSign(sign) {
            @Override
            String[] getLines() {
               return this.sign.getLines();
            }

            @Override
            String getLine(int line) {
               return this.sign.getLine(line);
            }

            @Override
            void setLine(int line, String text) {
               this.sign.setLine(line, text);
            }

            @Override
            boolean isFront() {
               return true;
            }

            @Override
            boolean isWaxed() {
               return false;
            }
         };
      }
   }

   private abstract static class ModifiableSign {
      protected final Sign sign;

      protected ModifiableSign(final Sign sign) {
         this.sign = sign;
      }

      abstract String getLine(int line);

      abstract String[] getLines();

      abstract void setLine(int line, String text);

      abstract boolean isFront();

      abstract boolean isWaxed();

      Block getBlock() {
         return this.sign.getBlock();
      }

      void update() {
         this.sign.update();
      }
   }
}
