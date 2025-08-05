package odbj;

import java.io.*;


public class MyInputStream extends ObjectInputStream {

    private InputStream is;
    ObjectInputStream ois;
    private boolean isodb;

    public MyInputStream() throws IOException {}

    public MyInputStream(InputStream is, boolean isodb, ObjectInputStream ois) throws IOException {
        this.is = is;
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
            return is.read();
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
            return is.read(buff, off, len);
        }
    }

    public void close() throws IOException {
        is.close();
    }
}
