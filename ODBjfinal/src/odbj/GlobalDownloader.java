package odbj;

public class GlobalDownloader extends Downloader {

    private static GlobalDownloader instance;

    public GlobalDownloader() {
        super();
        this.start();
    }

    public static synchronized GlobalDownloader getInstance() {

        if (instance == null)
            instance = new GlobalDownloader();

        return instance;

    }

}
