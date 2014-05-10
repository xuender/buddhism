package me.xuender.buddhism.app.calendar;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ender on 14-5-1.
 */
public final class ChineseCalendar extends GregorianCalendar {
    private static final long serialVersionUID = 8L;

    /**
     * 农历年
     */
    public static final int CHINESE_YEAR = 801;
    /**
     * 农历月
     */
    public static final int CHINESE_MONTH = 802;
    /**
     * 农历日
     */
    public static final int CHINESE_DATE = 803;
    /**
     * 当月的节气对应的公历日(前一个节气)
     */
    public static final int CHINESE_SECTIONAL_TERM = 804;
    /**
     * 当月的中气对应的公历日(后一个节气)
     */
    public static final int CHINESE_PRINCIPLE_TERM = 805;
    /**
     * 天干
     */
    public static final int CHINESE_HEAVENLY_STEM = 806;
    /**
     * 地支
     */
    public static final int CHINESE_EARTHLY_BRANCH = 807;
    /**
     * 农历年的属相(生肖)
     */
    public static final int CHINESE_ZODIAC = 808;
    /**
     * 节气或者农历日
     */
    public static final int CHINESE_TERM_OR_DATE = 888;


    // add by skywang
    /**
     * 农历节日
     */
    public static final int LUNAR_FESTIVAL = 809;
    /**
     * 佛教节日
     */
    public static final int FO_FESTIVAL = 814;
    /**
     * 观音斋期
     */
    public static final int GUAN_FESTIVAL = 815;
    /**
     * 阳历节日
     */
    public static final int SOLAR_FESTIVAL = 810;
    /**
     * 节气
     */
    public static final int CHINESE_TERM = 811;
    /**
     * 月或者农历日
     */
    public static final int CHINESE_MONTH_OR_DATE = 812;
    /**
     * 节日 或 节气 或 农历日
     */
    public static final int FESTIVAL_OR_TERM_OR_DATE = 813;

    private int chineseYear;
    private int chineseMonth; // 1起始，负数表示闰月
    private int chineseDate;
    private int sectionalTerm; // 当月节气的公历日
    private int principleTerm; // 当月中气的公历日

    private boolean areChineseFieldsComputed; // 农历日期是否已经经过计算确认
    private boolean areSolarTermsComputed; // 节气是否已经经过计算确认
    private boolean lastSetChinese; // 最后设置的是不是农历属性

    /**
     * 使用当前时间构造一个实例。
     */
    public ChineseCalendar() {
        super();
    }

    /**
     * 使用指定时间构造一个实例。
     */
    public ChineseCalendar(Date d) {
        super.setTime(d);
    }

    /**
     * 使用指定时间构造一个实例。
     */
    public ChineseCalendar(Calendar c) {
        this(c.getTime());
    }

    /**
     * 使用指定公历日期构造一个实例。
     */
    public ChineseCalendar(int y, int m, int d) {
        super(y, m, d);
    }

    /**
     * 使用指定日期构造一个实例。
     *
     * @param isChinese 是否为农历日期
     * @param y
     * @param m
     * @param d
     */
    public ChineseCalendar(boolean isChinese, int y, int m, int d) {
        if (isChinese) {
            set(CHINESE_YEAR, y);
            set(CHINESE_MONTH, m);
            set(CHINESE_DATE, d);
        } else {
            set(y, m, d);
        }
    }

    public void set(int field, int value) {
        computeIfNeed(field);

        if (isChineseField(field)) {
            // 农历属性
            switch (field) {
                case CHINESE_YEAR:
                    chineseYear = value;
                    break;
                case CHINESE_MONTH:
                    chineseMonth = value;
                    break;
                case CHINESE_DATE:
                    chineseDate = value;
                    break;
                default:
                    throw new IllegalArgumentException("不支持的field设置：" + field);
            }
            lastSetChinese = true;
        } else {
            // 非农历属性
            super.set(field, value);
            lastSetChinese = false;
        }
        areFieldsSet = false;
        areChineseFieldsComputed = false;
        areSolarTermsComputed = false;
    }

    public int get(int field) {
        computeIfNeed(field);

        if (!isChineseField(field)) {
            return super.get(field);
        }

        switch (field) {
            case CHINESE_YEAR:
                return chineseYear;
            case CHINESE_MONTH:
                return chineseMonth;
            case CHINESE_DATE:
                return chineseDate;
            case CHINESE_SECTIONAL_TERM:
                return sectionalTerm;
            case CHINESE_PRINCIPLE_TERM:
                return principleTerm;
            case CHINESE_HEAVENLY_STEM:
                return (chineseYear - 4) % 10 + 1;
            case CHINESE_EARTHLY_BRANCH:
            case CHINESE_ZODIAC:
                return (chineseYear - 4) % 12 + 1;
            case CHINESE_MONTH_OR_DATE:
                if (get(CHINESE_DATE) == 1) {
                    return CHINESE_MONTH;
                } else {
                    return CHINESE_DATE;
                }
            case CHINESE_TERM_OR_DATE:
                int option;
                if (get(Calendar.DATE) == get(CHINESE_SECTIONAL_TERM)) {
                    option = CHINESE_SECTIONAL_TERM;
                } else if (get(Calendar.DATE) == get(CHINESE_PRINCIPLE_TERM)) {
                    option = CHINESE_PRINCIPLE_TERM;
                } else if (get(CHINESE_DATE) == 1) {
                    option = CHINESE_MONTH;
                } else {
                    option = CHINESE_DATE;
                }
                return option;
            default:
                throw new IllegalArgumentException("不支持的field获取：" + field);
        }
    }

