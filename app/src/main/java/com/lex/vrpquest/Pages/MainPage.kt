package com.lex.vrpquest.Pages

import android.content.ContentValues.TAG
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lex.vrpquest.CustomColorScheme
import com.lex.vrpquest.Managers.Game
import com.lex.vrpquest.Utils.GetGameBitmap
import com.lex.vrpquest.Utils.RoundByteValue

@Composable
fun MainPage(Gamelist: MutableList<Game>, searchText: String, onClick: (Game) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 200.dp)
    ) {
        Log.i(TAG, Gamelist.count().toString())
        items(Gamelist.filter { it.GameName.contains(searchText, ignoreCase = true) }) { game ->
            Box(modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .clip(RoundedCornerShape(50.dp))
                .background(
                    CustomColorScheme.surface
                )
                .clickable { onClick(game) }) {
                Column(Modifier.padding(10.dp)) {
                    Image(modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(40.dp)),
                        bitmap = GetGameBitmap(game.Thumbnail),
                        contentDescription = "")
                    Spacer(modifier = Modifier.size(10.dp))
                    Text(
                        text = game.GameName,
                        modifier = Modifier.padding(20.dp, vertical = 0.dp),
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = RoundByteValue(game.Size),
                        modifier = Modifier.padding(20.dp, vertical = 0.dp),
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

