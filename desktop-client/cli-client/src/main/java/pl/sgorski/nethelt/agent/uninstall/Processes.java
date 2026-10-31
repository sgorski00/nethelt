package pl.sgorski.nethelt.agent.uninstall;

import java.io.IOException;
import java.nio.charset.Charset;

final class Processes {

  private Processes() {}

  static String output(String... command) {
    try {
      var process = new ProcessBuilder(command).redirectErrorStream(true).start();
      var output = new String(process.getInputStream().readAllBytes(), Charset.defaultCharset());
      return process.waitFor() == 0 ? output : "";
    } catch (IOException e) {
      return "";
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return "";
    }
  }
}