    public void add(int field, int amount) {
        computeIfNeed(field);

        if (!isChineseField(field)) {
            super.add(field, amount);
            lastSetChinese = false;
            areChineseFieldsComputed = false;
            areSolarTermsComputed = false;
            return;
        }

        switch (field) {
            case CHINESE_YEAR:
                chineseYear += amount;
                break;
            case CHINESE_MONTH:
                for (int i = 0; i < amount; i++) {
                    chineseMonth = nextChineseMonth(chineseYear, chineseMonth);
                    if (chineseMonth == 1) {
                        chineseYear++;
                    }
                }
                break;
            case CHINESE_DATE:
                int maxDate = daysInChineseMonth(chineseYear, chineseMonth);
                for (int i = 0; i < amount; i++) {
                    chineseDate++;
                    if (chineseDate > maxDate) {
                        chineseDate = 1;
                        chineseMonth = nextChineseMonth(chineseYear, chineseMonth);
                        if (chineseMonth == 1) {
                            chineseYear++;
                        }
                        maxDate = daysInChineseMonth(chineseYear, chineseMonth);
                    }
                }
            default:
                throw new IllegalArgumentException("不支持的field：" + field);
        }

        lastSetChinese = true;
        areFieldsSet = false;
        areChineseFieldsComputed = false;
        areSolarTermsComputed = false;
    }

    public void roll(int field, int amount) {
        computeIfNeed(field);

        if (!isChineseField(field)) {
            super.roll(field, amount);
            lastSetChinese = false;
            areChineseFieldsComputed = false;
            areSolarTermsComputed = false;
            return;
        }

        switch (field) {
            case CHINESE_YEAR:
                chineseYear += amount;
                break;
            case CHINESE_MONTH:
                for (int i = 0; i < amount; i++) {
                    chineseMonth = nextChineseMonth(chineseYear, chineseMonth);
                }
                break;
            case CHINESE_DATE:
                int maxDate = daysInChineseMonth(chineseYear, chineseMonth);
                for (int i = 0; i < amount; i++) {
                    chineseDate++;
                    if (chineseDate > maxDate) {
                        chineseDate = 1;
                    }
                }
            default:
                throw new IllegalArgumentException("不支持的field：" + field);
        }

        lastSetChinese = true;
        areFieldsSet = false;
        areChineseFieldsComputed = false;
        areSolarTermsComputed = false;
    }

    /**
     * 获得属性的中文，可以使用的属性字段为DAY_OF_WEEK以及所有农历属性字段。
     *
     * @param field
     * @return
     */
    public String getChinese(int field) {
        computeIfNeed(field);

        switch (field) {
            case CHINESE_YEAR:
                return getChinese(CHINESE_HEAVENLY_STEM)
                        + getChinese(CHINESE_EARTHLY_BRANCH) + "年";
            case CHINESE_MONTH:
                if (chineseMonth > 0)
                    return chineseMonthNames[chineseMonth] + "月";
                else
                    return "闰" + chineseMonthNames[-chineseMonth] + "月";
            case CHINESE_DATE:
                return chineseDateNames[chineseDate];
            case CHINESE_SECTIONAL_TERM:
                return sectionalTermNames[get(Calendar.MONTH)];
            case CHINESE_PRINCIPLE_TERM:
                return principleTermNames[get(Calendar.MONTH)];
            case CHINESE_HEAVENLY_STEM:
                return stemNames[get(field)];
            case CHINESE_EARTHLY_BRANCH:
                return branchNames[get(field)];
            case CHINESE_ZODIAC:
                return animalNames[get(field)];
            case Calendar.DAY_OF_WEEK:
                return chineseWeekNames[get(field)];
            case CHINESE_TERM_OR_DATE:
                return getChinese(get(CHINESE_TERM_OR_DATE));
            case LUNAR_FESTIVAL:
                return getLunarFestival();
            case FO_FESTIVAL:
                return getFoFestival();
            case GUAN_FESTIVAL:
                return getGuanFestival();
            case SOLAR_FESTIVAL:
                return getSolarFestival();
            case FESTIVAL_OR_TERM_OR_DATE:
                return getFestivalOrTermOrDate();
            // TODO CHECK
            case CHINESE_MONTH_OR_DATE:
                return getChinese(get(CHINESE_MONTH_OR_DATE));
            case CHINESE_TERM:
                return getChineseTerm();
            default:
                throw new IllegalArgumentException("不支持的field中文获取：" + field);
        }
    }

    public String getSimpleGregorianDateString() {
        return new StringBuffer().append(get(YEAR)).append("-")
                .append(get(MONTH) + 1).append("-").append(get(DATE))
                .toString();
    }

    public String getSimpleChineseDateString() {
        return new StringBuffer()
                .append(get(CHINESE_YEAR))
                .append("-")
                .append(get(CHINESE_MONTH) > 0 ? "" + get(CHINESE_MONTH) : "*"
                        + (-get(CHINESE_MONTH))).append("-")
                .append(get(CHINESE_DATE)).toString();
    }

