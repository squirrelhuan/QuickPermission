package cn.demomaster.quickpermission;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import cn.demomaster.quickpermission_library.PermissionHelper;
import cn.demomaster.quickpermission_library.model.PermissionModel;
import cn.demomaster.quickpermission_library.util.PermissionGroupUtil;

public class MainActivity extends AppCompatActivity {


    String[] permissions = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.SYSTEM_ALERT_WINDOW,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.INSTALL_PACKAGES};

    LinearLayout ll_root;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn_all = findViewById(R.id.btn_all);
        btn_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PermissionHelper.requestPermission(MainActivity.this,permissions, new PermissionHelper.PermissionListener() {
                    @Override
                    public void onPassed() {
                        Toast.makeText(MainActivity.this, "all通过", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onRefused() {
                        Toast.makeText(MainActivity.this, "all拒绝", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        ll_root = findViewById(R.id.ll_root);
        for(String permission:permissions){
            PermissionModel model = PermissionGroupUtil.getPermissionModel(permission);
            Button button = new Button(MainActivity.this);
            button.setTag(model);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PermissionModel model1 = (PermissionModel) v.getTag();
                    PermissionHelper.requestPermission(MainActivity.this, new String[]{
                            model1.getName()}, new PermissionHelper.PermissionListener() {
                        @Override
                        public void onPassed() {
                            Toast.makeText(MainActivity.this, "通过", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onRefused() {
                            Toast.makeText(MainActivity.this, "拒绝", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });
            Log.i("T",""+permission);
            button.setText(model.getDesc());
            ll_root.addView(button);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionHelper.onRequestPermissionsResult(MainActivity.this,requestCode,permissions,grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        PermissionHelper.onActivityResult(MainActivity.this,requestCode, resultCode, data);
    }
}