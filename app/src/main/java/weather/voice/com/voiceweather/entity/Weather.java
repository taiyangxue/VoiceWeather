package weather.voice.com.voiceweather.entity;

/**
 * Created by Administrator on 2016/12/1.
 */

public class Weather {
    public String heightTem;
    public String lowTem;
    public String introduction;
    @Override
    public String toString() {
        return "Weather{" +
                "heightTem='" + heightTem + '\'' +
                ", lowTem='" + lowTem + '\'' +
                ", introduction='" + introduction + '\'' +
                '}';
    }
}