    public String getChineseDateString() {
        return new StringBuffer().append(getChinese(CHINESE_YEAR))
                .append(getChinese(CHINESE_MONTH))
                .append(getChinese(CHINESE_DATE)).toString();
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(getSimpleGregorianDateString()).append(" | ")
                .append(getChinese(DAY_OF_WEEK)).append(" | [农历]")
                .append(getChineseDateString()).append(" ")
                .append(getChinese(CHINESE_ZODIAC)).append("年 ")
                .append(get(CHINESE_SECTIONAL_TERM)).append("日")
                .append(getChinese(CHINESE_SECTIONAL_TERM)).append(" ")
                .append(get(CHINESE_PRINCIPLE_TERM)).append("日")
                .append(getChinese(CHINESE_PRINCIPLE_TERM));
        return buf.toString();
    }

    /**
     * 判断是不是农历属性
     *
     * @param field
     * @return
     */
    private boolean isChineseField(int field) {
        switch (field) {
            case CHINESE_YEAR:
            case CHINESE_MONTH:
            case CHINESE_DATE:
            case CHINESE_SECTIONAL_TERM:
            case CHINESE_PRINCIPLE_TERM:
            case CHINESE_HEAVENLY_STEM:
            case CHINESE_EARTHLY_BRANCH:
            case CHINESE_ZODIAC:
            case CHINESE_TERM_OR_DATE:
            case CHINESE_MONTH_OR_DATE:
                return true;
            default:
                return false;
        }
    }

    /**
     * 判断是不是与节气有关的属性
     *
     * @param field
     * @return
     */
    private boolean isChineseTermsField(int field) {
        switch (field) {
            case CHINESE_SECTIONAL_TERM:
            case CHINESE_PRINCIPLE_TERM:
            case CHINESE_TERM_OR_DATE:
                return true;
            default:
                return false;
        }
    }

    /**
     * 如果上一次设置的与这次将要设置或获取的属性不是同一类（农历/公历），<br>
     * 例如上一次设置的是农历而现在要设置或获取公历，<br>
     * 则需要先根据之前设置的农历日期计算出公历日期。
     *
     * @param field
     */
    private void computeIfNeed(int field) {
        if (isChineseField(field)) {
            if (!lastSetChinese && !areChineseFieldsComputed) {
                super.complete();
                computeChineseFields();
                areFieldsSet = true;
                areChineseFieldsComputed = true;
                areSolarTermsComputed = false;
            }
            if (isChineseTermsField(field) && !areSolarTermsComputed) {
                computeSolarTerms();
                areSolarTermsComputed = true;
            }
        } else {
            if (lastSetChinese && !areFieldsSet) {
                computeGregorianFields();
                super.complete();
                areFieldsSet = true;
                areChineseFieldsComputed = true;
                areSolarTermsComputed = false;
            }
        }
    }

    /**
     * 使用农历日期计算出公历日期
     */
    private void computeGregorianFields() {
        int y = chineseYear;
        int m = chineseMonth;
        int d = chineseDate;
        areChineseFieldsComputed = true;
        areFieldsSet = true;
        lastSetChinese = false;

        // 调整日期范围
        if (y < 1900)
            y = 1899;
        else if (y > 2100)
            y = 2101;

        if (m < -12)
            m = -12;
        else if (m > 12)
            m = 12;

        if (d < 1)
            d = 1;
        else if (d > 30)
            d = 30;

        int dateint = y * 10000 + Math.abs(m) * 100 + d;
        if (dateint < 19001111) { // 太小
            set(1901, Calendar.JANUARY, 1);
            super.complete();
        } else if (dateint > 21001201) { // 太大
            set(2100, Calendar.DECEMBER, 31);
            super.complete();
        } else {
            if (Math.abs(m) > 12) {
                m = 12;
            }
            int days = ChineseCalendar.daysInChineseMonth(y, m);
            if (days == 0) {
                m = -m;
                days = ChineseCalendar.daysInChineseMonth(y, m);
            }
            if (d > days) {
                d = days;
            }
            set(y, Math.abs(m) - 1, d);
            computeChineseFields();

            int amount = 0;
            while (chineseYear != y || chineseMonth != m) {
                amount += daysInChineseMonth(chineseYear, chineseMonth);
                chineseMonth = nextChineseMonth(chineseYear, chineseMonth);
                if (chineseMonth == 1) {
                    chineseYear++;
                }
            }
            amount += d - chineseDate;

            super.add(Calendar.DATE, amount);
        }
        computeChineseFields();
    }

    /**
     * 使用公历日期计算出农历日期
     */
    void computeChineseFields() {
        int gregorianYear = internalGet(Calendar.YEAR);
        int gregorianMonth = internalGet(Calendar.MONTH) + 1;
        int gregorianDate = internalGet(Calendar.DATE);

        if (gregorianYear < 1901 || gregorianYear > 2100) {
            return;
        }

        int startYear, startMonth, startDate;
        if (gregorianYear < 2000) {
            startYear = baseYear;
            startMonth = baseMonth;
            startDate = baseDate;
            chineseYear = baseChineseYear;
            chineseMonth = baseChineseMonth;
            chineseDate = baseChineseDate;
        } else {
            // 第二个对应日，用以提高计算效率
            // 公历 2000 年 1 月 1 日，对应农历 4697(1999) 年 11 月 25 日
            startYear = baseYear + 99;
            startMonth = 1;
            startDate = 1;
            chineseYear = baseChineseYear + 99;
            chineseMonth = 11;
            chineseDate = 25;
        }

        int daysDiff = 0;

        // 年
        for (int i = startYear; i < gregorianYear; i++) {
            if (isGregorianLeapYear(i)) {
                daysDiff += 366; // leap year
            } else {
                daysDiff += 365;
            }
        }

        // 月
        for (int i = startMonth; i < gregorianMonth; i++) {
            daysDiff += daysInGregorianMonth(gregorianYear, i - 1);
        }

        // 日
        daysDiff += gregorianDate - startDate;

        chineseDate += daysDiff;

        int lastDate = daysInChineseMonth(chineseYear, chineseMonth);
        while (chineseDate > lastDate) {
            chineseDate -= lastDate;
            chineseMonth = nextChineseMonth(chineseYear, chineseMonth);
            if (chineseMonth == 1) {
                chineseYear++;
            }
            lastDate = daysInChineseMonth(chineseYear, chineseMonth);
        }

    }

