package com.lex.vrpquest.Utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.PlatformSpanStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextGeometricTransform
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lex.vrpquest.CustomColorScheme
import java.io.File

@Composable
fun TextFieldElement(modifier: Modifier, value: String, onValueChange: (String) -> Unit, placeholder: String) {
    Box(modifier = Modifier) {
        TextField(
            modifier = modifier,
            value = value,
            onValueChange = onValueChange,
            maxLines = 1,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = CustomColorScheme.onSurface),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text),
            textStyle = TextStyle(textAlign = TextAlign.Start,
                textDecoration = TextDecoration.Underline),
            placeholder = {
                Text(text = placeholder,
                    modifier
                        .fillMaxHeight()
                        .align(Alignment.CenterStart), color = CustomColorScheme.onSurface)
            }
        )
    }
}

@Composable
fun SearchBar(modifier: Modifier, value: String, onValueChange: (String) -> Unit, placeholder: String,sortChange: (Int) -> Unit, reverseChange: (Boolean) -> Unit) {
    Box(modifier = modifier
        .wrapContentWidth()
        .fillMaxHeight()
        .clip(CircleShape)
        .background(CustomColorScheme.surface)) {

        var expanded by remember { mutableStateOf(false) }
        var reversed by remember { mutableStateOf(false) }
        val context = LocalContext.current

        Row(Modifier.fillMaxSize()) {
            Box(
                Modifier
                    .weight(0.1F)
                    .fillMaxSize()) {
                TextFieldElement(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterStart)
                        .clip(CircleShape),
                    value = value,
                    onValueChange = onValueChange,
                    placeholder
                )
            }
            Box(
                Modifier
                    .aspectRatio(1F)
                    .fillMaxSize()) {
                IconButton(
                    modifier = Modifier.align(Alignment.Center),
                    onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More",
                        tint = CustomColorScheme.onSurface
                    )
                }
                DropdownMenu(
                    shape = RoundedCornerShape(40.dp),
                    offset = DpOffset(x = -65.dp, y = 15.dp),
                    containerColor = CustomColorScheme.surface,
                    modifier = Modifier.align(Alignment.TopStart),
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    Row(
                        Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(10.dp)) {
                        Text(text = "Sort by",
                            modifier = Modifier.align(Alignment.CenterVertically),
                            color = CustomColorScheme.onSurface)
                        IconButton(
                            modifier = Modifier.align(Alignment.CenterVertically),
                            onClick = {
                                reversed = !reversed
                                reverseChange(reversed)
                            }) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = "More",
                                tint = CustomColorScheme.onSurface
                            )
                        }
                    }
                    DropdownMenuItem(
                        text = { Text("name", color = CustomColorScheme.onSurface) },
                        onClick = { sortChange(0) }
                    )
                    DropdownMenuItem(
                        text = { Text("version", color = CustomColorScheme.onSurface) },
                        onClick = { sortChange(1) }
                    )
                    DropdownMenuItem(
                        text = { Text("size", color = CustomColorScheme.onSurface) },
                        onClick = { sortChange(2) }
                    )
                    DropdownMenuItem(
                        text = { Text("date", color = CustomColorScheme.onSurface) },
                        onClick = { sortChange(3) }
                    )
                }
            }
        }
    }
}

@Composable
fun FullText(value: String) {

    Box(modifier = Modifier
        .fillMaxSize()) {
        Box(modifier = Modifier
            .align(Alignment.Center)
            .clip(RoundedCornerShape(40.dp))
            .background(CustomColorScheme.surface)) {
            Text(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(20.dp),
                text = value, textAlign = TextAlign.Center,
                color = CustomColorScheme.onSurface)
        }
        Text(
            modifier = Modifier
                .align(Alignment.Center),
            text = value, textAlign = TextAlign.Center,
            color = CustomColorScheme.onSurface)
    }
}

@Composable
fun TextBar(modifier: Modifier, value: String) {
    Box(modifier = modifier
        .fillMaxSize()
        .clip(CircleShape)
        .background(CustomColorScheme.surface)) {
        Text(
            modifier = Modifier
                .align(Alignment.Center),
            text = value, textAlign = TextAlign.Center,
            color = CustomColorScheme.onSurface)
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
            contentDescription = "",
            tint = CustomColorScheme.onSurface)
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
    // Box for settings element
    Box(modifier = Modifier
        .clip(RoundedCornerShape(50.dp))
        .fillMaxWidth()
        .background(color = CustomColorScheme.surface)
    ) {
        Text(modifier = Modifier.padding(25.dp),
            text = text,
            color = CustomColorScheme.onSurface)
    }
}

@Composable
fun settingsGroup(content: @Composable () -> Unit) {
    // Box for Shizuku is not running message
    Spacer(modifier = Modifier.size(20.dp))
    Box(modifier = Modifier
        .padding(bottom = 20.dp)
        .clip(RoundedCornerShape(50.dp))
        .fillMaxWidth()
        .background(color = CustomColorScheme.surface)
    ) { Column() {content()} }
}

@Composable
fun SettingsTextButton(text:String, buttontext:String, onClick: () -> Unit) {
    Row(modifier = Modifier.padding(25.dp)) {
        Text(text = text,
            Modifier
                .align(Alignment.CenterVertically)
                .weight(1f),
            color = CustomColorScheme.onSurface)
        Spacer(modifier = Modifier
            .fillMaxWidth()
            .weight(0.1f))
        Button(onClick = onClick,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .weight(0.5f),
            colors = ButtonDefaults.buttonColors(
                containerColor = CustomColorScheme.tertiary,
                contentColor = CustomColorScheme.onSurface),) {
            Text(text = buttontext, color = CustomColorScheme.onSurface)
        }
    }
}

