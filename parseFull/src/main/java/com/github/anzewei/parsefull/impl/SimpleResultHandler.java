package com.github.anzewei.parsefull.impl;

import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.github.anzewei.parsefull.ParseFull;
import com.github.anzewei.parsefull.ResultHandler;

/**
 * ParseFull
 * @author An Zewei (anzewei88[at]gmail[dot]com)
 * @since 1.0.0
 */

public class SimpleResultHandler extends ResultHandler {

    @Override
    public void onParseFinish(final ParseFull parseFull, final ParseFull.ParseResult result) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
        Toast.makeText(parseFull.getContext(), String.format("result=%s", result.result), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
