package ai.tambo.foundation.ngoma

import ai.tambo.foundation.ngoma.bridge.AccountBridge
import ai.tambo.foundation.ngoma.bridge.AccountBridgeMock
import ai.tambo.foundation.ngoma.bridge.MessagingBridge
import ai.tambo.foundation.ngoma.bridge.MessagingBridgeMock
import ai.tambo.foundation.ngoma.ui.NGomaApp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

// Point de bascule mock -> TModeler : remplacer ces deux instances par des implémentations TModeler.
object Bridges {
    val account: AccountBridge = AccountBridgeMock()
    val messaging: MessagingBridge = MessagingBridgeMock()
}

fun main() {
    NGomaTm.start()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "NGoma",
            state = rememberWindowState(size = DpSize(1100.dp, 720.dp)),
        ) {
            NGomaApp(Bridges.account, Bridges.messaging)
        }
    }
}
