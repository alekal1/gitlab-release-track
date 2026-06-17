package ee.aleksale.releaseapp.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "jira")
public class JiraConfig {

  private String baseUrl;
  private String token;
  private String boardName;
  private String issueStatus;
}
