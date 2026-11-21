package com.sk89q.worldedit.util.paste;

import com.fastasyncworldedit.core.configuration.Caption;
import com.sk89q.worldedit.command.util.AsyncCommandBuilder;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.util.task.Supervisor;
import java.net.URL;
import java.util.concurrent.Callable;

public final class ActorCallbackPaste {
   private static final Paster paster = new EngineHubPaste();

   private ActorCallbackPaste() {
   }

   @Deprecated
   public static void pastebin(Supervisor supervisor, final Actor sender, String content, final String successMessage) {
      Callable<URL> task = paster.paste(content);
      AsyncCommandBuilder.wrap(task, sender)
         .registerWithSupervisor(supervisor, "Submitting content to a pastebin service.")
         .sendMessageAfterDelay("(Please wait... sending output to pastebin...)")
         .onSuccess((String)null, url -> sender.print(String.format(successMessage, url)))
         .onFailure("Failed to submit paste", null)
         .buildAndExec(Pasters.getExecutor());
   }

   public static void pastebin(Supervisor supervisor, final Actor sender, String content, final TranslatableComponent.Builder successMessage) {
      Callable<URL> task = paster.paste(content);
      AsyncCommandBuilder.wrap(task, sender)
         .registerWithSupervisor(supervisor, "Submitting content to a pastebin service.")
         .setDelayMessage(Caption.of("worldedit.pastebin.uploading"))
         .onSuccess((String)null, url -> sender.printInfo(successMessage.args(TextComponent.of(url.toString())).build()))
         .onFailure("Failed to submit paste", null)
         .buildAndExec(Pasters.getExecutor());
   }

   public static void pastebin(
      Supervisor supervisor, final Actor sender, String content, PasteMetadata pasteMetadata, final TranslatableComponent.Builder successMessage
   ) {
      Callable<URL> task = paster.paste(content, pasteMetadata);
      AsyncCommandBuilder.wrap(task, sender)
         .registerWithSupervisor(supervisor, "Submitting content to a pastebin service.")
         .setDelayMessage(TranslatableComponent.of("worldedit.pastebin.uploading"))
         .onSuccess((String)null, url -> sender.printInfo(successMessage.args(TextComponent.of(url.toString())).build()))
         .onFailure("Failed to submit paste", null)
         .buildAndExec(Pasters.getExecutor());
   }
}
