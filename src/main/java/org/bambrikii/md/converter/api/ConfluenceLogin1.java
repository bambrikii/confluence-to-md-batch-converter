package org.bambrikii.md.converter.api;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.io.IOException;
import java.net.URL;

/**
 * Created by Alexander Arakelyan on 24.10.16 18:58.
 */
public class ConfluenceLogin1 implements Loginable {
	private final String username;
	private final String password;

	public ConfluenceLogin1(String username, String password) {
		this.username = username;
		this.password = password;
	}

	@Override
	public void login(WebClient client, URL logonUrl) throws IOException {
		HtmlPage page = client.getPage(logonUrl);
		HtmlForm loginForm = page.getFormByName("loginform");
		HtmlInput uname = loginForm.getInputByName("os_username");
		uname.setValueAttribute(username);
		HtmlInput pwd = loginForm.getInputByName("os_password");
		pwd.setValueAttribute(password);
		HtmlInput btn = loginForm.getInputByName("login");
		btn.click();
	}
}
