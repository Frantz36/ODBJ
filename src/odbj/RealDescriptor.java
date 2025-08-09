package odbj;

import java.io.Serializable;

public class RealDescriptor implements Serializable {

    public int len;
    public byte[] buff = null;
    int b;

    public RealDescriptor(int len, byte[] buff) {
        this.len = len;
        this.buff = buff;
    }
    public RealDescriptor(int b) {
        this.len = 1;
        this.b = b;
    }
}
