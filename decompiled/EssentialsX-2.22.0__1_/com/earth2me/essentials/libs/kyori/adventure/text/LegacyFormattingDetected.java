package com.earth2me.essentials.libs.kyori.adventure.text;

import com.earth2me.essentials.libs.kyori.adventure.util.Nag;

final class LegacyFormattingDetected extends Nag {
   private static final long serialVersionUID = -947793022628807411L;

   LegacyFormattingDetected(final Component component) {
      super(
         "Legacy formatting codes have been detected in a component - this is unsupported behaviour. Please refer to the Adventure documentation (https://docs.papermc.io/adventure/) for more information. Component: "
            + component
      );
   }
}