@Composable
fun SettingsTextSwitch(text:String, switchValue:Boolean, onClick: () -> Unit) {
    Row(modifier = Modifier.padding(25.dp)) {
        Text(text = text,
            Modifier
                .align(Alignment.CenterVertically)
                .weight(1f),
            color = CustomColorScheme.onSurface)
        Spacer(modifier = Modifier
            .fillMaxWidth()
            .weight(0.1f))
        Switch(switchValue, { onClick() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = CustomColorScheme.onSurface,
                checkedTrackColor = CustomColorScheme.tertiary,
                uncheckedThumbColor = CustomColorScheme.surface,
                uncheckedTrackColor = CustomColorScheme.onSurface,
                uncheckedBorderColor = CustomColorScheme.onSurface))
    }
}

@Composable
fun SettingsTextDropdown(titleText:String, text:String, list:List<String>, onclick: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var temptext by remember { mutableStateOf(text) }

    Row(modifier = Modifier.padding(25.dp)) {
        Text(text = titleText,
            Modifier
                .align(Alignment.CenterVertically)
                .weight(1f),
            color = CustomColorScheme.onSurface)
        Spacer(modifier = Modifier
            .fillMaxWidth()
            .weight(0.1f))
        Button(
            onClick = { expanded = !expanded },
            colors = ButtonDefaults.buttonColors(
                containerColor = CustomColorScheme.tertiary,
                contentColor = CustomColorScheme.onSurface),
            ) {
            Text(text = temptext,
                Modifier.align(Alignment.CenterVertically),
                color = CustomColorScheme.onSurface
            )
        }

        DropdownMenu(
            shape = RoundedCornerShape(40.dp),
            offset = DpOffset(x = -65.dp, y = 15.dp),
            containerColor = CustomColorScheme.surface,
            modifier = Modifier.align(Alignment.CenterVertically),
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            list.forEach{
                DropdownMenuItem(
                    text = { Text(it, color = CustomColorScheme.onSurface) },
                    onClick = {
                        temptext = it
                        expanded = false
                        onclick(it)
                    }
                )
            }
        }
    }
}



@Composable
fun DatastoreTextSwitch(context: Context, text:String, id:String, defaultValue:Boolean = false) {
    var value by remember { mutableStateOf(SettingGetBoolean(context, id) ?: defaultValue) }
    SettingsTextSwitch(text, value) {
        value = !value
        SettingStoreBoolean(context, id, value)
    }
}

@Composable
fun SettingsTextField(text:String, value: String, placeholder:String, onChange: (String) -> Unit) {
    Row(modifier = Modifier.padding(25.dp, 10.dp)) {
        //Text(text = text, Modifier.align(Alignment.CenterVertically).weight(0.8f))
        //Spacer(modifier = Modifier.fillMaxWidth().weight(0.1f))
        Box(modifier = Modifier
            .align(Alignment.CenterVertically)
            .clip(RoundedCornerShape(50.dp))
            .fillMaxWidth()
            .background(color = CustomColorScheme.tertiary)
        ) {
            TextField(
                modifier = Modifier.align(Alignment.CenterStart),
                value = value,
                onValueChange = onChange,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = Color.Transparent),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text),
                maxLines = 1,
                textStyle = TextStyle(textAlign = TextAlign.Start),
                placeholder = {
                    Text(text = placeholder,
                        Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .align(Alignment.Center), color = CustomColorScheme.onSurface)
                }
            )
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

@Composable
fun RoundDivider(Color:Color = CustomColorScheme.background) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .padding(50.dp, 0.dp)
        .height(3.dp)
        .clip(RoundedCornerShape(10.dp))
        .background(color = Color))
}
@Composable
fun GroupDivider() {
    Box(modifier = Modifier
        .fillMaxWidth()
        .height(3.dp)
        .clip(RoundedCornerShape(10.dp))
        .background(color = CustomColorScheme.onSurface))
}

@Composable
fun GroupDropDown(value: Boolean, onChange: (Boolean) -> Unit, text:String) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(50.dp, 10.dp))
    {
        Box(modifier = Modifier.fillMaxWidth()) {
            Icon(modifier = Modifier
                .clip(RoundedCornerShape(50.dp))
                .size(35.dp)
                .clickable {
                    onChange(!value)
                }, imageVector = if (value) Icons.Rounded.KeyboardArrowDown else Icons.Rounded.KeyboardArrowUp,
                contentDescription = "",
                tint = CustomColorScheme.onSurface
            )

            Text(modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
                textAlign = TextAlign.Center,
                text = text,
                color = CustomColorScheme.onSurface)
        }
        GroupDivider()
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


//Modified text element that has hyperlink support
@Composable
fun LinkStringBuilder(
    text: String
): AnnotatedString {
    val context = LocalContext.current
    val pattern = """\bhttps?://[^\s()<>]+(?:\([\w\d]+\)|[^[:punct:]\s])""".toRegex()

    val tempRegex = splitByRegex(text, pattern)
    return tempRegex
}

fun splitByRegex(text: String, regex: Regex): AnnotatedString {
    val matches = regex.findAll(text).map { Pair(it.range, it.value)}.toList()
    val result:MutableList<String> = mutableListOf("")
    var lastEnd = 0
    val annotatedString = buildAnnotatedString {
        for (match in matches) {
            val before = text.substring(lastEnd, match.first.start)
            val after = text.substring(match.first.endInclusive + 1)

            append(before)

            withLink(LinkAnnotation.Url(url = match.second)) {
                withStyle(
                    SpanStyle(color = CustomColorScheme.tertiary)
                ) {
                    append(match.second)
                }
            }

            append(after)

            lastEnd = match.first.endInclusive + 1
        }
        if (matches.isEmpty()) {
            append(text)
        }
    }

    return annotatedString
}
