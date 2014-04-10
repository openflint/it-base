package com.infthink.libs.common.os;

import java.io.File;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class ApkInstall {

    public static void install(Context context, File apk) {
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(apk), "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

}
