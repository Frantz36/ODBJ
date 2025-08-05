import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static java.net.http.HttpRequest.newBuilder;

@WebServlet("/interserver")
public class InterServer extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String ine = request.getParameter("ine");
        OutputStream out = response.getOutputStream();
        response.setContentType("image/jpeg");

        HttpClient client =  HttpClient.newHttpClient();
        String uri =  String.format("http://128.110.217.110:8080/servlet-mod-is/intermodserver?ine=%s",ine);
        HttpRequest httpRequest =  newBuilder()
                .uri(URI.create(uri))
                .GET()
                .build();

        HttpResponse<byte[]> responseEntity = null;
        try {
            responseEntity = client.send(httpRequest, HttpResponse.BodyHandlers.ofByteArray());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        byte [] buffer = responseEntity.body();

        char c = (char) buffer[0];
        System.out.println(c);

        out.write(buffer);
        out.flush();
        out.close();

    }

}
