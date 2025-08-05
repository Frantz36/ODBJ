package odbj;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class Handler {

    public static Object deserializeBytesToObject(byte[] data) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
         ObjectInputStream ois = new ObjectInputStream(bis)) {
        return ois.readObject();
    }
    }
        public static void bufferFault(byte[] p) {
               
                System.out.println("#################################");
                System.out.println("got buffer fault !!!");
                System.out.println("#################################");

                //System.exit(0);
                try{
                    //System.out.println("payload "+ new String(p, StandardCharsets.UTF_8));
                    VirtualDescriptor d = (VirtualDescriptor)deserializeBytesToObject(p);
                    System.out.println("Handler.bufferFault: download payload("+d.len+")");
                    p = Downloader.download(d.host, d.port, d.payloadid);
                }catch (Exception e){
                    e.printStackTrace();
                }
               
        }

}