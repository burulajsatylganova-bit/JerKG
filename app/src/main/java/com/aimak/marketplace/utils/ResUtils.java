package com.aimak.marketplace.utils;

import com.aimak.marketplace.AimakApp;
import com.aimak.marketplace.R;

/**
 * Утилита для получения строк из репозиториев и ViewModels
 * где нет прямого доступа к Context.
 */
public class ResUtils {
    public static String str(int resId) {
        return AimakApp.getInstance().getString(resId);
    }
}
