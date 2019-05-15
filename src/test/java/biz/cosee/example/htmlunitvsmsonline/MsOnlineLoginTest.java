package biz.cosee.example.htmlunitvsmsonline;

import com.gargoylesoftware.htmlunit.ImmediateRefreshHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.JavaScriptEngine;
import net.sourceforge.htmlunit.corejs.javascript.Context;
import net.sourceforge.htmlunit.corejs.javascript.ContextFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class MsOnlineLoginTest {

    @Test
    void msOnlineLoginWithHtmlUnit() throws IOException {

        String usernameElementName = "loginfmt";
        String submitButtonId = "idSIButton9";
        String loginHeaderId = "loginHeader";
        String username = "whatever@ff.com";

        final WebClient webClient = getWebClient();
        HtmlPage page = webClient.getPage("https://login.microsoftonline.com");
        waitForBackgroundJavaScript(webClient);

        // still on username view
        HtmlElement loginHeaderElementBefore = (HtmlElement) page.getElementById(loginHeaderId);
        assertThat(loginHeaderElementBefore).isNotNull();
        assertThat(loginHeaderElementBefore.asText()).isEqualTo("Sign in");

        // fill username field
        HtmlElement usernameField = page.getElementByName(usernameElementName);
        assertThat(usernameField).isNotNull();
        usernameField.setAttribute("value", username);
        waitForBackgroundJavaScript(webClient);

        // submit username
        HtmlElement submitButton = (HtmlElement) page.getElementById(submitButtonId);
        assertThat(submitButton).isNotNull();
        submitButton.click();
        waitForBackgroundJavaScript(webClient);

        // confirm redirect to password view
        HtmlElement loginHeaderElement = (HtmlElement) page.getElementById(loginHeaderId);
        assertThat(loginHeaderElement).isNotNull();
        // fixme does not work with htmlunit versions > 2.13
        assertThat(loginHeaderElement.asText()).as("Check forward to Password View").isEqualTo("Enter password");

    }

    private WebClient getWebClient() {
        final WebClient webClient = new WebClient();

        increaseMaximumInterpreterStackDepth(webClient);

        webClient.setRefreshHandler(new ImmediateRefreshHandler());
        webClient.getOptions().setRedirectEnabled(true);
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setUseInsecureSSL(true);

        return webClient;
    }

    private void increaseMaximumInterpreterStackDepth(WebClient webClient) {
        ((JavaScriptEngine) webClient.getJavaScriptEngine()).getContextFactory().addListener(new ContextFactory.Listener() {
            public void contextCreated(Context context) {
                context.setMaximumInterpreterStackDepth(Integer.MAX_VALUE);
            }

            public void contextReleased(Context context) {
                // nothing to do
            }
        });
    }

    private void waitForBackgroundJavaScript(WebClient webClient) {
        int numberOfJsBackgroundTasks = webClient.waitForBackgroundJavaScript(5000);
        assertThat(numberOfJsBackgroundTasks).isEqualTo(0); // at least one background task is still running
    }
}