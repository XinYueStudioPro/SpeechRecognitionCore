package com.unity.speechrecognitionsdk;


public abstract interface ICmdCallback
{
    public abstract void onCmd(String paramString1, String paramString2, int paramInt);

    public abstract void onEvent(int paramInt1, int paramInt2);
}
