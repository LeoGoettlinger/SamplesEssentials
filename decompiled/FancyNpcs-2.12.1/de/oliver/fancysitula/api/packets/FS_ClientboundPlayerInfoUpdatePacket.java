package de.oliver.fancysitula.api.packets;

import de.oliver.fancysitula.api.utils.FS_GameProfile;
import de.oliver.fancysitula.api.utils.FS_GameType;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.kyori.adventure.text.Component;

public abstract class FS_ClientboundPlayerInfoUpdatePacket extends FS_ClientboundPacket {
   protected EnumSet<FS_ClientboundPlayerInfoUpdatePacket.Action> actions;
   protected List<FS_ClientboundPlayerInfoUpdatePacket.Entry> entries;

   public FS_ClientboundPlayerInfoUpdatePacket(
      EnumSet<FS_ClientboundPlayerInfoUpdatePacket.Action> actions, List<FS_ClientboundPlayerInfoUpdatePacket.Entry> entries
   ) {
      this.actions = actions;
      this.entries = entries;
   }

   public EnumSet<FS_ClientboundPlayerInfoUpdatePacket.Action> getActions() {
      return this.actions;
   }

   public void setActions(EnumSet<FS_ClientboundPlayerInfoUpdatePacket.Action> actions) {
      this.actions = actions;
   }

   public List<FS_ClientboundPlayerInfoUpdatePacket.Entry> getEntries() {
      return this.entries;
   }

   public void setEntries(List<FS_ClientboundPlayerInfoUpdatePacket.Entry> entries) {
      this.entries = entries;
   }

   public static enum Action {
      ADD_PLAYER,
      INITIALIZE_CHAT,
      UPDATE_GAME_MODE,
      UPDATE_LISTED,
      UPDATE_LATENCY,
      UPDATE_DISPLAY_NAME;
   }

   public record Entry(UUID uuid, FS_GameProfile profile, boolean listed, int latency, FS_GameType gameMode, @Nullable Component displayName) {
   }
}
