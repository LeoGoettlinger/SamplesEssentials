package me.usainsrht.announcement.utils;

import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Modifying;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.minimessage.tree.Node;
import org.jetbrains.annotations.NotNull;

public class CenterTag implements Modifying {
   private static final String CENTER = "centered";
   static final TagResolver RESOLVER = TagResolver.resolver("centered", CenterTag::create);
   public static MiniMessage centerTagMM = MiniMessage.builder()
      .tags(TagResolver.builder().resolvers(new TagResolver[]{RESOLVER, StandardTags.defaults()}).build())
      .build();
   public char centerChar = ' ';
   public int length = 320;
   public boolean visited;

   static Tag create(ArgumentQueue args, Context ctx) {
      int length = 320;
      char centerChar = ' ';
      if (args.hasNext()) {
         length = Integer.parseInt(args.pop().value());
      }

      if (args.hasNext()) {
         centerChar = args.pop().value().charAt(0);
      }

      return new CenterTag(length, centerChar);
   }

   private CenterTag(int length, char centerChar) {
      this.length = length;
      this.centerChar = centerChar;
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.length, this.centerChar);
   }

   public Component apply(@NotNull Component current, int depth) {
      if (depth == 0) {
         current = CenterUtil.centeredComponent(current, this.length, this.centerChar);
      } else {
         current = Component.empty();
      }

      return current;
   }

   public final void visit(@NotNull Node current, int depth) {
   }

   public final void postVisit() {
      this.visited = true;
   }
}
