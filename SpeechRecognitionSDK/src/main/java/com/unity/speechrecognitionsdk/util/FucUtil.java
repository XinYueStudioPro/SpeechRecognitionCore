package com.unity.speechrecognitionsdk.util;

import android.content.Context;

import com.iflytek.cloud.SpeechUtility;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;

public class FucUtil
{
    public static String readFile(Context mContext, String file, String code)
    {
        int len = 0;
        byte[] buf = null;
        String result = "";
        try
        {
            InputStream in = mContext.getAssets().open(file);
            len = in.available();
            buf = new byte[len];
            in.read(buf, 0, len);

            result = new String(buf, code);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return result;
    }

    public static ArrayList<byte[]> splitBuffer(byte[] buffer, int length, int spsize)
    {
        ArrayList<byte[]> array = new ArrayList();
        if ((spsize <= 0) || (length <= 0) || (buffer == null) || (buffer.length < length)) {
            return array;
        }
        int size = 0;
        while (size < length)
        {
            int left = length - size;
            if (spsize < left)
            {
                byte[] sdata = new byte[spsize];
                System.arraycopy(buffer, size, sdata, 0, spsize);
                array.add(sdata);
                size += spsize;
            }
            else
            {
                byte[] sdata = new byte[left];
                System.arraycopy(buffer, size, sdata, 0, left);
                array.add(sdata);
                size += left;
            }
        }
        return array;
    }

    public static String checkLocalResource()
    {
        String resource = SpeechUtility.getUtility().getParameter("asr");
        try
        {
            JSONObject result = new JSONObject(resource);
            int ret = result.getInt("ret");
            switch (ret)
            {
                case 0:
                    JSONArray asrArray = result.getJSONObject("result").optJSONArray("asr");
                    if (asrArray != null)
                    {
                        for (int i = 0; i < asrArray.length(); i++) {
                            if ("iat".equals(asrArray.getJSONObject(i).get("domain"))) {
                                break;
                            }
                        }
                        if (asrArray.length()==0)
                        {
                            SpeechUtility.getUtility().openEngineSettings("asr");
                            return "没有听写资源，跳转至资源下载页面";
                        }
                    }
                    else
                    {
                        SpeechUtility.getUtility().openEngineSettings("asr");
                        return "没有听写资源，跳转至资源下载页面";
                    }
                    break;
                case 20018:
                    return "语记版本过低，请更新后使用本地功能";
                case 20004:
                    SpeechUtility.getUtility().openEngineSettings("asr");
                    return "获取结果出错，跳转至资源下载页面";
            }
        }
        catch (Exception e)
        {
            SpeechUtility.getUtility().openEngineSettings("asr");
            return "获取结果出错，跳转至资源下载页面";
        }
        return "";
    }

    public static byte[] readAudioFile(Context context, String filename)
    {
        try
        {
            InputStream ins = context.getAssets().open(filename);
            byte[] data = new byte[ins.available()];

            ins.read(data);
            ins.close();

            return data;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
