package cn.demomaster.quickpermission_library.dialog;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.TextView;

import cn.demomaster.quickpermission_library.R;
import cn.demomaster.quickpermission_library.util.PermissionGroupUtil;

public class DialogWindowActivity extends DialogActivity {
    ValueAnimator alphaAnim;
    ViewGroup ll_panel;

    TextView tv_title,tv_desc;
    @Override
    public void generateView(LayoutInflater layoutInflater, ViewGroup viewParent) {
        layoutInflater.inflate(R.layout.activity_dialog_window, viewParent);
        Button button = viewParent.findViewById(R.id.btn_ok);
        button.setOnClickListener(v -> close());
        ll_panel = viewParent.findViewById(R.id.ll_panel);
        ll_panel.setVisibility(View.GONE);
        startBakcground();

        tv_title = viewParent.findViewById(R.id.tv_title);
        tv_desc = viewParent.findViewById(R.id.tv_desc);
        Bundle bundle = getIntent().getExtras();
        String permission = bundle.getString("permission", Manifest.permission.SYSTEM_ALERT_WINDOW);

        tv_title.setText("权限申请");
        tv_desc.setText(PermissionGroupUtil.getPermissionDescription(permission));
        /*switch (permission){
            case Manifest.permission.INSTALL_PACKAGES:
                break;
            case Manifest.permission.SYSTEM_ALERT_WINDOW:
                tv_desc.setText("");
                break;
            case Manifest.permission.PACKAGE_USAGE_STATS:
                break;
            case Manifest.permission.WRITE_SETTINGS:
                break;
        }*/
    }

    int alpha = 0x33;
    private void startBakcground() {
        alphaAnim = ValueAnimator.ofInt(0, alpha);
        //执行事件
        alphaAnim.setDuration(400);//anticipate_interpolator
        alphaAnim.setInterpolator(new AccelerateInterpolator());
        alphaAnim.addUpdateListener(animation -> {
            int value = (int) animation.getAnimatedValue();
            int value1 = value << (6 * 4);
            //System.out.println("value1=" + value1);
            getWindow().setBackgroundDrawable(new ColorDrawable(value1));
        });
        //延迟
        //alphaAnim.setStartDelay(300);
        alphaAnim.start();
        startPanelAnim();
    }

    private void startPanelAnim() {
        ScaleAnimation animation = new ScaleAnimation(0, 1, 0, 1, Animation.RELATIVE_TO_SELF, 0.5f, 1, 0.5f);
        animation.setDuration(400);
        animation.setFillAfter(true);
        //设置动画结束之后的状态是否是动画的最终状态，true，表示是保持动画结束时的最终状态
        animation.setRepeatCount(0);
        //animation.setStartDelay(300);
        animation.setInterpolator(new AccelerateInterpolator());
        //设置循环次数，0为1次
        ll_panel.startAnimation(animation);
        ll_panel.setVisibility(View.VISIBLE);
    }

    private void closePanelAnim() {
        ScaleAnimation animation = new ScaleAnimation(1, 0, 1, 0, Animation.RELATIVE_TO_SELF, 0.5f, 1, 0.5f);
        animation.setDuration(300);
        //设置持续时间
        animation.setFillAfter(true);
        //设置动画结束之后的状态是否是动画的最终状态，true，表示是保持动画结束时的最终状态
        animation.setRepeatCount(0);
        //animation.setStartDelay(300);
        animation.setInterpolator(new AccelerateInterpolator());
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                closeBackground();
                ll_panel.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        //设置循环次数，0为1次
        ll_panel.startAnimation(animation);
        ll_panel.setVisibility(View.VISIBLE);
    }

    @Override
    public void close() {
        closePanelAnim();
    }

    private void closeBackground() {
        ValueAnimator alphaAnim = ValueAnimator.ofInt(alpha, 0);
        //执行事件
        alphaAnim.setDuration(300);
        alphaAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                finish();
                // 参数1：MainActivity进场动画，参数2：SecondActivity出场动画
                overridePendingTransition(0, 0);//R.anim.abc_slide_out_bottom
            }
        });
        alphaAnim.addUpdateListener(animation -> {
            int value = (int) animation.getAnimatedValue();
            int value1 = value << (6 * 4);
            //System.out.println("value1=" + value1);
            getWindow().setBackgroundDrawable(new ColorDrawable(value1));
        });
        //alphaAnim.setStartDelay(400);
        alphaAnim.start();
    }
    /*@Override
    public void finish() {
        super.finish();
        // 参数1：MainActivity进场动画，参数2：SecondActivity出场动画
        overridePendingTransition(0, R.anim.fade_out);
    }*/
}