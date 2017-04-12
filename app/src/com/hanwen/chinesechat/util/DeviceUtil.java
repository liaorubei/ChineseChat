package com.hanwen.chinesechat.util;

import android.os.Build;

/**
 * 就打印下系统平台的信息就可以了，最好是用GSON解析
 */
public class DeviceUtil {
    public String BOARD = Build.BOARD;
    public String BOOTLOADER = Build.BOOTLOADER;
    public String BRAND = Build.BRAND;
    public String CPU_ABI = Build.CPU_ABI;
    public String CPU_ABI2 = Build.CPU_ABI2;
    public String DEVICE = Build.DEVICE;
    public String DISPLAY = Build.DISPLAY;
    public String FINGERPRINT = Build.FINGERPRINT;
    public String HARDWARE = Build.HARDWARE;
    public String HOST = Build.HOST;
    public String ID = Build.ID;
    public String MANUFACTURER = Build.MANUFACTURER;
    public String MODEL = Build.MODEL;
    public String PRODUCT = Build.PRODUCT;
    public String RADIO = Build.RADIO;
    public String SERIAL = Build.SERIAL;
    public String TAGS = Build.TAGS;
    public long TIME = Build.TIME;
    public String TYPE = Build.TYPE;
    public String USER = Build.USER;
    public String CODENAME = Build.VERSION.CODENAME;
    public String INCREMENTAL = Build.VERSION.INCREMENTAL;
    public String RELEASE = Build.VERSION.RELEASE;
    public String SDK = Build.VERSION.SDK;
    public int SDK_INT = Build.VERSION.SDK_INT;
}
