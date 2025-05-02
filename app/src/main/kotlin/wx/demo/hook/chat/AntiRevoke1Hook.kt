package wx.demo.hook.chat

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
object AntiRevoke1Hook : SwitchHook("AntiRevoke1Hook"), IDexFind {
    private object MethodDoRevokeMsg : DescriptorData("AntiRevoke1Hook.MethodDoRevokeMsg")

    override val location = "聊天"
    override val funcName = "阻止消息撤回1"
    override val funcDesc = "无撤回提示, 流畅款, 3选1"

    override fun initOnce() {
        MethodDoRevokeMsg.toDexMethod {
            hook {
                beforeIfEnabled {
                    resultNull()
                }
            }
        }
    }

    override fun dexFind(dexKit: DexKitBridge) {
        MethodDoRevokeMsg.findDexClassMethod(dexKit) {
            onMethod {
                matcher {
                    usingEqStrings("doRevokeMsg xmlSrvMsgId=%d talker=%s isGet=%s")
                }
            }
        }
    }
}
