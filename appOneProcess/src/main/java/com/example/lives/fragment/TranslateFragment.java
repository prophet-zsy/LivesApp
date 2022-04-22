package com.example.lives.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.lives.R;
import com.example.lives.beans.TranslateData;
import com.example.lives.network.translate.TranslateNetProxy;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class TranslateFragment extends Fragment implements UpdateTranslateUIListener {

    Unbinder unbinder;

    @BindView(R.id.content)
    EditText content;
    @BindView(R.id.submit)
    Button submit;
    @BindView(R.id.translateResult)
    TextView translateResult;

    public static TranslateFragment newInstance() {

        Bundle args = new Bundle();

        TranslateFragment fragment = new TranslateFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = getLayoutInflater().inflate(R.layout.translate, null);
        unbinder = ButterKnife.bind(this, view);
        registerListener();
        return view;
    }

    private void registerListener() {
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String contentStr = content.getText().toString();
                TranslateNetProxy.translate(contentStr,  TranslateFragment.this);
            }
        });
    }

    @Override
    public void UpdateTranslateUI(TranslateData translateData) {
        StringBuffer resultStr = new StringBuffer();
        if (translateData.getErrorCode() == 0) {  // 请求成功
            resultStr.append("翻译结果：\n");
            resultStr.append("\t\t" + translateData.getTranslation() + "\n");
//            词典解释
            TranslateData.Basic basic = translateData.getBasic();
            if (basic != null) {
                List<String> explains = basic.getExplains();
                if (explains != null && explains.size() > 0) {
                    resultStr.append("\n词典解释：\n");
                    resultStr.append(explains + "\n");
                }
            }
//            网络释义
            List<TranslateData.WebItem> web = translateData.getWeb();
            if (web != null && web.size() > 0) {
                resultStr.append("\n网络释义：\n");
                for (TranslateData.WebItem webItem:web) {
                    resultStr.append("\t\t\t原文：");
                    resultStr.append("\t" + webItem.getKey() + "\n");
                    resultStr.append("\t\t\t译文：");
                    resultStr.append("\t" + webItem.getValue() + "\n");
                }
            }
        } else {
            resultStr.append("翻译失败...\n");
        }
        translateResult.setText(resultStr.toString());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (unbinder != null) {
            unbinder.unbind();
        }
    }
}
