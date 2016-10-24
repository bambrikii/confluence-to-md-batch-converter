package org.bambrikii.md.converter.impl;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import net.sf.saxon.TransformerFactoryImpl;
import org.apache.commons.io.IOUtils;
import org.bambrikii.md.converter.Crawler;
import org.bambrikii.md.converter.api.Transformable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.w3c.tidy.Tidy;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.bambrikii.md.converter.impl.ViewStorageTransformer.CHARSET_NAME;

/**
 * Created by Alexander Arakelyan on 24.10.16 20:25.
 */
public class HtmTransformer implements Transformable {
	private static final String WRAPPER_TOP = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"\n" +
			"   \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n<html><body>";
	private static final String WRAPPER_BOTTOM = "</body></html>";
	private WebClient client;

	@Override
	public void setWebClient(WebClient client) {
		this.client = client;
	}

	@Override
	public String retrieveContent(HtmlPage page) throws IOException {
		try (InputStream is = page.getWebResponse().getContentAsStream()) {
			return IOUtils.toString(is, CHARSET_NAME);
		}
	}

	@Override
	public String transformContent(String content) throws ParserConfigurationException, IOException, SAXException, TransformerException {
		Document doc = Jsoup.parse(content);
		doc.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
		Elements main = doc.select("div#main-content");
		content = main.html();
		content = WRAPPER_TOP + content + WRAPPER_BOTTOM;
		Tidy tidy = new Tidy();
		tidy.setXHTML(true);
		try (
				InputStream is = IOUtils.toInputStream(content, CHARSET_NAME);
				ByteArrayOutputStream os = new ByteArrayOutputStream()
		) {
			org.w3c.dom.Document document = tidy.parseDOM(is, os);
			DOMSource source = new DOMSource(document);
			TransformerFactory transformerFactory = TransformerFactoryImpl.newInstance(TransformerFactoryImpl.class.getName(), TransformerFactoryImpl.class.getClassLoader());
			Transformer transformer = transformerFactory.newTransformer(new StreamSource(Crawler.class.getResourceAsStream("/xslt/htm/htm2md.xsl")));
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			Result result = new StreamResult(outputStream);
			transformer.transform(source, result);
			String resultAsString = new String(outputStream.toByteArray());
			return resultAsString;
		}
	}
}
