package cn.huse.prepare.util;

import android.content.Context;
import android.content.Entity;
import android.os.Handler;
import android.view.View;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import org.xutils.DbManager;
import org.xutils.ex.DbException;
import org.xutils.x;

public class Utils {
    public static void showSuceess(Context context, String string) {
        final QMUITipDialog success = new QMUITipDialog.Builder(context)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_SUCCESS)
                .setTipWord(string)
                .create();
        success.show();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                success.dismiss();
            }
        }, 500);
    }

    public static void saveEntity(final DbManager.DaoConfig daoConfig, final double values, final String time) {
        x.task().run(new Runnable() {
            @Override
            public void run() {
                final DbManager dbManager = x.getDb(daoConfig);
                POJO pojo = new POJO();
                pojo.setValue(values);
                pojo.setTime(time);
                try {
                    dbManager.save(pojo);
                } catch (DbException e) {
                    e.printStackTrace();
                }
            }
        });

    }
}