    /**
     * 计算节气
     */
    private void computeSolarTerms() {
        int gregorianYear = internalGet(Calendar.YEAR);
        int gregorianMonth = internalGet(Calendar.MONTH);

        if (gregorianYear < 1901 || gregorianYear > 2100) {
            return;
        }
        sectionalTerm = sectionalTerm(gregorianYear, gregorianMonth);
        principleTerm = principleTerm(gregorianYear, gregorianMonth);
    }

    /* 接下来是静态方法~ */

    /**
     * 是否为公历闰年
     *
     * @param year
     * @return
     */
    public static boolean isGregorianLeapYear(int year) {
        boolean isLeap = false;
        if (year % 4 == 0) {
            isLeap = true;
        }
        if (year % 100 == 0) {
            isLeap = false;
        }
        if (year % 400 == 0) {
            isLeap = true;
        }
        return isLeap;
    }

    /**
     * 计算公历年的当月天数，公历月从0起始！
     *
     * @param y
     * @param m
     * @return
     */
    public static int daysInGregorianMonth(int y, int m) {
        int d = daysInGregorianMonth[m];
        if (m == Calendar.FEBRUARY && isGregorianLeapYear(y)) {
            d++; // 公历闰年二月多一天
        }
        return d;
    }

    /**
     * 计算公历年当月的节气，公历月从0起始！
     *
     * @param y
     * @param m
     * @return
     */
    public static int sectionalTerm(int y, int m) {
        m++;
        if (y < 1901 || y > 2100) {
            return 0;
        }
        int index = 0;
        int ry = y - baseYear + 1;
        while (ry >= sectionalTermYear[m - 1][index]) {
            index++;
        }
        int term = sectionalTermMap[m - 1][4 * index + ry % 4];
        if ((ry == 121) && (m == 4)) {
            term = 5;
        }
        if ((ry == 132) && (m == 4)) {
            term = 5;
        }
        if ((ry == 194) && (m == 6)) {
            term = 6;
        }
        return term;
    }

    /**
     * 计算公历年当月的中气，公历月从0起始！
     *
     * @param y
     * @param m
     * @return
     */
    public static int principleTerm(int y, int m) {
        m++;
        if (y < 1901 || y > 2100) {
            return 0;
        }
        int index = 0;
        int ry = y - baseYear + 1;
        while (ry >= principleTermYear[m - 1][index]) {
            index++;
        }
        int term = principleTermMap[m - 1][4 * index + ry % 4];
        if ((ry == 171) && (m == 3)) {
            term = 21;
        }
        if ((ry == 181) && (m == 5)) {
            term = 21;
        }
        return term;
    }

    /**
     * 计算农历年的天数
     *
     * @param y
     * @param m
     * @return
     */
    public static int daysInChineseMonth(int y, int m) {
        // 注意：闰月 m < 0
        int index = y - baseChineseYear + baseIndex;
        int v = 0;
        int l = 0;
        int d = 30;
        if (1 <= m && m <= 8) {
            v = chineseMonths[2 * index];
            l = m - 1;
            if (((v >> l) & 0x01) == 1) {
                d = 29;
            }
        } else if (9 <= m && m <= 12) {
            v = chineseMonths[2 * index + 1];
            l = m - 9;
            if (((v >> l) & 0x01) == 1) {
                d = 29;
            }
        } else {
            v = chineseMonths[2 * index + 1];
            v = (v >> 4) & 0x0F;
            if (v != Math.abs(m)) {
                d = 0;
            } else {
                d = 29;
                for (int i = 0; i < bigLeapMonthYears.length; i++) {
                    if (bigLeapMonthYears[i] == index) {
                        d = 30;
                        break;
                    }
                }
            }
        }
        return d;
    }

    /**
     * 计算农历的下个月
     *
     * @param y
     * @param m
     * @return
     */
    public static int nextChineseMonth(int y, int m) {
        int n = Math.abs(m) + 1;
        if (m > 0) {
            int index = y - baseChineseYear + baseIndex;
            int v = chineseMonths[2 * index + 1];
            v = (v >> 4) & 0x0F;
            if (v == m) {
                n = -m;
            }
        }
        if (n == 13) {
            n = 1;
        }
        return n;
    }
//    /**
//     * 计算农历年的月份数
//     * @param y
//     * @return
//     */
//    public static int monthsInChineseYear(int y){
//    	int month=0;
//    	int index=1;
//    	while(true){
//    		month=nextChineseMonth(y, index);
//    		index++;
//    		if(month==1)
//    			break;
//    	}
//    	return index;
//    }

