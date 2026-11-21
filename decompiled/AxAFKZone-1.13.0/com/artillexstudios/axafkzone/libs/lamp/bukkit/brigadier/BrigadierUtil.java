package com.artillexstudios.axafkzone.libs.lamp.bukkit.brigadier;

import com.artillexstudios.axafkzone.libs.lamp.bukkit.Version;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

final class BrigadierUtil {
   private static final Field CHILDREN_FIELD;
   private static final Field LITERALS_FIELD;
   private static final Field ARGUMENTS_FIELD;
   private static final Method GET_BUKKIT_SENDER_METHOD;
   private static final Field[] CHILDREN_FIELDS;

   public static void removeChild(RootCommandNode root, String name) {
      try {
         for (Field field : CHILDREN_FIELDS) {
            Map<String, ?> children = (Map<String, ?>)field.get(root);
            children.remove(name);
         }
      } catch (ReflectiveOperationException var7) {
         throw new RuntimeException(var7);
      }
   }

   public static <S> LiteralCommandNode<S> renameLiteralNode(LiteralCommandNode<S> node, String newLiteral) {
      LiteralCommandNode<S> clone = new LiteralCommandNode(
         newLiteral, node.getCommand(), node.getRequirement(), node.getRedirect(), node.getRedirectModifier(), node.isFork()
      );

      for (CommandNode<S> child : node.getChildren()) {
         clone.addChild(child);
      }

      return clone;
   }

   @NotNull
   public static CommandSender getBukkitSender(Object commandSource) {
      Objects.requireNonNull(commandSource, "commandSource");

      try {
         return (CommandSender)GET_BUKKIT_SENDER_METHOD.invoke(commandSource);
      } catch (ReflectiveOperationException var2) {
         throw new RuntimeException(var2);
      }
   }

   static {
      try {
         Class<?> commandListenerWrapper;
         try {
            if (Version.getServerVersion().isNewerThan(Version.v1_16_5)) {
               commandListenerWrapper = BukkitVersion.findNmsClass("commands.CommandListenerWrapper");
            } else {
               commandListenerWrapper = BukkitVersion.findNmsClass("CommandListenerWrapper");
            }
         } catch (Exception var6) {
            try {
               commandListenerWrapper = Class.forName("net.minecraft.commands.CommandListenerWrapper");
            } catch (Exception var5) {
               commandListenerWrapper = Class.forName("net.minecraft.commands.CommandSourceStack");
            }
         }

         CHILDREN_FIELD = CommandNode.class.getDeclaredField("children");
         LITERALS_FIELD = CommandNode.class.getDeclaredField("literals");
         ARGUMENTS_FIELD = CommandNode.class.getDeclaredField("arguments");
         CHILDREN_FIELDS = new Field[]{CHILDREN_FIELD, LITERALS_FIELD, ARGUMENTS_FIELD};

         for (Field field : CHILDREN_FIELDS) {
            field.setAccessible(true);
         }

         GET_BUKKIT_SENDER_METHOD = commandListenerWrapper.getDeclaredMethod("getBukkitSender");
         GET_BUKKIT_SENDER_METHOD.setAccessible(true);
      } catch (ReflectiveOperationException var7) {
         throw new ExceptionInInitializerError(var7);
      }
   }
}
