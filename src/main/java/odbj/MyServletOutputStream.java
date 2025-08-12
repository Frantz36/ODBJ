package odbj;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class MyServletOutputStream extends ServletOutputStream {
    
    private final int pagesize = 2/*1*1024*/*1024;
    private ServletOutputStream sos;
    private ObjectOutputStream oos;
    private Downloader d;
    private boolean isodb;
    
    public static byte[] serializeObjectToBytes(Serializable obj) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(obj);
            return bos.toByteArray();
        }
    }

    public static Object deserializeBytesToObject(byte[] data) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            return ois.readObject();
        }
    }

    public MyServletOutputStream(ServletOutputStream sos, boolean isodb, ObjectOutputStream oos) {
        this.sos = sos;
        this.isodb = isodb;
        this.oos = oos;
        System.out.println("MyServletOutputStream: constructor ("+isodb+")");
        if (isodb)
            try {
                /*d = new Downloader();
                d.start();*/
                d = GlobalDownloader.getInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    public void write(int b) throws IOException {
//        if (isodb) {
//            oos.writeObject(new RealDescriptor(b));
//        } else {
            sos.write(b);
//        }
    }

    public void write(byte[] buff, int off, int len) throws IOException {

        try {
            Object obj = deserializeBytesToObject(buff);
            if(obj instanceof RealDescriptor || obj instanceof VirtualDescriptor){ // Real ou Virtual reçu
                System.out.println("Real ou Virtual reçu");
                if(isodb) {
                    sos.write(buff); sos.flush();
                    System.out.println("Real ou Virtual envoyé");
                }
                else {
                    if (obj instanceof RealDescriptor) {
                        System.out.println("Real reçu et payload à envoyer");
                        sos.write(((RealDescriptor) obj).getBuffer()); sos.flush();
                        System.out.println("Real reçu et payload envoyé");
                    } else if (obj instanceof VirtualDescriptor) {
                        System.out.println("Virtual reçu et payload à envoyer");
                        byte[] buf = d.download(((VirtualDescriptor) obj).getHost(),((VirtualDescriptor) obj).getPort(),((VirtualDescriptor) obj).getPayloadid());
                        sos.write(buf); sos.flush();
                        System.out.println("Virtual reçu et payload envoyé");
                    }
                }
            } else { // Payload classique reçu
                System.out.println("Payload classique reçue");
                if (buff.length > pagesize) { // Virtual à envoyer
                    System.out.println("Virtual à envoyer");
                    byte[] b =  new byte[buff.length];
                    System.arraycopy(buff, 0, b, 0, buff.length);
                    int id = d.addPayload(b);
                    VirtualDescriptor desc = new VirtualDescriptor("10.0.10.3"/*"localhost"*/, d.getPort(), id, buff.length);
                    byte[] buf = serializeObjectToBytes(desc);
                    sos.write(buf); sos.flush();
                } else {
                    System.out.println("Real à envoyer");
                    RealDescriptor real = new RealDescriptor(buff.length, buff);
                    byte[] buf = serializeObjectToBytes(real);
                    sos.write(buf); sos.flush();
                }
            }
        } catch (Exception e) { //Payload classique reçue
            System.out.println("Payload classique reçue: taille "+buff.length);
            e.printStackTrace();
            if (buff.length > pagesize) { // Virtual à envoyer
                System.out.println("Virtual à envoyer");
                byte[] b =  new byte[buff.length];
                System.arraycopy(buff, 0, b, 0, buff.length);
                int id = d.addPayload(b);
                VirtualDescriptor desc = new VirtualDescriptor("10.0.10.3"/*"localhost"*/, d.getPort(), id, buff.length);
                byte[] buf = serializeObjectToBytes(desc);
                sos.write(buf); sos.flush();
            } else {
                System.out.println("Real à envoyer");
                RealDescriptor real = new RealDescriptor(buff.length, buff);
                byte[] buf = serializeObjectToBytes(real);
                sos.write(buf);  sos.flush();
            }
        }

    }

    public void write(byte[] buff) throws IOException {
        this.write(buff, 0, buff.length);
    }

    public void flush() throws IOException {
        sos.flush();
    }

    public void close() throws IOException {
        sos.close();
//        d.kill();
    }

    @Override
    public boolean isReady() {
        return false;
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {

    }
}