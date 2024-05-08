package com.unity.speechrecognitionsdk.util;

import com.unity.speechrecognitionsdk.AsrResult;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

public class JsonParser
{
    public static String parseIatResult(String json)
    {
        StringBuffer ret = new StringBuffer();
        try
        {
            JSONTokener tokener = new JSONTokener(json);
            JSONObject joResult = new JSONObject(tokener);

            JSONArray words = joResult.getJSONArray("ws");
            for (int i = 0; i < words.length(); i++)
            {
                JSONArray items = words.getJSONObject(i).getJSONArray("cw");
                JSONObject obj = items.getJSONObject(0);
                ret.append(obj.getString("w"));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return ret.toString();
    }

    public static String parseGrammarResult(String json, String engType)
    {
        StringBuffer ret = new StringBuffer();
        try
        {
            JSONTokener tokener = new JSONTokener(json);
            JSONObject joResult = new JSONObject(tokener);

            JSONArray words = joResult.getJSONArray("ws");
            if ("cloud".equals(engType))
            {
                for (int i = 0; i < words.length(); i++)
                {
                    JSONArray items = words.getJSONObject(i).getJSONArray("cw");
                    for (int j = 0; j < items.length(); j++)
                    {
                        JSONObject obj = items.getJSONObject(j);
                        if (obj.getString("w").contains("nomatch"))
                        {
                            ret.append("没有匹配结果.");
                            return ret.toString();
                        }
                        ret.append("【结果】" + obj.getString("w"));
                        ret.append("【置信度】" + obj.getInt("sc"));
                        ret.append("\n");
                    }
                }
            }
            else if ("local".equals(engType))
            {
                ret.append("【结果】");
                for (int i = 0; i < words.length(); i++)
                {
                    JSONObject wsItem = words.getJSONObject(i);
                    JSONArray items = wsItem.getJSONArray("cw");
                    if ("<contact>".equals(wsItem.getString("slot")))
                    {
                        ret.append("【");
                        for (int j = 0; j < items.length(); j++)
                        {
                            JSONObject obj = items.getJSONObject(j);
                            if (obj.getString("w").contains("nomatch"))
                            {
                                ret.append("没有匹配结果.");
                                return ret.toString();
                            }
                            ret.append(obj.getString("w")).append("|");
                        }
                        ret.setCharAt(ret.length() - 1, '】');
                    }
                    else
                    {
                        JSONObject obj = items.getJSONObject(0);
                        if (obj.getString("w").contains("nomatch"))
                        {
                            ret.append("没有匹配结果.");
                            return ret.toString();
                        }
                        ret.append(obj.getString("w"));
                    }
                }
                ret.append("【置信度】" + joResult.getInt("sc"));
                ret.append("\n");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            ret.append("没有匹配结果.");
        }
        return ret.toString();
    }

    public static AsrResult getGrammarResult(String json, String engType)
    {
        AsrResult result = new AsrResult();
        try
        {
            JSONTokener tokener = new JSONTokener(json);
            JSONObject joResult = new JSONObject(tokener);

            JSONArray words = joResult.getJSONArray("ws");
            if ("local".equals(engType))
            {
                for (int i = 0; i < words.length(); i++)
                {
                    JSONObject wsItem = words.getJSONObject(i);
                    result.setSlot(wsItem.getString("slot"));
                    JSONArray items = wsItem.getJSONArray("cw");


                    JSONObject obj = items.getJSONObject(0);
                    if (obj.getString("w").contains("nomatch"))
                    {
                        result.setSlot("");
                        return result;
                    }
                    result.setWord(obj.getString("w"));
                }
                result.setConf(joResult.getInt("sc"));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            result.setSlot("");
        }
        return result;
    }

    public static String parseGrammarResult(String json)
    {
        StringBuffer ret = new StringBuffer();
        try
        {
            JSONTokener tokener = new JSONTokener(json);
            JSONObject joResult = new JSONObject(tokener);

            JSONArray words = joResult.getJSONArray("ws");
            for (int i = 0; i < words.length(); i++)
            {
                JSONArray items = words.getJSONObject(i).getJSONArray("cw");
                for (int j = 0; j < items.length(); j++)
                {
                    JSONObject obj = items.getJSONObject(j);
                    if (obj.getString("w").contains("nomatch"))
                    {
                        ret.append("没有匹配结果.");
                        return ret.toString();
                    }
                    ret.append("【结果】" + obj.getString("w"));
                    ret.append("【置信度】" + obj.getInt("sc"));
                    ret.append("\n");
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            ret.append("没有匹配结果.");
        }
        return ret.toString();
    }

    public static String parseLocalGrammarResult(String json)
    {
        StringBuffer ret = new StringBuffer();
        try
        {
            JSONTokener tokener = new JSONTokener(json);
            JSONObject joResult = new JSONObject(tokener);

            JSONArray words = joResult.getJSONArray("ws");
            for (int i = 0; i < words.length(); i++)
            {
                JSONArray items = words.getJSONObject(i).getJSONArray("cw");
                for (int j = 0; j < items.length(); j++)
                {
                    JSONObject obj = items.getJSONObject(j);
                    if (obj.getString("w").contains("nomatch"))
                    {
                        ret.append("没有匹配结果.");
                        return ret.toString();
                    }
                    ret.append("【结果】" + obj.getString("w"));
                    ret.append("\n");
                }
            }
            ret.append("【置信度】" + joResult.optInt("sc"));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            ret.append("没有匹配结果.");
        }
        return ret.toString();
    }

    public static String parseTransResult(String json, String key)
    {
        StringBuffer ret = new StringBuffer();
        try
        {
            JSONTokener tokener = new JSONTokener(json);
            JSONObject joResult = new JSONObject(tokener);
            String errorCode = joResult.optString("ret");
            if (!errorCode.equals("0")) {
                return joResult.optString("errmsg");
            }
            JSONObject transResult = joResult.optJSONObject("trans_result");
            ret.append(transResult.optString(key));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return ret.toString();
    }
}
