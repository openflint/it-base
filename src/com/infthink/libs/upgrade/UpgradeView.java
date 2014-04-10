package com.infthink.libs.upgrade;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.infthink.libs.common.utils.IDebuggable;
import com.infthink.libs.common.utils.MeasureUtils;

public class UpgradeView implements IDebuggable {

    private static final String TAG = UpgradeView.class.getSimpleName();
    private static final String UPGRADE_TITLE = "软件升级";
    private static final String UPGRADE_CHECKING = "检查升级信息...";
    private static final String UPGRADE_CHECKING_ERROR = "获取升级信息失败,请检查网络链接";
    private static final String UPGRADE_CHECKING_LASTEST = "当前已经是最新版本";
    private static final String UPGRADE_CONFIRM = "发现新版本,是否升级?";
    private static final String UPGRADE_DOWNLOADING = "下载新版本...";
    private static final String UPGRADE_DOWNLOAD_FAIL = "新版本下载失败,请检查网络链接";
    private static final String UPGRADE_OK = "确定";
    private static final String UPGRADE_CANCEL = "取消";
    private Activity mContext;
    private UpgradeDialogFragment mUpgradeDialogFragment;
    private boolean mShowChecking;

    UpgradeView(Activity context) {
        mContext = context;
        mUpgradeDialogFragment = UpgradeDialogFragment.newInstance(context);
    }

    void showChecking(boolean showChecking) {
        mShowChecking = showChecking;
    }

    Activity getContxt() {
        return mContext;
    }

    void dismiss() {
        mUpgradeDialogFragment.dismiss();
    }

    private void show() {
        if (mContext.getFragmentManager().findFragmentByTag("UpgradeDialogFragment") == null) {
            mUpgradeDialogFragment.show(mContext.getFragmentManager(), "UpgradeDialogFragment");
        }
    }

