package oikos.app.common.utils;



import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@Component
public class PdfGenaratorUtil {
	@Autowired
	private TemplateEngine templateEngine;
	private final Path root = Paths.get("upload/pdf/");

	public String createPdf(String templateName, Map<Object, Object> map,String idbien) throws Exception {
		String res;
		Assert.notNull(templateName, "The templateName can not be null");
		Context ctx = new Context();

		if (map != null) {
			map.forEach((k, v) -> {
				System.out.println("key: " + k + ", value: " + v);
				ctx.setVariable(k.toString(), v);
			});

		}
		String processedHtml = templateEngine.process(templateName, ctx);
		FileOutputStream os = null;

		try {
			log.info(root.toAbsolutePath().toString());
			final File outputFile = File.createTempFile(idbien, ".pdf", new File(root.toAbsolutePath().toString()));

			os = new FileOutputStream(outputFile);
			ITextRenderer renderer = new ITextRenderer();
			renderer.setDocumentFromString(processedHtml);
			renderer.layout();
			renderer.createPDF(os, false);
			renderer.finishPDF();
			res=outputFile.getName();
			renderer.getDocument().getDocumentURI();
			log.info("PDF created successfully");
			
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) { /* ignore */
				}
			}
		}
		return res;

	}
}
