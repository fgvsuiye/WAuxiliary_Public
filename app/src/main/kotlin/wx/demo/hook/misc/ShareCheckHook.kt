package wx.demo.hook.misc

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
object ShareCheckHook : SwitchHook("ShareSignatureHook"), IDexFind {
    private object MethodCheckSign : DescriptorData("ShareSignatureHook.MethodCheckSign")

    override val location = "杂项"
    override val funcName = "分享签名校验"
    override val funcDesc = "绕过第三方应用分享到微信的签名校验"

    override fun initOnce() {
        MethodCheckSign.toDexMethod {
            hook {
                beforeIfEnabled {
                    resultTrue()
                }
            }
        }
    }

    override fun dexFind(dexKit: DexKitBridge) {
        MethodCheckSign.findDexClassMethod(dexKit) {
            onMethod {
                matcher {
                    usingEqStrings("checkAppSignature get local signature failed")
                }
            }
        }
    }
}
