package org.bambrikii.md.converter.impl;

import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlHiddenInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import net.sf.saxon.TransformerFactoryImpl;
import org.apache.commons.io.IOUtils;
import org.bambrikii.md.converter.Crawler;
import org.bambrikii.md.converter.api.Transformable;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
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
import java.net.URL;
import java.nio.charset.Charset;

/**
 * Created by Alexander Arakelyan on 22.10.16 19:20.
 */
public class ViewStorageTransformer implements Transformable {
	public static final String CHARSET_NAME = "UTF-8";
	private static final String WRAPPER_TOP = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<!DOCTYPE ac:confluence SYSTEM \"dtd/confluence-all.dtd\" [ "
			+ "<!ENTITY clubs    \"&#9827;\">"
			+ "<!ENTITY nbsp   \"&#160;\">"
			+ "<!ENTITY ndash   \"&#8211;\">"
			+ "<!ENTITY mdash   \"&#8212;\">"
			+ " ]>"
			+ "<ac:confluence xmlns:ac=\"http://www.atlassian.com/schema/confluence/4/ac/\" xmlns:ri=\"http://www.atlassian.com/schema/confluence/4/ri/\" xmlns=\"http://www.atlassian.com/schema/confluence/4/\">";
	private static final String WRAPPER_BOTTOM = "</ac:confluence>";
	public static final String TREE_PAGE_ID = "treePageId";

	private final URL url;
	private WebClient client;

	public ViewStorageTransformer(URL url) {
		this.url = url;
	}

	@Override
	public void setWebClient(WebClient client) {
		this.client = client;
	}

	public String transformContent(String content) throws ParserConfigurationException, IOException, SAXException, TransformerException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		builder.setEntityResolver((publicId, systemId) -> {
			if (systemId.contains("dtd/confluence-all.dtd")) {
				return new InputSource(Crawler.class.getResourceAsStream("/dtd/confluence-all.dtd"));
			} else {
				return null;
			}
		});
		org.w3c.dom.Document document = builder.parse(IOUtils.toInputStream(WRAPPER_TOP + content + WRAPPER_BOTTOM, Charset.forName(CHARSET_NAME)));
		DOMSource source = new DOMSource(document);
		TransformerFactory transformerFactory = TransformerFactoryImpl.newInstance(TransformerFactoryImpl.class.getName(), TransformerFactoryImpl.class.getClassLoader());
		Transformer transformer = transformerFactory.newTransformer(new StreamSource(Crawler.class.getResourceAsStream("/xslt/ac/c2md.xsl")));
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		Result result = new StreamResult(outputStream);
		transformer.transform(source, result);
		String resultAsString = new String(outputStream.toByteArray());
		return resultAsString;
	}

	@Override
	public String retrieveContent(HtmlPage page1) throws IOException {
		// Fetch PageId
		HtmlHiddenInput treePageId = page1.getElementByName(TREE_PAGE_ID);
		String pageId = treePageId.getValueAttribute();

		String url = this.url.getProtocol() + "://" + this.url.getAuthority() + "/plugins/viewstorage/viewpagestorage.action?pageId=" + pageId;
		TextPage page2 = client.getPage(url);
		return page2.getContent();
	}

}
