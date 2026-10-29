package br.org.profetas.app.support.ui

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PasswordField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    cornerRadius: Dp = 12.dp,
) {
    // Cada campo começa oculto; a escolha não persiste ao sair da tela.
    var visible by remember { mutableStateOf(false) }
    AppTextField(
        label = label,
        value = value,
        onChange = onChange,
        modifier = modifier,
        placeholder = placeholder,
        cornerRadius = cornerRadius,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingContent = {
            TextButton(
                onClick = { visible = !visible },
                modifier = Modifier.heightIn(min = 48.dp).semantics {
                    contentDescription = "${if (visible) "Ocultar" else "Mostrar"} senha: $label"
                },
            ) { Text(if (visible) "Ocultar" else "Mostrar", fontSize = 16.sp) }
        },
    )
}
