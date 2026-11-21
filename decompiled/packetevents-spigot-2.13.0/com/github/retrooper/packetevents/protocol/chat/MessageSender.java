package com.github.retrooper.packetevents.protocol.chat;

import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MessageSender {
   @NotNull
   private UUID uuid;
   @Nullable
   private Component displayName;
   @Nullable
   private Component teamName;

   public MessageSender(@NotNull UUID uuid, @Nullable Component displayName, @Nullable Component teamName) {
      this.uuid = uuid;
      this.displayName = displayName;
      this.teamName = teamName;
   }

   public MessageSender(@Nullable Component displayName, @Nullable Component teamName) {
      this(new UUID(0L, 0L), displayName, teamName);
   }

   public MessageSender() {
      this(null, null);
   }

   public UUID getUUID() {
      return this.uuid;
   }

   public void setUUID(@NotNull UUID uuid) {
      this.uuid = uuid;
   }

   @Nullable
   public Component getDisplayName() {
      return this.displayName;
   }

   public void setDisplayName(@Nullable Component displayName) {
      this.displayName = displayName;
   }

   @Nullable
   public Component getTeamName() {
      return this.teamName;
   }

   public void setTeamName(@Nullable Component teamName) {
      this.teamName = teamName;
   }
}
