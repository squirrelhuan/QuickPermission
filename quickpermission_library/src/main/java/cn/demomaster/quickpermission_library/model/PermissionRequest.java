package cn.demomaster.quickpermission_library.model;

import java.util.List;

import cn.demomaster.quickpermission_library.PermissionHelper;

public class PermissionRequest {
    int baseCode =0;
    int index =0;
    List<PermissionModel> permissionModelList;
    PermissionHelper.PermissionListener listener;

    public int getBaseCode() {
        return baseCode;
    }

    public void setBaseCode(int baseCode) {
        this.baseCode = baseCode;
    }

    public int getRequestCode() {
        return baseCode+index;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public List<PermissionModel> getPermissionModelList() {
        return permissionModelList;
    }

    public void setPermissionModelList(List<PermissionModel> permissionModelList) {
        this.permissionModelList = permissionModelList;
    }

    public PermissionHelper.PermissionListener getListener() {
        return listener;
    }

    public void setListener(PermissionHelper.PermissionListener listener) {
        this.listener = listener;
    }
}
