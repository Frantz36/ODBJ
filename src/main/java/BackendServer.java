import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@WebServlet("/backend")
public class BackendServer extends HttpServlet {

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setHeader("Allow", "GET, POST, OPTIONS");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, java.io.IOException {

        try {
            long start = System.currentTimeMillis();
            System.out.println("BackendServer: doGet");

            response.setContentType("image/jpeg");
            ServletOutputStream out = response.getOutputStream();
            String ine = request.getParameter("ine");
            System.out.println("From BackendServer: RemoteAddr"+request.getRemoteAddr());
            System.out.println("From BackendServer: RemotePort"+request.getRemotePort());
            System.out.println("From BackendServer: LocalPort"+request.getLocalPort());
            System.out.println("From BackendServer: LocalAddr"+request.getLocalAddr());
            System.out.println("From BackendServer: LocalNAme"+request.getLocalName());
            System.out.println("From BackendServer: ServerName"+request.getServerName());
            InputStream file = new FileInputStream("/opt/images/"+ine+".jpeg");
//            InputStream file = new FileInputStream("/home/sepia/Downloads/images/"+ine+".jpeg");
            byte [] buffer = new byte[190000];
            int len;
            while ((len = file.read(buffer)) != -1) {

                //On crée un buffer tronqué à la taille de la payload à lire
                byte[] exact = new byte[len];

                //On copie uniquement le nombre d'octets lus
                System.arraycopy(buffer, 0, exact, 0, len);

                //On envoie dans le flux
                out.write(exact, 0, len);
            }
            long end = System.currentTimeMillis();
            System.out.println("Processing took: " + (end - start) + " ms");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
