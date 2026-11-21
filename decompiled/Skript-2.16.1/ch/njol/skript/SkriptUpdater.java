package ch.njol.skript;

import ch.njol.skript.localization.ArgsMessage;
import ch.njol.skript.localization.Message;
import ch.njol.skript.update.ReleaseManifest;
import ch.njol.skript.update.ReleaseStatus;
import ch.njol.skript.update.UpdateManifest;
import ch.njol.skript.update.Updater;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.StringConcatFactory;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import org.bukkit.command.CommandSender;
import org.skriptlang.skript.bukkit.text.TextComponentParser;

public class SkriptUpdater extends Updater {
   public static final Message m_not_started = new Message("updater.not started");
   public static final Message m_checking = new Message("updater.checking");
   public static final Message m_check_in_progress = new Message("updater.check in progress");
   public static final Message m_updater_disabled = new Message("updater.updater disabled");
   public static final ArgsMessage m_check_error = new ArgsMessage("updater.check error");
   public static final Message m_running_latest_version = new Message("updater.running latest version");
   public static final Message m_running_latest_version_beta = new Message("updater.running latest version (beta)");
   public static final ArgsMessage m_update_available = new ArgsMessage("updater.update available");
   public static final ArgsMessage m_downloading = new ArgsMessage("updater.downloading");
   public static final Message m_download_in_progress = new Message("updater.download in progress");
   public static final ArgsMessage m_download_error = new ArgsMessage("updater.download error");
   public static final ArgsMessage m_downloaded = new ArgsMessage("updater.downloaded");
   public static final Message m_internal_error = new Message("updater.internal error");
   public static final Message m_custom_version = new Message("updater.custom version");
   public static final Message m_nightly = new Message("updater.nightly build");

   SkriptUpdater() {
      super(loadManifest());
   }

   private static ReleaseManifest loadManifest() {
      String manifest;
      try (
         InputStream is = Skript.getInstance().getResource("release-manifest.json");
         Scanner s = new Scanner(is);
      ) {
         s.useDelimiter("\\\\A");
         manifest = s.next();
      } catch (IOException var9) {
         throw new IllegalStateException("Skript is missing release-manifest.json!");
      }

      assert manifest != null;

      return ReleaseManifest.load(manifest);
   }

   public CompletableFuture<Void> updateCheck(CommandSender sender) {
      CompletableFuture<Void> future = this.checkUpdates()
         .thenAccept(
            none -> {
               ReleaseStatus status = this.getReleaseStatus();
               switch (status) {
                  case CUSTOM:
                     Skript.info(sender, m_custom_version + "");
                     break;
                  case DEVELOPMENT:
                     Skript.info(sender, m_nightly + "");
                     break;
                  case LATEST:
                     Skript.info(sender, m_running_latest_version + "");
                     break;
                  case OUTDATED:
                     UpdateManifest update = this.getUpdateManifest();

                     assert update != null;

                     Skript.info(
                        sender,
                        StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(
                           m_update_available.toString(update.id, Skript.getVersion())
                        )
                     );
                     sender.sendMessage(
                        TextComponentParser.instance()
                           .parse("Download it at: <aqua><underlined><click:open_url:" + update.downloadUrl + ">" + update.downloadUrl)
                     );
                     break;
                  case UNKNOWN:
                     if (this.isEnabled()) {
                        Skript.error(sender, m_internal_error + "");
                     } else {
                        Skript.info(sender, m_updater_disabled + "");
                     }
               }
            }
         );

      assert future != null;

      return future;
   }

   public CompletableFuture<Void> changesCheck(CommandSender sender) {
      CompletableFuture<Void> future = this.updateCheck(sender)
         .thenAccept(
            none -> {
               if (this.getReleaseStatus() == ReleaseStatus.OUTDATED) {
                  UpdateManifest update = this.getUpdateManifest();
                  if (update != null) {
                     sender.sendMessage("");
                     Skript.info(sender, "Patch notes:");

                     for (String line : update.patchNotes.split("\\n")) {
                        line = line.replace("\r", "");
                        String processed = line;

                        for (int start = line.indexOf(35); start != -1; start = line.indexOf(35, start + 1)) {
                           StringBuilder issue = new StringBuilder();
                           int i = start + 1;

                           while (i < line.length()) {
                              int c = line.codePointAt(i);
                              if (!Character.isDigit(c)) {
                                 break;
                              }

                              issue.appendCodePoint(c);
                              i += Character.charCount(c);
                           }

                           if (issue.length() > 0) {
                              processed = processed.replace(
                                 "#" + issue, "<aqua><underlined><click:open_url:https://github.com/SkriptLang/Skript/issues/" + issue + ">#" + issue + "<r>"
                              );
                           }
                        }

                        assert processed != null;

                        sender.sendMessage(TextComponentParser.instance().parse(processed));
                     }
                  }
               }
            }
         );

      assert future != null;

      return future;
   }
}
