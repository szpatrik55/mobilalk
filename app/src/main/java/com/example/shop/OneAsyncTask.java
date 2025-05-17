package com.example.shop;

import android.os.AsyncTask;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.Random;

public class OneAsyncTask extends AsyncTask<Void,Void, String> {

    private WeakReference<TextView> mTextView;

    OneAsyncTask(TextView tv) {
        mTextView = new WeakReference<>(tv);
    }

    @Override
    protected String doInBackground(Void... voids) {
        Random r = new Random();
        int n = r.nextInt(11);
        int ms = n * 300;
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return "Látogató.";
    }

    protected void onPostExecute(String result) {
        mTextView.get().setText(result);
    }
}