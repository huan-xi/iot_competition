package cn.huse.prepare;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import cn.com.newland.nle_sdk.requestEntity.SignIn;
import cn.com.newland.nle_sdk.responseEntity.User;
import cn.com.newland.nle_sdk.responseEntity.base.BaseResponseEntity;
import cn.com.newland.nle_sdk.util.NetWorkBusiness;
import cn.huse.myapplication.R;
import cn.huse.prepare.base.BaseActivity;
import cn.huse.prepare.util.Constans;
import cn.huse.prepare.util.DataCache;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;
import org.xutils.view.annotation.ViewInject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends BaseActivity {
    @ViewInject(R.id.btn_login)
    private QMUIRoundButton bt_login;
    @ViewInject(R.id.username)
    private EditText et_username;
    @ViewInject(R.id.passwd)
    private EditText et_passwd;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.login_layout);
        super.onCreate(savedInstanceState);
        init();
    }
    private void login(String username,String passwd){
        final QMUITipDialog tipDialog;
        final NetWorkBusiness netWorkBusiness = new NetWorkBusiness( "", Constans.getApiUrl());
        tipDialog = new QMUITipDialog.Builder(this)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .setTipWord("正在登入")
                .create();
        tipDialog.show();
        final Context context=this;
        netWorkBusiness.signIn(new SignIn(username, passwd), new Callback<BaseResponseEntity<User>>() {
            @Override
            public void onResponse(Call<BaseResponseEntity<User>> call, Response<BaseResponseEntity<User>> response) {
                BaseResponseEntity<User> baseResponseEntity = response.body();
                if (baseResponseEntity != null) {
                    if (baseResponseEntity.getStatus() == 0) {
//                        DataCache.updateUserName(getApplicationContext(), userName);
//                        DataCache.updatePwd(getApplicationContext(), pwd);
                        String accessToken = baseResponseEntity.getResultObj().getAccessToken();
                        DataCache.setToken(accessToken);
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("userBaseResponseEntity", baseResponseEntity);
                        intent.putExtras(bundle);
                        startActivity(intent);
                        tipDialog.dismiss();
                       QMUITipDialog success = new QMUITipDialog.Builder(context)
                                .setIconType(QMUITipDialog.Builder.ICON_TYPE_SUCCESS)
                                .setTipWord("登入成功")
                                .create();
                       success.show();
                        finish();
                    }else{
                        Toast.makeText(LoginActivity.this, baseResponseEntity.getMsg(), Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(LoginActivity.this, "请求地址出错", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponseEntity<User>> call, Throwable t) {
                Toast.makeText(getApplicationContext(),t.getMessage() ,Toast.LENGTH_SHORT ).show();
            }
        });
    }
    private void init() {
        bt_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login(et_username.getText().toString(), et_passwd.getText().toString());
            }
        });
    }
}
