package net.coreprotect.listener.player.inspector;

import java.sql.Connection;
import java.sql.Statement;
import net.coreprotect.database.lookup.SignMessageLookup;
import net.coreprotect.utility.Chat;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class SignInspector extends BaseInspector {
   public void performSignLookup(final Player player, final Location location) {
      class BasicThread implements Runnable {
         @Override
         public void run() {
            try {
               SignInspector.this.checkPreconditions(player);

               try (Connection connection = SignInspector.this.getDatabaseConnection(player)) {
                  Statement statement = connection.createStatement();

                  for (String signMessage : SignMessageLookup.performLookup(null, statement, location, player, 1, 7)) {
                     String bypass = null;
                     if (signMessage.contains("\n")) {
                        String[] split = signMessage.split("\n");
                        signMessage = split[0];
                        bypass = split[1];
                     }

                     if (signMessage.length() > 0) {
                        Chat.sendComponent(player, signMessage, bypass);
                     }
                  }

                  statement.close();
               }
            } catch (BaseInspector.InspectionException var16) {
               Chat.sendMessage(player, var16.getMessage());
            } catch (Exception var17) {
               var17.printStackTrace();
            } finally {
               SignInspector.this.finishInspection(player);
            }
         }
      }

      Runnable runnable = new BasicThread();
      Thread thread = new Thread(runnable);
      thread.start();
   }
}
