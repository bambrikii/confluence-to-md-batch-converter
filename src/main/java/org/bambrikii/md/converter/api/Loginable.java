package org.bambrikii.md.converter.api;

import com.gargoylesoftware.htmlunit.WebClient;

import java.io.IOException;
import java.net.URL;

/**
 * Created by Alexander Arakelyan on 24.10.16 18:54.
 */
public interface Loginable {
	void login(WebClient client, URL logonUrl) throws IOException;
}
