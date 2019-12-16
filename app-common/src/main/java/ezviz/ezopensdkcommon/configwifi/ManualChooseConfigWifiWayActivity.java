package ezviz.ezopensdkcommon.configwifi;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import ezviz.ezopensdkcommon.R;
import ezviz.ezopensdkcommon.common.IntentConstants;
import ezviz.ezopensdkcommon.common.LogUtil;
import ezviz.ezopensdkcommon.common.RootActivity;
import ezviz.ezopensdkcommon.common.TitleBar;

public class ManualChooseConfigWifiWayActivity extends RootActivity {

    private final String TAG = ManualChooseConfigWifiWayActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_choose_config_wifi_way);

        initTitleBar();
        initUi();
    }

    protected void initUi() {
        showAvailableConfigWays();
    }

    private void showAvailableConfigWays() {
        boolean isUsingFullSdk = getIntent().getBooleanExtra(IntentConstants.USING_FULL_EZVIZ_SDK, false);
        View toApView = findViewById(R.id.btn_to_ap);
        if (toApView != null){
            if (isUsingFullSdk){
                toApView.setVisibility(getIntent().getBooleanExtra(IntentConstants.EXTRA_SUPPORT_AP, false) ? View.VISIBLE : View.GONE);
            }
            toApView.setOnClickListener(mChooseConfigWifiListener);
        }
        View toSmartConfigView = findViewById(R.id.btn_to_smart_config);
        if (toSmartConfigView != null){
            if (isUsingFullSdk){
                toSmartConfigView.setVisibility(getIntent().getBooleanExtra(IntentConstants.EXTRA_SUPPORT_SMART_CONFIG, false) ? View.VISIBLE : View.GONE);
            }
            toSmartConfigView.setOnClickListener(mChooseConfigWifiListener);
        }
        View toSoundWaveView = findViewById(R.id.btn_to_sound_wave);
        if (toSoundWaveView != null){
            if (isUsingFullSdk){
                toSoundWaveView.setVisibility(getIntent().getBooleanExtra(IntentConstants.EXTRA_SUPPORT_SOUND_WAVE, false) ? View.VISIBLE : View.GONE);
            }
            toSoundWaveView.setOnClickListener(mChooseConfigWifiListener);
        }
    }

    private View.OnClickListener mChooseConfigWifiListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent toConfigIntent = new Intent();
            toConfigIntent.putExtras(getIntent());
            String presenterType = null;
            ComponentName componentName = null;
            int id = v.getId();
            if (id == R.id.btn_to_ap){
                if (getIntent().getBooleanExtra(IntentConstants.USING_CONFIG_WIFI_SDK, false)){
                    presenterType = ConfigWifiTypeConstants.CONFIG_WIFI_SDK_AP;
                    componentName = new ComponentName(mContext, ManualInputDeviceHotspotInfoActivity.class);
                }else{
                    presenterType = ConfigWifiTypeConstants.FULL_SDK_AP;
                    componentName = new ComponentName(mContext, ConfigWifiExecutingActivity.class);
                }
            }else if(id == R.id.btn_to_smart_config){
                if (getIntent().getBooleanExtra(IntentConstants.USING_CONFIG_WIFI_SDK, false)){
                    presenterType = ConfigWifiTypeConstants.CONFIG_WIFI_SDK_SMART_CONFIG;
                }else{
                    presenterType = ConfigWifiTypeConstants.FULL_SDK_SMART_CONFIG;
                }
                componentName = new ComponentName(mContext, ConfigWifiExecutingActivity.class);
                toConfigIntent.putExtra(IntentConstants.EXTRA_SUPPORT_SOUND_WAVE, false);
            }else if(id == R.id.btn_to_sound_wave){
                if (getIntent().getBooleanExtra(IntentConstants.USING_CONFIG_WIFI_SDK, false)){
                    presenterType = ConfigWifiTypeConstants.CONFIG_WIFI_SDK_SOUND_WAVE;
                }else{
                    presenterType = ConfigWifiTypeConstants.FULL_SDK_SOUND_WAVE;
                }
                componentName = new ComponentName(mContext, ConfigWifiExecutingActivity.class);
                toConfigIntent.putExtra(IntentConstants.EXTRA_SUPPORT_SMART_CONFIG, false);
            }else{
                toConfigIntent = null;
            }
            if (toConfigIntent != null){
                toConfigIntent.putExtra(IntentConstants.SELECTED_PRESENTER_TYPE, presenterType);
                toConfigIntent.setComponent(componentName);
                startActivity(toConfigIntent);
            }else{
                LogUtil.e(TAG, "not find any suitable config wifi way!");
            }
        }

    };

    private void initTitleBar() {
        TitleBar titleBar = (TitleBar) findViewById(R.id.title_bar);
        if (titleBar != null){
            titleBar.setTitle(getString(R.string.choose_config_wifi_way));
            titleBar.addBackButton(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }
    }



}
