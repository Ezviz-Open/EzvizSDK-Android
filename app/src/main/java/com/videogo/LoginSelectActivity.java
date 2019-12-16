/* 
 * @ProjectName ezviz-openapi-android-demo
 * @Copyright null
 * 
 * @FileName LoginSelectActivity.java
 * @Description 这里对文件进行描述
 * 
 * @author chenxingyf1
 * @data 2014-12-6
 * 
 * @note 这里写本文件的详细功能描述和注释
 * @note 历史记录
 * 
 * @warning 这里写本文件的相关警告
 */
package com.videogo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;
import com.videogo.ui.LanDevice.LanDeviceActivity;
import com.videogo.ui.cameralist.EZCameraListActivity;
import com.videogo.ui.util.ActivityUtils;
import ezviz.ezopensdkcommon.R;

public class LoginSelectActivity extends Activity implements OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);
        
        initData();
        initView();
    }
    
    private void initData() {

    }
    
    private void initView() {
        
    }

    /* (non-Javadoc)
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */

    int position = 1;
    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch(v.getId()) {
            case R.id.web_login_btn:
                if (TextUtils.isEmpty(EzvizApplication.mAppKey)){
                    Toast.makeText(this,"Appkey为空",Toast.LENGTH_LONG).show();
                    return;
                }
                //if (position++%2==0){
                //    EZOpenSDK.getInstance().setAccessToken("at.b7auodp5b9w1p1xxc5a1sdswbto3dt11-8z3rac25db-0sk7hca-wvvz6dals");
                //}else{
                //    EZOpenSDK.getInstance().setAccessToken("at.0r3whcehbliw25ln31jyudve6xlmkze0-4jlc52necs-09ctxls-uggtd6wqd");
                //
                //}
                //EZOpenSDK.getInstance().setAccessToken("at.bae1jthhbtalcsbi6i0g7t2z5zuzb8nn-3r8gjto73b-0fhxx20-dnxmptju1");

                //碧桂园
               //EZOpenSDK.getInstance().setAccessToken("at.8ew5dyfs80rqvr6s2e0aaewxaf726dh1-9nx0ul6fwj-024fc8e-ddlws86uu");

                //海外预览慢
                //EZGlobalSDK.getInstance().setAccessToken("at.4yx0ur2d9ccj6j0c9knk31e4ce1gq7zi-1am89ugnjw-1dsq6xc-tkn9gfap4");
                //Intent toIntent = new Intent(this, SelectDeviceList.class);
                //toIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ///*******
                // * 获取登录成功之后的EZAccessToken对象
                // * Gets the EZAccessToken object after the login is successful
                // * *****/
                //EZAccessToken token = com.videogo.EzvizApplication.getOpenSDK().getEZAccessToken();
                //startActivity(toIntent);
                if (EzvizApplication.getOpenSDK().isLogin()) {
                    Intent toIntent = new Intent(this, OptionActivity.class);
                    toIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    this.startActivity(toIntent);

                }else{
                    ActivityUtils.goToLoginAgain(LoginSelectActivity.this);
                }
                return;
            case R.id.id_ll_join_qq_group:
                //String name = "p57CNgQ_uf2gZMY0eYTvgQ_S_ZDzZz44";
                //joinQQGroup(name);
                //EZAuthAPI.sendOpenPage(this, EZAuthAPI.EZAuthSDKOpenPage.OpenPage_DeviceList, EZAuthAPI.EZAuthPlatform.EZVIZ);
                break;
            case R.id.btn_landevice:
                intent = new Intent(LoginSelectActivity.this, LanDeviceActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }
    
    private void openPlatformLoginDialog() {
        final EditText editText = new EditText(this);
        new  AlertDialog.Builder(this)  
        .setTitle(R.string.please_input_platform_accesstoken_txt)   
        .setIcon(android.R.drawable.ic_dialog_info)   
        .setView(editText)  
        .setPositiveButton(R.string.certain, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                //String getAccessTokenSign = SignUtil.getGetAccessTokenSign();
                EzvizApplication.getOpenSDK().setAccessToken(editText.getText().toString());
                Intent toIntent = new Intent(LoginSelectActivity.this, EZCameraListActivity.class);
                toIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                LoginSelectActivity.this.startActivity(toIntent);
            }
            
        })   
        .setNegativeButton(R.string.cancel, null)
        .show();  
    }

    private boolean joinQQGroup(String key) {
        Intent intent = new Intent();
        intent.setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D" + key));
        // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            startActivity(intent);
            return true;
        } catch (Exception e) {
            // 未安装手Q或安装的版本不支持
            return false;
        }
    }
}
