package com.example.livesapp.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.livesMultiProcess.R;
import com.example.livesapp.app.MyApp;
import com.example.livesapp.model.beans.TranslateData;
import com.example.livesapp.model.local.SharedPreferenceManager;
import com.example.livesapp.model.network.translate.TranslateNetProxy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class TranslateFragment extends Fragment implements TranslateNetProxy.CallBack{
    private static final String TAG = "TranslateFragment";
    Unbinder unbinder;

    @BindView(R.id.scrollView)
    ScrollView scrollView;
    @BindView(R.id.content)
    EditText content;
    @BindView(R.id.submit)
    Button submit;
    @BindView(R.id.translateResult)
    TextView translateResult;

    private static final String SPToken = "com.example.lives.fragment.TranslateFragment";
    private Set<String> history;
    private TranslateHistoryHint translateHistoryHint;


    public static TranslateFragment newInstance() {
        TranslateFragment fragment = new TranslateFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        history = new HashSet<>(SharedPreferenceManager.getList(SPToken, new ArrayList<>()));
        translateHistoryHint = new TranslateHistoryHint(MyApp.getContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = getLayoutInflater().inflate(R.layout.translate, null);
        unbinder = ButterKnife.bind(this, view);
        registerListener();
        return view;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void registerListener() {
//        todo scrollView?????????????????????????????????????????????edittext??????????????????????????????????????????onTouch??????????????????
        scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d(TAG, "onTouch: ");
                scrollView.requestFocus();
                content.clearFocus();
                return false;
            }
        });
//        ???????????????????????????????????????popupWindow
        content.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                translateHistoryHint.showAsDropDown(content);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
//        ???????????????????????????popupWindow
//        content.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                Log.d(TAG, "onFocusChange: "+  hasFocus);
//                if (hasFocus) {
//                    translateHistoryHint.showAsDropDown(content);
//                } else {
//                    translateHistoryHint.dismiss();
//                }
//            }
//        });
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String contentStr = content.getText().toString();
                history.add(contentStr);
                translateHistoryHint.notifyHistoryChanged(history);
                TranslateNetProxy.translate(contentStr,  TranslateFragment.this);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (unbinder != null) {
            unbinder.unbind();
        }
        SharedPreferenceManager.storeList(SPToken, new ArrayList<>(history));
    }

    @Override
    public void onSuccess(TranslateData translateData) {
        StringBuffer resultStr = new StringBuffer();
        if (translateData.getErrorCode() == 0) {  // ????????????
            resultStr.append("???????????????\n");
            resultStr.append("\t\t" + translateData.getTranslation() + "\n");
//            ????????????
            TranslateData.Basic basic = translateData.getBasic();
            if (basic != null) {
                List<String> explains = basic.getExplains();
                if (explains != null && explains.size() > 0) {
                    resultStr.append("\n???????????????\n");
                    resultStr.append(explains + "\n");
                }
            }
//            ????????????
            List<TranslateData.WebItem> web = translateData.getWeb();
            if (web != null && web.size() > 0) {
                resultStr.append("\n???????????????\n");
                for (TranslateData.WebItem webItem:web) {
                    resultStr.append("\t\t\t?????????");
                    resultStr.append("\t" + webItem.getKey() + "\n");
                    resultStr.append("\t\t\t?????????");
                    resultStr.append("\t" + webItem.getValue() + "\n");
                }
            }
        } else {
            resultStr.append("????????????...\n");
        }
        translateResult.setText(resultStr.toString());
    }

    @Override
    public void onFailed(Throwable e) {

    }

    /**
     * ????????????????????????????????????PopupWindow?????????????????????
     */
    class TranslateHistoryHint extends PopupWindow {

        private Context context;
        private SimpleAdapter adapter;
        private List<Map<String, String>> data;

        public TranslateHistoryHint(Context context) {
            super(context);
            this.context = context;
            prepareView();
        }

        public TranslateHistoryHint(Context context, AttributeSet attrs) {
            super(context, attrs);
            this.context = context;
            prepareView();
        }

        private void configDataToAdapter(Set<String> newData) {
            data.clear();
            for (String s: newData) {
                Map<String, String> map = new HashMap<>();
                map.put("item", s);
                data.add(map);
            }
        }
        private void prepareView() {
            ListView listView = new ListView(context);
            data = new ArrayList<>();
            configDataToAdapter(history);
            adapter = new SimpleAdapter(context,
                    data, android.R.layout.simple_list_item_1,
                    new String[]{"item"}, new int[]{android.R.id.text1});
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    content.setText(data.get((int)id).get("item"));
                    dismiss();
                }
            });
            setContentView(listView);
        }
        public void notifyHistoryChanged(Set<String> newData) {
            configDataToAdapter(newData);
            adapter.notifyDataSetChanged();
        }
    }
}
