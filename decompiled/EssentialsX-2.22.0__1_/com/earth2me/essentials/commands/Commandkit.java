package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.Kit;
import com.earth2me.essentials.User;
import com.earth2me.essentials.adventure.AdventureUtil;
import com.earth2me.essentials.utils.StringUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import net.ess3.api.TranslatableException;
import org.bukkit.Server;

public class Commandkit extends EssentialsCommand {
   public Commandkit() {
      super("kit");
   }

   @Override
   public void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
      if (args.length < 1) {
         String kitList = this.ess.getKits().listKits(this.ess, user);
         user.sendTl(kitList.length() > 0 ? "kits" : "noKits", AdventureUtil.parsed(kitList));
         throw new NoChargeException();
      } else {
         if (args.length > 1 && user.isAuthorized("essentials.kit.others")) {
            this.giveKits(this.getPlayer(server, user, args, 1), user, StringUtil.sanitizeString(args[0].toLowerCase(Locale.ENGLISH)).trim());
         } else {
            this.giveKits(user, user, StringUtil.sanitizeString(args[0].toLowerCase(Locale.ENGLISH)).trim());
         }
      }
   }

   @Override
   public void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
      if (args.length < 2) {
         String kitList = this.ess.getKits().listKits(this.ess, null);
         sender.sendTl(kitList.length() > 0 ? "kits" : "noKits", AdventureUtil.parsed(kitList));
         throw new NoChargeException();
      } else {
         User userTo = this.getPlayer(server, args, 1, true, false);

         for (String kitName : args[0].toLowerCase(Locale.ENGLISH).split(",")) {
            new Kit(kitName, this.ess).expandItems(userTo);
            sender.sendTl("kitGiveTo", kitName, userTo.getDisplayName());
            userTo.sendTl("kitReceive", kitName);
         }
      }
   }

   private void giveKits(final User userTo, final User userFrom, final String kitNames) throws Exception {
      if (kitNames.isEmpty()) {
         throw new TranslatableException("kitNotFound");
      } else {
         List<Kit> kits = new ArrayList<>();

         for (String kitName : kitNames.split(",")) {
            if (kitName.isEmpty()) {
               throw new TranslatableException("kitNotFound");
            }

            Kit kit = new Kit(kitName, this.ess);
            kit.checkPerms(userFrom);
            kit.checkDelay(userFrom);
            kit.checkAffordable(userFrom);
            kits.add(kit);
         }

         for (Kit kit : kits) {
            try {
               kit.checkDelay(userFrom);
               kit.checkAffordable(userFrom);
               if (kit.expandItems(userTo)) {
                  kit.setTime(userFrom);
                  kit.chargeUser(userTo);
                  if (!userFrom.equals(userTo)) {
                     userFrom.sendTl("kitGiveTo", kit.getName(), userTo.getDisplayName());
                  }

                  userTo.sendTl("kitReceive", kit.getName());
               }
            } catch (NoChargeException var10) {
               if (this.ess.getSettings().isDebug()) {
                  this.ess.getLogger().log(Level.INFO, "Soft kit error, abort spawning " + kit.getName(), (Throwable)var10);
               }
            } catch (Exception var11) {
               this.ess.showError(userFrom.getSource(), var11, "\\ kit: " + kit.getName());
            }
         }
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final User user, final String commandLabel, final String[] args) {
      if (args.length == 1) {
         List<String> options = new ArrayList<>();

         for (String kitName : this.ess.getKits().getKitKeys()) {
            if (user.isAuthorized("essentials.kits." + kitName)) {
               options.add(kitName);
            }
         }

         return options;
      } else {
         return args.length == 2 && user.isAuthorized("essentials.kit.others") ? this.getPlayers(user) : Collections.emptyList();
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String[] args) {
      if (args.length == 1) {
         return new ArrayList<>(this.ess.getKits().getKitKeys());
      } else {
         return args.length == 2 ? this.getPlayers(sender) : Collections.emptyList();
      }
   }
}
