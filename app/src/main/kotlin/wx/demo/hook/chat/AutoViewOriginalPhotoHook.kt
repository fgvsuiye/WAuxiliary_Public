package wx.demo.hook.chat

import android.widget.Button
import androidx.core.view.isVisible
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.type.android.ButtonClass
import me.hd.wauxv.data.config.DescriptorData
import me.hd.wauxv.hook.anno.HookAnno
import me.hd.wauxv.hook.anno.ViewAnno
import me.hd.wauxv.hook.base.SwitchHook
import me.hd.wauxv.hook.core.api.IDexFind
import me.hd.wauxv.hook.factory.findDexClassMethod
import me.hd.wauxv.hook.factory.toDexMethod
import org.lsposed.lsparanoid.Obfuscate
import org.luckypray.dexkit.DexKitBridge

@Obfuscate
@HookAnno
@ViewAnno
object AutoViewOriginalPhotoHook : SwitchHook("AutoViewOriginalPhotoHook"), IDexFind {
    private object MethodImgBtnVisibility : DescriptorData("AutoViewOriginalPhotoHook.MethodImgBtnVisibility")

    override val location = "聊天"
    override val funcName = "自动查看原图"
    override val funcDesc = "在打开图片和视频时自动点击查看原图"

    override fun initOnce() {
        MethodImgBtnVisibility.toDexMethod {
            hook {
                afterIfEnabled {
                    instanceClass!!.field {
                        type = ButtonClass
                    }.all(instance).forEach {
                        it.cast<Button?>()?.let { imgBtn ->
                            if (imgBtn.isVisible) {
                                val keywords = listOf("查看原图", "Full Image")
                                if (keywords.any { text -> imgBtn.text.contains(text, true) }) {
                                    imgBtn.performClick()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun dexFind(dexKit: DexKitBridge) {
        MethodImgBtnVisibility.findDexClassMethod(dexKit) {
            onMethod {
                matcher {
                    usingEqStrings("MicroMsg.ImageGalleryUI", "parse cdnInfo failed. [%s]")
                }
            }
        }
    }
}

