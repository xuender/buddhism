package me.xuender.buddhism.app.calendar;

/**
 * Created by ender on 14-5-2.
 */
public class Day {
    private boolean no = false;
    private String title;
    private boolean now = false;

    public Day(int day, String title, String chineseGala, String gala) {
        StringBuilder sb = new StringBuilder();
        sb.append(day).append('\n');
        sb.append(title);
        int i = 2;
        if (chineseGala != null) {
            sb.append('\n');
            i--;
            sb.append(chineseGala);
        }
        if (gala != null) {
            sb.append('\n');
            sb.append(gala);
            this.no = true;
        } else {
            for (int f = 0; f < i; f++) {
                sb.append('\n');
            }
        }
        this.title = sb.toString();
    }

    public void no() {
        this.no = true;
    }

    public void now() {
        this.now = true;
    }

    public boolean isNow() {
        return now;
    }

    public boolean isNo() {
        return no;
    }

    public String getTitle() {
        return title;
    }
}