    /* 日历第一天的日期 */
    private static final int baseYear = 1901;
    private static final int baseMonth = 1;
    private static final int baseDate = 1;
    private static final int baseIndex = 0;
    private static final int baseChineseYear = 1900;
    private static final int baseChineseMonth = 11;
    private static final int baseChineseDate = 11;

    /* 中文字符串 */
    public static final String[] chineseWeekNames = {"", "星期日", "星期一", "星期二",
            "星期三", "星期四", "星期五", "星期六"};

//    public static final String[] chineseMonthNames = { "", "正", "二", "三", "四",
//            "五", "六", "七", "八", "九", "十", "十一", "十二" };
//    public static final String[] chineseDateNames = { "", "初一", "初二", "初三",
//            "初四", "初五", "初六", "初七", "初八", "初九", "初十", "十一", "十二", "十三", "十四",
//            "十五", "十六", "十七", "十八", "十九", "二十", "廿一", "廿二", "廿三", "廿四", "廿五",
//            "廿六", "廿七", "廿八", "廿九", "三十" };

    public static final String[] chineseMonthNames = {"", "一", "二", "三", "四",
            "五", "六", "七", "八", "九", "十", "冬", "腊"};
    public static final String[] chineseMonthNames_1 = {"一月", "二月", "三月", "四月",
            "五月", "六月", "七月", "八月", "九月", "十月", "冬月", "腊月"};
    public static final String[] chineseDateNames = {"", "初一", "初二", "初三",
            "初四", "初五", "初六", "初七", "初八", "初九", "初十", "十一", "十二", "十三", "十四",
            "十五", "十六", "十七", "十八", "十九", "二十", "廿一", "廿二", "廿三", "廿四", "廿五",
            "廿六", "廿七", "廿八", "廿九", "三十"};
    public static final String[] chineseDateNames_1 = {"初一", "初二", "初三",
            "初四", "初五", "初六", "初七", "初八", "初九", "初十", "十一", "十二", "十三", "十四",
            "十五", "十六", "十七", "十八", "十九", "二十", "廿一", "廿二", "廿三", "廿四", "廿五",
            "廿六", "廿七", "廿八", "廿九", "三十"};
    public static final String[] principleTermNames = {"大寒", "雨水", "春分",
            "谷雨", "夏满", "夏至", "大暑", "处暑", "秋分", "霜降", "小雪", "冬至"};
    public static final String[] sectionalTermNames = {"小寒", "立春", "惊蛰",
            "清明", "立夏", "芒种", "小暑", "立秋", "白露", "寒露", "立冬", "大雪"};
    public static final String[] stemNames = {"", "甲", "乙", "丙", "丁", "戊",
            "己", "庚", "辛", "壬", "癸"};
    public static final String[] branchNames = {"", "子", "丑", "寅", "卯", "辰",
            "巳", "午", "未", "申", "酉", "戌", "亥"};
    public static final String[] animalNames = {"", "鼠", "牛", "虎", "兔", "龙",
            "蛇", "马", "羊", "猴", "鸡", "狗", "猪"};

