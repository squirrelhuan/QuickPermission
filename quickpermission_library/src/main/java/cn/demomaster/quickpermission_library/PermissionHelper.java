package cn.demomaster.quickpermission_library;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.demomaster.quickpermission_library.dialog.DialogWindowActivity;
import cn.demomaster.quickpermission_library.model.PermissionModel;
import cn.demomaster.quickpermission_library.model.PermissionRequest;

import static cn.demomaster.quickpermission_library.util.PermissionGroupUtil.getPermissionModels;

/**
 * 系统权限管理
 * Created by huan on 2017/11/14.
 */
public class PermissionHelper {
    public static String TAG = "PermissionManager";
    public static final int REQUEST_PERMISS_COMMON_CODE = 32418;
    //请求悬浮
    public static final int REQUEST_PERMISS_SPECIAL_CODE = 32419;
    private static PermissionHelper instance;

    public static PermissionHelper getInstance() {
        if (instance == null) {
            instance = new PermissionHelper();
        }
        return instance;
    }

    private PermissionHelper() {
    }

    static AlertDialog.Builder builder;
    static AlertDialog dialog;

    /**
     * alert 消息提示框显示
     *
     * @param context  上下文
     * @param title    标题
     * @param message  消息
     * @param listener 监听器
     */
    public static void showAlert(Context context, String title, String message, boolean cancelable, DialogInterface.OnClickListener listener, DialogInterface.OnClickListener dissmisslistener) {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("确定", listener);
        if (cancelable) {
            builder.setNegativeButton("取消", dissmisslistener);
        }
        builder.setCancelable(cancelable);
        //builder.setIcon(R.mipmap.quickdevelop_ic_launcher);
        dialog = builder.create();
        dialog.show();
    }

    //跳转到设置-请求悬浮窗权限
    @TargetApi(Build.VERSION_CODES.M)
    public static void requestOverlayPermission(Activity context, Class<? extends Activity> guidActivityClass) {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
        intent.setData(Uri.parse("package:" + context.getPackageName()));
        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ((Activity) context).startActivityForResult(intent, REQUEST_PERMISS_SPECIAL_CODE);
    }

