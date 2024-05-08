package com.unity.speechrecognitionsdk.util;

import android.content.Context;
import android.preference.EditTextPreference;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SettingTextWatcher
        implements TextWatcher
{
    private int editStart;
    private int editCount;
    private EditTextPreference mEditTextPreference;
    int minValue;
    int maxValue;
    private Context mContext;

    public SettingTextWatcher(Context context, EditTextPreference e, int min, int max)
    {
        this.mContext = context;
        this.mEditTextPreference = e;
        this.minValue = min;
        this.maxValue = max;
    }

    public void onTextChanged(CharSequence s, int start, int before, int count)
    {
        this.editStart = start;
        this.editCount = count;
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    public void afterTextChanged(Editable s)
    {
        if (TextUtils.isEmpty(s)) {
            return;
        }
        String content = s.toString();
        if (isNumeric(content))
        {
            int num = Integer.parseInt(content);
            if ((num > this.maxValue) || (num < this.minValue))
            {
                s.delete(this.editStart, this.editStart + this.editCount);
                this.mEditTextPreference.getEditText().setText(s);
                Toast.makeText(this.mContext, "超出有效值范围", 0).show();
            }
        }
        else
        {
            s.delete(this.editStart, this.editStart + this.editCount);
            this.mEditTextPreference.getEditText().setText(s);
            Toast.makeText(this.mContext, "只能输入数字哦", 0).show();
        }
    }

    public static boolean isNumeric(String str)
    {
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }
}
