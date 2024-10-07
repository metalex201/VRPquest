package com.lex.vrpquest.Pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.lex.vrpquest.CustomColorScheme
import com.lex.vrpquest.Managers.Game
import com.lex.vrpquest.Managers.MetadataInitialize
import com.lex.vrpquest.Managers.MetadataInitializeFTP
import com.lex.vrpquest.Utils.SettingGetBoolean
import com.lex.vrpquest.Utils.SettingGetSting
import com.lex.vrpquest.Utils.SettingStoreBoolean
import com.lex.vrpquest.Managers.testfpt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun LoadPage(Page: () -> Unit, metadata: (ArrayList<Game>) -> Unit) {
    val context = LocalContext.current

    var username by remember { mutableStateOf(SettingGetSting(context, "username") ?: "") }
    var password by remember { mutableStateOf(SettingGetSting(context, "pass") ?: "") }
    var host by remember { mutableStateOf(SettingGetSting(context, "host") ?: "") }

    var IsFTP by remember { mutableStateOf(SettingGetBoolean(context, "isPrivateFtp") ?: false) }

    var progress by remember { mutableStateOf(0f) }
    var isRun by remember { mutableStateOf(false) }
    var state by remember { mutableStateOf(0) }
    var text by remember { mutableStateOf("") }
    LaunchedEffect(true) {
        CoroutineScope(Dispatchers.IO).launch {
            val testftp = testfpt(username, password, host)
            if (!isRun) {
                if (IsFTP && testftp) {
                    MetadataInitializeFTP(context, {state = it }, {progress = it}, {metadata(it)})
                } else {
                    SettingStoreBoolean(context, "isPrivateFtp", false)
                    MetadataInitialize(context, {state = it }, {progress = it}, {metadata(it)})
                }
            }
            isRun = true
        }
    }

    when(state) {
        0 -> { text = "Downloading Metadata"}
        1 -> { text = "Extracting Metadata"}
        2 -> { text = "Parsing Metadata"}
        3 -> { Page()}
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(color = CustomColorScheme.background)
        .pointerInput(Unit) {}) {
        Box(modifier = Modifier
            .align(Alignment.Center)
            .width(300.dp)
            .clip(RoundedCornerShape(50.dp))
            .background(color = CustomColorScheme.surface)
        ) {
            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(50.dp, 25.dp)) {
                Text(modifier = Modifier.align(Alignment.Start), text = text)
                Spacer(modifier = Modifier.size(25.dp))
                LinearProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .fillMaxWidth(),
                    color = CustomColorScheme.tertiary,
                    trackColor = CustomColorScheme.onSurface,
                    progress = { progress })
            }
        }
    }

}
