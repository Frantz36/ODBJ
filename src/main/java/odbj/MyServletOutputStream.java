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
        /*if (len>0)
        try {
            VirtualDescriptor desc = (VirtualDescriptor) deserializeBytesToObject(buff);
            if ((off==0) && (len==desc.len) && isodb) {
                // send virtual
                System.out.println("MyServletOutputStream.write: virtual remote ("+desc.len+")");
                oos.writeObject(desc);
                return;
            } else {
                // download the payload
                System.out.println("MyServletOutputStream.write: download payload ("+desc.len+")");
                byte[] ret = Downloader.download(desc.host, desc.port, desc.payloadid);
                System.arraycopy(ret, 0, buff, 0, ret.length);
                // will send the payload below
            }
        }catch (Exception e){
            
        }
        if (isodb) {
            if (len > pagesize) {
                // send virtual
                byte[] b = new byte[len];
                System.arraycopy(buff, off, b, 0, len);
                int id = d.addPayload(b);
                VirtualDescriptor desc = new VirtualDescriptor("localhost", d.getPort(), id, len);
                System.out.println("MyServletOutputStream.write: virtual local ("+desc.len+")");
                oos.writeObject(desc);
            } else {
                System.out.println("MyServletOutputStream.write: real ("+len+")");
                // send real
                byte[] b = new byte[len];
                RealDescriptor buf = null;
                try {
                    buf = (RealDescriptor) deserializeBytesToObject(buff);
                    System.arraycopy(buf.getBuffer(), off, b, 0, len);
                    System.out.println("MyServletOutputStream.write: RD sent / RD Object got ("+len+")");
                    oos.writeObject(new RealDescriptor(len, b));
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                    System.arraycopy(buff, off, b, 0, len);
                    System.out.println("MyServletOutputStream.write: RD sent / not RD Object got ("+len+")");
                    oos.writeObject(new RealDescriptor(len, buff));
                    return;
                }
            }
        } else {
            byte[] b = new byte[len];
            RealDescriptor buf = null;
            try{
                buf = (RealDescriptor)  deserializeBytesToObject(buff);
                System.arraycopy(buf.getBuffer(), off, b, 0, len);
                System.out.println("MyServletOutputStream.write: not RD sent / RD Object got ("+len+")");
                sos.write(b, 0, len);
                return;
            } catch(Exception e){
                e.printStackTrace();
                System.arraycopy(buff, 0, b, 0, len);
                System.out.println("MyServletOutputStream.write: not RD sent / not RD Object got ("+len+")");
                sos.write(b, 0, len);
                return;
            }
        }*/

        /*if (isodb) { //Dans ce cas, le présent serveur va envoyer une donnée encapsulée Real ou Virtual
            try{ // On vérifie si c'est Real qu'il a reçu
                RealDescriptor rd = (RealDescriptor)deserializeBytesToObject(buff);
                System.out.println("Donnée Real reçu");
                sos.write(buff);
                System.out.println("Donnée Real envoyée");
            } catch (Exception e) { // N'est pas Real
                e.printStackTrace();
                try { // On vérifie si c'est Virtual
                    VirtualDescriptor vd = (VirtualDescriptor)deserializeBytesToObject(buff);
                    System.out.println("Donnée Virtual reçu");
                    sos.write(buff);
                    System.out.println("Donnée Virtual envoyée");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    System.out.println("Neither Real nor Virtual: first server (BE)");
                    //On va maintenant décider en fonction du poids si on envoie des données Real ou Virtual
                    if (buff.length > pagesize) {
                        System.out.println("Supérieur à pagesize: Donnée Virtual à enoyer");
                        byte[] b =  new byte[buff.length];
                        System.arraycopy(buff, 0, b, 0, buff.length);
                        int id = d.addPayload(b);
                        VirtualDescriptor desc = new VirtualDescriptor("localhost", d.getPort(), id, buff.length);
                        byte[] buf = serializeObjectToBytes(desc);
                        sos.write(buf);
                    }
                    else {
                        System.out.println("Inférieur à pagesize: Donnée Real à enoyer");
                        RealDescriptor real = new RealDescriptor(buff.length,buff);
                        byte[] buf = serializeObjectToBytes(real);
                        sos.write(buf);
                    }
                }
            }
        }
        else { // On veut envoyer une payload non encapsulée (Sûrement au client)
            try {
                RealDescriptor real = (RealDescriptor) deserializeBytesToObject(buff);
                System.out.println("Donnée reçue Real");
                sos.write(real.getBuffer());
                System.out.println("Payload envoyée");
            } catch (Exception e) { // Donnée reçue n'est pas un Real
                e.printStackTrace();
                try {
                    VirtualDescriptor virtual = (VirtualDescriptor) deserializeBytesToObject(buff);
                    System.out.println("Donnée reçue Virtual");
                    byte[] buffer = d.download(virtual.getHost(),virtual.getPort(), virtual.getPayloadid());
                    sos.write(buffer);
                    System.out.println("Payload envoyée");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    System.out.println("Neither Real nor Virtual: last server (FE)");
                }
            }
        }*/

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

        /*sos.write(buff, off, buff.length);*/

    }

    public void write(byte[] buff) throws IOException {
        /*try {
//            VirtualDescriptor desc = (VirtualDescriptor) deserializeBytesToObject(buff);
            write(buff, 0, desc.len);
        } catch (Exception e){
            e.printStackTrace();
            write(buff, 0, buff.length);
        }*/
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