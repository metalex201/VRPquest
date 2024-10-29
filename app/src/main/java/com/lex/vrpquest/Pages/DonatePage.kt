package com.lex.vrpquest.Pages

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.packInts
import com.lex.vrpquest.CustomColorScheme
import com.lex.vrpquest.Managers.DonateGame
import com.lex.vrpquest.Managers.DonateQueue
import com.lex.vrpquest.Utils.SettingGetStringSet
import com.lex.vrpquest.Utils.SettingStoreStringSet

val checkboxcolor = CheckboxColors(
    checkedCheckmarkColor = CustomColorScheme.onTertiary,
    uncheckedCheckmarkColor = CustomColorScheme.error,
    checkedBoxColor = CustomColorScheme.tertiary,
    uncheckedBoxColor = CustomColorScheme.surface,
    disabledCheckedBoxColor = Color.Transparent,
    disabledUncheckedBoxColor = Color.Transparent,
    disabledIndeterminateBoxColor = Color.Transparent,
    checkedBorderColor = Color.Transparent,
    uncheckedBorderColor = CustomColorScheme.error,
    disabledBorderColor = Color.Transparent,
    disabledUncheckedBorderColor = Color.Transparent,
    disabledIndeterminateBorderColor = Color.Transparent,
)

@Composable
fun DonatePage(Page: () -> Unit, update: (ArrayList<DonateQueue>) -> Unit, donatelist: MutableList<DonateGame>) {
    val context = LocalContext.current
    Box(modifier = Modifier
        .fillMaxSize()
        .background(color = CustomColorScheme.background)
        .pointerInput(Unit) {}) {
        Box(modifier = Modifier
            .padding(10.dp)
            .clip(RoundedCornerShape(50.dp))
            .width(280.dp)
            .align(Alignment.Center)
            .background(
                CustomColorScheme.surface
            )) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(modifier = Modifier.padding(15.dp), text = "Donatable Apps were found")
                Text(modifier = Modifier.padding(15.dp), text = "All Apps are donated by users! Without them none of this would be possible!")
                Column(modifier = Modifier.weight(1f).fillMaxSize().verticalScroll(rememberScrollState()) ) {
                    donatelist.forEach() { donategame ->
                        val applicationInfo = context.packageManager.getApplicationInfo(donategame.packageinfo.packageName, 0);

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {

                            Box(){
                                Checkbox(checked = donategame.checked.value,
                                    onCheckedChange = { donategame.checked.value = it },
                                    colors = checkboxcolor
                                )
                                if (!donategame.checked.value) {
                                    Icon(Icons.Rounded.Close, "", Modifier.align(Alignment.Center), CustomColorScheme.error)
                                }
                            }

                            Text(text = applicationInfo.loadLabel(context.getPackageManager()).toString())
                        }
                        Spacer(modifier = Modifier.size(10.dp))
                    }
                }
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(15.dp, 10.dp)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CustomColorScheme.tertiary,
                        contentColor = CustomColorScheme.onSurface
                    ),
                    onClick = {
                        ProcessCheckList(context, donatelist, update)
                        Page()
                    },
                ) { Text(text = "CONTINUE") }
            }
        }
    }
}

fun ProcessCheckList(context: Context, donatelist: List<DonateGame>, update: (ArrayList<DonateQueue>) -> Unit) {
    val getblacklist = SettingGetStringSet(context, "DonateBlacklist") ?: mutableSetOf()
    var blacklist = getblacklist.toMutableSet()

    var returned:ArrayList<DonateQueue> = ArrayList<DonateQueue>()

    donatelist.forEach() {
        if (it.checked.value == false) {
            println(it.packageinfo.packageName)
            blacklist.add(it.packageinfo.packageName)
        } else {
            println("returned" + it.packageinfo.packageName)
            returned.add(DonateQueue(mutableStateOf(it.packageinfo.packageName)))
        }
    }
    SettingStoreStringSet(context, "DonateBlacklist", blacklist)
    println("added to blocklist" + blacklist)

    println("returned full " + returned)
    if(donatelist.size != 0) {
        println("processchecklist returned is not empty")
        update(returned)
    }
}

fun RemoveFromCheckList(context: Context, PackageToRemove: String) {
    val getblacklist = SettingGetStringSet(context, "DonateBlacklist") ?: mutableSetOf()
    var blacklist = getblacklist.toMutableSet()

    //blacklist.remove(PackageToRemove)
    println("DONATEREMOVE")
    println(blacklist.joinToString())
    blacklist.add(PackageToRemove)
    SettingStoreStringSet(context, "DonateBlacklist", blacklist)
}