    void showChecking(final Upgrade upgrade) {
        if (mShowChecking) {
            show();
        }
        mUpgradeDialogFragment.mStepDes.setVisibility(View.VISIBLE);
        mUpgradeDialogFragment.mStepDes.setText(UPGRADE_CHECKING);
        mUpgradeDialogFragment.mProgressBar.setVisibility(View.GONE);
        mUpgradeDialogFragment.mBtnCancel.setVisibility(View.VISIBLE);
        mUpgradeDialogFragment.mBtnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mUpgradeDialogFragment.dismiss();
                upgrade.cancel();
            }
        });
        mUpgradeDialogFragment.mBtnOK.setVisibility(View.GONE);
    }

    void showCheckingError(final Upgrade upgrade) {
        if (mShowChecking) {
            show();
        }
        mUpgradeDialogFragment.mStepDes.setVisibility(View.VISIBLE);
        mUpgradeDialogFragment.mStepDes.setText(UPGRADE_CHECKING_ERROR);
        mUpgradeDialogFragment.mProgressBar.setVisibility(View.GONE);
        mUpgradeDialogFragment.mBtnCancel.setVisibility(View.GONE);
        mUpgradeDialogFragment.mBtnOK.setVisibility(View.VISIBLE);
        mUpgradeDialogFragment.mBtnOK.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mUpgradeDialogFragment.dismiss();
                upgrade.cancel();
            }
        });
    }

    void showCheckingLatest(final Upgrade upgrade) {
        if (mShowChecking) {
            show();
        }
        mUpgradeDialogFragment.mStepDes.setVisibility(View.VISIBLE);
        mUpgradeDialogFragment.mStepDes.setText(UPGRADE_CHECKING_LASTEST);
        mUpgradeDialogFragment.mProgressBar.setVisibility(View.GONE);
        mUpgradeDialogFragment.mBtnCancel.setVisibility(View.GONE);
        mUpgradeDialogFragment.mBtnOK.setVisibility(View.VISIBLE);
        mUpgradeDialogFragment.mBtnOK.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mUpgradeDialogFragment.dismiss();
                upgrade.cancel();
            }
        });
    }

    void showConfirm(final Upgrade upgrade) {
        show();
        mUpgradeDialogFragment.mStepDes.setVisibility(View.VISIBLE);
        mUpgradeDialogFragment.mStepDes.setText(UPGRADE_CONFIRM);
        mUpgradeDialogFragment.mProgressBar.setVisibility(View.GONE);
        mUpgradeDialogFragment.mBtnCancel.setVisibility(View.VISIBLE);
        mUpgradeDialogFragment.mBtnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mUpgradeDialogFragment.dismiss();
                upgrade.cancel();
            }
        });
        mUpgradeDialogFragment.mBtnOK.setVisibility(View.VISIBLE);
        mUpgradeDialogFragment.mBtnOK.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                upgrade.confirm();
            }
        });
    }

    void showDownloading(final Upgrade upgrade, int progressPercent) {
        show();
        mUpgradeDialogFragment.mStepDes.setVisibility(View.VISIBLE);
        mUpgradeDialogFragment.mStepDes.setText(UPGRADE_DOWNLOADING);
        mUpgradeDialogFragment.mProgressBar.setVisibility(View.VISIBLE);
        if (progressPercent < 0) {
            mUpgradeDialogFragment.mProgressBar.setIndeterminate(true);
        } else {
            mUpgradeDialogFragment.mProgressBar.setIndeterminate(false);
            mUpgradeDialogFragment.mProgressBar.setProgress(progressPercent);
        }
        mUpgradeDialogFragment.mBtnCancel.setVisibility(View.VISIBLE);
        mUpgradeDialogFragment.mBtnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mUpgradeDialogFragment.dismiss();
                upgrade.cancel();
            }
        });
        mUpgradeDialogFragment.mBtnOK.setVisibility(View.GONE);
    }

    void showDownloadingFail(final Upgrade upgrade) {
        show();
        mUpgradeDialogFragment.mStepDes.setVisibility(View.VISIBLE);
        mUpgradeDialogFragment.mStepDes.setText(UPGRADE_DOWNLOAD_FAIL);
        mUpgradeDialogFragment.mProgressBar.setVisibility(View.GONE);
        mUpgradeDialogFragment.mBtnCancel.setVisibility(View.GONE);
        mUpgradeDialogFragment.mBtnOK.setVisibility(View.VISIBLE);
        mUpgradeDialogFragment.mBtnOK.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mUpgradeDialogFragment.dismiss();
                upgrade.cancel();
            }
        });
    }

    public static class UpgradeDialogFragment extends DialogFragment {

        private View mRoot;
        private TextView mStepDes;
        private ProgressBar mProgressBar;
        private Button mBtnCancel;
        private Button mBtnOK;

        public static UpgradeDialogFragment newInstance(Activity context) {
            Resources resources = context.getResources();
            ApplicationInfo appInfo = context.getApplicationInfo();
            Drawable icon = appInfo.loadIcon(context.getPackageManager());
            String lable = UPGRADE_TITLE;
            if (appInfo.labelRes != 0) {
                lable = resources.getString(appInfo.labelRes);
            }

            UpgradeDialogFragment instance = new UpgradeDialogFragment();
            LinearLayout root = new LinearLayout(context);
            root.setOrientation(LinearLayout.VERTICAL);
            int padding10 = MeasureUtils.dp2px(context, 10);
            root.setPadding(padding10, padding10, padding10, padding10);

            TextView title = new TextView(context);
            title.setCompoundDrawables(icon, null, null, null);
            title.setText(lable);
            title.setGravity(Gravity.CENTER_VERTICAL);
            title.setTextAppearance(context, android.R.style.TextAppearance_DeviceDefault_Large);
            title.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            root.addView(title);

            TextView stepDes = new TextView(context);
            stepDes.setText(UPGRADE_CHECKING);
            stepDes.setGravity(Gravity.CENTER_VERTICAL);
            stepDes.setTextAppearance(context, android.R.style.TextAppearance_DeviceDefault_Widget_TextView_PopupMenu);
            stepDes.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            root.addView(stepDes);

            ProgressBar progressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
            progressBar.setMinimumHeight(padding10 * 3);
            progressBar.setMax(100);
            progressBar.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            root.addView(progressBar);

            LinearLayout btns = new LinearLayout(context);
            btns.setOrientation(LinearLayout.HORIZONTAL);
            btns.setGravity(Gravity.CENTER_VERTICAL);
            btns.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            root.addView(btns);

            Button btnCancel = new Button(context);
            btnCancel.setText(UPGRADE_CANCEL);
            btnCancel.setTextAppearance(context, android.R.style.TextAppearance_DeviceDefault_Widget_TextView_PopupMenu);
            btnCancel.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            btns.addView(btnCancel);

            Button btnOK = new Button(context);
            btnOK.setText(UPGRADE_OK);
            btnOK.setTextAppearance(context, android.R.style.TextAppearance_DeviceDefault_Widget_TextView_PopupMenu);
            btnOK.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            btns.addView(btnOK);

            instance.mStepDes = stepDes;
            instance.mProgressBar = progressBar;
            instance.mBtnCancel = btnCancel;
            instance.mBtnOK = btnOK;
            instance.mRoot = root;

            return instance;
        }

        @Override
        public Dialog onCreateDialog(Bundle saveInstanceState) {
            if (DEBUG)
                Log.d(TAG, "UpgradeDialogFragment#onCreateDialog");
            if (mRoot.getParent() != null) {
                ((ViewGroup)mRoot.getParent()).removeView(mRoot);
            }
            AlertDialog dialog = new AlertDialog.Builder(getActivity()).setView(mRoot).create();
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setOnKeyListener(new OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    Log.d(TAG, String.format("UpgradeDialogFragment#onCreateDialog#AlertDialog#onKey keyCode:%s", new Object[] { keyCode }));
                    return true;
                }
            });
            return dialog;
        }

    }

}
