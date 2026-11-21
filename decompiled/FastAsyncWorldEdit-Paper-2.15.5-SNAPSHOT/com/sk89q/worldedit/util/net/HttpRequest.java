package com.sk89q.worldedit.util.net;

import com.google.common.base.Joiner;
import com.google.common.base.Joiner.MapJoiner;
import com.google.common.collect.Maps;
import com.google.common.net.UrlEscapers;
import com.sk89q.worldedit.util.io.Closer;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;

public class HttpRequest implements Closeable {
   private static final int CONNECT_TIMEOUT = 5000;
   private static final int READ_TIMEOUT = 5000;
   private static final int READ_BUFFER_SIZE = 8192;
   private final Map<String, String> headers = new HashMap<>();
   private final String method;
   private final URL url;
   private String contentType;
   private byte[] body;
   private HttpURLConnection conn;
   private InputStream inputStream;
   private long contentLength = -1L;
   private long readBytes = 0L;

   private HttpRequest(String method, URL url) {
      this.method = method;
      this.url = url;
   }

   public HttpRequest body(String data) {
      this.body = data.getBytes();
      return this;
   }

   public HttpRequest bodyUrlEncodedForm(HttpRequest.Form form) {
      this.contentType = "application/x-www-form-urlencoded";
      this.body = form.toUrlEncodedString().getBytes(StandardCharsets.UTF_8);
      return this;
   }

   public HttpRequest bodyMultipartForm(HttpRequest.Form form) {
      this.contentType = "multipart/form-data;boundary=" + form.getFormDataSeparator();
      this.body = form.toFormDataString().getBytes(StandardCharsets.UTF_8);
      return this;
   }

   public HttpRequest header(String key, String value) {
      if (key.equalsIgnoreCase("Content-Type")) {
         this.contentType = value;
      } else {
         this.headers.put(key, value);
      }

      return this;
   }

   public HttpRequest execute() throws IOException {
      boolean successful = false;

      try {
         if (this.conn != null) {
            throw new IllegalArgumentException("Connection already executed");
         }

         this.conn = (HttpURLConnection)reformat(this.url).openConnection();
         this.conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Java)");
         if (this.body != null) {
            this.conn.setRequestProperty("Content-Type", this.contentType);
            this.conn.setRequestProperty("Content-Length", Integer.toString(this.body.length));
            this.conn.setDoInput(true);
         }

         for (Entry<String, String> entry : this.headers.entrySet()) {
            this.conn.setRequestProperty(entry.getKey(), entry.getValue());
         }

         this.conn.setRequestMethod(this.method);
         this.conn.setUseCaches(false);
         this.conn.setDoOutput(true);
         this.conn.setConnectTimeout(5000);
         this.conn.setReadTimeout(5000);
         this.conn.connect();
         if (this.body != null) {
            DataOutputStream out = new DataOutputStream(this.conn.getOutputStream());
            out.write(this.body);
            out.flush();
            out.close();
         }

         this.inputStream = this.conn.getResponseCode() == 200 ? this.conn.getInputStream() : this.conn.getErrorStream();
         successful = true;
      } finally {
         if (!successful) {
            this.close();
         }
      }

