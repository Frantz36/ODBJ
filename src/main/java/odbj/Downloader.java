package odbj;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Downloader extends Thread {

    private boolean running = true;
    private int port;
    private AtomicInteger index = new AtomicInteger(0);
    private ConcurrentHashMap<Integer, byte[]> payloads = new ConcurrentHashMap<>();
    private ServerSocket ss = null;
    // Thread pool pour traiter de multiples clients en parallèle
    private ExecutorService pool = Executors.newCachedThreadPool();

    public Downloader() {}

    public int getPort() {
        return this.port;
    }

    public int addPayload(byte[] b) {
        int id = index.getAndIncrement();
        payloads.put(id, b);
        return id;
    }

    public void kill() {
        running = false;
        if (ss != null && !ss.isClosed()) {
            try {
                ss.close(); // Débloque également le accept()
            } catch (IOException e) {
                System.err.println("Error closing socket in kill");
            }
        }
        pool.shutdown(); // Ferme proprement le pool de threads
    }

    public static byte[] download(String host, int port, int payloadid) {
        try {
            Socket s = new Socket(host, port);
            ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
            oos.writeObject(payloadid);
            byte[] ret = null;
            try {
                ret = (byte[]) ois.readObject();
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
            ss = new ServerSocket();
            ss.bind(null);
            this.port = ss.getLocalPort();
            ss.setSoTimeout(1700); // Permet un arrêt réactif

            while (running) {
                try {
                    final Socket s = ss.accept();
                    // Chaque client est géré dans son propre thread
                    pool.execute(() -> handleClient(s));
                } catch (java.net.SocketTimeoutException ste) {
                    // Timeout normal, permet de vérifier 'running'
                    continue;
                } catch (java.net.SocketException se) {
                    if (!running || (ss.isClosed())) {
                        // Fermeture normale du serveur
                        break;
                    } else {
                        se.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (ss != null && !ss.isClosed()) {
                try {
                    ss.close();
                } catch (Exception e) {
                    // Log optionnel
                }
            }
        }
    }

    private void handleClient(Socket s) {
        try (
                ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
                ObjectInputStream ois = new ObjectInputStream(s.getInputStream())
        ) {
            int payloadid = (int) ois.readObject();
            byte[] ret = payloads.get(payloadid);
            oos.writeObject(ret);
            System.out.println("Downloader: provide payload (" + (ret != null ? ret.length : 0) + ")");
            payloads.remove(payloadid);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { s.close(); } catch (Exception ignore) {}
        }
    }
}
