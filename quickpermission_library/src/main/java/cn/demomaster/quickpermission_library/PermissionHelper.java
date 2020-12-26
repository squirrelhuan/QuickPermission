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
import android.os.Handler;
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
    public static void showAlert(Context context, String title, String message,boolean cancelable, DialogInterface.OnClickListener listener,DialogInterface.OnClickListener dissmisslistener) {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("确定", listener);
        if(cancelable) {
            builder.setNegativeButton("取消", dissmisslistener);
        }
        builder.setCancelable(cancelable);
        //builder.setIcon(R.mipmap.quickdevelop_ic_launcher);
        dialog = builder.create();
        dialog.show();
    }

    //跳转到设置-请求悬浮窗权限
    @TargetApi(Build.VERSION_CODES.M)
    public static void getOverlayPermission(Activity context) {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
        intent.setData(Uri.parse("package:" + context.getPackageName()));
        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivityForResult(intent, REQUEST_PERMISS_SPECIAL_CODE);
    }

    // 跳转到设置-允许安装未知来源-页面
    @TargetApi(Build.VERSION_CODES.O)
    public static void startInstallPermissionSettingActivity(final Context context) {
        Uri packageURI = Uri.parse("package:" + context.getPackageName());
        Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageURI);
        ((Activity) context).startActivityForResult(intent,REQUEST_PERMISS_SPECIAL_CODE);
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
                default:
                    int r = ContextCompat.checkSelfPermission(context.getApplicationContext(), permissionName);
                    return (r == PackageManager.PERMISSION_GRANTED);// 权限是否已经 授权 GRANTED---授权  DINIED---拒绝
            }
        }
        return true;
    }

    /**
     * 在manifest中是否已经注册
     * @param context
     * @param permissionName
     * @return
     */
    private static boolean isInAndroidManifest(Context context, String permissionName) {
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
    public static void requestPermission(Context context, String[] permissions, PermissionListener listener) {
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

    private static void requestPermission(Context context, PermissionRequest request) {
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
    private static void requestPermissionImpl(final Context context, PermissionRequest request) {
        String permissionName = request.getPermissionModelList().get(request.getIndex()).getName();
        if(getPermissionStatus(context,permissionName)) {//自定义申请权限
           dealPermissionResult(context,request);
           return;
        }
        PermissionListener listener = request.getListener();
        if(listener!=null){
            boolean b = getPermissionStatus(context,permissionName);
            System.out.println(permissionName+"="+b);
            if(listener.handRequest(context,request)){//自定义申请权限
               return;
           }
        }

        requestPermissionImpl2(context,request);
    }

    private static void requestPermissionImpl2(Context context, PermissionRequest request) {
        String permissionName = request.getPermissionModelList().get(request.getIndex()).getName();
        String[] permissions = new String[]{permissionName};
        System.out.println("权限申请:" + request.getIndex() +",RequestCode:"+request.getRequestCode()+ ",[" + permissionName + "]," + request.getPermissionModelList().get(request.getIndex()).getDesc());
        switch (permissionName) {
            case Manifest.permission.INSTALL_PACKAGES://弹框提示用户手动打开
                requestMap.put(REQUEST_PERMISS_SPECIAL_CODE, request);
                startInstallPermissionSettingActivity(context);
                break;
            case Manifest.permission.SYSTEM_ALERT_WINDOW://System.out.println("悬浮窗权限");
                requestMap.put(REQUEST_PERMISS_SPECIAL_CODE, request);
                getOverlayPermission((Activity) context);
                break;
            case Manifest.permission.PACKAGE_USAGE_STATS://查看网络访问状态
                requestMap.put(REQUEST_PERMISS_SPECIAL_CODE, request);
                Intent intent3 = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                ((Activity) context).startActivityForResult(intent3, REQUEST_PERMISS_SPECIAL_CODE);
                break;
            case Manifest.permission.WRITE_SETTINGS:
                requestMap.put(REQUEST_PERMISS_SPECIAL_CODE, request);
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ((Activity) context).startActivityForResult(intent,REQUEST_PERMISS_SPECIAL_CODE);
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
    public static void onRequestPermissionsResult(Context context, int requestCode, @NonNull String[] permissions,
                                                  @NonNull int[] grantResults) {
        System.out.println("权限申请结果:" + requestCode + "," + permissions[0] + "," + grantResults);
        if (requestMap.containsKey(requestCode)) {
            PermissionRequest request = requestMap.get(requestCode);
            requestMap.remove(requestCode);
            dealPermissionResult(context,request);
        }
    }

    private static void dealPermissionResult(Context context,PermissionRequest request) {
        if (request != null) {
            PermissionListener listener = request.getListener();
            List<PermissionModel> permissionModelList = request.getPermissionModelList();
            String permissionName = permissionModelList.get(request.getIndex()).getName();

            Log.i(TAG,"dealPermissionResult="+permissionName);
            if (!getPermissionStatus(context, permissionName)&&listener != null) {
                Log.i(TAG,"请求被拒绝，是否强制重新发起申请="+listener.handRefused(context,request));
                if (listener.handRefused(context,request)) {//请求被拒绝，是否强制重新发起申请
                    Log.e(TAG,"请求被拒绝，是否强制重新发起申请2");
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

    public static void onActivityResult(Context context, int requestCode, int resultCode, @Nullable Intent data) {
        System.out.println("权限申请结果:" + requestCode + ",resultCode=" + resultCode);
        if (requestCode==REQUEST_PERMISS_SPECIAL_CODE){
            if (requestMap.containsKey(requestCode)) {
                PermissionRequest request = requestMap.get(requestCode);
                requestMap.remove(requestCode);
                dealPermissionResult(context,request);
            }
        }
    }

    /**
     * 监听器，监听权限是否通过
     */
    public static interface PermissionListenerInterface {
        void onPassed();//所有权限均已授权
        void onRefused();//权限未能全部通过
        boolean handRequest(Context context,PermissionRequest permissionRequest);//重写权限申请
        boolean handRefused(Context context,PermissionRequest permissionRequest);//处理拒绝事件
    }

    public static abstract class PermissionListener implements PermissionListenerInterface {
        /**
         * 可以重写，权限申请流程
         * @param context
         * @param request
         * @return
         */
        @Override
        public boolean handRequest(Context context, PermissionRequest request) {
            PermissionModel permissionModel = request.getPermissionModelList().get(request.getIndex());
            System.out.println("权限被拒绝：" + permissionModel.getName());
            //弹框提示用户手动打开
            switch (permissionModel.getName()) {
                case Manifest.permission.INSTALL_PACKAGES:
                case Manifest.permission.SYSTEM_ALERT_WINDOW:
                case Manifest.permission.PACKAGE_USAGE_STATS:
                case Manifest.permission.WRITE_SETTINGS:
                    showPermissionDialog(context,request);
                    return true;
            }
            return false;
        }

        public void showPermissionDialog(Context context,PermissionRequest request){
            PermissionModel permissionModel = request.getPermissionModelList().get(request.getIndex());
            String title="权限申请";
            String tip = "";
            switch (permissionModel.getName()){
                    case Manifest.permission.INSTALL_PACKAGES:
                        tip="安装应用权限";
                        break;
                    case Manifest.permission.SYSTEM_ALERT_WINDOW:
                        tip="悬浮窗权限";
                        break;
                    case Manifest.permission.PACKAGE_USAGE_STATS:
                        tip="查看网络访问状态";
                        break;
                    case Manifest.permission.WRITE_SETTINGS:
                        tip="需改系统设置";
                        break;
                }
                showAlert(context, title, tip,!handRefused(context,request), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestPermissionImpl2(context, request);
                    }
                },new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        dealPermissionResult(context, request);
                    }
                });
        }

        /**
         * 权限被拒绝，
         * @param context
         * @param request
         * @return true 重新申请 false 忽略继续申请下一个
         */
        @Override
        public boolean handRefused(Context context, PermissionRequest request) {
            PermissionModel permissionModel = request.getPermissionModelList().get(request.getIndex());
            switch (permissionModel.getName()) {
                case Manifest.permission.SYSTEM_ALERT_WINDOW:
                    return false;
                case Manifest.permission.INSTALL_PACKAGES:
                    return false;
            }
            return false;//返回此权限是否必须，若是必要权限可以返回true，重复请求
        }
    }
}