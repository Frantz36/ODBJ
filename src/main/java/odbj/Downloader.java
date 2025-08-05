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
    private Map<Integer, byte[]> payloads = new HashMap<Integer, byte[]>();
    private ServerSocket ss = null;

    public Downloader() {
    }

    public int getPort() {
        return this.port;
    }

    public int addPayload(byte[] b) {
        payloads.put(index, b);
        return index++;
    }

    public void kill() {
        running = false;
        if (ss != null && !ss.isClosed()) {
            try {
                ss.close(); // Ceci débloque aussi le ss.accept() en cours.
            } catch (IOException e) {
                System.err.println("Error closing socket in kill");
            }
        }
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

/*
    public void run() {
        try {
            ss = new ServerSocket();
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
}*/

    public void run() {
        try {
            ss = new ServerSocket();
            ss.bind(null);
            this.port = ss.getLocalPort();
            ss.setSoTimeout(1700); // pour permettre des arrêts réactifs

            while (running) {
                try {
                    Socket s = ss.accept();
                    try {
                        ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
                        ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
                        int payloadid = (int) ois.readObject();
                        byte[] ret = payloads.get(payloadid);
                        oos.writeObject(ret);
                        System.out.println("Downloader: provide payload (" + (ret == null ? 0 : ret.length) + ")");
                        payloads.remove(payloadid);
                    } finally {
                        s.close();
                    }
                } catch (java.net.SocketTimeoutException ste) {
                    // Timeout normal, permet de vérifier 'running' régulièrement.
                    continue;
                } catch (java.net.SocketException se) {
                    if (!running || (ss.isClosed())) {
                        // Fermeture normale du serveur, on sort de la boucle
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
                    // Optionnel : log en cas d’échec de fermeture
                }
            }
        }
    }
}
