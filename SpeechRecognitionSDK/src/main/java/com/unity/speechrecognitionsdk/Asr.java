package com.unity.speechrecognitionsdk;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import com.iflytek.cloud.GrammarListener;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.LexiconListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.util.ResourceUtil;
import com.unity.speechrecognitionsdk.util.FucUtil;
import com.unity.speechrecognitionsdk.util.JsonParser;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Asr
{
    private static final String TAG = "Asr";
    private static boolean mscInitialize = false;
    private SpeechRecognizer mAsr;
    private String mLocalGrammar = null;
    private String mLocalLexicon = null;
    private String grmPath;
    private String mResultType = "json";
    private String mAudioSource = "1";
    String mContent = "";
    private final String KEY_GRAMMAR_ABNF_ID = "grammar_abnf_id";
    private final String GRAMMAR_TYPE_ABNF = "abnf";
    private final String GRAMMAR_TYPE_BNF = "bnf";
    private final ICmdCallback mCmdCB;
    private Context mContext;
    private String[] mCmdStringList = { "前一步", "后一步", "开启拍照", "隐藏面板", "显示面板", "显示图片", "开始引路", "播放视频", "重新播放", "确定", "取消", "发送", "播放动画", "重新引导", "查看邮件", "前一封", "后一封", "查看详情", "关闭", "删除邮件", "固定面板", "移动面板", "跳过展示", "固定模型", "移动模型", "跳过引路" };
    private HashMap<String, String> mCmdList;
    public static int EVENT_ASR_START = 1;
    public static int EVENT_ASR_STOP = 2;
    public static int EVENT_ASR_ERROR = 3;

    @TargetApi(Build.VERSION_CODES.O)
    public Asr(Context context, ICmdCallback cb)
    {
        this.mContext = context;
        this.mCmdCB = cb;
        initializeMsc(context);
        this.grmPath = (this.mContext.getExternalFilesDir("msc").getAbsolutePath() + "/test");

        this.mAsr = SpeechRecognizer.createRecognizer(this.mContext, this.mInitListener);
        if (this.mAsr == null) {
            Log.e("Asr", "masr is null");
        }
        this.mLocalLexicon = String.join("\n", this.mCmdStringList);
        this.mLocalGrammar = FucUtil.readFile(context, "call.bnf", "utf-8");
    }

    public int init()
    {
        int ret = 20999;
        if (null == this.mAsr)
        {
            showTip("创建对象失败，请确认 libmsc.so 放置正确，\n 且有调用 createUtility 进行初始化");
            return 21001;
        }
        if (this.mContent == "") {
            this.mContent = new String(this.mLocalGrammar);
        }
        this.mAsr.setParameter("params", null);

        this.mAsr.setParameter("text_encoding", "utf-8");

        this.mAsr.setParameter("engine_type", "local");

        this.mAsr.setParameter("grm_build_path", this.grmPath);



        this.mAsr.setParameter("asr_res_path", getResourcePath());

        ret = this.mAsr.buildGrammar("bnf", this.mContent, this.grammarListener);
        if (ret != 0)
        {
            showTip("语法构建失败,错误码：" + ret);

            return ret;
        }
        this.mAsr.setParameter("params", null);

        this.mAsr.setParameter("engine_type", "local");

        this.mAsr.setParameter("asr_res_path", getResourcePath());



        this.mAsr.setParameter("grm_build_path", this.grmPath);

        this.mAsr.setParameter("grammar_list", "rainbow");

        this.mAsr.setParameter("text_encoding", "utf-8");

        return ret;
    }

    public int startRecognize(boolean test)
    {
        int ret = -1;
        if (test) {
            this.mAudioSource = "-1";
        }
        if (!setParam())
        {
            showTip("请先构建语法。");
            return ret;
        }
        ret = this.mAsr.startListening(this.mRecognizerListener);
        if (ret != 0) {
            showTip("识别失败,错误码: " + ret);
        }
        showTip("startRecognize OK");
        return ret;
    }

    public void stopRecognize()
    {
        this.mAsr.stopListening();
        showTip("停止识别");
    }

    public void cancelRecognize()
    {
        this.mAsr.cancel();
        showTip("取消识别");
    }

    public int writeAudio(String name)
    {
        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream(name);
            byte[] data = new byte[fis.available()];
            fis.read(data);
            fis.close();
            return this.mAsr.writeAudio(data, 0, data.length);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return -1;
    }

    public int writeAudio(InputStream stream)
    {
        try
        {
            byte[] data = new byte[stream.available()];
            stream.read(data);
            stream.close();
            return this.mAsr.writeAudio(data, 0, data.length);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return -1;
    }

    public int setCommandList(String jsonString)
    {
        try
        {
            HashMap<String, String> cmdMap = new HashMap();
            JSONObject root = new JSONObject(jsonString);
            JSONArray cmdList = root.getJSONArray("list");
            for (int i = 0; i < cmdList.length(); i++)
            {
                JSONObject cmd = cmdList.getJSONObject(i);
                cmdMap.put(cmd.getString("key"), cmd.getString("word"));
            }
            this.mContent = getGrammarContent(cmdMap);
        }
        catch (JSONException e)
        {
            Log.e("Asr", "setCommandList: " + e.getMessage());
        }
        return 0;
    }

    public int setCommandList(HashMap<String, String> map)
    {
        this.mContent = getGrammarContent(map);
        return 0;
    }

    public boolean setParam()
    {
        boolean result = false;

        this.mAsr.setParameter("params", null);

        this.mAsr.setParameter("engine_type", "local");

        this.mAsr.setParameter("asr_res_path", getResourcePath());

        this.mAsr.setParameter("grm_build_path", this.grmPath);

        this.mAsr.setParameter("result_type", this.mResultType);

        this.mAsr.setParameter("local_grammar", "rainbow");

        this.mAsr.setParameter("mixed_threshold", "30");


        result = true;


        this.mAsr.setParameter("audio_format", "wav");
        this.mAsr.setParameter("asr_audio_path", this.mContext
                .getExternalFilesDir("msc").getAbsolutePath() + "/asr.wav");

        this.mAsr.setParameter("audio_source", this.mAudioSource);
        this.mAsr.setParameter("asr_source_path", this.mContext.getExternalFilesDir("msc").getAbsolutePath() + "test.wav");

        return result;
    }

    private String getResourcePath()
    {
        StringBuffer tempBuffer = new StringBuffer();

        tempBuffer.append(ResourceUtil.generateResourcePath(this.mContext, ResourceUtil.RESOURCE_TYPE.assets, "asr/common.jet"));
        return tempBuffer.toString();
    }

    public static void initializeMsc(Context context)
    {
        if (mscInitialize) {
            return;
        }
        StringBuffer param = new StringBuffer();
        param.append("appid=" + context.getString(R.string.app_id));
        param.append(",");

        param.append("engine_mode=msc");
        SpeechUtility.createUtility(context, param.toString());
        mscInitialize = true;
    }

    private void showTip(String msg)
    {
        Log.i("Asr", msg);
    }

    private String getGrammarContent(HashMap<String, String> dic)
    {
        if (dic.size() == 0) {
            return "";
        }
        ArrayList<String> text = new ArrayList();
        text.add("#BNF+IAT 1.0 UTF-8;");
        text.add("!grammar rainbow;");

        Iterator<String> iterator = dic.keySet().iterator();
        StringBuilder wordList = new StringBuilder("<callStart>:");
        while (iterator.hasNext())
        {
            String key = (String)iterator.next();
            text.add(String.format("!slot <%s>;", new Object[] { key }));
            wordList.append(String.format("<%s>|", new Object[] { key }));
        }
        text.add("!start <callStart>;");

        int i = wordList.length() - 1;
        if (i > 0) {
            wordList.setCharAt(i, ';');
        }
        text.add(String.format(wordList.toString(), new Object[0]));

        iterator = dic.keySet().iterator();
        while (iterator.hasNext())
        {
            String key = (String)iterator.next();
            text.add(String.format("<%s>:%s;", new Object[] { key, dic.get(key) }));
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (String line : text) {
            stringBuilder.append(line).append("\n");
        }
        return stringBuilder.toString();
    }

    private InitListener mInitListener = new InitListener()
    {
        public void onInit(int code)
        {
            Log.d("Asr", "SpeechRecognizer init() code = " + code);
            if (code != 0) {
                Asr.this.showTip("初始化失败,错误码：" + code + ",请点击网址https://www.xfyun.cn/document/error-code查询解决方案");
            }
        }
    };
    private LexiconListener lexiconListener = new LexiconListener()
    {
        public void onLexiconUpdated(String lexiconId, SpeechError error)
        {
            if (error == null) {
                Asr.this.showTip("词典更新成功");
            } else {
                Asr.this.showTip("词典更新失败,错误码：" + error.getErrorCode());
            }
        }
    };
    private GrammarListener grammarListener = new GrammarListener()
    {
        public void onBuildFinish(String grammarId, SpeechError error)
        {
            if (error == null) {
                Asr.this.showTip("语法构建成功：" + grammarId);
            } else {
                Asr.this.showTip("语法构建失败,错误码：" + error.getErrorCode());
            }
        }
    };
    private RecognizerListener mRecognizerListener = new RecognizerListener()
    {
        public void onVolumeChanged(int volume, byte[] data)
        {
            Asr.this.showTip("当前正在说话，音量大小：" + volume);
            Log.d("Asr", "返回音频数据：" + data.length);
        }

        public void onResult(RecognizerResult result, boolean isLast)
        {
            if ((null != result) && (!TextUtils.isEmpty(result.getResultString())))
            {
                Log.d("Asr", "recognizer result：" + result.getResultString());
                String text = "";
                if (Asr.this.mResultType.equals("json"))
                {
                    text = JsonParser.parseGrammarResult(result.getResultString(), "local");
                    AsrResult r = JsonParser.getGrammarResult(result.getResultString(), "local");
                    if (Asr.this.mCmdCB != null) {
                        Asr.this.mCmdCB.onCmd(r.getSlot(), r.getWord(), r.getConf());
                    }
                    Log.i("Asr", "onResult: " + r);
                }
                Log.i("Asr", "onResult: " + text);
            }
            else
            {
                Log.d("Asr", "recognizer result : null");
            }
        }

        public void onEndOfSpeech()
        {
            Asr.this.showTip("结束说话");
            if (Asr.this.mCmdCB != null) {
                Asr.this.mCmdCB.onEvent(Asr.EVENT_ASR_STOP, 0);
            }
        }

        public void onBeginOfSpeech()
        {
            Asr.this.showTip("开始说话");
            if (Asr.this.mCmdCB != null) {
                Asr.this.mCmdCB.onEvent(Asr.EVENT_ASR_START, 0);
            }
        }

        public void onError(SpeechError error)
        {
            Asr.this.showTip("onError Code：" + error.getErrorCode());
            if (Asr.this.mCmdCB != null) {
                Asr.this.mCmdCB.onEvent(Asr.EVENT_ASR_ERROR, error.getErrorCode());
            }
        }

        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {}
    };
}