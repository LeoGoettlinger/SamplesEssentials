package net.kyori.adventure.text.event;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.renderer.ComponentRenderer;

final class HoverEventRenderers {
   static final HoverEventRenderers.ShowText SHOW_TEXT = new HoverEventRenderers.ShowText();
   static final HoverEventRenderers.ShowItem SHOW_ITEM = new HoverEventRenderers.ShowItem();
   static final HoverEventRenderers.ShowEntity SHOW_ENTITY = new HoverEventRenderers.ShowEntity();
   static final HoverEventRenderers.ShowAchievement SHOW_ACHIEVEMENT = new HoverEventRenderers.ShowAchievement();

   private HoverEventRenderers() {
   }

   static final class ShowAchievement implements HoverEvent.Action.Renderer<String> {
      private ShowAchievement() {
      }

      public <C> String render(final ComponentRenderer<C> renderer, final C context, final String value) {
         return value;
      }
   }

   static final class ShowEntity implements HoverEvent.Action.Renderer<HoverEvent.ShowEntity> {
      private ShowEntity() {
      }

      public <C> HoverEvent.ShowEntity render(final ComponentRenderer<C> renderer, final C context, final HoverEvent.ShowEntity value) {
         Component name = value.name();
         return name == null ? value : value.name(renderer.render(name, context));
      }
   }

   static final class ShowItem implements HoverEvent.Action.Renderer<HoverEvent.ShowItem> {
      private ShowItem() {
      }

      public <C> HoverEvent.ShowItem render(final ComponentRenderer<C> renderer, final C context, final HoverEvent.ShowItem value) {
         return value;
      }
   }

   static final class ShowText implements HoverEvent.Action.Renderer<Component> {
      private ShowText() {
      }

      public <C> Component render(final ComponentRenderer<C> renderer, final C context, final Component value) {
         return renderer.render(value, context);
      }
   }
}
