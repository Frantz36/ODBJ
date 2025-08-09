package odbj;

import java.io.Serializable;

public class VirtualDescriptor implements Serializable {
    public String host;
    public int port;
    public int payloadid;
    public int len;

    public VirtualDescriptor(String host, int port, int payloadid, int len) {
        this.host = host;
        this.port = port;
        this.payloadid = payloadid;
        this.len = len;
    }
    
    
}
