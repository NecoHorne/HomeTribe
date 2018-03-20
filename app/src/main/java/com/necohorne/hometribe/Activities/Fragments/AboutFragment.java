package com.necohorne.hometribe.Activities.Fragments;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import com.necohorne.hometribe.R;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by necoh on 2018/03/20.
 */

public class AboutFragment extends Fragment {

    private static final String TAG = "AboutFragment";
    private View mView;
    private Context mContext;
    private TextView versionNum;
    private TextView link;
    private TextView facebookLink;
    private WebView mWebView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate( R.layout.fragment_about, container, false);
        mContext = getActivity();
        versionNum = (TextView) mView.findViewById( R.id.about_version_num );
        link = (TextView) mView.findViewById( R.id.about_link);
        facebookLink = (TextView) mView.findViewById( R.id.about_follow_link);
        mWebView = (WebView) mView.findViewById( R.id.about_webview);

        try {
            PackageInfo pInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            String version = pInfo.versionName;
            versionNum.setText(version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        link.setText(Html.fromHtml("<a href=\"https://www.necohorne.com/hometribe\">hometribe</a>"));
        link.setMovementMethod(LinkMovementMethod.getInstance());
        facebookLink.setText(Html.fromHtml( "<a href=\"https://www.facebook.com/hometribeapp/\">Facebook</a>"));
        facebookLink.setMovementMethod(LinkMovementMethod.getInstance());

        String mData = getStringFromAssets( "Acknowledgements.html", mContext );
        mWebView.loadDataWithBaseURL("file///android_asset/",mData, "text/html", "utf-8", null );

        return mView;
    }

    public static String getStringFromAssets(String name, Context p_context){
        try{
            InputStream is = p_context.getAssets().open(name);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String bufferString = new String(buffer);
            return bufferString;
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;

    }
}
