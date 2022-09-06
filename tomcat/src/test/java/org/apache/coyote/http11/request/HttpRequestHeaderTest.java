package org.apache.coyote.http11.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class HttpRequestHeaderTest {

    @DisplayName("Content-Length 헤더가 존재하는지 반환한다.")
    @ParameterizedTest
    @MethodSource("provideRequestLinesAndExpected")
    void hasZeroContentLength(List<String> requestHeaders, boolean expected) throws IOException {
        HttpRequestHeader httpRequestHeader = HttpRequestHeader.from(requestHeaders);

        boolean actual = httpRequestHeader.hasContentLength();

        assertThat(actual).isEqualTo(expected);
    }

    private static Stream<Arguments> provideRequestLinesAndExpected() {
        return Stream.of(
                Arguments.of(List.of(
                        "Host: localhost:8080",
                        "Connection: keep-alive",
                        "Content-Length: 80",
                        "Content-Type: application/x-www-form-urlencoded",
                        "Accept: */*"), true),
                Arguments.of(List.of(
                        "Host: localhost:8080",
                        "Connection: keep-alive",
                        "Accept: */*"), false)
        );
    }
}