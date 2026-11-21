package org.jsoup.helper;

import java.io.UnsupportedEncodingException;
import java.net.IDN;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import org.jsoup.Connection;
import org.jsoup.internal.StringUtil;
import org.jspecify.annotations.Nullable;

final class UrlBuilder {
   URL u;
   @Nullable
   StringBuilder q;

   UrlBuilder(URL inputUrl) {
      this.u = inputUrl;
      if (this.u.getQuery() != null) {
         this.q = StringUtil.borrowBuilder().append(this.u.getQuery());
      }
   }

   URL build() {
      try {
         URI uri = new URI(this.u.getProtocol(), this.u.getUserInfo(), IDN.toASCII(decodePart(this.u.getHost())), this.u.getPort(), null, null, null);
         StringBuilder normUrl = StringUtil.borrowBuilder().append(uri.toASCIIString());
         appendToAscii(this.u.getPath(), false, normUrl);
         if (this.q != null) {
            normUrl.append('?');
            appendToAscii(StringUtil.releaseBuilder(this.q), true, normUrl);
         }

         if (this.u.getRef() != null) {
            normUrl.append('#');
            appendToAscii(this.u.getRef(), false, normUrl);
         }

         this.u = new URL(StringUtil.releaseBuilder(normUrl));
         return this.u;
      } catch (URISyntaxException | UnsupportedEncodingException | MalformedURLException var3) {
         assert Validate.assertFail(var3.toString());

         return this.u;
      }
   }

   void appendKeyVal(Connection.KeyVal kv) throws UnsupportedEncodingException {
      if (this.q == null) {
         this.q = StringUtil.borrowBuilder();
      } else {
         this.q.append('&');
      }

      this.q.append(URLEncoder.encode(kv.key(), DataUtil.UTF_8.name())).append('=').append(URLEncoder.encode(kv.value(), DataUtil.UTF_8.name()));
   }

   private static String decodePart(String encoded) {
      try {
         return URLDecoder.decode(encoded, DataUtil.UTF_8.name());
      } catch (UnsupportedEncodingException var2) {
         throw new RuntimeException(var2);
      }
   }

   private static void appendToAscii(String s, boolean spaceAsPlus, StringBuilder sb) throws UnsupportedEncodingException {
      for (int i = 0; i < s.length(); i++) {
         int c = s.codePointAt(i);
         if (c == 32) {
            sb.append(spaceAsPlus ? '+' : "%20");
         } else if (c > 127) {
            sb.append(URLEncoder.encode(new String(Character.toChars(c)), DataUtil.UTF_8.name()));
            if (Character.charCount(c) == 2) {
               i++;
            }
         } else {
            sb.append((char)c);
         }
      }
   }
}
