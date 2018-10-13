package cn.huse.prepare;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import cn.huse.myapplication.R;
import cn.huse.prepare.base.BaseActivity;
import me.wangyuwei.particleview.ParticleView;
import org.xutils.view.annotation.ViewInject;

public class StartActivity extends BaseActivity {
    @ViewInject(R.id.pv_1)
    private ParticleView mPv1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_start);
        super.onCreate(savedInstanceState);
        mPv1.setOnParticleAnimListener(new ParticleView.ParticleAnimListener() {
            @Override
            public void onAnimationEnd() {
                Intent intent=new Intent(StartActivity.this,LoginActivity.class);
                startActivity(intent);
            }
        });
        mPv1.postDelayed(new Runnable() {
            @Override
            public void run() {
                mPv1.startAnim();
            }
        }, 200) ;
    }

}
