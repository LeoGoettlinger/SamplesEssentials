package me.usainsrht.ujobs.yaml;

import java.util.List;
import lombok.Generated;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class YamlCommand {
   @NotNull
   private String name;
   private String description;
   private String usage;
   private List<String> aliases;
   @Nullable
   private String permission;
   @Nullable
   private YamlMessage permissionMessage;

   @Generated
   YamlCommand(
      @NotNull String name, String description, String usage, List<String> aliases, @Nullable String permission, @Nullable YamlMessage permissionMessage
   ) {
      if (name == null) {
         throw new NullPointerException("name is marked non-null but is null");
      } else {
         this.name = name;
         this.description = description;
         this.usage = usage;
         this.aliases = aliases;
         this.permission = permission;
         this.permissionMessage = permissionMessage;
      }
   }

   @Generated
   public static YamlCommand.YamlCommandBuilder builder() {
      return new YamlCommand.YamlCommandBuilder();
   }

   @NotNull
   @Generated
   public String getName() {
      return this.name;
   }

   @Generated
   public String getDescription() {
      return this.description;
   }

   @Generated
   public String getUsage() {
      return this.usage;
   }

   @Generated
   public List<String> getAliases() {
      return this.aliases;
   }

   @Nullable
   @Generated
   public String getPermission() {
      return this.permission;
   }

   @Nullable
   @Generated
   public YamlMessage getPermissionMessage() {
      return this.permissionMessage;
   }

   @Generated
   public static class YamlCommandBuilder {
      @Generated
      private String name;
      @Generated
      private String description;
      @Generated
      private String usage;
      @Generated
      private List<String> aliases;
      @Generated
      private String permission;
      @Generated
      private YamlMessage permissionMessage;

      @Generated
      YamlCommandBuilder() {
      }

      @Generated
      public YamlCommand.YamlCommandBuilder name(@NotNull String name) {
         if (name == null) {
            throw new NullPointerException("name is marked non-null but is null");
         } else {
            this.name = name;
            return this;
         }
      }

      @Generated
      public YamlCommand.YamlCommandBuilder description(String description) {
         this.description = description;
         return this;
      }

      @Generated
      public YamlCommand.YamlCommandBuilder usage(String usage) {
         this.usage = usage;
         return this;
      }

      @Generated
      public YamlCommand.YamlCommandBuilder aliases(List<String> aliases) {
         this.aliases = aliases;
         return this;
      }

      @Generated
      public YamlCommand.YamlCommandBuilder permission(@Nullable String permission) {
         this.permission = permission;
         return this;
      }

      @Generated
      public YamlCommand.YamlCommandBuilder permissionMessage(@Nullable YamlMessage permissionMessage) {
         this.permissionMessage = permissionMessage;
         return this;
      }

      @Generated
      public YamlCommand build() {
         return new YamlCommand(this.name, this.description, this.usage, this.aliases, this.permission, this.permissionMessage);
      }

      @Generated
      @Override
      public String toString() {
         return "YamlCommand.YamlCommandBuilder(name="
            + this.name
            + ", description="
            + this.description
            + ", usage="
            + this.usage
            + ", aliases="
            + this.aliases
            + ", permission="
            + this.permission
            + ", permissionMessage="
            + this.permissionMessage
            + ")";
      }
   }
}
