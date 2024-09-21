package com.lex.vrpquest

import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.io.File
import java.util.Locale

@Composable
fun TextFieldElement(modifier: Modifier, value: String, onValueChange: (String) -> Unit, placeholder: String) {
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
                Text(text = placeholder,
                    modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .align(Alignment.CenterStart), color = CustomColorScheme.onSurface)
            }
        )
    }
}
@Composable
fun SearchBar(modifier: Modifier, value: String, onValueChange: (String) -> Unit, placeholder: String) {
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
            onValueChange = onValueChange,
            placeholder)

    }
}
@Composable
fun CircleButton(
    modifier:Modifier = Modifier,
    color: Color = CustomColorScheme.surface,
    Icon: ImageVector = Icons.Default.Edit,
    onClick: () -> Unit,
    IsDoted: Boolean = false,
    DotColor: Color = CustomColorScheme.tertiary
) {
    Box(modifier = modifier
        .fillMaxHeight()
        .aspectRatio(1f)
    ) {
        Box(modifier = Modifier
            .fillMaxSize()
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(color = color)
            .clickable { onClick() },
        ) { Icon(
            modifier = Modifier.align(Alignment.Center),
            imageVector = Icon,
            contentDescription = "")
        }
        if (IsDoted) {
            Box(modifier = Modifier
                .align(Alignment.TopEnd)
                .size(20.dp)
                .clip(CircleShape)
                .background(DotColor)) {
            }
        }
    }
}

@Composable
fun settingsText(text:String) {
    // Box for Shizuku is not running message
    Box(modifier = Modifier
        .padding(bottom = 20.dp)
        .clip(RoundedCornerShape(50.dp))
        .fillMaxWidth()
        .background(color = CustomColorScheme.surface)
    ) {
        Text(modifier = Modifier.padding(25.dp),
            text = text)
    }
}

@Composable
fun SettingsTextButton(text:String, buttontext:String, onClick: () -> Unit) {
    Box(modifier = Modifier
        .padding(bottom = 20.dp)
        .clip(RoundedCornerShape(50.dp))
        .fillMaxWidth()
        .background(color = CustomColorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(25.dp)) {
            Text(text = text, Modifier.align(Alignment.CenterVertically).weight(1f))
            Spacer(modifier = Modifier.fillMaxWidth().weight(0.1f))
            Button(onClick = onClick,
                modifier = Modifier.align(Alignment.CenterVertically).weight(0.5f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CustomColorScheme.tertiary,
                    contentColor = CustomColorScheme.onSurface),) {
                Text(text = buttontext)
            }
        }
    }
}

@Composable
fun GetGameBitmap(thumbnail: String): ImageBitmap {
    if (File(thumbnail).exists()) {
        return BitmapFactory.decodeFile(thumbnail).asImageBitmap()
    } else {
        val EmptyThumb: Bitmap = Bitmap.createBitmap(374, 214, Bitmap.Config.ARGB_8888)
        EmptyThumb.eraseColor(CustomColorScheme.background.toArgb())
        return EmptyThumb.asImageBitmap()
    }
}

fun RoundByteValue(bytes:Int) : String {
    if (bytes < 1000) { return "$bytes MB"
    } else {
        return (bytes / 1000).toString() + " GB"
    }
}

fun isPackageInstalled(context: Context, packageName: String): Boolean {
    return try {
        context.packageManager.getPackageInfo(packageName, 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}

fun isPermissionAllowed(context: Context, permission:String): Boolean {
    val packageInfo: PackageInfo = context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS)
    val list: List<String>  = packageInfo.requestedPermissions!!.filterIndexed { index, permission ->
        (packageInfo.requestedPermissionsFlags[index] and PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0
    }
    if (list.contains(permission)) { return true } else {return false}
}