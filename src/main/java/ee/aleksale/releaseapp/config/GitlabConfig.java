package ee.aleksale.releaseapp.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "gitlab")
public class GitlabConfig {

  private String baseUrl;
  private String token;

}
