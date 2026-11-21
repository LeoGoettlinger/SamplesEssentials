package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.User;
import com.earth2me.essentials.adventure.AdventureUtil;
import com.earth2me.essentials.utils.FloatUtil;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.bukkit.Server;
import org.bukkit.entity.Player;

public class Commandspeed extends EssentialsCommand {
   private static final List<String> types = Arrays.asList("walk", "fly", "1", "1.5", "1.75", "2");
   private static final List<String> speeds = Arrays.asList("1", "1.5", "1.75", "2");

   public Commandspeed() {
      super("speed");
   }

   @Override
   protected void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
      if (args.length < 2) {
         throw new NotEnoughArgumentsException();
      } else {
         this.speedOtherPlayers(server, sender, this.isFlyMode(args[0]), true, this.getMoveSpeed(args[1]), args[2]);
      }
   }

   @Override
   protected void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         boolean isBypass = user.isAuthorized("essentials.speed.bypass");
         boolean isFly;
         float speed;
         if (args.length == 1) {
            boolean inferredFly = this.isFlyAlias(commandLabel) ? true : (this.isWalkAlias(commandLabel) ? false : user.getBase().isFlying());
            isFly = this.flyPermCheck(user, inferredFly);
            speed = this.getMoveSpeed(args[0]);
         } else {
            isFly = this.flyPermCheck(user, this.isFlyMode(args[0]));
            speed = this.getMoveSpeed(args[1]);
            if (args.length > 2 && user.isAuthorized("essentials.speed.others")) {
               if (args[2].trim().length() < 2) {
                  throw new PlayerNotFoundException();
               }

               this.speedOtherPlayers(server, user.getSource(), isFly, isBypass, speed, args[2]);
               return;
            }
         }

         if (isFly) {
            user.getBase().setFlySpeed(this.getRealMoveSpeed(speed, true, isBypass));
            user.sendTl("moveSpeed", AdventureUtil.parsed(user.playerTl("flying")), speed, user.getDisplayName());
         } else {
            user.getBase().setWalkSpeed(this.getRealMoveSpeed(speed, false, isBypass));
            user.sendTl("moveSpeed", AdventureUtil.parsed(user.playerTl("walking")), speed, user.getDisplayName());
         }
      }
   }

   private void speedOtherPlayers(
      final Server server, final CommandSource sender, final boolean isFly, final boolean isBypass, final float speed, final String name
   ) throws PlayerNotFoundException {
      boolean skipHidden = sender.isPlayer() && !this.ess.getUser(sender.getPlayer()).canInteractVanished();
      boolean foundUser = false;

      for (Player matchPlayer : server.matchPlayer(name)) {
         User player = this.ess.getUser(matchPlayer);
         if (!skipHidden || !player.isHidden(sender.getPlayer()) || !player.isHiddenFrom(sender.getPlayer())) {
            foundUser = true;
            if (isFly) {
               matchPlayer.setFlySpeed(this.getRealMoveSpeed(speed, true, isBypass));
               sender.sendTl("moveSpeed", AdventureUtil.parsed(sender.tl("flying")), speed, matchPlayer.getDisplayName());
            } else {
               matchPlayer.setWalkSpeed(this.getRealMoveSpeed(speed, false, isBypass));
               sender.sendTl("moveSpeed", AdventureUtil.parsed(sender.tl("walking")), speed, matchPlayer.getDisplayName());
            }
         }
      }

      if (!foundUser) {
         throw new PlayerNotFoundException();
      }
   }

   private Boolean flyPermCheck(final User user, final boolean input) {
      boolean canFly = user.isAuthorized("essentials.speed.fly");
      boolean canWalk = user.isAuthorized("essentials.speed.walk");
      return (!input || !canFly) && (input || !canWalk) && (canFly || canWalk) ? !canWalk : input;
   }

   private boolean isFlyAlias(final String label) {
      return label.contains("fly") || label.equalsIgnoreCase("fspeed") || label.equalsIgnoreCase("efspeed");
   }

   private boolean isWalkAlias(final String label) {
      return label.contains("walk") || label.equalsIgnoreCase("wspeed") || label.equalsIgnoreCase("ewspeed");
   }

   private boolean isFlyMode(final String modeString) throws NotEnoughArgumentsException {
      boolean isFlyMode;
      if (!modeString.contains("fly") && !modeString.equalsIgnoreCase("f")) {
         if (!modeString.contains("walk") && !modeString.contains("run") && !modeString.equalsIgnoreCase("w") && !modeString.equalsIgnoreCase("r")) {
            throw new NotEnoughArgumentsException();
         }

         isFlyMode = false;
      } else {
         isFlyMode = true;
      }

      return isFlyMode;
   }

   private float getMoveSpeed(final String moveSpeed) throws NotEnoughArgumentsException {
      try {
         float userSpeed = FloatUtil.parseFloat(moveSpeed);
         if (userSpeed > 10.0F) {
            userSpeed = 10.0F;
         } else if (userSpeed < 1.0E-4F) {
            userSpeed = 1.0E-4F;
         }

         return userSpeed;
      } catch (NumberFormatException var4) {
         throw new NotEnoughArgumentsException();
      }
   }

   private float getRealMoveSpeed(final float userSpeed, final boolean isFly, final boolean isBypass) {
      float defaultSpeed = isFly ? 0.1F : 0.2F;
      float maxSpeed = 1.0F;
      if (!isBypass) {
         maxSpeed = (float)(isFly ? this.ess.getSettings().getMaxFlySpeed() : this.ess.getSettings().getMaxWalkSpeed());
      }

      if (userSpeed < 1.0F) {
         return defaultSpeed * userSpeed;
      } else {
         float ratio = (userSpeed - 1.0F) / 9.0F * (maxSpeed - defaultSpeed);
         return ratio + defaultSpeed;
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String[] args) {
      if (args.length == 1) {
         return types;
      } else if (args.length == 2) {
         return speeds;
      } else {
         return args.length == 3 && sender.isAuthorized("essentials.speed.others") ? this.getPlayers(sender) : Collections.emptyList();
      }
   }
}
