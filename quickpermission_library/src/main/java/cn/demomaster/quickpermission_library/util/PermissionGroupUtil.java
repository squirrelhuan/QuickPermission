package cn.demomaster.quickpermission_library.util;

import android.Manifest;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import cn.demomaster.quickpermission_library.model.PermissionModel;

public class PermissionGroupUtil {


    public static List<String> getPermissionGroup(String[] permissions) {
        return getPermissionGroup(getPermissionModels(permissions));
    }

    public static List<String> getPermissionGroup(List<PermissionModel> permissionModels) {
        List<String> stringList = new ArrayList<>();
        for (PermissionModel model : permissionModels) {
            stringList.add(model.getGroupName());
        }
        return stringList;
    }

    public static List<PermissionModel> getPermissionModels(String[] permissions) {
        if(permissions==null||permissions.length==0){
            return null;
        }
        List<PermissionModel> permissionModelList = new ArrayList<>();
        for (String permission :permissions){
            permissionModelList.add(getPermissionModel(permission));
        }
        return permissionModelList;
    }
    public static PermissionModel getPermissionModel(String permission) {
        PermissionModel model = null;
        String groupName = getPermissionGroup(permission);
        if (!TextUtils.isEmpty(groupName)) {
            model = new PermissionModel();
            model.setName(permission);
            model.setGroupName(groupName);
            model.setDesc(getPermissionDescription(permission));
        }
        return model;
    }

    public static String getPermissionDescription(String permission) {
        switch (permission) {
            case Manifest.permission.READ_CALENDAR:
                return  "读取日历";
            case Manifest.permission.WRITE_CALENDAR:
                return "修改日历";
            case Manifest.permission.READ_EXTERNAL_STORAGE:
                return "读取存储文件";
            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                return "写入存储文件";
            case Manifest.permission.CAMERA:
                return "访问摄像头进行拍照";
            case Manifest.permission.READ_CONTACTS:
                return "访问联系人通讯录信息";
            case Manifest.permission.WRITE_CONTACTS:
                return "写入联系人";
            case Manifest.permission.GET_ACCOUNTS:
                return "获取设备帐户";
            case Manifest.permission.ACCESS_COARSE_LOCATION:
                return "允许程序通过GPS芯片接收卫星的定位信息";
            case Manifest.permission.ACCESS_FINE_LOCATION:
                return "允许程序通过WIFI或移动基站的方式获取用户错略的经纬度信息";
            case Manifest.permission.RECORD_AUDIO:
                return "允许程序录制声音通过手机或耳机的麦克";
            case Manifest.permission.READ_PHONE_STATE:
                return "允许程序访问电话状态";
            case Manifest.permission.CALL_PHONE:
               return  "允许程序从非系统拨号器里拨打电话";
            case Manifest.permission.READ_CALL_LOG:
                return "读取通话记录";
            case Manifest.permission.ADD_VOICEMAIL:
                return "添加系统中的语音邮件";
            case Manifest.permission.WRITE_CALL_LOG:
                return "允许程序写入用户的联系人数据";
            case Manifest.permission.USE_SIP:
                return "允许程序使用SIP视频服务";
            case Manifest.permission.PROCESS_OUTGOING_CALLS:
                return "允许程序监视/修改/放弃播出电话";
            case Manifest.permission.BODY_SENSORS:
                return "访问与您生命体征相关的传感器数据";
            case Manifest.permission.SEND_SMS:
                return "允许程序发短信";
            case Manifest.permission.RECEIVE_SMS:
                return "允许程序接受短信";
            case Manifest.permission.READ_SMS:
                return "允许程序读取短信内容";
            case Manifest.permission.RECEIVE_WAP_PUSH:
                return "允许程序接收WAP PUSH信息";
            case Manifest.permission.RECEIVE_MMS:
                return "允许程序接收彩信";
            case Manifest.permission.SYSTEM_ALERT_WINDOW:
                return "允许弹出悬浮窗";
            case Manifest.permission.INSTALL_PACKAGES:
                return "允许安装应用";
            case Manifest.permission.PACKAGE_USAGE_STATS:
                return "数据包状态查看";
            case Manifest.permission.WRITE_SETTINGS:
                return "修改系统设置";
            case Manifest.permission.BIND_ACCESSIBILITY_SERVICE:
                return "无障碍服务";
        }
        return null;
    }
    public static String getPermissionGroup(String permission) {
        switch (permission) {
            case Manifest.permission.READ_CALENDAR:
            case Manifest.permission.WRITE_CALENDAR:
            case Manifest.permission.READ_EXTERNAL_STORAGE:
            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                return Manifest.permission_group.CALENDAR;
            case Manifest.permission.CAMERA:
                return Manifest.permission_group.CAMERA;
            case Manifest.permission.READ_CONTACTS:
            case Manifest.permission.WRITE_CONTACTS:
            case Manifest.permission.GET_ACCOUNTS:
                return Manifest.permission_group.CONTACTS;

            case Manifest.permission.ACCESS_COARSE_LOCATION:
            case Manifest.permission.ACCESS_FINE_LOCATION:
                return Manifest.permission_group.LOCATION;
            case Manifest.permission.RECORD_AUDIO:
                return Manifest.permission_group.MICROPHONE;
            case Manifest.permission.READ_PHONE_STATE:
            case Manifest.permission.CALL_PHONE:
            case Manifest.permission.READ_CALL_LOG:
            case Manifest.permission.ADD_VOICEMAIL:
            case Manifest.permission.WRITE_CALL_LOG:
            case Manifest.permission.USE_SIP:
            case Manifest.permission.PROCESS_OUTGOING_CALLS:
                return Manifest.permission_group.PHONE;
            case Manifest.permission.BODY_SENSORS:
                return Manifest.permission_group.SENSORS;
            case Manifest.permission.SEND_SMS:
            case Manifest.permission.RECEIVE_SMS:
            case Manifest.permission.READ_SMS:
            case Manifest.permission.RECEIVE_WAP_PUSH:
            case Manifest.permission.RECEIVE_MMS:
            //case Manifest.permission.READ_CALL_BROADCASTS:
                return Manifest.permission_group.SMS;
            default:
                return permission;
        }
    }
}
