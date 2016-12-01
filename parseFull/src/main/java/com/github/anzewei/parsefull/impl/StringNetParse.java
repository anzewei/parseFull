package com.github.anzewei.parsefull.impl;

import com.github.anzewei.parsefull.NetParser;
import com.github.anzewei.parsefull.ParseFull;

import okhttp3.ResponseBody;

/**
 * ParseFull
 * @author An Zewei (anzewei88[at]gmail[dot]com)
 * @since 1.0.0
 */

public class StringNetParse extends NetParser {
    @Override
    public boolean parse(ParseFull.ParseResult result) {
        if (result.status < 300 && result.status > 199) {
            try {
                ResponseBody body = (ResponseBody) result.result;
                result.result = body.string();
            } catch (Exception e) {
                e.printStackTrace();
                result.result = "";
            }
        }
        return true;
    }
}
