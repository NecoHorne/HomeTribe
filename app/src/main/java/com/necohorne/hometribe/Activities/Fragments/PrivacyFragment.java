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

/**
 * Created by necoh on 2018/03/20.
 */

public class PrivacyFragment extends Fragment {

    private static final String TAG = "PrivacyFragment";
    private View mView;
    private Context mContext;
    private WebView mWebView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate( R.layout.fragment3_about, container, false);
        mContext = getActivity();
        String privacyPolicy = "https://www.iubenda.com/privacy-policy/75710776/full-legal";
        mWebView = (WebView) mView.findViewById( R.id.policy_webview);
        mWebView.loadUrl(privacyPolicy);

        return mView;
    }
}
