package odbj;

import java.io.FileInputStream;
import java.io.IOException;

public class MyFileInputStream extends MyInputStream {

    private FileInputStream fis;

    public MyFileInputStream(String name) throws IOException {
        this.fis = new FileInputStream(name);
    }

    public int read() throws IOException {
            return fis.read();
    }

    public int read(byte[] buff) throws IOException {
        return read(buff, 0, buff.length);
    }

    public int read(byte[] buff, int off, int len) throws IOException {
        return fis.read(buff, off, len);
    }

    public void close() throws IOException {
        fis.close();
    }
}
