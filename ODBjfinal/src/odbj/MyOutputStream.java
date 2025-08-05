package odbj;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

public class MyOutputStream extends ObjectOutputStream {

    private final int pagesize = 1*1024*1024;
    private OutputStream os;
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
    public MyOutputStream(OutputStream os, boolean isodb, ObjectOutputStream oos) throws IOException {
        this.os = os;
        this.isodb = isodb;
        this.oos = oos;
        System.out.println("MyOutputStream: constructor ("+isodb+")");
        if (isodb)
            try {
                d = new Downloader();
                d.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    public void write(int b) throws IOException {
        if (isodb) {
            oos.writeObject(new RealDescriptor(b));
        } else {
            os.write(b);
        }
    }

    public void write(byte[] buff, int off, int len) throws IOException {
        if (len>0)
        try {
            VirtualDescriptor desc = (VirtualDescriptor) deserializeBytesToObject(buff); 
            if ((off==0) && (len==desc.len) && isodb) {
                // send virtual
                System.out.println("MyOutputStream.write: virtual remote ("+desc.len+")");
                oos.writeObject(desc);
                return;
            } else {
                // download the payload
                System.out.println("MyOutputStream.write: download payload ("+desc.len+")");
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
                System.out.println("MyOutputStream.write: virtual local ("+desc.len+")");
                oos.writeObject(desc);
            } else {
                System.out.println("MyOutputStream.write: real ("+len+")");
                // send real
                byte[] b = new byte[len];
                System.arraycopy(buff, off, b, 0, len);
                oos.writeObject(new RealDescriptor(len, b));
            }
        } else {
            os.write(buff, off, len);
        }
    }

    public void write(byte[] buff) throws IOException {
        try {
            VirtualDescriptor desc = (VirtualDescriptor) deserializeBytesToObject(buff); 
            write(buff, 0, desc.len);
        } catch (Exception e){
            write(buff, 0, buff.length);
        }
        }

    public void flush() throws IOException {
        os.flush();
    }

    public void close() throws IOException {
        os.close();
        d.kill();
    }
}