    /* 接下来是数据压缩表~ */
    private static final int[] bigLeapMonthYears = {6, 14, 19, 25, 33, 36, 38,
            41, 44, 52, 55, 79, 117, 136, 147, 150, 155, 158, 185, 193};
    private static final char[][] sectionalTermMap = {
            {7, 6, 6, 6, 6, 6, 6, 6, 6, 5, 6, 6, 6, 5, 5, 6, 6, 5, 5, 5, 5, 5,
                    5, 5, 5, 4, 5, 5},
            {5, 4, 5, 5, 5, 4, 4, 5, 5, 4, 4, 4, 4, 4, 4, 4, 4, 3, 4, 4, 4, 3,
                    3, 4, 4, 3, 3, 3},
            {6, 6, 6, 7, 6, 6, 6, 6, 5, 6, 6, 6, 5, 5, 6, 6, 5, 5, 5, 6, 5, 5,
                    5, 5, 4, 5, 5, 5, 5},
            {5, 5, 6, 6, 5, 5, 5, 6, 5, 5, 5, 5, 4, 5, 5, 5, 4, 4, 5, 5, 4, 4,
                    4, 5, 4, 4, 4, 4, 5},
            {6, 6, 6, 7, 6, 6, 6, 6, 5, 6, 6, 6, 5, 5, 6, 6, 5, 5, 5, 6, 5, 5,
                    5, 5, 4, 5, 5, 5, 5},
            {6, 6, 7, 7, 6, 6, 6, 7, 6, 6, 6, 6, 5, 6, 6, 6, 5, 5, 6, 6, 5, 5,
                    5, 6, 5, 5, 5, 5, 4, 5, 5, 5, 5},
            {7, 8, 8, 8, 7, 7, 8, 8, 7, 7, 7, 8, 7, 7, 7, 7, 6, 7, 7, 7, 6, 6,
                    7, 7, 6, 6, 6, 7, 7},
            {8, 8, 8, 9, 8, 8, 8, 8, 7, 8, 8, 8, 7, 7, 8, 8, 7, 7, 7, 8, 7, 7,
                    7, 7, 6, 7, 7, 7, 6, 6, 7, 7, 7},
            {8, 8, 8, 9, 8, 8, 8, 8, 7, 8, 8, 8, 7, 7, 8, 8, 7, 7, 7, 8, 7, 7,
                    7, 7, 6, 7, 7, 7, 7},
            {9, 9, 9, 9, 8, 9, 9, 9, 8, 8, 9, 9, 8, 8, 8, 9, 8, 8, 8, 8, 7, 8,
                    8, 8, 7, 7, 8, 8, 8},
            {8, 8, 8, 8, 7, 8, 8, 8, 7, 7, 8, 8, 7, 7, 7, 8, 7, 7, 7, 7, 6, 7,
                    7, 7, 6, 6, 7, 7, 7},
            {7, 8, 8, 8, 7, 7, 8, 8, 7, 7, 7, 8, 7, 7, 7, 7, 6, 7, 7, 7, 6, 6,
                    7, 7, 6, 6, 6, 7, 7}};
    private static final char[][] sectionalTermYear = {
            {13, 49, 85, 117, 149, 185, 201, 250, 250},
            {13, 45, 81, 117, 149, 185, 201, 250, 250},
            {13, 48, 84, 112, 148, 184, 200, 201, 250},
            {13, 45, 76, 108, 140, 172, 200, 201, 250},
            {13, 44, 72, 104, 132, 168, 200, 201, 250},
            {5, 33, 68, 96, 124, 152, 188, 200, 201},
            {29, 57, 85, 120, 148, 176, 200, 201, 250},
            {13, 48, 76, 104, 132, 168, 196, 200, 201},
            {25, 60, 88, 120, 148, 184, 200, 201, 250},
            {16, 44, 76, 108, 144, 172, 200, 201, 250},
            {28, 60, 92, 124, 160, 192, 200, 201, 250},
            {17, 53, 85, 124, 156, 188, 200, 201, 250}};
    private static final char[][] principleTermMap = {
            {21, 21, 21, 21, 21, 20, 21, 21, 21, 20, 20, 21, 21, 20, 20, 20,
                    20, 20, 20, 20, 20, 19, 20, 20, 20, 19, 19, 20},
            {20, 19, 19, 20, 20, 19, 19, 19, 19, 19, 19, 19, 19, 18, 19, 19,
                    19, 18, 18, 19, 19, 18, 18, 18, 18, 18, 18, 18},
            {21, 21, 21, 22, 21, 21, 21, 21, 20, 21, 21, 21, 20, 20, 21, 21,
                    20, 20, 20, 21, 20, 20, 20, 20, 19, 20, 20, 20, 20},
            {20, 21, 21, 21, 20, 20, 21, 21, 20, 20, 20, 21, 20, 20, 20, 20,
                    19, 20, 20, 20, 19, 19, 20, 20, 19, 19, 19, 20, 20},
            {21, 22, 22, 22, 21, 21, 22, 22, 21, 21, 21, 22, 21, 21, 21, 21,
                    20, 21, 21, 21, 20, 20, 21, 21, 20, 20, 20, 21, 21},
            {22, 22, 22, 22, 21, 22, 22, 22, 21, 21, 22, 22, 21, 21, 21, 22,
                    21, 21, 21, 21, 20, 21, 21, 21, 20, 20, 21, 21, 21},
            {23, 23, 24, 24, 23, 23, 23, 24, 23, 23, 23, 23, 22, 23, 23, 23,
                    22, 22, 23, 23, 22, 22, 22, 23, 22, 22, 22, 22, 23},
            {23, 24, 24, 24, 23, 23, 24, 24, 23, 23, 23, 24, 23, 23, 23, 23,
                    22, 23, 23, 23, 22, 22, 23, 23, 22, 22, 22, 23, 23},
            {23, 24, 24, 24, 23, 23, 24, 24, 23, 23, 23, 24, 23, 23, 23, 23,
                    22, 23, 23, 23, 22, 22, 23, 23, 22, 22, 22, 23, 23},
            {24, 24, 24, 24, 23, 24, 24, 24, 23, 23, 24, 24, 23, 23, 23, 24,
                    23, 23, 23, 23, 22, 23, 23, 23, 22, 22, 23, 23, 23},
            {23, 23, 23, 23, 22, 23, 23, 23, 22, 22, 23, 23, 22, 22, 22, 23,
                    22, 22, 22, 22, 21, 22, 22, 22, 21, 21, 22, 22, 22},
            {22, 22, 23, 23, 22, 22, 22, 23, 22, 22, 22, 22, 21, 22, 22, 22,
                    21, 21, 22, 22, 21, 21, 21, 22, 21, 21, 21, 21, 22}};
    private static final char[][] principleTermYear = {
            {13, 45, 81, 113, 149, 185, 201},
            {21, 57, 93, 125, 161, 193, 201},
            {21, 56, 88, 120, 152, 188, 200, 201},
            {21, 49, 81, 116, 144, 176, 200, 201},
            {17, 49, 77, 112, 140, 168, 200, 201},
            {28, 60, 88, 116, 148, 180, 200, 201},
            {25, 53, 84, 112, 144, 172, 200, 201},
            {29, 57, 89, 120, 148, 180, 200, 201},
            {17, 45, 73, 108, 140, 168, 200, 201},
            {28, 60, 92, 124, 160, 192, 200, 201},
            {16, 44, 80, 112, 148, 180, 200, 201},
            {17, 53, 88, 120, 156, 188, 200, 201}};

