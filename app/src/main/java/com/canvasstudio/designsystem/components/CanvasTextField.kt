package com.canvasstudio.designsystem.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.canvasstudio.designsystem.CanvasTheme
import com.canvasstudio.designsystem.tokens.CanvasDimens

@Composable
fun CanvasTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    placeholder: String? = null,
    label: String? = null,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    shape: Shape = CanvasDimens.shapeMd,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    val colors = CanvasTheme.colors

    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        singleLine = singleLine,
        maxLines = maxLines,
        shape = shape,
        textStyle = TextStyle(color = colors.textMain, fontSize = 13.sp),
        label = label?.let { { Text(it, color = colors.textMuted, fontSize = 12.sp) } },
        placeholder = placeholder?.let { { Text(it, color = colors.textMuted.copy(alpha = 0.6f), fontSize = 13.sp) } },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        colors = TextFieldDefaults.textFieldColors(
            textColor = colors.textMain,
            cursorColor = colors.accent,
            focusedIndicatorColor = colors.accent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            backgroundColor = colors.bgInput
        )
    )
}
