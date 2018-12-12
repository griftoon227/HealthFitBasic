package com.example.tmdm9.healthfitbasic;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

class HistoryListView extends ListView {
    public HistoryListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HistoryListView(Context context) {
        super(context);
    }

    public HistoryListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }

}