    private static final char[] daysInGregorianMonth = {31, 28, 31, 30, 31,
            30, 31, 31, 30, 31, 30, 31};
    private static final char[] chineseMonths = {0x00, 0x04, 0xad, 0x08, 0x5a,
            0x01, 0xd5, 0x54, 0xb4, 0x09, 0x64, 0x05, 0x59, 0x45, 0x95, 0x0a,
            0xa6, 0x04, 0x55, 0x24, 0xad, 0x08, 0x5a, 0x62, 0xda, 0x04, 0xb4,
            0x05, 0xb4, 0x55, 0x52, 0x0d, 0x94, 0x0a, 0x4a, 0x2a, 0x56, 0x02,
            0x6d, 0x71, 0x6d, 0x01, 0xda, 0x02, 0xd2, 0x52, 0xa9, 0x05, 0x49,
            0x0d, 0x2a, 0x45, 0x2b, 0x09, 0x56, 0x01, 0xb5, 0x20, 0x6d, 0x01,
            0x59, 0x69, 0xd4, 0x0a, 0xa8, 0x05, 0xa9, 0x56, 0xa5, 0x04, 0x2b,
            0x09, 0x9e, 0x38, 0xb6, 0x08, 0xec, 0x74, 0x6c, 0x05, 0xd4, 0x0a,
            0xe4, 0x6a, 0x52, 0x05, 0x95, 0x0a, 0x5a, 0x42, 0x5b, 0x04, 0xb6,
            0x04, 0xb4, 0x22, 0x6a, 0x05, 0x52, 0x75, 0xc9, 0x0a, 0x52, 0x05,
            0x35, 0x55, 0x4d, 0x0a, 0x5a, 0x02, 0x5d, 0x31, 0xb5, 0x02, 0x6a,
            0x8a, 0x68, 0x05, 0xa9, 0x0a, 0x8a, 0x6a, 0x2a, 0x05, 0x2d, 0x09,
            0xaa, 0x48, 0x5a, 0x01, 0xb5, 0x09, 0xb0, 0x39, 0x64, 0x05, 0x25,
            0x75, 0x95, 0x0a, 0x96, 0x04, 0x4d, 0x54, 0xad, 0x04, 0xda, 0x04,
            0xd4, 0x44, 0xb4, 0x05, 0x54, 0x85, 0x52, 0x0d, 0x92, 0x0a, 0x56,
            0x6a, 0x56, 0x02, 0x6d, 0x02, 0x6a, 0x41, 0xda, 0x02, 0xb2, 0xa1,
            0xa9, 0x05, 0x49, 0x0d, 0x0a, 0x6d, 0x2a, 0x09, 0x56, 0x01, 0xad,
            0x50, 0x6d, 0x01, 0xd9, 0x02, 0xd1, 0x3a, 0xa8, 0x05, 0x29, 0x85,
            0xa5, 0x0c, 0x2a, 0x09, 0x96, 0x54, 0xb6, 0x08, 0x6c, 0x09, 0x64,
            0x45, 0xd4, 0x0a, 0xa4, 0x05, 0x51, 0x25, 0x95, 0x0a, 0x2a, 0x72,
            0x5b, 0x04, 0xb6, 0x04, 0xac, 0x52, 0x6a, 0x05, 0xd2, 0x0a, 0xa2,
            0x4a, 0x4a, 0x05, 0x55, 0x94, 0x2d, 0x0a, 0x5a, 0x02, 0x75, 0x61,
            0xb5, 0x02, 0x6a, 0x03, 0x61, 0x45, 0xa9, 0x0a, 0x4a, 0x05, 0x25,
            0x25, 0x2d, 0x09, 0x9a, 0x68, 0xda, 0x08, 0xb4, 0x09, 0xa8, 0x59,
            0x54, 0x03, 0xa5, 0x0a, 0x91, 0x3a, 0x96, 0x04, 0xad, 0xb0, 0xad,
            0x04, 0xda, 0x04, 0xf4, 0x62, 0xb4, 0x05, 0x54, 0x0b, 0x44, 0x5d,
            0x52, 0x0a, 0x95, 0x04, 0x55, 0x22, 0x6d, 0x02, 0x5a, 0x71, 0xda,
            0x02, 0xaa, 0x05, 0xb2, 0x55, 0x49, 0x0b, 0x4a, 0x0a, 0x2d, 0x39,
            0x36, 0x01, 0x6d, 0x80, 0x6d, 0x01, 0xd9, 0x02, 0xe9, 0x6a, 0xa8,
            0x05, 0x29, 0x0b, 0x9a, 0x4c, 0xaa, 0x08, 0xb6, 0x08, 0xb4, 0x38,
            0x6c, 0x09, 0x54, 0x75, 0xd4, 0x0a, 0xa4, 0x05, 0x45, 0x55, 0x95,
            0x0a, 0x9a, 0x04, 0x55, 0x44, 0xb5, 0x04, 0x6a, 0x82, 0x6a, 0x05,
            0xd2, 0x0a, 0x92, 0x6a, 0x4a, 0x05, 0x55, 0x0a, 0x2a, 0x4a, 0x5a,
            0x02, 0xb5, 0x02, 0xb2, 0x31, 0x69, 0x03, 0x31, 0x73, 0xa9, 0x0a,
            0x4a, 0x05, 0x2d, 0x55, 0x2d, 0x09, 0x5a, 0x01, 0xd5, 0x48, 0xb4,
            0x09, 0x68, 0x89, 0x54, 0x0b, 0xa4, 0x0a, 0xa5, 0x6a, 0x95, 0x04,
            0xad, 0x08, 0x6a, 0x44, 0xda, 0x04, 0x74, 0x05, 0xb0, 0x25, 0x54,
            0x03};

