package com.carolinagandin.cuidadog.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Composable
fun AlimentacionIcon(
    modifier: Modifier = Modifier,
    size: TextUnit = 24.sp
) {
    Text(
        text = "🥣",
        fontSize = size,
        modifier = modifier
    )
}

@Composable
fun AlimentacionIcon(
    modifier: Modifier = Modifier,
    size: Dp
) {
    Text(
        text = "🥣",
        fontSize = size.value.sp,
        modifier = modifier
    )
}
