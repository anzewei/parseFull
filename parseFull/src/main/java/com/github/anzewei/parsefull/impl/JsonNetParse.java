package com.github.anzewei.parsefull.impl;

import com.github.anzewei.parsefull.NetParser;
import com.github.anzewei.parsefull.ParseFull;
import com.google.gson.Gson;

import okhttp3.ResponseBody;

/**
 * ParseFull
 * @author An Zewei (anzewei88[at]gmail[dot]com)
 * @since 1.0.0
 */

public class JsonNetParse extends NetParser {
    private Class mParseClass;
    public static final int CODE_PARSE_ERROR = -1001;

    public JsonNetParse(Class parseClass) {
        mParseClass = parseClass;
    }

    @Override
    public boolean parse(ParseFull.ParseResult result) {
        if (result.status < 300 && result.status > 199) {
            String html;
            if (result.result instanceof String)
                html = (String) result.result;
            else
                try {
                    ResponseBody body = (ResponseBody) result.result;
                    html = body.string();
                } catch (Exception e) {
                    e.printStackTrace();
                    html = "";
                }

            try {
                result.result = new Gson().fromJson(html, mParseClass);
            } catch (Exception e) {
                result.status = CODE_PARSE_ERROR;
            }
        }
        return true;
    }
}
