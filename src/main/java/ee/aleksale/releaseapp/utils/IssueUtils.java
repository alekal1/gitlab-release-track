package ee.aleksale.releaseapp.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Slf4j
@UtilityClass
public class IssueUtils {

  private static final Pattern ISSUE_KEY_PATTERN = Pattern.compile("[A-Z][A-Z0-9-]*");

  public List<IssueUtils.IssueEntry> parseIssues(String issuesRaw) {
    if (issuesRaw == null || issuesRaw.isBlank()) {
      return List.of();
    }

    return Stream.of(issuesRaw.split("\\s*,\\s*"))
            .map(IssueUtils::toIssueEntry)
            .filter(Objects::nonNull)
            .toList();
  }

  private IssueUtils.IssueEntry toIssueEntry(String rawIssue) {
    if (rawIssue == null || rawIssue.isBlank()) {
      return null;
    }

    var separatorIndex = rawIssue.indexOf(':');
    if (separatorIndex < 0) {
      return null;
    }

    var key = rawIssue.substring(0, separatorIndex).trim();
    if (!isValidIssueKey(key)) {
      return null;
    }

    var title = rawIssue.substring(separatorIndex + 1).trim();
    return new IssueUtils.IssueEntry(key, title);
  }

  private boolean isValidIssueKey(String key) {
    return key != null && ISSUE_KEY_PATTERN.matcher(key.trim()).matches();
  }

  public record IssueEntry(String key, String title) {

    public String display() {
      return title == null || title.isBlank() ? key : key + ": " + title;
    }
  }
}