    // 跳转到设置-允许安装未知来源-页面
    @TargetApi(Build.VERSION_CODES.O)
    public static void startInstallPermissionSettingActivity(final Context context) {
        Uri packageURI = Uri.parse("package:" + context.getPackageName());
        //注意这个是8.0新API
        Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageURI);
        ((Activity) context).startActivityForResult(intent, REQUEST_PERMISS_SPECIAL_CODE);
        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //context.startActivity(intent);//startActivityForResult
    }

    private static void startPermissionActivity(Activity context, PermissionRequest request) {
        Intent intent = null;
        int code = REQUEST_PERMISS_SPECIAL_CODE;
        String permissionName = request.getPermissionModelList().get(request.getIndex()).getName();
        requestMap.put(REQUEST_PERMISS_SPECIAL_CODE, request);

        Uri packageURI = Uri.parse("package:" + context.getPackageName());
        switch (permissionName) {
            case Manifest.permission.WRITE_SETTINGS:
                intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                break;
            case Manifest.permission.PACKAGE_USAGE_STATS://查看网络访问状态
                intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                break;
            case Manifest.permission.REQUEST_INSTALL_PACKAGES://弹框提示用户手动打开
            case Manifest.permission.INSTALL_PACKAGES://弹框提示用户手动打开
                intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                break;
            case Manifest.permission.SYSTEM_ALERT_WINDOW://悬浮窗权限
                intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                break;
            case Manifest.permission.BIND_ACCESSIBILITY_SERVICE://跳转系统自带界面 辅助功能界面
                intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
                //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                packageURI = null;
                break;
            case Manifest.permission.MANAGE_EXTERNAL_STORAGE:
                intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                break;
        }
        if(packageURI!=null) {
            intent.setData(packageURI);
        }
        ((Activity) context).startActivityForResult(intent, code);
        PermissionListener listener = request.getListener();
        if(listener!=null) {
            listener.onStartPermissionActivity(context, request);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static boolean isGrantedUsagePremission(Context context) {
        AppOpsManager appOps = (AppOpsManager) context
                .getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), context.getPackageName());
        if (mode == AppOpsManager.MODE_DEFAULT) {
            return (context.checkCallingOrSelfPermission(
                    Manifest.permission.PACKAGE_USAGE_STATS)
                    == PackageManager.PERMISSION_GRANTED);
        } else {
            return (mode == AppOpsManager.MODE_ALLOWED);
        }
    }

    /**
     * 获取权限是否可用
     *
     * @param context
     * @param permissionName
     * @return
     */
    public static boolean getPermissionStatus(Context context, String permissionName) {
        boolean isRegisted = isInAndroidManifest(context, permissionName);
        if (!isRegisted) {
            Log.e(TAG, "[" + permissionName + "]权限未在AndroidManifest注册");
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {// 当手机系统大于 23 时，才有必要去判断权限是否获取
            switch (permissionName) {
                case Manifest.permission.REQUEST_INSTALL_PACKAGES:
                case Manifest.permission.INSTALL_PACKAGES:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        return context.getPackageManager().canRequestPackageInstalls();
                    }
                    break;
                case Manifest.permission.SYSTEM_ALERT_WINDOW:
                    return Settings.canDrawOverlays(context);
                case Manifest.permission.PACKAGE_USAGE_STATS:
                    return isGrantedUsagePremission(context);
                case Manifest.permission.WRITE_SETTINGS:
                    return Settings.System.canWrite(context);
                case Manifest.permission.WRITE_SECURE_SETTINGS:
                    return Settings.System.canWrite(context);
                case Manifest.permission.BIND_ACCESSIBILITY_SERVICE:
                    return false;
                case Manifest.permission.MANAGE_EXTERNAL_STORAGE:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        boolean p = Environment.isExternalStorageManager();
                        return p;// 权限是否已经 授权 GRANTED---授权  DINIED---拒绝
                    }
                    break;
                default:
                    int r = ContextCompat.checkSelfPermission(context.getApplicationContext(), permissionName);
                    return (r == PackageManager.PERMISSION_GRANTED);// 权限是否已经 授权 GRANTED---授权  DINIED---拒绝
                /*Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(myIntent);*/
            }
        }
        return true;
    }

    /**
     * 在manifest中是否已经注册
     *
     * @param context
     * @param permissionName
     * @return
     */
    private static boolean isInAndroidManifest(Context context, String permissionName) {
        if(permissionName.equals(Manifest.permission.BIND_ACCESSIBILITY_SERVICE)){
            return true;
        }
        PackageManager pm = context.getPackageManager();
        PackageInfo pack = null;
        try {
            pack = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS);
            String[] permissionStrings = pack.requestedPermissions;
            if (permissionStrings != null) {
                for (String str : permissionStrings) {
                    if (str.equals(permissionName)) {
                        return true;
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    //普通权限
    public static boolean getPermissionStatus(Context context, String[] permissions) {
        for (String str : permissions) {
            if (!getPermissionStatus(context, str)) {
                return false;
            }
        }
        return true;
    }

    //普通权限
    public static boolean getPermissionStatus(Context context, List<PermissionModel> permissions) {
        for (PermissionModel model : permissions) {
            if (!getPermissionStatus(context, model.getName())) {
                return false;
            }
        }
        return true;
    }

    /**
     * 权限检查
     *
     * @param context
     * @param permissions
     * @param listener
     */
    public static void requestPermission(Activity context, String[] permissions, PermissionListener listener) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { //当手机系统大于 23 时，才有必要去判断权限是否获取
            List<PermissionModel> permissionModelList = getPermissionModels(permissions);
            //List<String> list = getPermissionGroup(permissionModelList);
            PermissionRequest request = new PermissionRequest();
            request.setIndex(0);
            request.setListener(listener);
            request.setPermissionModelList(permissionModelList);
            request.setBaseCode((int) (System.currentTimeMillis() % 10000));
            requestPermission(context, request);
        } else {
            if (listener != null) {
                listener.onPassed();
            }
        }
    }

    private static void requestPermission(Activity context, PermissionRequest request) {
        if (request.getPermissionModelList() == null || request.getIndex() >= request.getPermissionModelList().size()) {
            if (request.getListener() != null) {
                request.getListener().onPassed();
            }
            return;
        }
        requestPermissionImpl(context, request);
    }

    private static Map<Integer, PermissionRequest> requestMap = new HashMap<>();

    /**
     * 发起权限申请
     *
     * @param context
     * @param request
     */
    private static void requestPermissionImpl(final Activity context, PermissionRequest request) {
        String permissionName = request.getPermissionModelList().get(request.getIndex()).getName();
        if (getPermissionStatus(context, permissionName)) {//自定义申请权限
            dealPermissionResult(context, request);
            return;
        }
        PermissionListener listener = request.getListener();
        if (listener != null) {
            boolean b = getPermissionStatus(context, permissionName);
            System.out.println(permissionName + "=" + b);
            if (listener.handRequest(context, request)) {//自定义申请权限
                return;
            }
        }

        requestPermissionImpl2(context, request);
    }

    private static void requestPermissionImpl2(Activity context, PermissionRequest request) {
        String permissionName = request.getPermissionModelList().get(request.getIndex()).getName();
        String[] permissions = new String[]{permissionName};
        System.out.println("权限申请:" + request.getIndex() + ",RequestCode:" + request.getRequestCode() + ",[" + permissionName + "]," + request.getPermissionModelList().get(request.getIndex()).getDesc());
        switch (permissionName) {
            case Manifest.permission.REQUEST_INSTALL_PACKAGES://弹框提示用户手动打开
            case Manifest.permission.INSTALL_PACKAGES://弹框提示用户手动打开
            case Manifest.permission.SYSTEM_ALERT_WINDOW://System.out.println("悬浮窗权限");
            case Manifest.permission.PACKAGE_USAGE_STATS://查看网络访问状态
            case Manifest.permission.WRITE_SETTINGS:
            case Manifest.permission.BIND_ACCESSIBILITY_SERVICE:
            case Manifest.permission.MANAGE_EXTERNAL_STORAGE:
                startPermissionActivity(context, request);
                break;
            default:
                requestMap.put(request.getRequestCode(), request);
                ActivityCompat.requestPermissions((Activity) context, permissions, request.getRequestCode());
                break;
        }
    }

    /**
     * 权限请求结果
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    public static void onRequestPermissionsResult(Activity context, int requestCode, @NonNull String[] permissions,
                                                  @NonNull int[] grantResults) {
        //System.out.println("权限申请结果:" + requestCode + "," + permissions[0] + "," + grantResults);
        if (requestMap.containsKey(requestCode)) {
            PermissionRequest request = requestMap.get(requestCode);
            requestMap.remove(requestCode);
            dealPermissionResult(context, request);
        }
    }

    private static void dealPermissionResult(Activity context, PermissionRequest request) {
        if (request != null) {
            PermissionListener listener = request.getListener();
            List<PermissionModel> permissionModelList = request.getPermissionModelList();
            String permissionName = permissionModelList.get(request.getIndex()).getName();
            //Log.i(TAG, "权限申请结果：" + permissionName);
            if (!getPermissionStatus(context, permissionName) && listener != null) {
                Log.i(TAG, "请求被拒绝，是否强制重新发起申请=" + listener.handRefused(context, request));
                if (listener.handRefused(context, request)) {//请求被拒绝，是否强制重新发起申请
                    Log.e(TAG, "请求被拒绝，是否强制重新发起申请2");
                    requestPermission(context, request);
                    return;
                }
            }

            if (request.getIndex() + 1 >= permissionModelList.size()) {//有权限未通过
                if (listener != null) {
                    if (!getPermissionStatus(context, permissionModelList)) {
                        request.getListener().onRefused();
                    } else {//全部权限通过
                        request.getListener().onPassed();
                    }
                }
                return;
            } else {
                request.setIndex(request.getIndex() + 1);
                requestPermission(context, request);
            }
        }
    }

    public static void onActivityResult(Activity context, int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_PERMISS_SPECIAL_CODE) {
            //System.out.println("权限申请结果:" + requestCode + ",resultCode=" + resultCode);
            if (requestMap.containsKey(requestCode)) {
                PermissionRequest request = requestMap.get(requestCode);
                requestMap.remove(requestCode);
                dealPermissionResult(context, request);
            }
        }
    }

    /**
     * 监听器，监听权限是否通过
     */
    public interface PermissionListenerInterface {
        void onPassed();//所有权限均已授权

        void onRefused();//权限未能全部通过

        boolean handRequest(Activity context, PermissionRequest permissionRequest);//重写权限申请

        boolean handRefused(Activity context, PermissionRequest permissionRequest);//处理拒绝事件

        void onStartPermissionActivity(Activity context, PermissionRequest request);//页面跳转申请权限
    }

    public static abstract class PermissionListener implements PermissionListenerInterface {
        /**
         * 可以重写，权限申请流程
         *
         * @param context
         * @param request
         * @return
         */
        @Override
        public boolean handRequest(Activity context, PermissionRequest request) {
            PermissionModel permissionModel = request.getPermissionModelList().get(request.getIndex());
            System.out.println("权限被拒绝：" + permissionModel.getName());
            //弹框提示用户手动打开
            switch (permissionModel.getName()) {
                case Manifest.permission.INSTALL_PACKAGES:
                case Manifest.permission.REQUEST_INSTALL_PACKAGES:
                case Manifest.permission.SYSTEM_ALERT_WINDOW:
                case Manifest.permission.PACKAGE_USAGE_STATS:
                case Manifest.permission.WRITE_SETTINGS:
                case Manifest.permission.BIND_ACCESSIBILITY_SERVICE:
                    showPermissionDialog(context, request);
                    return true;
                case Manifest.permission.MANAGE_EXTERNAL_STORAGE:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        showPermissionDialog(context, request);
                        return true;
                    }
                    return false;
            }
            return false;
        }

        public void showPermissionDialog(Activity context, PermissionRequest request) {
            PermissionModel permissionModel = request.getPermissionModelList().get(request.getIndex());
            String title = "权限申请";
            String tip = "";
            switch (permissionModel.getName()) {
                case Manifest.permission.INSTALL_PACKAGES:
                case Manifest.permission.REQUEST_INSTALL_PACKAGES:
                    tip = "安装应用权限";
                    break;
                case Manifest.permission.SYSTEM_ALERT_WINDOW:
                    tip = "悬浮窗权限";
                    break;
                case Manifest.permission.PACKAGE_USAGE_STATS:
                    tip = "查看网络访问状态";
                    break;
                case Manifest.permission.WRITE_SETTINGS:
                    tip = "需改系统设置";
                    break;
                case Manifest.permission.BIND_ACCESSIBILITY_SERVICE:
                    tip = "无障碍服务";
                    break;
                case Manifest.permission.MANAGE_EXTERNAL_STORAGE:
                    tip = "文件管理权限";
                    break;
            }
            showAlert(context, title, tip, !handRefused(context, request), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    requestPermissionImpl2(context, request);
                }
            }, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    dealPermissionResult(context, request);
                }
            });
        }

        /**
         * 权限被拒绝，
         *
         * @param context
         * @param request
         * @return true 重新申请 false 忽略继续申请下一个
         */
        @Override
        public boolean handRefused(Activity context, PermissionRequest request) {
            PermissionModel permissionModel = request.getPermissionModelList().get(request.getIndex());
            switch (permissionModel.getName()) {
                case Manifest.permission.SYSTEM_ALERT_WINDOW:
                    return false;
                case Manifest.permission.INSTALL_PACKAGES:
                    return false;
            }
            return false;//返回此权限是否必须，若是必要权限可以返回true，重复请求
        }

        @Override
        public void onStartPermissionActivity(Activity context, PermissionRequest request) {
            String permissionName = request.getPermissionModelList().get(request.getIndex()).getName();
            if (permissionName.equals(Manifest.permission.SYSTEM_ALERT_WINDOW)
            ||permissionName.equals(Manifest.permission.BIND_ACCESSIBILITY_SERVICE)) {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(context, DialogWindowActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("permission", permissionName);
                        intent.putExtras(bundle);
                        (context).startActivity(intent);
                        // 参数1：SecondActivity进场动画，参数2：MainActivity出场动画
                        ((Activity) context).overridePendingTransition(0, 0);
                    }
                });
            }
        }
    }
}