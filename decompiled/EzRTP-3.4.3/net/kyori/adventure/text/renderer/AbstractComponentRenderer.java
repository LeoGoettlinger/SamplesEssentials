package net.kyori.adventure.text.renderer;

import java.lang.runtime.SwitchBootstraps;
import java.util.Objects;
import net.kyori.adventure.text.BlockNBTComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.EntityNBTComponent;
import net.kyori.adventure.text.KeybindComponent;
import net.kyori.adventure.text.ObjectComponent;
import net.kyori.adventure.text.ScoreComponent;
import net.kyori.adventure.text.SelectorComponent;
import net.kyori.adventure.text.StorageNBTComponent;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.VirtualComponent;

public abstract class AbstractComponentRenderer<C> implements ComponentRenderer<C> {
   @Override
   public Component render(final Component component, final C context) {
      return this.render(component, context, false);
   }

   protected final Component render(final Component component, final C context, final boolean skipVirtual) {
      Objects.requireNonNull(component);
      Component var4 = component;
      byte var5 = 0;

      while (true) {
         Component var10000;
         switch (SwitchBootstraps.typeSwitch<"typeSwitch",TranslatableComponent,KeybindComponent,ScoreComponent,SelectorComponent,VirtualComponent,TextComponent,BlockNBTComponent,EntityNBTComponent,StorageNBTComponent,ObjectComponent>(
            var4, var5
         )) {
            case 0: {
               TranslatableComponent tc = (TranslatableComponent)var4;
               var10000 = this.renderTranslatable(tc, context);
               break;
            }
            case 1:
               KeybindComponent kc = (KeybindComponent)var4;
               var10000 = this.renderKeybind(kc, context);
               break;
            case 2: {
               ScoreComponent sc = (ScoreComponent)var4;
               var10000 = this.renderScore(sc, context);
               break;
            }
            case 3: {
               SelectorComponent sc = (SelectorComponent)var4;
               var10000 = this.renderSelector(sc, context);
               break;
            }
            case 4:
               VirtualComponent vc = (VirtualComponent)var4;
               if (skipVirtual) {
                  var5 = 5;
                  continue;
               }

               var10000 = this.render(this.renderVirtual(vc, context), context, true);
               break;
            case 5: {
               TextComponent tc = (TextComponent)var4;
               var10000 = this.renderText(tc, context);
               break;
            }
            case 6:
               BlockNBTComponent bnc = (BlockNBTComponent)var4;
               var10000 = this.renderBlockNbt(bnc, context);
               break;
            case 7:
               EntityNBTComponent enc = (EntityNBTComponent)var4;
               var10000 = this.renderEntityNbt(enc, context);
               break;
            case 8:
               StorageNBTComponent snc = (StorageNBTComponent)var4;
               var10000 = this.renderStorageNbt(snc, context);
               break;
            case 9:
               ObjectComponent oc = (ObjectComponent)var4;
               var10000 = this.renderObject(oc, context);
               break;
            default:
               throw new MatchException(null, null);
         }

         return var10000;
      }
   }

   protected abstract Component renderObject(final ObjectComponent component, final C context);

   protected abstract Component renderBlockNbt(final BlockNBTComponent component, final C context);

   protected abstract Component renderEntityNbt(final EntityNBTComponent component, final C context);

   protected abstract Component renderStorageNbt(final StorageNBTComponent component, final C context);

   protected abstract Component renderKeybind(final KeybindComponent component, final C context);

   protected abstract Component renderScore(final ScoreComponent component, final C context);

   protected abstract Component renderSelector(final SelectorComponent component, final C context);

   protected abstract Component renderText(final TextComponent component, final C context);

   protected Component renderVirtual(final VirtualComponent component, final C context) {
      return component;
   }

   protected abstract Component renderTranslatable(final TranslatableComponent component, final C context);
}
