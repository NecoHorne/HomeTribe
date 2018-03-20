package com.necohorne.hometribe.Activities.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.necohorne.hometribe.R;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by necoh on 2018/03/20.
 */

public class TandCFragment extends Fragment {

    private static final String TAG = "TandCFragment";
    private View mView;
    private Context mContext;
    private WebView mWebView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate( R.layout.fragment2_about, container, false);
        mContext = getActivity();
        mWebView = (WebView) mView.findViewById( R.id.terms_webview);
        String mData = getStringFromAssets( "tc.html", mContext );
        mWebView.loadDataWithBaseURL("file///android_asset/",mData, "text/html", "utf-8", null );

        return mView;
    }

    public static String getStringFromAssets(String name, Context p_context){
        try {
            InputStream is = p_context.getAssets().open(name);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String bufferString = new String(buffer);
            return bufferString;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
