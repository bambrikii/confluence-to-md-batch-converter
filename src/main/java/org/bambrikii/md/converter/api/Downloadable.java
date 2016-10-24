package org.bambrikii.md.converter.api;

import com.gargoylesoftware.htmlunit.WebClient;
import org.bambrikii.md.converter.PageLinks;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.Map;

/**
 * Created by Alexander Arakelyan on 24.10.16 19:02.
 */
public interface Downloadable {
	PageLinks download(String displayLink) throws IOException, TransformerException, SAXException, ParserConfigurationException;

	void downloadAttachments(Map<String, String> attachmentLinks) throws IOException;

	void setClient(WebClient client);
}
