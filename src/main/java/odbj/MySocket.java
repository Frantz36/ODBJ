package odbj;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

public class MySocket extends Socket {

    Socket s;
    boolean isodb;
    ObjectInputStream ois = null;
    ObjectOutputStream oos = null;
    
    
    static List<String> clients = List.of("localhost:2001","localhost:2002");
    static List<Integer> servers = List.of(2001,2002);

    public MySocket(String host, int port) throws UnknownHostException, IOException {
        s = new Socket(host, port);
        System.out.println("MySocket: client connected");
        isodb = clients.contains(host+":"+port);
        if (isodb) {
            oos = new ObjectOutputStream(s.getOutputStream());
            ois = new ObjectInputStream(s.getInputStream());
        }
    }

    public MySocket(Socket s) throws IOException {
        this.s = s;
        isodb = servers.contains(s.getLocalPort());
        if (isodb) {
            oos = new ObjectOutputStream(s.getOutputStream());
            ois = new ObjectInputStream(s.getInputStream());
        }
    }

    public MyOutputStream getOutputStream() throws IOException {
        System.out.println("MySocket: getOutputStream");
        return new MyOutputStream(s.getOutputStream(), isodb, oos);
    }

    public MyInputStream getInputStream() throws IOException {
        System.out.println("MySocket: getInputStream");
        return new MyInputStream(s.getInputStream(), isodb, ois);
    }

    public void close() throws IOException {
        s.close();
    }
}
