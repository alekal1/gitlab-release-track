package ee.aleksale.releaseapp.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.net.URI;

@Slf4j
@UtilityClass
public class BrowserUtils {

  private static final String OS_NAME = System.getProperty("os.name").toLowerCase();
  private static final String LINUX_OPEN = "xdg-open";
  private static final String MAC_OPEN = "open";
  private static final String WIN_OPEN_DLL = "url.dll,FileProtocolHandler";

  public static void openInBrowser(String url) {
    try {
      if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
        Desktop.getDesktop().browse(new URI(url));
      } else {
        if (OS_NAME.contains("nix") || OS_NAME.contains("nux")) {
          new ProcessBuilder(LINUX_OPEN, url).start();
        } else if (OS_NAME.contains("mac")) {
          new ProcessBuilder(MAC_OPEN, url).start();
        } else if (OS_NAME.contains("win")) {
          new ProcessBuilder("rundll32", WIN_OPEN_DLL, url).start();
        } else {
          log.error("Unsupported OS for opening browser: {}", OS_NAME);
        }
      }
    } catch (Exception e) {
      log.error("Failed to open browser for URL: {}", url, e);
    }
  }
}
