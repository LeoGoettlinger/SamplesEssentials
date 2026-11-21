package com.earth2me.essentials.commands;

import com.earth2me.essentials.Backup;
import com.earth2me.essentials.CommandSource;
import net.ess3.api.TranslatableException;
import org.bukkit.Server;

public class Commandbackup extends EssentialsCommand {
   public Commandbackup() {
      super("backup");
   }

   @Override
   protected void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
      Backup backup = this.ess.getBackup();
      if (backup == null) {
         throw new TranslatableException("backupDisabled");
      } else {
         String command = this.ess.getSettings().getBackupCommand();
         if (command != null && !"".equals(command) && !"save-all".equalsIgnoreCase(command)) {
            backup.run();
            sender.sendTl("backupStarted");
         } else {
            throw new TranslatableException("backupDisabled");
         }
      }
   }
}
