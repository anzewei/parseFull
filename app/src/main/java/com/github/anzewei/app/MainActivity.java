package com.github.anzewei.app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.github.anzewei.parsefull.ParseFull;
import com.github.anzewei.parsefull.impl.DialogNetDisplay;
import com.github.anzewei.parsefull.impl.JsonNetParse;
import com.github.anzewei.parsefull.impl.SimpleResultHandler;
import com.github.anzewei.parsefull.impl.StringNetParse;

import okhttp3.OkHttpClient;

public class MainActivity extends AppCompatActivity {

    OkHttpClient mOkHttpClient = new OkHttpClient();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_string:
                ParseFull.ParseBuilder builder = new ParseFull.ParseBuilder(this)
                        .url("http://www.ourkp.com/bk/get")
                        .displayer(new DialogNetDisplay(this))
                        .addNetParser(new StringNetParse(){
                            @Override
                            public boolean parse(ParseFull.ParseResult result) {
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                return super.parse(result);
                            }
                        })
                        .resultHandler(new SimpleResultHandler());
                builder.build().execute(mOkHttpClient);
                break;
            case R.id.btn_json:
                 builder = new ParseFull.ParseBuilder(this)
                        .url("http://www.ourkp.com/bk/get")
                        .displayer(new DialogNetDisplay(this))
                        .addNetParser(new StringNetParse())
                        .addNetParser(new JsonNetParse(JsonResult.class))
                        .resultHandler(new SimpleResultHandler());
                builder.build().execute(mOkHttpClient);
                break;
        }
    }
}
