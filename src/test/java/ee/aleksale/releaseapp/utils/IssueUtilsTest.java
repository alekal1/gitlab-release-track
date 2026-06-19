package ee.aleksale.releaseapp.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class IssueUtilsTest {

  private static Stream<Arguments> blankValues() {
    return Stream.of(
            Arguments.of(""),
            Arguments.of(" "),
            Arguments.of("  "),
            Arguments.of((Object) null)
    );
  }

  private static Stream<Arguments> invalidFormatIssues() {
    return Stream.of(
            Arguments.of("1"),
            Arguments.of("?::?"),
            Arguments.of("?::?::"),
            Arguments.of("::?::?::"),
            Arguments.of("?.?"),
            Arguments.of("?/?"),
            Arguments.of("?\\?"),
            Arguments.of("???")
    );
  }

  private static Stream<Arguments> validFormatIssues() {
    return Stream.of(
            Arguments.of("A:B,A:B"),
            Arguments.of("A:B-?,A:B-?"),
            Arguments.of("A:B-?:C,A:B-?:C"),
            Arguments.of("A:B-?:C-123?:,A:B-?:C-123?:"),
            Arguments.of("A:B-?:C-123?:\"smt\",A:B-?:C-123?:\"smt\"")
    );
  }

  @ParameterizedTest
  @MethodSource("blankValues")
  void shouldReturnEmptyList_whenNoIssues(String value) {
    final var result = IssueUtils.parseIssues(value);

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @ParameterizedTest
  @MethodSource("invalidFormatIssues")
  void shouldReturnEmptyList_whenInvalidFormat(String value) {
    final var result = IssueUtils.parseIssues(value);

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @ParameterizedTest
  @MethodSource("validFormatIssues")
  void shouldReturnAllIssues_whenValidFormat(String value) {
    final var result = IssueUtils.parseIssues(value);

    assertNotNull(result);
    assertEquals(2, result.size());
  }
}
