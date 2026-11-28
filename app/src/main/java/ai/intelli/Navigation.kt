package ai.intelli

import androidx.compose.runtime.Composable
import ai.intelli.ui.chat.ChatScreen
import ai.intelli.ui.chat.ChatViewModel

@Composable
fun MainNavigation(viewModel: ChatViewModel? = null) {
    if (viewModel != null) {
        ChatScreen(viewModel = viewModel)
    } else {
        ChatScreen()
    }
}
