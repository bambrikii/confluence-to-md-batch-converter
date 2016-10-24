package org.bambrikii.md.converter.api;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;

/**
 * Created by Alexander Arakelyan on 24.10.16 19:47.
 */
public interface Transformable {
	void setWebClient(WebClient client);

	String retrieveContent(HtmlPage pageId) throws IOException;

	String transformContent(String viewStorageContent) throws ParserConfigurationException, IOException, SAXException, TransformerException;
}
