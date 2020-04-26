package xiue.network;

import xiue.plugin.Sakura;

import java.io.IOException;

public class SakuraClient extends BaseClient {
    private static final String STRING_CHARSET = "gb2312";

    public SakuraClient() {
        super();
    }

    public String getAnimesCalendar() throws IOException {
        return formatStrCharset(get(Sakura.getCALENDAR_URL()).body().bytes(),STRING_CHARSET);
    }

    public String getAnimeDetail(int id) throws IOException {
        return formatStrCharset(get(Sakura.getDetailURLByID(id)).body().bytes(), STRING_CHARSET);
    }

}
