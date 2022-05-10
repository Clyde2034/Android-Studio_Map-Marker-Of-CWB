package com.brian19109.weatherapi;

public class DistanceSplit {
    String hour = "0";
    String day = "0";
    String minute = "0";

    public String getMinute() {
        return minute;
    }

    public void setMinute(String minute) {
        this.minute = minute;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getHour() {
        return hour;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    public DistanceSplit(String duration) {
        String[] split_day = new String[]{};
        String[] split_hour = new String[]{};
        String[] split_minute = new String[]{};
        try {
            //string split後以array存放，day、hour、minute預設值為0，用字串原因是之後在做轉換比較方便且可保留前綴的0，
            //才不會出現例如抓到資料為01但 convert to Integer後變成1
            if (duration.contains("天") && duration.contains("小時")) {
                split_day = duration.split("天");
                this.day = split_day[0].trim();

                split_hour = split_day[1].split("小時");
                this.hour = split_hour[0].trim();
                this.minute ="0";
            } else if (duration.contains("天") && !duration.contains("小時")) {
                split_day = duration.split("天");
                this.day = split_day[0].trim();

                this.hour = "0";
                this.minute = "0";
            } else if (duration.contains("小時") && duration.contains("分鐘")) {
                this.day="0";

                split_hour = duration.split("小時");
                this.hour = split_hour[0].trim();

                split_minute=split_hour[1].split("分鐘");
                this.minute =split_minute[0].trim();
            } else if(!duration.contains("小時") && duration.contains("分鐘")) {
                this.day = "0";

                this.hour = "0";

                split_minute=duration.split("分鐘");
                this.minute=split_minute[0].trim();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
