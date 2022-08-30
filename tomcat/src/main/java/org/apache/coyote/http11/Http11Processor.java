package org.apache.coyote.http11;

import static nextstep.jwp.db.InMemoryUserRepository.findByAccount;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import nextstep.jwp.exception.UncheckedServletException;
import nextstep.jwp.model.User;
import org.apache.coyote.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Http11Processor implements Runnable, Processor {

    private static final Logger log = LoggerFactory.getLogger(Http11Processor.class);

    private final Socket connection;

    public Http11Processor(final Socket connection) {
        this.connection = connection;
    }

    @Override
    public void run() {
        process(connection);
    }

    @Override
    public void process(final Socket connection) {
        try (var inputStream = connection.getInputStream();
             var outputStream = connection.getOutputStream()) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String firstLine = bufferedReader.readLine();
            if (firstLine == null) {
                return;
            }

            if (firstLine.contains("login")) {
                showUser(firstLine);
                return;
            }

            var response = generateResponse(firstLine);
            outputStream.write(response.getBytes());
            outputStream.flush();
        } catch (IOException | UncheckedServletException e) {
            log.error(e.getMessage(), e);
        }
    }

    private void showUser(String resource) {
        int questionIndex = resource.indexOf("?");
        String queryString = resource.substring(questionIndex);
        String account = queryString.split("account=")[1].split("&")[0];
        String password = queryString.split("password=")[1].split(" ")[0];
        User user = findByAccount(account)
                .orElseThrow(IllegalArgumentException::new);
        if (!user.checkPassword(password)) {
            throw new IllegalArgumentException();
        }
        log.debug(user.toString());
    }

    private String generateResponse(String resource) throws IOException {
        String responseBody = generateResponseBody(resource);
        String contentType = getContentType(resource);
        return String.join("\r\n",
                "HTTP/1.1 200 OK ",
                contentType,
                "Content-Length: " + responseBody.getBytes().length + " ",
                "",
                responseBody);
    }

    private String getContentType(String resource) {
        if (resource.contains("/css")) {
            return "Content-Type: text/css;charset=utf-8 ";
        }
        if (resource.contains("/js")) {
            return "Content-Type: text/js;charset=utf-8 ";
        }
        return "Content-Type: text/html;charset=utf-8 ";
    }

    private String generateResponseBody(String resource) throws IOException {
        String fileName = resource.split(" ")[1];
        if (fileName.equals("/")) {
            return "Hello world!";
        }
        return generateResponseBodyByFile(fileName);
    }

    private String generateResponseBodyByFile(String fileName) throws IOException {
        Path path = new File(Objects.requireNonNull(
                getClass().getClassLoader().getResource("static" + fileName)).getFile()
        ).toPath();

        return Files.readString(path);
    }
}
