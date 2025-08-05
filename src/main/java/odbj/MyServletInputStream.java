package odbj;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;

import java.io.*;


public class MyServletInputStream extends ServletInputStream {

    private ServletInputStream sis;
    ObjectInputStream ois;
    private boolean isodb;

    public MyServletInputStream() throws IOException {}

    @Override
    public boolean isFinished() {
        return false;
    }

    @Override
    public boolean isReady() {
        return false;
    }

    @Override
    public void setReadListener(ReadListener readListener) {

    }

    public MyServletInputStream(ServletInputStream is, boolean isodb, ObjectInputStream ois) throws IOException {
        this.sis = is;
        this.isodb = isodb;
        this.ois = ois;
        //System.out.println("MyInputStream.constructor: isodb="+isodb);
    }

    public int read() throws IOException {
        if (isodb) {
            if (index != -1) {
                int ret = cache[index++];
                if (index == cache.length) index = -1;
                return ret;
            } else {
                Object obj;
                try {
                    obj = ois.readObject();
                } catch (ClassNotFoundException ex) {
                    throw new IOException("Virtual or Real Descriptor's class not found");
                }
                if (obj instanceof VirtualDescriptor) {
                    VirtualDescriptor d = (VirtualDescriptor)obj;
                    System.out.println("MyInputStreamw.read: download payload ("+d.len+")");
                    cache = Downloader.download(d.host, d.port, d.payloadid);
                } else {
                    RealDescriptor d = (RealDescriptor)obj;
                    cache = d.buff;
                    System.out.println("MyInputStream.read: receive real("+cache.length+")");
                }
                index = 0;
                int ret = cache[index++];
                if (index == cache.length) index = -1;
                return ret;
            }
        } else
            return sis.read();
    }

    public int read(byte[] buff) throws IOException {
        return read(buff, 0, buff.length);
    }

    private byte[] cache;
    private int index = -1;

    public static byte[] serializeObjectToBytes(Serializable obj) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(obj);
            return bos.toByteArray();
        }
    }

    public int read(byte[] buff, int off, int len) throws IOException {
        if (isodb) {
            if (index == -1) {
                Object obj;
                try {
                    obj = ois.readObject();
                } catch (ClassNotFoundException ex) {
                    throw new IOException("Virtual or Real Descriptor's class not found");
                }
                if (obj instanceof VirtualDescriptor) {
                    VirtualDescriptor d = (VirtualDescriptor)obj;
                    if ((off==0) && (len==d.len)) {

                        buff = serializeObjectToBytes(d);
                        System.out.println("MyInputStream.read: virtual payload("+d.len+")");
                        return d.len;
                    }
                    // else download the paylaod
                    System.out.println("MyInputStream.read: download payload("+d.len+")");
                    cache = Downloader.download(d.host, d.port, d.payloadid);
                } else {
                    RealDescriptor d = (RealDescriptor)obj;
                    cache = d.buff;
                    System.out.println("MyInputStream.read: receive real("+d.len+")");
                }
                index = 0;
            }
            int nb = cache.length - index;
            int ret;
            int offcache = index;
            if (len >= nb) {
                ret = nb;
                index = -1;
            } else {
                ret = len;
                index += len;
            }
            System.arraycopy(cache, offcache, buff, off, ret);
            //System.out.println("MyInputStream.read: return("+ret+")");
            return ret;
        } else {
            return sis.read(buff, off, len);
        }
    }

    public void close() throws IOException {
        sis.close();
    }
}
