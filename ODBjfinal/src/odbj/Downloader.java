package odbj;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Downloader extends Thread {

    private boolean running = true;
    private int port;
    private int index = 0;
    private Map<Integer,byte[]> payloads = new HashMap<Integer,byte[]>();

    public Downloader() {
    }

    public int getPort() {
        return this.port;
    }

    public int addPayload(byte[] b) {
        payloads.put(index,b);
        return index++;
    }

    public void kill() {
        running = false;
    }

    public static byte[] download(String host, int port, int payloadid) {
        try {
            Socket s = new Socket(host, port);
            ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
            oos.writeObject(payloadid);
            byte[] ret = null;
            try {
                ret = (byte[])ois.readObject();
                s.close();
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void run() {
        try {
            ServerSocket ss = new ServerSocket();
            try {
                ss.bind(null);
                this.port = ss.getLocalPort();
                ss.setSoTimeout(1700);

                while (running) {
                    Socket s = ss.accept();
                    ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
                    ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
                    int payloadid = (int) ois.readObject();
                    byte[] ret = payloads.get(payloadid);
                    oos.writeObject(ret);
                    System.out.println("Downloader: provide payload (" + ret.length + ")");
                    payloads.remove(payloadid);
                    s.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                ss.close();
            }
        } catch (IOException ioex) {
            ioex.printStackTrace();
        }
    }
}