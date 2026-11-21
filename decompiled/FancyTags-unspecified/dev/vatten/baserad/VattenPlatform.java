package dev.vatten.baserad;

import dev.vatten.baserad.commands.Command;

public interface VattenPlatform<P, T> {
   ScheduledTask<T> scheduleTask(Runnable var1, int var2);

   ScheduledTask<T> scheduleRepeatingTask(Runnable var1, int var2, int var3);

   void registerCommand(Command var1);

   VattenPlayer wrapPlayer(P var1);
}
