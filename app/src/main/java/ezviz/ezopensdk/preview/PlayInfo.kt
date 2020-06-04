package ezviz.ezopensdk.preview

import android.os.Handler
import com.videogo.openapi.EZPlayer
import com.videogo.openapi.bean.EZCameraInfo
import com.videogo.openapi.bean.EZDeviceInfo

/**
 * 此处简要说明此文件用途
 * Created on 2020/4/24
 */
data class PlayInfo(
        val key: String,
        var deviceInfo: EZDeviceInfo,
        var cameraInfo: EZCameraInfo,
        var adapter: MultiScreenPreviewAdapter,
        var holder: MultiScreenPreviewAdapter.MultiScreenViewHolder
        ){
        var player: EZPlayer? = null
        var handler: Handler? = null
        var lastError: Int = 0
        var verifyCode: String? = null
        var playStatus: MultiScreenPreviewAdapter.PlayStatusEnum? = null
}