package cn.demomaster.quickpermission_library.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.lang.ref.WeakReference;

import cn.demomaster.quickpermission_library.util.StatusBarUtil;

public abstract class DialogActivity extends Activity {

    private long id;
    FrameLayout frameLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        if(bundle!=null&&bundle.containsKey("QDDialogId")){
            id = bundle.getLong("QDDialogId");
        }
        frameLayout = new FrameLayout(this);
        generateView(getLayoutInflater(),frameLayout);
        setContentView(frameLayout);
        StatusBarUtil.transparencyBar(new WeakReference<Activity>(this));
        //getWindow().setBackgroundDrawable(new ColorDrawable(0x55000000));
        frameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                close();
            }
        });
    }
    
    public abstract void generateView(LayoutInflater layoutInflater, ViewGroup viewParent);

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //ActivityDialogHelper.onDialogActivityDismiss(id);
    }

    public void close() {
        finish();
    }
    
}