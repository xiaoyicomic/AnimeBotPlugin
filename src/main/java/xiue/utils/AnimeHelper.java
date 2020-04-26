package xiue.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import xiue.model.Anime;
import xiue.network.SakuraClient;
import xiue.plugin.Sakura;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnimeHelper {
    private List<Anime> todayAnimes;
    private long time;//上次获取今日放送的时间(当日00:00时的时间戳)
    private final SakuraClient sakuraClient;

    public AnimeHelper() {
        sakuraClient = new SakuraClient();
        todayAnimes = new ArrayList<>();
    }

    /**
     * 获取樱花动漫中动漫的详情
     *
     * @param id 樱花动漫的动漫ID
     * @return 动漫的相应数据
     */
    public Anime getAnimeDetail(int id) throws IOException {
        String con = sakuraClient.getAnimeDetail(id);
        if (con == null)
            return null;
        else if ("".equals(con))
            return null;
        Document document = Jsoup.parse(con);
        Element name = document.getElementsByClass("names").first();
        if (name == null) {
            return null;
        }
        Anime anime = new Anime(name.html());
        anime.setId(id);
        Element alex = document.getElementsByClass("alex").first();
        Pattern pattern = Pattern.compile("(\\d)+");
        String tmp = alex.html();
        final String status = tmp.substring(tmp.lastIndexOf("<p>"));
        Matcher matcher = pattern.matcher(status);
        matcher.find();
        anime.setNums(Integer.parseInt(matcher.group()));
        anime.setCurNum(anime.getNums());//更新至多少话保持一致
        if (alex.html().contains("更新至") && status.contains("每") && status.contains("更新")) {
            anime.setFinish(false);
            //解析更新时间
            anime.setTime(
                    status.substring(
                            status.indexOf("每"),
                            status.lastIndexOf("更新")
                    )
            );
        } else {
            anime.setFinish(true);
        }
        //解析类型
        Elements spans = alex.getElementsByTag("span");
        Elements categories = spans.get(1).getElementsByTag("a");
        for (Element category : categories)
            anime.addCategory(category.html());
        String year = spans.get(2).getElementsByTag("a").first().html();
        anime.setYear(year);
        //封面
        String img = document.getElementsByClass("tpic l").first().getElementsByTag("img").first().attr("src");
        if (img.indexOf("http") == 0)
            anime.setImg(img);
        else
            anime.setImg(Sakura.getURL(img));//可能是相对链接
        //简介
        String info = document.getElementsByClass("info").first().html();
        anime.setInfo(info);
        //地区
        String area = spans.get(0).getElementsByTag("a").first().html();
        anime.setArea(area);
        return anime;
    }

    public boolean isLatestTodayAnimes() {
        return Calendar.getInstance().getTimeInMillis() - getTime() < 1000 * 60 * 60 * 24
                && todayAnimes != null && !todayAnimes.isEmpty();
    }

    /**
     * 获取某一天的动漫更新列表
     *
     * @param dayOfWeek 周几(1-7分别对应周一到周七)
     * @return 动漫列表
     */
    public List<Anime> getDayAnimes(int dayOfWeek) {
        if (dayOfWeek > 7 || dayOfWeek < 1)
            return null;
        String con = null;
        try {
            con = sakuraClient.getAnimesCalendar();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        List<Anime> animes = new ArrayList<>();
        //保存下时间
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        time = calendar.getTimeInMillis();
        if (con == null) {
            return null;
        }
        Document document = Jsoup.parse(con);
        Element tists = document.getElementsByClass("tists").first();
        Element today = tists.getElementsByTag("ul").get(dayOfWeek - 1);
        Elements list = today.getElementsByTag("a");
        for (int i = 0; i < list.size(); i += 2) {
            Anime anime = new Anime(list.get(i + 1).html());
            //匹配更新到第几集
            Pattern pattern = Pattern.compile("(\\d)+");
            Matcher matcher = pattern.matcher(list.get(i).html());
            matcher.find();//推进计数
            anime.setCurNum(Integer.parseInt(matcher.group()));
            anime.setId(Sakura.getIDFromRelativeURL(list.get(i).attr("href")));
            animes.add(anime);
        }
        return animes;
    }

    /**
     * 加载今日更新的动漫
     * 数据来源：樱花动漫
     *
     * @return 是否加载成功
     */
    public boolean loadTodayAnimes() {
        todayAnimes.clear();
        int dayOfWeek = DateUtil.getDayOfWeek();
        List<Anime> tmp = getDayAnimes(dayOfWeek);
        if (tmp == null)
            return false;
        else {
            todayAnimes = tmp;
            //保存下时间
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            //储存缓存
            time = calendar.getTimeInMillis();
        }
        return true;
    }

    public List<Anime> getTodayAnimes() {
        return this.todayAnimes;
    }


    public long getTime() {
        return this.time;
    }
}
