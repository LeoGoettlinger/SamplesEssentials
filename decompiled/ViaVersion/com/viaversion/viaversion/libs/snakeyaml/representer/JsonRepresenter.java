package com.viaversion.viaversion.libs.snakeyaml.representer;

import com.viaversion.viaversion.libs.snakeyaml.DumperOptions;
import com.viaversion.viaversion.libs.snakeyaml.nodes.Node;
import com.viaversion.viaversion.libs.snakeyaml.nodes.Tag;
import java.util.Base64;
import java.util.Date;

public class JsonRepresenter extends Representer {
   public JsonRepresenter(DumperOptions options) {
      super(options);
      this.representers.put(byte[].class, new JsonRepresenter.RepresentByteArray());
      this.multiRepresenters.put(Date.class, new JsonRepresenter.RepresentDate());
      if (options.getDefaultScalarStyle() != DumperOptions.ScalarStyle.JSON_SCALAR_STYLE) {
         throw new IllegalStateException("JSON requires ScalarStyle.JSON_SCALAR_STYLE");
      } else if (options.getNonPrintableStyle() != DumperOptions.NonPrintableStyle.ESCAPE) {
         throw new IllegalStateException("JSON requires NonPrintableStyle.ESCAPE");
      }
   }

   protected class RepresentByteArray implements Represent {
      @Override
      public Node representData(Object data) {
         String binary = Base64.getEncoder().encodeToString((byte[])data);
         return JsonRepresenter.this.representScalar(Tag.STR, binary);
      }
   }

   protected class RepresentDate extends SafeRepresenter.RepresentDate {
      @Override
      public Tag getDefaultTag() {
         return Tag.STR;
      }
   }
}
