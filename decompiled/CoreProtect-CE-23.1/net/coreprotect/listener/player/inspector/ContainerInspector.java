package net.coreprotect.listener.player.inspector;

import java.sql.Connection;
import java.sql.Statement;
import net.coreprotect.database.lookup.ChestTransactionLookup;
import net.coreprotect.utility.Chat;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class ContainerInspector extends BaseInspector {
   public void performContainerLookup(final Player player, final Location finalLocation) {
      class BasicThread implements Runnable {
         @Override
         public void run() {
            try {
               ContainerInspector.this.checkPreconditions(player);

               try (Connection connection = ContainerInspector.this.getDatabaseConnection(player)) {
                  Statement statement = connection.createStatement();

                  for (String data : ChestTransactionLookup.performLookup(null, statement, finalLocation, player, 1, 7, false)) {
                     Chat.sendComponent(player, data);
                  }

                  statement.close();
               }
            } catch (BaseInspector.InspectionException var14) {
               Chat.sendMessage(player, var14.getMessage());
            } catch (Exception var15) {
               var15.printStackTrace();
            } finally {
               ContainerInspector.this.finishInspection(player);
            }
         }
      }

      Runnable runnable = new BasicThread();
      Thread thread = new Thread(runnable);
      thread.start();
   }
}