      return this;
   }

   public HttpRequest expectResponseCode(int... codes) throws IOException {
      int responseCode = this.getResponseCode();

      for (int code : codes) {
         if (code == responseCode) {
            return this;
         }
      }

      this.close();
      throw new IOException("Did not get expected response code, got " + responseCode + " for " + this.url);
   }

   public int getResponseCode() throws IOException {
      if (this.conn == null) {
         throw new IllegalArgumentException("No connection has been made");
      } else {
         return this.conn.getResponseCode();
      }
   }

   public InputStream getInputStream() {
      return this.inputStream;
   }

   public HttpRequest.BufferedResponse returnContent() throws IOException, InterruptedException {
      if (this.inputStream == null) {
         throw new IllegalArgumentException("No input stream available");
      } else {
         HttpRequest.BufferedResponse var3;
         try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            int b = 0;

            while ((b = this.inputStream.read()) != -1) {
               bos.write(b);
            }

            var3 = new HttpRequest.BufferedResponse(bos.toByteArray());
         } finally {
            this.close();
         }

         return var3;
      }
   }

   public HttpRequest saveContent(File file) throws IOException, InterruptedException {
      Closer closer = Closer.create();

      try {
         FileOutputStream fos = closer.register(new FileOutputStream(file));
         BufferedOutputStream bos = closer.register(new BufferedOutputStream(fos));
         this.saveContent(bos);
      } finally {
         closer.close();
      }

      return this;
   }

   public HttpRequest saveContent(OutputStream out) throws IOException, InterruptedException {
      try {
         String field = this.conn.getHeaderField("Content-Length");
         if (field != null) {
            long len = Long.parseLong(field);
            if (len >= 0L) {
               this.contentLength = len;
            }
         }
      } catch (NumberFormatException var9) {
      }

      try {
         BufferedInputStream bis = new BufferedInputStream(this.inputStream);
         byte[] data = new byte[8192];
         int len = 0;

         while ((len = bis.read(data, 0, 8192)) >= 0) {
            out.write(data, 0, len);
            this.readBytes += len;
         }
      } finally {
         this.close();
      }

      return this;
   }

   @Override
   public void close() {
      if (this.conn != null) {
         this.conn.disconnect();
      }
   }

   public static HttpRequest get(URL url) {
      return request("GET", url);
   }

   public static HttpRequest post(URL url) {
      return request("POST", url);
   }

   public static HttpRequest request(String method, URL url) {
      return new HttpRequest(method, url);
   }

   public static URL url(String url) {
      try {
         return URI.create(url).toURL();
      } catch (MalformedURLException var2) {
         throw new RuntimeException(var2);
      }
   }

   private static URL reformat(URL existing) {
      try {
         return existing.toURI().toURL();
      } catch (URISyntaxException | MalformedURLException var2) {
         return existing;
      }
   }

   public class BufferedResponse {
      private final byte[] data;

      private BufferedResponse(byte[] data) {
         this.data = data;
      }

      public byte[] asBytes() {
         return this.data;
      }

      public String asString(String encoding) throws IOException {
         return new String(this.data, encoding);
      }

      public HttpRequest.BufferedResponse saveContent(File file) throws IOException, InterruptedException {
         Closer closer = Closer.create();
         file.getParentFile().mkdirs();

         try {
            FileOutputStream fos = closer.register(new FileOutputStream(file));
            BufferedOutputStream bos = closer.register(new BufferedOutputStream(fos));
            this.saveContent(bos);
         } finally {
            closer.close();
         }

         return this;
      }

      public HttpRequest.BufferedResponse saveContent(OutputStream out) throws IOException, InterruptedException {
         out.write(this.data);
         return this;
      }
   }

   public static final class Form {
      private static final MapJoiner URL_ENCODER = Joiner.on('&').withKeyValueSeparator('=');
      private static final Joiner CRLF_JOINER = Joiner.on("\r\n");
      public final Map<String, String> elements = new LinkedHashMap<>();
      private final String formDataSeparator = "EngineHubFormData" + ThreadLocalRandom.current().nextInt(10000, 99999);

      private Form() {
      }

      public HttpRequest.Form add(String key, String value) {
         this.elements.put(key, value);
         return this;
      }

      public String getFormDataSeparator() {
         return this.formDataSeparator;
      }

      public String toFormDataString() {
         String separatorWithDashes = "--" + this.formDataSeparator;
         StringBuilder builder = new StringBuilder();

         for (Entry<String, String> element : this.elements.entrySet()) {
            CRLF_JOINER.appendTo(
               builder, separatorWithDashes, "Content-Disposition: form-data; name=\"" + element.getKey() + "\"", new Object[]{"", element.getValue(), ""}
            );
         }

         builder.append(separatorWithDashes).append("--");
         return builder.toString();
      }

      public String toUrlEncodedString() {
         return URL_ENCODER.join(
            this.elements
               .entrySet()
               .stream()
               .map(
                  e -> Maps.immutableEntry(UrlEscapers.urlFormParameterEscaper().escape(e.getKey()), UrlEscapers.urlFormParameterEscaper().escape(e.getValue()))
               )
               .iterator()
         );
      }

      public static HttpRequest.Form create() {
         return new HttpRequest.Form();
      }
   }
}
