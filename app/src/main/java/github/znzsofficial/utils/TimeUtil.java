package github.znzsofficial.utils;

public class TimeUtil {
  public static String main(String milli) {
    long milliseconds = Long.valueOf(milli);
    long minutes = (milliseconds / 1000) / 60;
    long seconds = (milliseconds / 1000) % 60;
    String result = minutes + "分" + seconds + "秒";
    return result;
  }
}
