package com.github.anzewei.parsefull.impl;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.ImageView;

import com.github.anzewei.parsefull.NetDisplay;
import com.github.anzewei.parsefull.ParseFull;
import com.github.anzewei.parsefull.R;

import java.util.Vector;

/**
 * ParseFull
 * @author An Zewei (anzewei88[at]gmail[dot]com)
 * @since 1.0.0
 */

public class DialogNetDisplay extends NetDisplay {

    private static final int CIRCLE_BG_LIGHT = 0xFFFAFAFA;
    private  MaterialProgressDrawable mProgress;
    private int mnRequestCount = 0;
    private Dialog mDialogLoading;
    private Context mContext;
    private Vector<ParseFull> mTasks = new Vector<>();

    public DialogNetDisplay(Context context) {
        if (context instanceof Activity && !((Activity) context).isFinishing()) {
            mContext = context;
        }
    }

    private void showDialog() {
        ensureDialog();
        if (mDialogLoading.isShowing()) {
            return;
        }
        mProgress.start();
        mDialogLoading.show();
    }

    private void ensureDialog() {
        if (mDialogLoading != null)
            return;
        mDialogLoading = createDialog(mContext);
        mDialogLoading.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                cancel();
            }
        });
    }

    public void cancel() {
        while (mTasks.size() > 0) {
            ParseFull listener = mTasks.elementAt(0);
            listener.cancel();
            mTasks.remove(listener);
        }
        if (mDialogLoading != null && mDialogLoading.isShowing()) {
            mProgress.stop();
            mDialogLoading.dismiss();
        }
    }

    protected Dialog createDialog(Context context) {
        ImageView imageView;
        Dialog dialog = new Dialog(context, R.style.dialog);
        dialog.setContentView(imageView = new CircleImageView(context, Color.WHITE));
//        imageView = (ImageView) dialog.findViewById(R.id.progress);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        imageView.setVisibility(View.VISIBLE);
        imageView.setImageDrawable(null);
        mProgress = new MaterialProgressDrawable(mContext, imageView);
        mProgress.setBackgroundColor(CIRCLE_BG_LIGHT);
        mProgress.setAlpha(255);
        mProgress.updateSizes(MaterialProgressDrawable.LARGE);
        imageView.setImageDrawable(mProgress);
        return dialog;
    }

    @Override
    public void onPreExecute(ParseFull parseFull) {
        mTasks.add(parseFull);
        mnRequestCount++;
        showDialog();
    }

    @Override
    public void onPostExecute(ParseFull parseFull) {
        mTasks.remove(parseFull);
        mnRequestCount--;
        mnRequestCount = mnRequestCount < 0 ? 0 : mnRequestCount;
        if (mnRequestCount == 0) {
            if (mDialogLoading != null && mDialogLoading.isShowing() && !((Activity) mContext).isFinishing()) {
                mDialogLoading.dismiss();
            }
        }
    }
}
