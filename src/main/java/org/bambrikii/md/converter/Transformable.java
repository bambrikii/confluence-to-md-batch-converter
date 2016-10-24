package org.bambrikii.md.converter;

import com.gargoylesoftware.htmlunit.WebClient;

import java.io.IOException;

/**
 * Created by Alexander Arakelyan on 24.10.16 19:47.
 */
public interface Transformable {
	void setWebClient(WebClient client);

	String downloadViewStorage(String pageId) throws IOException;
}
