/*
 * Copyright (c) 2017.
 * author:kuan
 * SunStar
 *
 */

package com.cugkuan.text;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatTextView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Kuan on 2017/8/30.
 *
 * @Author Kuan
 * @Date 2017/8/30
 * 小鸟校园
 * <p>
 * 文字折叠
 * 这里的代码需要进行大量的优化，现在我没时间来搞这个，等会儿再来搞这个东西
 */

public class OmitTextView extends AppCompatTextView {

    private int tagTextColor = Color.parseColor("#60b631");

    private String tag = "..展开";

    /**
     * 是否强制显示省略符号
     */
    private boolean showOmitForce = false;


    private boolean isExpand = false;

    /**
     * 原内容
     */
    private CharSequence mText;

    private boolean clickExpand = false;

    public OmitTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (attrs != null) {
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.OmitTextView);
            tagTextColor = array.getColor(R.styleable.OmitTextView_omitColor, Color.parseColor("#60b631"));
            tag = array.getString(R.styleable.OmitTextView_omitText);
            clickExpand = array.getBoolean(R.styleable.OmitTextView_clickExpand,false);

            if (TextUtils.isEmpty(tag)) {
                tag = "..展开";
            }
            array.recycle();
        }
        setEllipsize(null);

        if (clickExpand) {
            setMovementMethod(LinkMovementMethod.getInstance());
        }

        mText = getText();

    }


    public OmitTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OmitTextView(Context context) {
        this(context, null);
    }


    public void setTextValue(CharSequence sequence) {
        super.setText(sequence);
    }


    /**
     * 显示省略符号
     */
    public void showOmitForce(boolean isShow) {
        this.showOmitForce = isShow;
    }


    @Override
    protected void onDraw(Canvas canvas) {

        if (!isExpand) {
            replaceText();
        }
        super.onDraw(canvas);
    }


    /**
     * 思路，先检查行数，在检查 最大的字符数，然后看看是不是有强制省略的.
     * 这个方法放在OnDraw中实在是不妥，但是，放在那儿好呢？只能保证大小不引起重绘。
     */
    private void replaceText() {

      //  mText = getText().subSequence();
        int count = getLineCount();
        /**
         * 没有字数的限制和行数的限制
         */
        if (getMaxEms() == -1 && getMaxLines() == -1) {
            return;
        }
        if (count > getMaxLines() && getMaxLines() > 0) {
            int st = getLayout().getLineEnd(getMaxLines() - 1);
            String content = getText().toString().substring(0, st);
            Paint paint = getPaint();
            float pointWidth = paint.measureText(tag);
            char[] textCharArray = content.toCharArray();
            float drawedWidth = 0;
            float charWidth;
            for (int i = textCharArray.length - 1; i > 0; i--) {
                charWidth = paint.measureText(textCharArray, i, 1);
                if (drawedWidth < pointWidth) {
                    drawedWidth += charWidth;
                } else {
                    content = content.substring(0, i) + tag;
                    break;
                }
            }
            CharSequence sequence = getOmitText(content, content.length() - tag.length(), content.length(), tagTextColor);
            setText(sequence);

        } else {
            int length = getText().length();
            if (length >= getMaxEms() && getMaxEms() > 0) {
                String content = getText().subSequence(0, getMaxEms() - tag.length()).toString();
                String result = content + tag;
                CharSequence vlaue = getOmitText(result, content.length(),
                        content.length() + tag.length(), tagTextColor);
                setText(vlaue);
            } else {

                if (showOmitForce) {
                    String content = null;
                    CharSequence value = null;

                    if (getText().length() < tag.length()) {
                        String text = getText() + tag;
                        Paint paint = getPaint();
                        if (paint.measureText(text) > getMeasuredWidth() - getPaddingLeft() - getPaddingRight()) {
                            content = tag;
                            value = getOmitText(content, 0, tag.length(), tagTextColor);
                        } else {
                            value = getOmitText(text, getText().length(), text.length(), tagTextColor);
                        }
                    } else {
                        content = getText().subSequence(0, getText().length() - tag.length()).toString();
                        String result = content + tag;
                        value = getOmitText(result, content.length(), result.length(), tagTextColor);
                    }
                    setText(value);
                }
            }
        }
        isExpand = true;
    }


    private CharSequence getOmitText(String content, int start, int end, int textColor) {
        if (start <= end) {
            SpannableStringBuilder style = new SpannableStringBuilder(content);
            style.setSpan(new ForegroundColorSpan(textColor), start, end,
                    Spannable.SPAN_EXCLUSIVE_INCLUSIVE);

            if (clickExpand) {
                style.setSpan(new ClickableSpan() {
                    @Override
                    public void onClick(@NonNull View widget) {
                        setMaxLines(Integer.MAX_VALUE);
                        setText(mText);
                    }
                }, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            return style;
        }else {
            return content;
        }
    }
}