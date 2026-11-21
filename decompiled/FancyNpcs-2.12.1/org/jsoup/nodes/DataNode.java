package org.jsoup.nodes;

import java.io.IOException;

public class DataNode extends LeafNode {
   public DataNode(String data) {
      this.value = data;
   }

   @Override
   public String nodeName() {
      return "#data";
   }

   public String getWholeData() {
      return this.coreValue();
   }

   public DataNode setWholeData(String data) {
      this.coreValue(data);
      return this;
   }

   @Override
   void outerHtmlHead(Appendable accum, int depth, Document.OutputSettings out) throws IOException {
      String data = this.getWholeData();
      if (out.syntax() != Document.OutputSettings.Syntax.xml || data.contains("<![CDATA[")) {
         accum.append(this.getWholeData());
      } else if (this.parentNameIs("script")) {
         accum.append("//<![CDATA[\n").append(data).append("\n//]]>");
      } else if (this.parentNameIs("style")) {
         accum.append("/*<![CDATA[*/\n").append(data).append("\n/*]]>*/");
      } else {
         accum.append("<![CDATA[").append(data).append("]]>");
      }
   }

   @Override
   void outerHtmlTail(Appendable accum, int depth, Document.OutputSettings out) {
   }

   @Override
   public String toString() {
      return this.outerHtml();
   }

   public DataNode clone() {
      return (DataNode)super.clone();
   }
}
