package com.unity.speechrecognitionsdk;


public class AsrResult
{
    private String slot;
    private String word;
    private int conf;

    public String getSlot()
    {
        return this.slot;
    }

    public void setSlot(String slot)
    {
        this.slot = slot;
    }

    public String getWord()
    {
        return this.word;
    }

    public void setWord(String word)
    {
        this.word = word;
    }

    public int getConf()
    {
        return this.conf;
    }

    public void setConf(int conf)
    {
        this.conf = conf;
    }


    public String toString()
    {
        String str = String.format("slot= %s, word= %s, conf=%d", new Object[] { this.slot, this.word,


                Integer.valueOf(this.conf) });
        return str;
    }
}
