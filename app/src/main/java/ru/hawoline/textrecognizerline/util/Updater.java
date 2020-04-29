package ru.hawoline.textrecognizerline.util;

import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.Task;

public final class Updater {

    private static Updater instance;

    private AppUpdateManager updateManager;

    public static final int UPDATE_REQUEST = 1003;
    private static final int DAYS_FOR_FLEXIBLE_UPDATE = 3;

    private Updater() {

    }

    public static Updater getInstance() {
        if (instance == null)
            instance = new Updater();

        return instance;
    }

    public void checkForUpdates(Activity activity) {
        Task<AppUpdateInfo> appUpdateInfoTask = updateManager.getAppUpdateInfo();

        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                startUpdate(appUpdateInfo, activity);
            }
        });
    }

    private void startUpdate(AppUpdateInfo updateInfo, Activity activity) {
        try {
            updateManager.startUpdateFlowForResult(
                    updateInfo,
                    AppUpdateType.IMMEDIATE,
                    activity,
                    UPDATE_REQUEST);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }

    public void createUpdateManager(Context context) {
        updateManager = AppUpdateManagerFactory.create(context);
    }
}
