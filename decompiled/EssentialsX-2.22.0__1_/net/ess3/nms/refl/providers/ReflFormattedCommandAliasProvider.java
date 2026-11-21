package net.ess3.nms.refl.providers;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import net.ess3.nms.refl.ReflUtil;
import net.ess3.provider.FormattedCommandAliasProvider;
import net.essentialsx.providers.ProviderData;
import org.bukkit.command.CommandSender;
import org.bukkit.command.FormattedCommandAlias;

@ProviderData(
   description = "Reflection Formatted Command Alias Provider"
)
public class ReflFormattedCommandAliasProvider implements FormattedCommandAliasProvider {
   private final boolean senderArg;
   private final Field formatStringsField;
   private final MethodHandle buildCommandMethodHandle;

   public ReflFormattedCommandAliasProvider() {
      boolean senderArg = true;
      Field formatStringsField = null;
      MethodHandle buildCommandMethodHandle = null;

      try {
         Class<? extends FormattedCommandAlias> formattedCommandAliasClass = FormattedCommandAlias.class;
         formatStringsField = ReflUtil.getFieldCached(formattedCommandAliasClass, "formatStrings");
         Method buildCommandMethod = ReflUtil.getMethodCached(formattedCommandAliasClass, "buildCommand", CommandSender.class, String.class, String[].class);
         if (buildCommandMethod == null) {
            senderArg = false;
            buildCommandMethod = ReflUtil.getMethodCached(formattedCommandAliasClass, "buildCommand", String.class, String[].class);
         }

         if (buildCommandMethod == null) {
            throw new NoSuchMethodException("Could not find buildCommand method in FormattedCommandAlias");
         }

         buildCommandMethod.setAccessible(true);
         buildCommandMethodHandle = MethodHandles.lookup().unreflect(buildCommandMethod);
      } catch (Exception var9) {
         var9.printStackTrace();
      } finally {
         this.formatStringsField = formatStringsField;
         this.buildCommandMethodHandle = buildCommandMethodHandle;
         this.senderArg = senderArg;
      }
   }

   @Override
   public List<String> createCommands(FormattedCommandAlias command, CommandSender sender, String[] args) {
      List<String> commands = new ArrayList<>();

      for (String formatString : this.getFormatStrings(command)) {
         String cmd;
         try {
            cmd = this.buildCommand(command, sender, formatString, args);
         } catch (Throwable var11) {
            continue;
         }

         if (cmd != null) {
            commands.add(cmd.trim());
         }
      }

      return commands;
   }

   @Override
   public String[] getFormatStrings(FormattedCommandAlias command) {
      try {
         return (String[])this.formatStringsField.get(command);
      } catch (ReflectiveOperationException var3) {
         throw new RuntimeException(var3);
      }
   }

   @Override
   public String buildCommand(FormattedCommandAlias command, CommandSender sender, String formatString, String[] args) {
      try {
         return this.senderArg
            ? (String)this.buildCommandMethodHandle.invoke((FormattedCommandAlias)command, (CommandSender)sender, (String)formatString, (String[])args)
            : (String)this.buildCommandMethodHandle.invoke((FormattedCommandAlias)command, (String)formatString, (String[])args);
      } catch (Throwable var6) {
         throw new RuntimeException(var6);
      }
   }
}
