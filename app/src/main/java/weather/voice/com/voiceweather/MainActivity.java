package weather.voice.com.voiceweather;

import android.app.KeyguardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechEvent;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.VoiceWakeuper;
import com.iflytek.cloud.WakeuperListener;
import com.iflytek.cloud.WakeuperResult;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.iflytek.cloud.util.ResourceUtil;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import weather.voice.com.voiceweather.entity.Weather;
import weather.voice.com.voiceweather.utils.FtpUtils;
import weather.voice.com.voiceweather.utils.JsonParser;
import weather.voice.com.voiceweather.utils.MyUtils;


public class MainActivity extends AppCompatActivity {
    //    private static final String HOST = "218.201.9.103";
//    private static final int PORT = 21;
    private static final String TAG = "MainActivity";
    //    private String USERNAME = "administrator";
//    private String PASSWORD = "admin@121.cq";
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0x001) {
//                adapter.add((String) msg.obj);
//           findViewById(R.id.)
                Weather weather = (Weather) msg.obj;
                refreshList("最高温度" + weather.heightTem + ",最低温度" + weather.lowTem + "," + weather.introduction + "。", OTHER);
                //3.开始合成
                mTts.startSpeaking("最高温度" + weather.heightTem + ",最低温度" + weather.lowTem + "," + weather.introduction + "。", mSynListener);
            } else if (msg.what == 0x002) {
                Toast.makeText(MainActivity.this,
                        "connect fail", Toast.LENGTH_SHORT)
                        .show();
                mTts.startSpeaking("请检查您的网络连接", mSynListener);
            }
        }
    };
    private String string;
    public final static int OTHER = 1;
    public final static int ME = 0;

    protected ListView chatListView = null;
    protected MyChatAdapter adapter = null;
    ArrayList<HashMap<String, Object>> chatList = null;
    String[] from = {"image", "text"};
    int[] to = {R.id.chatlist_image_me, R.id.chatlist_text_me, R.id.chatlist_image_other, R.id.chatlist_text_other};
    int[] layout = {R.layout.item_chat_mine, R.layout.item_chat_robot};
    private SpeechRecognizer mIat;
    private RecognizerDialog iatDialog;
    private SpeechSynthesizer mTts;
    private boolean flag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        //保持屏幕常亮
        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initXf();
    }

    @Override
    protected void onStop() {
        super.onStop();
        flag=true;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        flag=false;
    }

    /**
     * 听写UI监听器
     */
    private RecognizerDialogListener mRecognizerDialogListener = new RecognizerDialogListener() {
        public void onResult(RecognizerResult results, boolean isLast) {
            Log.d(TAG, "recognizer result：" + results.getResultString());
            if (!isLast) {
                String text = JsonParser.parseIatResult(results.getResultString());
//                MyUtils.showToast(MainActivity.this, text);
                refreshList(text, ME);
                matchResult(text);
            }
        }

        /**
         * 识别回调错误.
         */
        public void onError(SpeechError error) {
            showTip(error.getPlainDescription(true));
            if (error.getErrorCode() == 10118) {
                iatDialog.dismiss();
            }
        }

    };

    /**
     * 匹配天气数据
     *
     * @param text
     */
    private void matchResult(String text) {
        if (text.contains("重庆主城区")) {
//        if (text.contains("高平")) {
            getResultFromFtp("zhuchengqu01.xml");
        } else if (text.contains("巴南")) {
            getResultFromFtp("banan/banan01.xml");
        } else if (text.contains("北碚")) {
            getResultFromFtp("beibei/beibei01.xml");
        } else if (text.contains("璧山")) {
            getResultFromFtp("bishan/bishan01.xml");
        } else if (text.contains("长寿")) {
            getResultFromFtp("changshou/changshou01.xml");
        } else if (text.contains("城口")) {
            getResultFromFtp("chengkou/chengkou01.xml");
        } else if (text.contains("大足")) {
            getResultFromFtp("dazu/dazu01.xml");
        } else if (text.contains("垫江")) {
            getResultFromFtp("dianjiang/dianjiang01.xml");
        } else if (text.contains("丰都")) {
            getResultFromFtp("fengdu/fengdu01.xml");
        } else if (text.contains("奉节")) {
            getResultFromFtp("fengjie/fengjie01.xml");
        } else if (text.contains("涪陵")) {
            getResultFromFtp("fuling/fuling01.xml");
        } else if (text.contains("合川")) {
            getResultFromFtp("hechuan/hechuan01.xml");
        } else if (text.contains("江津")) {
            getResultFromFtp("jiangjin/jiangjin01.xml");
        } else if (text.contains("开县")) {
            getResultFromFtp("kaixian/kaixian01.xml");
        } else if (text.contains("梁平")) {
            getResultFromFtp("liangping/liangping01.xml");
        } else if (text.contains("南川")) {
            getResultFromFtp("nanchuan/nanchuan01.xml");
        } else if (text.contains("彭水")) {
            getResultFromFtp("pengshui/pengshui01.xml");
        } else if (text.contains("黔江")) {
            getResultFromFtp("qianjiang/qianjiang01.xml");
        } else if (text.contains("黔江")) {
            getResultFromFtp("qianjiang/qianjiang01.xml");
        } else if (text.contains("綦江")) {
            getResultFromFtp("qijiang/qijiang01.xml");
        } else if (text.contains("荣昌")) {
            getResultFromFtp("rongchang/rongchang01.xml");
        } else if (text.contains("石柱")) {
            getResultFromFtp("shizhu/shizhu01.xml");
        } else if (text.contains("潼南")) {
            getResultFromFtp("tongnan/tongnan01.xml");
        } else if (text.contains("铜梁")) {
            getResultFromFtp("tongliang/tongliang01.xml");
        } else if (text.contains("万盛")) {
            getResultFromFtp("wansheng/wansheng01.xml");
        } else if (text.contains("万州")) {
            getResultFromFtp("wanzhou/wanzhou01.xml");
        } else if (text.contains("武隆")) {
            getResultFromFtp("wulong/wulong01.xml");
        } else if (text.contains("巫山")) {
            getResultFromFtp("wushan/wushan01.xml");
        } else if (text.contains("巫溪")) {
            getResultFromFtp("wuxi/wuxi01.xml");
        } else if (text.contains("秀山")) {
            getResultFromFtp("xiushan/xiushan01.xml");
        } else if (text.contains("永川")) {
            getResultFromFtp("yongchuan/youchang01.xml");
        } else if (text.contains("酉阳")) {
            getResultFromFtp("youyang/youyang01.xm");
        } else if (text.contains("渝北")) {
            getResultFromFtp("yubei/yubei01.xml");
        } else if (text.contains("云阳")) {
            getResultFromFtp("yunyang/yunyang01.xml");
        } else if (text.contains("忠县")) {
            getResultFromFtp("zhongxian/zhongxian01.xml");
        } else {
            refreshList("请说出你要查询的城市名称。", OTHER);
            mTts.startSpeaking("请说出你要查询的城市名称。", mSynListener);
        }
    }

    /**
     * 初始化监听器。
     */
    private InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            Log.d(TAG, "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                showTip("初始化失败，错误码：" + code);
            }
        }
    };

    private void showTip(final String str) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MyUtils.showToast(MainActivity.this, str);
            }
        });
    }

    public static void wakeUpAndUnlock(Context context){
        KeyguardManager km= (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock kl = km.newKeyguardLock("unLock");
        //解锁
        kl.disableKeyguard();
        //获取电源管理器对象
        PowerManager pm=(PowerManager) context.getSystemService(Context.POWER_SERVICE);
        //获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK,"bright");
        //点亮屏幕
        wl.acquire();
        //释放
        wl.release();
    }
    //初始化语音听写
    private void initXf() {
        //初始化语音听写
        //1.创建SpeechRecognizer对象，第二个参数：本地听写时传InitListener
        mIat = SpeechRecognizer.createRecognizer(this, null);
//2.设置听写参数，详见《科大讯飞MSC API手册(Android)》SpeechConstant类
        mIat.setParameter(SpeechConstant.DOMAIN, "iat");
        mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        mIat.setParameter(SpeechConstant.ACCENT, "mandarin ");
        mIat.setParameter(SpeechConstant.ASR_INTERRUPT_ERROR, "true");
        //初始化语音合成
        //1.创建SpeechSynthesizer对象, 第二个参数：本地合成时传InitListener
        mTts = SpeechSynthesizer.createSynthesizer(this, null);
//2.合成参数设置，详见《科大讯飞MSC API手册(Android)》SpeechSynthesizer 类
        mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");//设置发音人
//        mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoyu");//设置发音人
        mTts.setParameter(SpeechConstant.SPEED, "50");//设置语速
        mTts.setParameter(SpeechConstant.VOLUME, "80");//设置音量，范围0~100
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD); //设置云端
        mTts.startSpeaking("欢迎使用智能语音服务系统！", mSynListener);
        //初始化语音唤醒
        //1.加载唤醒词资源，resPath为唤醒资源路径
        StringBuffer param = new StringBuffer();
        String resPath = ResourceUtil.generateResourcePath(MainActivity.this, ResourceUtil.RESOURCE_TYPE.assets, "ivw/" + getString(R.string.app_id) + ".jet");
        param.append(ResourceUtil.IVW_RES_PATH + "=" + resPath);
        param.append("," + ResourceUtil.ENGINE_START + "=" + SpeechConstant.ENG_IVW);
        SpeechUtility.getUtility().setParameter(ResourceUtil.ENGINE_START, param.toString());
//2.创建VoiceWakeuper对象
        VoiceWakeuper mIvw = VoiceWakeuper.createWakeuper(this, null);
//3.设置唤醒参数，详见《科大讯飞MSC API手册(Android)》SpeechConstant类
//唤醒门限值，根据资源携带的唤醒词个数按照“id:门限;id:门限”的格式传入
        mIvw.setParameter(SpeechConstant.IVW_THRESHOLD, "0:" + 0);
//设置当前业务类型为唤醒
        mIvw.setParameter(SpeechConstant.IVW_SST, "wakeup");
//设置唤醒一直保持，直到调用stopListening，传入0则完成一次唤醒后，会话立即结束（默认0）
        mIvw.setParameter(SpeechConstant.KEEP_ALIVE, "1");
        // 设置闭环优化网络模式
        mIvw.setParameter(SpeechConstant.IVW_NET_MODE, "2");
        // 设置唤醒资源路径
        mIvw.setParameter(SpeechConstant.IVW_RES_PATH, resPath);
//4.开始唤醒
        mIvw.startListening(mWakeuperListener);
    }

    //听写监听器
    private WakeuperListener mWakeuperListener = new WakeuperListener() {
        public void onResult(WakeuperResult result) {
//            try {
            String text = result.getResultString();
            Log.i(TAG,text);
            if(!TextUtils.isEmpty(text)&&flag){
                //startActivity(new Intent(MainActivity.thi));
                Intent intent = new Intent();
//                        intent.setComponent(new ComponentName("com.icitipay.visa", "com.hongjingjr.ltbh.activity.LoadingAct"));
                intent.setComponent(new ComponentName("weather.voice.com.voiceweather", "weather.voice.com.voiceweather.MainActivity"));
                intent.setAction(Intent.ACTION_VIEW);
                startActivity(intent);
                mTts.startSpeaking("欢迎使用智能语音服务系统！", mSynListener);
                wakeUpAndUnlock(MainActivity.this);
            }
//            MyUtils.showToast(MainActivity.this,text);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }}
        }
        public void onError(SpeechError error) {
        }

        public void onBeginOfSpeech() {
        }

        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            if (SpeechEvent.EVENT_IVW_RESULT == eventType) {
//当使用唤醒+识别功能时获取识别结果
//arg1:是否最后一个结果，1:是，0:否。
                RecognizerResult reslut = ((RecognizerResult) obj.get(SpeechEvent.KEY_EVENT_IVW_RESULT));
            }
        }

        @Override
        public void onVolumeChanged(int i) {

        }
    };
    private SynthesizerListener mSynListener = new SynthesizerListener() {
        //会话结束回调接口，没有错误时，error为null
        public void onCompleted(SpeechError error) {
        }

        //缓冲进度回调
        //percent为缓冲进度0~100，beginPos为缓冲音频在文本中开始位置，endPos表示缓冲音频在文本中结束位置，info为附加信息。
        public void onBufferProgress(int percent, int beginPos, int endPos, String info) {
        }

        //开始播放
        public void onSpeakBegin() {
        }

        //暂停播放
        public void onSpeakPaused() {
        }

        //播放进度回调
        //percent为播放进度0~100,beginPos为播放音频在文本中开始位置，endPos表示播放音频在文本中结束位置.
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
        }

        //恢复播放回调接口
        public void onSpeakResumed() {
        }

        //会话事件回调接口
        public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {
        }
    };

    private void initView() {
        chatList = new ArrayList<HashMap<String, Object>>();
        addTextToList("欢迎使用智能语音服务系统！", OTHER);
        chatListView = (ListView) findViewById(R.id.chat_list);
        findViewById(R.id.chat_bottom_sendbutton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //3.开始听写
//        mIat.startListening(mRecoListener);
                //1.创建SpeechRecognizer对象，第二个参数：本地听写时传InitListener
                iatDialog = new RecognizerDialog(MainActivity.this, mInitListener);
//3.设置回调接口
                iatDialog.setListener(mRecognizerDialogListener);
//4.开始听写
                iatDialog.show();
            }
        });
        adapter = new MyChatAdapter(this, chatList, layout, from, to);
        chatListView.setAdapter(adapter);
    }

    private void refreshList(String result, int flag) {
        addTextToList(result, flag);
        if (flag == OTHER) {
            //语音播报

        }
        /**
         * 更新数据列表，并且通过setSelection方法使ListView始终滚动在最底端
         */
        adapter.notifyDataSetChanged();
        chatListView.setSelection(chatList.size() - 1);
    }

    protected void addTextToList(String text, int who) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("person", who);
        map.put("image", who == ME ? R.mipmap.logo : R.mipmap.h0);
        map.put("text", text);
        chatList.add(map);
    }

    private void getResultFromFtp(final String filename) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    string = new FtpUtils().readFile(filename);
                    Log.i(TAG, string);
                    xmlParseString(string);

                } catch (Exception e) {
                    handler.sendEmptyMessage(0x002);
                    return;
                }
            }
        }).start();
    }

    private void xmlParseString(String string) {
        InputStream inputStream = null;
        //获得XmlPullParser解析器
        XmlPullParser xmlParser = Xml.newPullParser();
        Weather weather = new Weather();
        try {
            inputStream = getStringStream(string);
            //得到文件流，并设置编码方式
            xmlParser.setInput(inputStream, "utf-8");
            //获得解析到的事件类别，这里有开始文档，结束文档，开始标签，结束标签，文本等等事件。
            int evtType = xmlParser.getEventType();
            //一直循环，直到文档结束
            while (evtType != XmlPullParser.END_DOCUMENT) {
                switch (evtType) {
                    case XmlPullParser.START_TAG:
                        String tag = xmlParser.getName();
                        if (tag.equals("h")) {
                            //取出h标签中的一些属性值
                            weather.heightTem = xmlParser.getAttributeValue(null, "value");
                        } else if (tag.equals("l")) {
                            weather.lowTem = xmlParser.getAttributeValue(null, "value");
                        } else if (tag.equals("w3")) {
                            weather.introduction = xmlParser.getAttributeValue(null, "value");
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                    default:
                        break;
                }
                evtType = xmlParser.next();
            }
            Log.i(TAG, weather.toString());
            Message message = handler
                    .obtainMessage(0x001, weather);
            handler.sendMessage(message);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    /**
     * 将字符传转换为输入流
     *
     * @param sInputString
     * @return
     */
    public InputStream getStringStream(String sInputString) {

        if (sInputString != null && !sInputString.trim().equals("")) {

            try {
                ByteArrayInputStream tInputStringStream = new ByteArrayInputStream(sInputString.getBytes());

                return tInputStringStream;

            } catch (Exception ex) {

                ex.printStackTrace();
            }
        }
        return null;

    }

    // 唤醒结果内容
    private String resultString;
//    private WakeuperListener mWakeuperListener = new WakeuperListener() {
//
//        @Override
//        public void onResult(WakeuperResult result) {
//            Log.d(TAG, "onResult");
//            if(!"1".equalsIgnoreCase(keep_alive)) {
//                setRadioEnable(true);
//            }
//            try {
//                String text = result.getResultString();
//                JSONObject object;
//                object = new JSONObject(text);
//                StringBuffer buffer = new StringBuffer();
//                buffer.append("【RAW】 "+text);
//                buffer.append("\n");
//                buffer.append("【操作类型】"+ object.optString("sst"));
//                buffer.append("\n");
//                buffer.append("【唤醒词id】"+ object.optString("id"));
//                buffer.append("\n");
//                buffer.append("【得分】" + object.optString("score"));
//                buffer.append("\n");
//                buffer.append("【前端点】" + object.optString("bos"));
//                buffer.append("\n");
//                buffer.append("【尾端点】" + object.optString("eos"));
//                resultString =buffer.toString();
//            } catch (JSONException e) {
//                resultString = "结果解析出错";
//                e.printStackTrace();
//            }
////            textView.setText(resultString);
//        }
//
//        @Override
//        public void onError(SpeechError error) {
////            showTip(error.getPlainDescription(true));
////            setRadioEnable(true);
//        }
//
//        @Override
//        public void onBeginOfSpeech() {
//        }
//
//        @Override
//        public void onEvent(int eventType, int isLast, int arg2, Bundle obj) {
//            if (SpeechEvent.EVENT_IVW_RESULT == eventType) {
//                //当使用唤醒+识别功能时获取识别结果
//                //arg1:是否最后一个结果，1:是，0:否。
//                RecognizerResult reslut = ((RecognizerResult) obj.get(SpeechEvent.KEY_EVENT_IVW_RESULT));
//            }
//        }
//        @Override
//        public void onVolumeChanged(int volume) {
//
//        }
//    };

    private void test() {
        //1.加载唤醒词资源，resPath为唤醒资源路径
        StringBuffer param = new StringBuffer();
        String resPath = ResourceUtil.generateResourcePath(MainActivity.this, ResourceUtil.RESOURCE_TYPE.assets, "ivw/ivModel_zhimakaimen.jet");
        param.append(ResourceUtil.IVW_RES_PATH + "=" + resPath);
        param.append("," + ResourceUtil.ENGINE_START + "=" + SpeechConstant.ENG_IVW);
        SpeechUtility.getUtility().setParameter(ResourceUtil.ENGINE_START, param.toString());
        //2.创建VoiceWakeuper对象
        VoiceWakeuper mIvw = VoiceWakeuper.createWakeuper(this, null);
        //3.设置唤醒参数，详见《科大讯飞MSC API手册(Android)》SpeechConstant类
        //唤醒门限值，根据资源携带的唤醒词个数按照“id:门限;id:门限”的格式传入
//        mIvw.setParameter(SpeechConstant.IVW_THRESHOLD, "0:" + curThresh);
        //设置当前业务类型为唤醒
        mIvw.setParameter(SpeechConstant.IVW_SST, "wakeup");
        //设置唤醒一直保持，直到调用stopListening，传入0则完成一次唤醒后，会话立即结束（默认0）
        mIvw.setParameter(SpeechConstant.KEEP_ALIVE, "1");
        //4.开始唤醒
//        mIvw.startListening(mWakeuperListener);
        //听写监听器

    }

    private class MyChatAdapter extends BaseAdapter {

        Context context = null;
        ArrayList<HashMap<String, Object>> chatList = null;
        int[] layout;
        String[] from;
        int[] to;

        public MyChatAdapter(Context context,
                             ArrayList<HashMap<String, Object>> chatList, int[] layout,
                             String[] from, int[] to) {
            super();
            this.context = context;
            this.chatList = chatList;
            this.layout = layout;
            this.from = from;
            this.to = to;
        }

        @Override
        public int getCount() {
            return chatList.size();
        }

        @Override
        public Object getItem(int arg0) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        class ViewHolder {
            public ImageView imageView = null;
            public TextView textView = null;

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            int who = (Integer) chatList.get(position).get("person");

            convertView = LayoutInflater.from(context).inflate(
                    layout[who == ME ? 0 : 1], null);
            holder = new ViewHolder();
            holder.imageView = (ImageView) convertView.findViewById(to[who * 2 + 0]);
            holder.textView = (TextView) convertView.findViewById(to[who * 2 + 1]);


            System.out.println(holder);
            System.out.println("WHYWHYWHYWHYW");
            System.out.println(holder.imageView);
            holder.imageView.setBackgroundResource((Integer) chatList.get(position).get(from[0]));
            holder.textView.setText(chatList.get(position).get(from[1]).toString());
            return convertView;
        }

    }
}
