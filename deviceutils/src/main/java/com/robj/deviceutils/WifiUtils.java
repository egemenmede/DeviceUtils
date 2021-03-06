package com.robj.deviceutils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;

/**
 * Created by Rob J on 16/06/17.
 */

public class WifiUtils {

    public static Observable<List<Device>> getSavedWifiNetworks(Context context) {
        return Observable.create(subscriber -> {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            List<WifiConfiguration> wifiNetworks = wifiManager.getConfiguredNetworks();
            List<Device> devices = new ArrayList();
            if(wifiNetworks != null && wifiNetworks.size() > 0)
                for(WifiConfiguration wifiConfiguration : wifiNetworks) {
                    Device device = new Device(wifiConfiguration);
                    devices.add(device);
                }
            if(subscriber != null && !subscriber.isDisposed()) {
                subscriber.onNext(devices);
                subscriber.onComplete();
            }
        });
    }

    @SuppressLint("MissingPermission")
    public static String getWifiName(Context context, String typeIdentifier) {
        WifiManager wifiMgr = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        List<WifiConfiguration> wifiConfigurations = wifiMgr.getConfiguredNetworks();
        if(wifiConfigurations != null) {
            for (WifiConfiguration wifiConfiguration : wifiConfigurations)
                if (String.valueOf(wifiConfiguration.networkId).equals(typeIdentifier))
                    return wifiConfiguration.SSID;
        }
        return context.getString(R.string.device_name_unknown);
    }

    public static String getWifiId(WifiInfo wifiInfo) {
        return String.valueOf(wifiInfo.getNetworkId());
    }

    private static String getWifiId(WifiConfiguration wifiConfiguration) {
        return String.valueOf(wifiConfiguration.networkId);
    }

    @SuppressLint("MissingPermission")
    public static WifiInfo getConnectedWifi(Context context) {
        WifiManager manager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (manager.isWifiEnabled()) {
            WifiInfo wifiInfo = manager.getConnectionInfo();
            if (wifiInfo != null) {
                NetworkInfo.DetailedState state = WifiInfo.getDetailedStateOf(wifiInfo.getSupplicantState());
                if (state == NetworkInfo.DetailedState.CONNECTED || state == NetworkInfo.DetailedState.OBTAINING_IPADDR) {
                    return wifiInfo;
                }
            }
        }
        return null;
    }

    public static Observable<Optional<WifiInfo>> getConnectedWifiObservable(Context context) {
        return Observable.create(subscriber -> {
            try {
                WifiInfo wifiInfo = getConnectedWifi(context);
                if(subscriber != null && !subscriber.isDisposed()) {
                    if (wifiInfo != null)
                        subscriber.onNext(new Optional(wifiInfo));
                    else
                        subscriber.onNext(new Optional(null));
                    subscriber.onComplete();
                }
            } catch (Exception e) {
                if(!subscriber.isDisposed())
                    subscriber.onError(e);
            }
        });
    }

    public static class Device extends BaseDevice {

        protected Device(WifiConfiguration wifiConfiguration) {
            super(String.valueOf(wifiConfiguration.networkId), wifiConfiguration.SSID);
        }
    }

}
