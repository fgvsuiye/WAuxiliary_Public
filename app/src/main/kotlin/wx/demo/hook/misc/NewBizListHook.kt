package wx.demo.hook.misc

import android.content.ComponentName
import android.content.Intent
import com.highcapable.yukihookapi.hook.param.HookParam
import me.hd.wauxv.data.factory.HostInfo
import me.hd.wauxv.hook.anno.HookAnno
import me.hd.wauxv.hook.anno.ViewAnno
import me.hd.wauxv.hook.api.IStartActivity
import me.hd.wauxv.hook.base.SwitchHook
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
@HookAnno
@ViewAnno
object NewBizListHook : SwitchHook("NewBizListHook"), IStartActivity {
    override val location = "杂项"
    override val funcName = "订阅消息列表"
    override val funcDesc = "订阅号消息从瀑布流模式改为列表模式"

    override fun initOnce() {
    }

    override fun onStartActivityIntent(param: HookParam, intent: Intent) {
        if (!isEnabled) return
        val bizFlutterView = "com.tencent.mm.plugin.brandservice.ui.flutter.BizFlutterTLFlutterViewActivity"
        val bizTimeLine = "com.tencent.mm.plugin.brandservice.ui.timeline.BizTimeLineUI"
        when (intent.component?.className) {
            bizFlutterView, bizTimeLine -> {
                intent.component = ComponentName(HostInfo.appPackageName, "com.tencent.mm.ui.conversation.NewBizConversationUI")
            }
        }
    }
}
