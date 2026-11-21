package com.mysql.cj.jdbc;

import com.mysql.cj.Messages;
import com.mysql.cj.exceptions.CJException;
import com.mysql.cj.exceptions.ExceptionInterceptor;
import com.mysql.cj.jdbc.exceptions.SQLError;
import com.mysql.cj.jdbc.exceptions.SQLExceptionsMapping;
import com.mysql.cj.jdbc.result.ResultSetInternalMethods;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stax.StAXResult;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public class MysqlSQLXML implements SQLXML {
   private final Lock lock = new ReentrantLock();
   private XMLInputFactory inputFactory;
   private XMLOutputFactory outputFactory;
   private String stringRep;
   private ResultSetInternalMethods owningResultSet;
   private int columnIndexOfXml;
   private boolean fromResultSet;
   private boolean isClosed = false;
   private boolean workingWithResult;
   private DOMResult asDOMResult;
   private SAXResult asSAXResult;
   private MysqlSQLXML.SimpleSaxToReader saxToReaderConverter;
   private StringWriter asStringWriter;
   private ByteArrayOutputStream asByteArrayOutputStream;
   private ExceptionInterceptor exceptionInterceptor;

   public MysqlSQLXML(ResultSetInternalMethods owner, int index, ExceptionInterceptor exceptionInterceptor) {
      this.owningResultSet = owner;
      this.columnIndexOfXml = index;
      this.fromResultSet = true;
      this.exceptionInterceptor = exceptionInterceptor;
   }

   public MysqlSQLXML(ExceptionInterceptor exceptionInterceptor) {
      this.fromResultSet = false;
      this.exceptionInterceptor = exceptionInterceptor;
   }

   @Override
   public void free() throws SQLException {
      try {
         this.lock.lock();

         try {
            this.stringRep = null;
            this.asDOMResult = null;
            this.asSAXResult = null;
            this.inputFactory = null;
            this.outputFactory = null;
            this.owningResultSet = null;
            this.workingWithResult = false;
            this.isClosed = true;
         } finally {
            this.lock.unlock();
         }
      } catch (CJException var6) {
         throw SQLExceptionsMapping.translateException(var6, this.exceptionInterceptor);
      }
   }

   @Override
   public String getString() throws SQLException {
      try {
         this.lock.lock();

         String var1;
         try {
            this.checkClosed();
            this.checkWorkingWithResult();
            if (!this.fromResultSet) {
               return this.stringRep;
            }

            var1 = this.owningResultSet.getString(this.columnIndexOfXml);
         } finally {
            this.lock.unlock();
         }

         return var1;
      } catch (CJException var7) {
         throw SQLExceptionsMapping.translateException(var7, this.exceptionInterceptor);
      }
   }

   private void checkClosed() throws SQLException {
      this.lock.lock();

      try {
         if (this.isClosed) {
            throw SQLError.createSQLException(Messages.getString("MysqlSQLXML.0"), this.exceptionInterceptor);
         }
      } finally {
         this.lock.unlock();
      }
   }

   private void checkWorkingWithResult() throws SQLException {
      this.lock.lock();

      try {
         if (this.workingWithResult) {
            throw SQLError.createSQLException(Messages.getString("MysqlSQLXML.1"), "S1009", this.exceptionInterceptor);
         }
      } finally {
         this.lock.unlock();
      }
   }

   @Override
   public void setString(String str) throws SQLException {
      try {
         this.lock.lock();

         try {
            this.checkClosed();
            this.checkWorkingWithResult();
            this.stringRep = str;
            this.fromResultSet = false;
         } finally {
            this.lock.unlock();
         }
      } catch (CJException var7) {
         throw SQLExceptionsMapping.translateException(var7, this.exceptionInterceptor);
      }
   }

   public boolean isEmpty() throws SQLException {
      this.lock.lock();

      boolean var1;
      try {
         this.checkClosed();
         this.checkWorkingWithResult();
         if (this.fromResultSet) {
            return false;
         }

         var1 = this.stringRep == null || this.stringRep.length() == 0;
      } finally {
         this.lock.unlock();
      }

      return var1;
   }

   @Override
   public InputStream getBinaryStream() throws SQLException {
      try {
         this.lock.lock();

         InputStream var1;
         try {
            this.checkClosed();
            this.checkWorkingWithResult();
            var1 = this.owningResultSet.getBinaryStream(this.columnIndexOfXml);
         } finally {
            this.lock.unlock();
         }

         return var1;
      } catch (CJException var7) {
         throw SQLExceptionsMapping.translateException(var7, this.exceptionInterceptor);
      }
   }

   @Override
   public Reader getCharacterStream() throws SQLException {
      try {
         this.lock.lock();

         Reader var1;
         try {
            this.checkClosed();
            this.checkWorkingWithResult();
            var1 = this.owningResultSet.getCharacterStream(this.columnIndexOfXml);
         } finally {
            this.lock.unlock();
         }

         return var1;
      } catch (CJException var7) {
         throw SQLExceptionsMapping.translateException(var7, this.exceptionInterceptor);
      }
   }

   @Override
   public <T extends Source> T getSource(Class<T> clazz) throws SQLException {
      try {
         this.lock.lock();

         StAXSource ex;
         try {
            this.checkClosed();
            this.checkWorkingWithResult();
            if (clazz == null || clazz.equals(SAXSource.class)) {
               try {
                  XMLReader reader = XMLReaderFactory.createXMLReader();
                  reader.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);
                  setFeature(reader, "http://apache.org/xml/features/disallow-doctype-decl", true);
                  setFeature(reader, "http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
                  setFeature(reader, "http://xml.org/sax/features/external-general-entities", false);
                  setFeature(reader, "http://xml.org/sax/features/external-parameter-entities", false);
                  return (T)(new SAXSource(
                     reader,
                     this.fromResultSet
                        ? new InputSource(this.owningResultSet.getCharacterStream(this.columnIndexOfXml))
                        : new InputSource(new StringReader(this.stringRep))
                  ));
               } catch (SAXException var14) {
                  SQLException sqlEx = SQLError.createSQLException(var14.getMessage(), "S1009", var14, this.exceptionInterceptor);
                  throw sqlEx;
               }
            }

            if (clazz.equals(DOMSource.class)) {
               try {
                  DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
                  builderFactory.setNamespaceAware(true);
                  setFeature(builderFactory, "http://javax.xml.XMLConstants/feature/secure-processing", true);
                  setFeature(builderFactory, "http://apache.org/xml/features/disallow-doctype-decl", true);
                  setFeature(builderFactory, "http://xml.org/sax/features/external-general-entities", false);
                  setFeature(builderFactory, "http://xml.org/sax/features/external-parameter-entities", false);
                  setFeature(builderFactory, "http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
                  builderFactory.setXIncludeAware(false);
                  builderFactory.setExpandEntityReferences(false);
                  builderFactory.setAttribute("http://javax.xml.XMLConstants/property/accessExternalSchema", "");
                  DocumentBuilder builder = builderFactory.newDocumentBuilder();
                  return (T)(new DOMSource(
                     builder.parse(
                        this.fromResultSet
                           ? new InputSource(this.owningResultSet.getCharacterStream(this.columnIndexOfXml))
                           : new InputSource(new StringReader(this.stringRep))
                     )
                  ));
               } catch (Throwable var12) {
                  SQLException sqlEx = SQLError.createSQLException(var12.getMessage(), "S1009", var12, this.exceptionInterceptor);
                  throw sqlEx;
               }
            }

            if (clazz.equals(StreamSource.class)) {
               return (T)(new StreamSource(
                  (Reader)(this.fromResultSet ? this.owningResultSet.getCharacterStream(this.columnIndexOfXml) : new StringReader(this.stringRep))
               ));
            }

            if (!clazz.equals(StAXSource.class)) {
               throw SQLError.createSQLException(Messages.getString("MysqlSQLXML.2", new Object[]{clazz.toString()}), "S1009", this.exceptionInterceptor);
            }

            try {
               ex = new StAXSource(
                  this.inputFactory
                     .createXMLStreamReader(
                        (Reader)(this.fromResultSet ? this.owningResultSet.getCharacterStream(this.columnIndexOfXml) : new StringReader(this.stringRep))
                     )
               );
            } catch (XMLStreamException var13) {
               SQLException sqlEx = SQLError.createSQLException(var13.getMessage(), "S1009", var13, this.exceptionInterceptor);
               throw sqlEx;
            }
         } finally {
            this.lock.unlock();
         }

         return (T)ex;
      } catch (CJException var16) {
         throw SQLExceptionsMapping.translateException(var16, this.exceptionInterceptor);
      }
   }

   private static void setFeature(Object factory, String name, boolean value) {
      try {
         if (factory instanceof DocumentBuilderFactory) {
            ((DocumentBuilderFactory)factory).setFeature(name, value);
         } else if (factory instanceof XMLReader) {
            ((XMLReader)factory).setFeature(name, value);
         }
      } catch (Exception var4) {
      }
   }

   @Override
   public OutputStream setBinaryStream() throws SQLException {
      try {
         this.lock.lock();

         OutputStream var1;
         try {
            this.checkClosed();
            this.checkWorkingWithResult();
            this.workingWithResult = true;
            var1 = this.setBinaryStreamInternal();
         } finally {
            this.lock.unlock();
         }

         return var1;
      } catch (CJException var7) {
         throw SQLExceptionsMapping.translateException(var7, this.exceptionInterceptor);
      }
   }

   private OutputStream setBinaryStreamInternal() throws SQLException {
      this.lock.lock();

      ByteArrayOutputStream var1;
      try {
         this.asByteArrayOutputStream = new ByteArrayOutputStream();
         var1 = this.asByteArrayOutputStream;
      } finally {
         this.lock.unlock();
      }

      return var1;
   }

   @Override
   public Writer setCharacterStream() throws SQLException {
      try {
         this.lock.lock();

         Writer var1;
         try {
            this.checkClosed();
            this.checkWorkingWithResult();
            this.workingWithResult = true;
            var1 = this.setCharacterStreamInternal();
         } finally {
            this.lock.unlock();
         }

         return var1;
      } catch (CJException var7) {
         throw SQLExceptionsMapping.translateException(var7, this.exceptionInterceptor);
      }
   }

   private Writer setCharacterStreamInternal() throws SQLException {
      this.lock.lock();

      StringWriter var1;
      try {
         this.asStringWriter = new StringWriter();
         var1 = this.asStringWriter;
      } finally {
         this.lock.unlock();
      }

      return var1;
   }

   @Override
   public <T extends Result> T setResult(Class<T> clazz) throws SQLException {
      try {
         this.lock.lock();

         StAXResult ex;
         try {
            this.checkClosed();
            this.checkWorkingWithResult();
            this.workingWithResult = true;
            this.asDOMResult = null;
            this.asSAXResult = null;
            this.saxToReaderConverter = null;
            this.stringRep = null;
            this.asStringWriter = null;
            this.asByteArrayOutputStream = null;
            if (clazz == null || clazz.equals(SAXResult.class)) {
               this.saxToReaderConverter = new MysqlSQLXML.SimpleSaxToReader();
               this.asSAXResult = new SAXResult(this.saxToReaderConverter);
               return (T)this.asSAXResult;
            }

            if (clazz.equals(DOMResult.class)) {
               this.asDOMResult = new DOMResult();
               return (T)this.asDOMResult;
            }

            if (clazz.equals(StreamResult.class)) {
               return (T)(new StreamResult(this.setCharacterStreamInternal()));
            }

            if (!clazz.equals(StAXResult.class)) {
               throw SQLError.createSQLException(Messages.getString("MysqlSQLXML.3", new Object[]{clazz.toString()}), "S1009", this.exceptionInterceptor);
            }

            try {
               if (this.outputFactory == null) {
                  this.outputFactory = XMLOutputFactory.newInstance();
               }

               ex = new StAXResult(this.outputFactory.createXMLEventWriter(this.setCharacterStreamInternal()));
            } catch (XMLStreamException var9) {
               SQLException sqlEx = SQLError.createSQLException(var9.getMessage(), "S1009", var9, this.exceptionInterceptor);
               throw sqlEx;
            }
         } finally {
            this.lock.unlock();
         }

         return (T)ex;
      } catch (CJException var11) {
         throw SQLExceptionsMapping.translateException(var11, this.exceptionInterceptor);
      }
   }

   private Reader binaryInputStreamStreamToReader(ByteArrayOutputStream out) {
      try {
         String encoding = "UTF-8";

         try {
            ByteArrayInputStream bIn = new ByteArrayInputStream(out.toByteArray());
            XMLStreamReader reader = this.inputFactory.createXMLStreamReader(bIn);
            int eventType = 0;

            while ((eventType = reader.next()) != 8) {
               if (eventType == 7) {
                  String possibleEncoding = reader.getEncoding();
                  if (possibleEncoding != null) {
                     encoding = possibleEncoding;
                  }
                  break;
               }
            }
         } catch (Throwable var7) {
         }

         return new StringReader(new String(out.toByteArray(), encoding));
      } catch (UnsupportedEncodingException var8) {
         throw new RuntimeException(var8);
      }
   }

   protected String readerToString(Reader reader) throws SQLException {
      StringBuilder buf = new StringBuilder();
      int charsRead = 0;
      char[] charBuf = new char[512];

      try {
         while ((charsRead = reader.read(charBuf)) != -1) {
            buf.append(charBuf, 0, charsRead);
         }
      } catch (IOException var7) {
         SQLException sqlEx = SQLError.createSQLException(var7.getMessage(), "S1009", var7, this.exceptionInterceptor);
         throw sqlEx;
      }

      return buf.toString();
   }

   protected Reader serializeAsCharacterStream() throws SQLException {
      this.lock.lock();

      try {
         this.checkClosed();
         if (this.workingWithResult || this.owningResultSet == null) {
            if (this.stringRep != null) {
               return new StringReader(this.stringRep);
            }

            if (this.asDOMResult != null) {
               return new StringReader(this.domSourceToString());
            }

            if (this.asStringWriter != null) {
               return new StringReader(this.asStringWriter.toString());
            }

            if (this.asSAXResult != null) {
               return this.saxToReaderConverter.toReader();
            }

            if (this.asByteArrayOutputStream != null) {
               return this.binaryInputStreamStreamToReader(this.asByteArrayOutputStream);
            }
         }

         return this.owningResultSet.getCharacterStream(this.columnIndexOfXml);
      } finally {
         this.lock.unlock();
      }
   }

   protected String domSourceToString() throws SQLException {
      try {
         DOMSource source = new DOMSource(this.asDOMResult.getNode());
         Transformer identity = TransformerFactory.newInstance().newTransformer();
         StringWriter stringOut = new StringWriter();
         Result result = new StreamResult(stringOut);
         identity.transform(source, result);
         return stringOut.toString();
      } catch (Throwable var5) {
         SQLException sqlEx = SQLError.createSQLException(var5.getMessage(), "S1009", var5, this.exceptionInterceptor);
         throw sqlEx;
      }
   }

   protected String serializeAsString() throws SQLException {
      this.lock.lock();

      try {
         this.checkClosed();
         if (this.workingWithResult) {
            if (this.stringRep != null) {
               return this.stringRep;
            }

            if (this.asDOMResult != null) {
               return this.domSourceToString();
            }

            if (this.asStringWriter != null) {
               return this.asStringWriter.toString();
            }

            if (this.asSAXResult != null) {
               return this.readerToString(this.saxToReaderConverter.toReader());
            }

            if (this.asByteArrayOutputStream != null) {
               return this.readerToString(this.binaryInputStreamStreamToReader(this.asByteArrayOutputStream));
            }
         }

         return this.owningResultSet.getString(this.columnIndexOfXml);
      } finally {
         this.lock.unlock();
      }
   }

   class SimpleSaxToReader extends DefaultHandler {
      StringBuilder buf = new StringBuilder();
      private boolean inCDATA = false;

      @Override
      public void startDocument() throws SAXException {
         this.buf.append("<?xml version='1.0' encoding='UTF-8'?>");
      }

      @Override
      public void endDocument() throws SAXException {
      }

      @Override
      public void startElement(String namespaceURI, String sName, String qName, Attributes attrs) throws SAXException {
         this.buf.append("<");
         this.buf.append(qName);
         if (attrs != null) {
            for (int i = 0; i < attrs.getLength(); i++) {
               this.buf.append(" ");
               this.buf.append(attrs.getQName(i)).append("=\"");
               this.escapeCharsForXml(attrs.getValue(i), true);
               this.buf.append("\"");
            }
         }

         this.buf.append(">");
      }

      @Override
      public void characters(char[] buffer, int offset, int len) throws SAXException {
         if (!this.inCDATA) {
            this.escapeCharsForXml(buffer, offset, len, false);
         } else {
            this.buf.append(buffer, offset, len);
         }
      }

      @Override
      public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
         this.characters(ch, start, length);
      }

      public void startCDATA() throws SAXException {
         this.buf.append("<![CDATA[");
         this.inCDATA = true;
      }

      public void endCDATA() throws SAXException {
         this.inCDATA = false;
         this.buf.append("]]>");
      }

      public void comment(char[] ch, int start, int length) throws SAXException {
         this.buf.append("<!--");

         for (int i = 0; i < length; i++) {
            this.buf.append(ch[start + i]);
         }

         this.buf.append("-->");
      }

      Reader toReader() {
         return new StringReader(this.buf.toString());
      }

      private void escapeCharsForXml(String str, boolean isAttributeData) {
         if (str != null) {
            int strLen = str.length();

            for (int i = 0; i < strLen; i++) {
               this.escapeCharsForXml(str.charAt(i), isAttributeData);
            }
         }
      }

      private void escapeCharsForXml(char[] buffer, int offset, int len, boolean isAttributeData) {
         if (buffer != null) {
            for (int i = 0; i < len; i++) {
               this.escapeCharsForXml(buffer[offset + i], isAttributeData);
            }
         }
      }

      private void escapeCharsForXml(char c, boolean isAttributeData) {
         switch (c) {
            case '\r':
               this.buf.append("&#xD;");
               break;
            case '"':
               if (!isAttributeData) {
                  this.buf.append("\"");
               } else {
                  this.buf.append("&quot;");
               }
               break;
            case '&':
               this.buf.append("&amp;");
               break;
            case '<':
               this.buf.append("&lt;");
               break;
            case '>':
               this.buf.append("&gt;");
               break;
            default:
               if ((c < 1 || c > 31 || c == '\t' || c == '\n') && (c < 127 || c > 159) && c != 8232 && (!isAttributeData || c != '\t' && c != '\n')) {
                  this.buf.append(c);
               } else {
                  this.buf.append("&#x");
                  this.buf.append(Integer.toHexString(c).toUpperCase());
                  this.buf.append(";");
               }
         }
      }
   }
}
