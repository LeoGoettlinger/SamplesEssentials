package com.earth2me.essentials.libs.kyori.adventure.text.minimessage.tag.standard;

import com.earth2me.essentials.libs.kyori.adventure.text.Component;
import com.earth2me.essentials.libs.kyori.adventure.text.ScoreComponent;
import com.earth2me.essentials.libs.kyori.adventure.text.minimessage.Context;
import com.earth2me.essentials.libs.kyori.adventure.text.minimessage.ParsingException;
import com.earth2me.essentials.libs.kyori.adventure.text.minimessage.internal.serializer.Emitable;
import com.earth2me.essentials.libs.kyori.adventure.text.minimessage.internal.serializer.SerializableResolver;
import com.earth2me.essentials.libs.kyori.adventure.text.minimessage.tag.Tag;
import com.earth2me.essentials.libs.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import com.earth2me.essentials.libs.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.Nullable;

final class ScoreTag {
   public static final String SCORE = "score";
   static final TagResolver RESOLVER = SerializableResolver.claimingComponent("score", ScoreTag::create, ScoreTag::emit);

   private ScoreTag() {
   }

   static Tag create(final ArgumentQueue args, final Context ctx) throws ParsingException {
      String name = args.popOr("A scoreboard member name is required").value();
      String objective = args.popOr("An objective name is required").value();
      return Tag.inserting((Component)Component.score(name, objective));
   }

   @Nullable
   static Emitable emit(final Component component) {
      if (!(component instanceof ScoreComponent)) {
         return null;
      } else {
         ScoreComponent score = (ScoreComponent)component;
         return emit -> emit.tag("score").argument(score.name()).argument(score.objective());
      }
   }
}
