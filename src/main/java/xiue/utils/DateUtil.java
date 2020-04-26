package xiue.utils;

import java.util.Calendar;

public class DateUtil {

    public static int getDayOfWeek() {
        int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        return dayOfWeek - 1 < 0 ? 7 : dayOfWeek - 1;//矫正偏差
    }

}
