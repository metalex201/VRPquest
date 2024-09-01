package com.lex.vrpquest

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Paint.Align
import android.graphics.drawable.Icon
import android.os.Bundle
import android.widget.Space
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColor
import androidx.lifecycle.ReportFragment.Companion.reportFragment
import rikka.shizuku.Shizuku
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext


var CustomColorScheme =
    darkColorScheme(
        background = Color(27, 43, 59, 255), //Color(27, 43, 59, 255),
        surface = Color(50,63,76,255),
        error = Color(187,0,19,255),
        onSurface = Color(240,244,245,255),
        onSurfaceVariant = Color(54,63,78,255),
        tertiary = Color(3,100,237,255)
    )

@Composable
fun TextFieldElement(modifier: Modifier, value: String, onValueChange: (String) -> Unit) {
    Box(modifier = Modifier.fillMaxWidth()) {
        TextField(
            modifier = modifier,
            value = value,
            onValueChange = onValueChange,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                cursorColor = CustomColorScheme.onSurface),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text),
            textStyle = TextStyle(textAlign = TextAlign.Start,
                textDecoration = TextDecoration.Underline),
            placeholder = {
                Text(text = "Search here",
                    modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterStart), color = CustomColorScheme.onSurface)
            }
        )
    }
}
@Composable
fun SearchBar(modifier: Modifier, value: String, onValueChange: (String) -> Unit) {
    Box(modifier = modifier
        .fillMaxHeight()
        .fillMaxWidth()
        .clip(CircleShape)
        .background(CustomColorScheme.surface)) {
        TextFieldElement(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center)
                .clip(CircleShape),
            value = value,
            onValueChange = onValueChange)
    }
}
@Composable
fun BarButton(Icon: ImageVector, onClick: () -> Unit) {
    Box(modifier = Modifier
        .fillMaxHeight()
        .aspectRatio(1f)
        .clip(CircleShape)
        .background(CustomColorScheme.surface)
        .clickable { onClick() },
    ) {
        Icon(
            modifier = Modifier.align(Alignment.Center),
            imageVector = Icon,
            contentDescription = ""
        )
    }
}
class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Shizuku.addRequestPermissionResultListener(REQUEST_PERMISSION_RESULT_LISTENER);
        //checkPermission(0)
        //enableEdgeToEdge()
        setContent {
            MaterialTheme(colorScheme = CustomColorScheme) {
                Surface(modifier = Modifier.fillMaxSize(), color = CustomColorScheme.background) {
                    MainPage()
                    Load()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Shizuku.removeRequestPermissionResultListener(REQUEST_PERMISSION_RESULT_LISTENER)
    }
}

@Composable
fun Load() {
    var progress by remember { mutableStateOf(0.5f) }
    var text by remember { mutableStateOf("") }
    val context = LocalContext.current
    Box(modifier = Modifier
        .fillMaxSize()
        .background(color = CustomColorScheme.background)) {
        Box(modifier = Modifier
            .align(Alignment.Center)
            .size(300.dp, 200.dp)
            .clip(RoundedCornerShape(50.dp))
            .background(color = CustomColorScheme.surface)
            ) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), progress = { progress })
            Text(modifier = Modifier.align(Alignment.Center), text = text)
            MetadataInitialize(context, {text = it }, {progress = it})
        }

    }
}

@Composable
fun MainPage() {
    var text by remember { mutableStateOf("") }
    var IsDownloadingList by remember { mutableStateOf(false) }
    Column() {
        Box(modifier = Modifier
            .height(80.dp)
            .padding(10.dp)
            .fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxSize()) {
                BarButton(if (IsDownloadingList) Icons.Default.Close else Icons.Default.Refresh ,
                    onClick = {IsDownloadingList = !IsDownloadingList})
                Spacer(modifier = Modifier.width(10.dp))
                SearchBar(Modifier.weight(0.1f), text, {text = it})
                Spacer(modifier = Modifier.width(10.dp))
                BarButton(Icons.Default.Settings, {})
            }
        }
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(10.dp, 0.dp)) {
            if(IsDownloadingList) {
                DownloadingList()
            } else {
                AppGridList()
            }
        }
    }
}

@Composable
fun AppGridList() {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 250.dp)
    ) {
        items(300) { Box(modifier = Modifier
            .size(280.dp)
            .padding(10.dp)
            .clip(RoundedCornerShape(50.dp))
            .background(
                CustomColorScheme.surface
            )
            .clickable {}) {}
        }
    }
}

@Composable
fun DownloadingList() {
    LazyColumn(
    ) {
        items(300) { Box(modifier = Modifier
            .height(150.dp)
            .fillMaxWidth()
            .padding(10.dp)
            .clip(RoundedCornerShape(50.dp))
            .background(
                CustomColorScheme.surface
            )
            .clickable {}) {}
        }
    }
}