package oikos.app.common.utils;

import com.itextpdf.text.DocumentException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.w3c.dom.Document;
import org.xhtmlrenderer.pdf.ITextRenderer;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Slf4j
public class PDFUtil {
  private PDFUtil() {}

  /**
   * Generate html string according to template and parameters, and then convert it to Document
   * recognized by flying-saucer
   *
   * @param templateName freemarker template name
   * @param variables freemarker template parameters
   * @return Document
   */
  private static Document generateDoc(
      TemplateEngine templateEngine, String templateName, Map<String, Object> variables) {
    // Declare a context object and put the data to be stored in the template
    final var context = new Context();
    context.setVariables(variables);
    final var stringWriter = new StringWriter();
    try (final var writer = new BufferedWriter(stringWriter)) {
      templateEngine.process(templateName, context, writer);
      writer.flush();
      final var factory = DocumentBuilderFactory.newInstance();
      factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
      factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
      final var builder = factory.newDocumentBuilder();
      return builder.parse(new ByteArrayInputStream(stringWriter.toString().getBytes()));
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return null;
    }
  }

  /**
   * Core: Generate pdf documents based on freemarker templates
   *
   * @param templateEngine configuration
   * @param templateName template name
   * @param out output stream
   * @param listVars template parameters
   */
  private static void generateAll(
      TemplateEngine templateEngine,
      String templateName,
      OutputStream out,
      List<Map<String, Object>> listVars) {
    if (CollectionUtils.isEmpty(listVars)) {
      log.warn("Warning: template parameter is empty!");
      return;
    }

    final var renderer = new ITextRenderer();
    // Call the template cyclically according to the number of parameter sets and append to the same
    // pdf document
    // (Note: This starts from 1, because the 0th is to create the pdf, and the content is added to
    // the pdf after 1)
    for (var i = 0; i < listVars.size(); i++) {
      Document docAppend = generateDoc(templateEngine, templateName, listVars.get(i));
      renderer.setDocument(docAppend, null);
      // Display and output pdf
      renderer.layout();
      if (i == 0) {
        try {
          renderer.createPDF(out, false);
        } catch (DocumentException | IOException e) {
          log.error("creating PDF", e);
        }
      } else {
        // Write a pdf page
        try {
          renderer.writeNextDocument();
        } catch (DocumentException | IOException e) {
          log.error("Writing Document", e);
        }
      }
    }
    renderer.finishPDF(); // complete pdf writing
  }

  /**
   * pdf download
   *
   * @param templateEngine configuration
   * @param templateName template name (with suffix .ftl)
   * @param listVars template parameter set
   * @param response HttpServletResponse
   * @param fileName download file name (with file extension suffix)
   */
  public static void download(
      TemplateEngine templateEngine,
      String templateName,
      List<Map<String, Object>> listVars,
      HttpServletResponse response,
      String fileName) {
    // Set encoding, file ContentType, file header, download file name
    response.setCharacterEncoding("utf-8");
    response.setContentType("multipart/form-data");
    try {
      response.setHeader(
          "Content-Disposition",
          "attachment;fileName="
              + new String(fileName.getBytes("gb2312"), StandardCharsets.ISO_8859_1));
    } catch (UnsupportedEncodingException e) {
      log.error(e.getMessage(), e);
    }
    try (ServletOutputStream out = response.getOutputStream()) {
      generateAll(templateEngine, templateName, out, listVars);
      out.flush();
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }

  /**
   * pdf preview
   *
   * @param templateEngine configuration
   * @param templateName template name (with suffix .ftl)
   * @param listVars template parameter set
   * @param response HttpServletResponse
   */
  public static void preview(
      TemplateEngine templateEngine,
      String templateName,
      List<Map<String, Object>> listVars,
      HttpServletResponse response) {
    try (ServletOutputStream out = response.getOutputStream()) {
      generateAll(templateEngine, templateName, out, listVars);
      out.flush();
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }
}
