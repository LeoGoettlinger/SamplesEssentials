package com.artillexstudios.axtrade.libs.lamp.bukkit.brigadier;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import java.lang.reflect.Field;
import java.util.function.Predicate;

final class NodeReflection {
   private static final Field command;
   private static final Field requirement;
   private static final Field customSuggestions;

   public static <T> void setCommand(CommandNode<T> node, Command<T> command) {
      try {
         NodeReflection.command.set(node, command);
      } catch (IllegalAccessException var3) {
         var3.printStackTrace();
      }
   }

   public static <T> void setRequirement(CommandNode<T> node, Predicate<T> requirement) {
      try {
         NodeReflection.requirement.set(node, requirement);
      } catch (IllegalAccessException var3) {
         var3.printStackTrace();
      }
   }

   public static <S, T> void setSuggestionProvider(ArgumentCommandNode<S, T> node, SuggestionProvider<S> provider) {
      try {
         customSuggestions.set(node, provider);
      } catch (IllegalAccessException var3) {
         var3.printStackTrace();
      }
   }

   static {
      try {
         command = CommandNode.class.getDeclaredField("command");
         command.setAccessible(true);
         requirement = CommandNode.class.getDeclaredField("requirement");
         requirement.setAccessible(true);
         customSuggestions = ArgumentCommandNode.class.getDeclaredField("customSuggestions");
         customSuggestions.setAccessible(true);
      } catch (NoSuchFieldException var1) {
         throw new RuntimeException(var1);
      }
   }
}
