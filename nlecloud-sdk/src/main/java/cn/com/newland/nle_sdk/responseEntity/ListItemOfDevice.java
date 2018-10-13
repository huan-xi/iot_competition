package cn.com.newland.nle_sdk.responseEntity;

import java.util.List;

public class ListItemOfDevice {
    private int DeviceID;
    private String Name;
    private List<ListItemOfNewestData> Datas;

    public int getDeviceID() {
        return DeviceID;
    }

    public void setDeviceID(int deviceID) {
        DeviceID = deviceID;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public List<ListItemOfNewestData> getDatas() {
        return Datas;
    }

    public void setDatas(List<ListItemOfNewestData> datas) {
        Datas = datas;
    }
}
