package cn.huse.prepare.util;

import android.content.Context;
import android.os.Handler;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;

public class Utils {
    public static void showSuceess(Context context,String string){
        final QMUITipDialog success = new QMUITipDialog.Builder(context)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_SUCCESS)
                .setTipWord(string)
                .create();
        success.show();
        Handler handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                success.dismiss();
            }
        }, 500);
    }
}