    private String getChineseTerm() {
        if (get(Calendar.DATE) == get(CHINESE_SECTIONAL_TERM)) {
            return sectionalTermNames[get(Calendar.MONTH)];
        } else if (get(Calendar.DATE) == get(CHINESE_PRINCIPLE_TERM)) {
            return principleTermNames[get(Calendar.MONTH)];
        } else
            return null;
    }

    // add by skywang
    private String getLunarFestival() {
        return lFestival.get(getKey());
    }

    private String getKey() {
        int day = get(CHINESE_DATE);
        int month = get(CHINESE_MONTH);
        String sToday = day < 10 ? "0" + day : "" + day;
        String sMonth = month < 10 ? "0" + (month) : "" + (month);
        return sMonth + sToday;
    }
    private String getFoFestival() {
        return fFestival.get(getKey());
    }
    private String getGuanFestival() {
        return gFestival.get(getKey());
    }

    private String getSolarFestival() {
        int day = get(Calendar.DATE);
        int month = get(Calendar.MONTH);
        String sToday = day < 10 ? "0" + day : "" + day;
        String sMonth = month < 10 ? "0" + (month + 1) : "" + (month + 1);

        return sFestival.get(sMonth + sToday);
    }

    private String getFestivalOrTermOrDate() {
        String ret;
        if ((ret = getSolarFestival()) != null)
            return ret;
        if ((ret = getLunarFestival()) != null)
            return ret;
        return getChinese(get(CHINESE_TERM_OR_DATE));
    }


    //公历节日
    private static Map<String, String> sFestival = new HashMap<String, String>();
    // 农历节日
    private static Map<String, String> lFestival = new HashMap<String, String>();
    //佛教节日
    private static Map<String, String> fFestival = new HashMap<String, String>();
    //观音斋期
    private static Map<String, String> gFestival = new HashMap<String, String>();

    /**
     * 增加阳历节日
     *
     * @param day
     * @param title
     */
    public static void addS(String day, String title) {
        sFestival.put(day, title);
    }

    /**
     * 增加阴历节日
     *
     * @param day
     * @param title
     */
    public static void addL(String day, String title) {
        lFestival.put(day, title);
    }

    static {
        sFestival.put("0101", "元旦");
        sFestival.put("0214", "情人节");
        sFestival.put("0308", "妇女节");
        sFestival.put("0312", "植树节");
        sFestival.put("0401", "愚人节");
        sFestival.put("0501", "劳动节");
        sFestival.put("0504", "青年节");
        sFestival.put("0601", "儿童节");
        sFestival.put("0701", "建党节");
        sFestival.put("0801", "建军节");
        sFestival.put("0910", "教师节");
        sFestival.put("1001", "国庆节");
        sFestival.put("1031", "万圣节");
        sFestival.put("1225", "圣诞节");
        //农历
        lFestival.put("0101", "春节");
        lFestival.put("0115", "元宵");
        lFestival.put("0505", "端午");
        lFestival.put("0707", "七夕");
        lFestival.put("0815", "中秋");
        lFestival.put("0909", "重阳");

        fFestival.put("0101", "弥勒菩萨圣诞");
        fFestival.put("0208", "释迦摩尼佛出家");
        fFestival.put("0215", "释迦摩尼佛涅槃");
        fFestival.put("0219", "观音菩萨圣诞");
        fFestival.put("0221", "普贤菩萨圣诞");
        fFestival.put("0404", "文殊菩萨圣诞");
        fFestival.put("0408", "释迦摩尼佛圣诞");
        fFestival.put("0603", "韦陀菩萨圣诞");
        fFestival.put("0619", "观音菩萨成道");
        fFestival.put("0713", "大势至菩萨圣诞");
        fFestival.put("0730", "地藏王菩萨圣诞");
        fFestival.put("0919", "观音菩萨出家");
        fFestival.put("0930", "药师佛圣诞");
        fFestival.put("1117", "阿弥陀佛圣诞");
        fFestival.put("1208", "释迦摩尼佛成道");

        gFestival.put("0108", "观音斋");
        gFestival.put("0207", "观音斋");
        gFestival.put("0209", "观音斋");
        gFestival.put("0219", "观音斋");
        gFestival.put("0303", "观音斋");
        gFestival.put("0306", "观音斋");
        gFestival.put("0313", "观音斋");
        gFestival.put("0422", "观音斋");
        gFestival.put("0503", "观音斋");
        gFestival.put("0517", "观音斋");
        gFestival.put("0616", "观音斋");
        gFestival.put("0618", "观音斋");
        gFestival.put("0619", "观音斋");
        gFestival.put("0623", "观音斋");
        gFestival.put("0713", "观音斋");
        gFestival.put("0816", "观音斋");
        gFestival.put("0919", "观音斋");
        gFestival.put("0923", "观音斋");
        gFestival.put("1002", "观音斋");
        gFestival.put("1119", "观音斋");
        gFestival.put("1124", "观音斋");
        gFestival.put("1225", "观音斋");

    }
}  