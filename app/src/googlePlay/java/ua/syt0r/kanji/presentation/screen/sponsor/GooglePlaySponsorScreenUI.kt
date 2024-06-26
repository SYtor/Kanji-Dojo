package ua.syt0r.kanji.presentation.screen.sponsor

import androidx.compose.animation.Animatable
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import ua.syt0r.kanji.R
import ua.syt0r.kanji.core.app_data.data.buildFuriganaString
import ua.syt0r.kanji.presentation.common.theme.neutralColors
import ua.syt0r.kanji.presentation.common.ui.FancyLoading
import ua.syt0r.kanji.presentation.common.ui.FuriganaText
import ua.syt0r.kanji.presentation.screen.main.screen.sponsor.SponsorScreenDefaultContent
import ua.syt0r.kanji.presentation.screen.sponsor.GooglePlaySponsorScreenContract.ScreenState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GooglePlaySponsorScreenUI(
    state: State<ScreenState>,
    onUpClick: () -> Unit,
    fillDetails: () -> Unit,
    startPurchase: () -> Unit,
    retry: () -> Unit
) {

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onUpClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                title = { }
            )
        }
    ) {

        val transition = updateTransition(
            targetState = state.value,
            label = null
        )
        transition.AnimatedContent(
            transitionSpec = {
                val enterTransition = if (targetState is ScreenState.Completed)
                    fadeIn() + scaleIn(
                        tween(easing = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f))
                    )
                else fadeIn()
                enterTransition togetherWith fadeOut()

            },
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .wrapContentWidth()
                    .padding(20.dp)
                    .widthIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (it) {
                    ScreenState.Init -> {
                        ScreenStateInit(
                            onShowDetailsClick = fillDetails
                        )
                    }

                    is ScreenState.Error -> {
                        ScreenStateError(
                            screenState = it,
                            onRetryClick = retry
                        )
                    }

                    is ScreenState.Input -> {
                        ScreenStateInput(
                            screenState = it,
                            startPurchaseClick = startPurchase
                        )
                    }

                    ScreenState.Loading -> {
                        FancyLoading(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f)
                                .wrapContentSize()
                        )
                    }

                    ScreenState.Completed -> {
                        ScreenStateCompleted()
                    }

                }
            }

        }

    }

}

@Composable
private fun ColumnScope.ScreenStateInit(
    onShowDetailsClick: () -> Unit
) {
    SponsorScreenDefaultContent()
    Spacer(modifier = Modifier.weight(1f))
    Button(
        onClick = onShowDetailsClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.onSurface,
            contentColor = MaterialTheme.colorScheme.surface
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Text(text = GooglePlaySponsorScreenStrings.getLocalized().initButton)
    }
}

@Composable
private fun ColumnScope.ScreenStateError(
    screenState: ScreenState.Error,
    onRetryClick: () -> Unit
) {
    val strings = GooglePlaySponsorScreenStrings.getLocalized()

    Spacer(modifier = Modifier.weight(1f))
    Text(
        text = strings.errorLabel,
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier.align(Alignment.CenterHorizontally)
    )
    screenState.message?.let {
        Text(
            text = it,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
    Spacer(modifier = Modifier.weight(1f))
    Button(
        onClick = onRetryClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.onSurface,
            contentColor = MaterialTheme.colorScheme.surface
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Text(text = strings.errorRetryButton)
    }
}


@Composable
private fun ColumnScope.ScreenStateInput(
    screenState: ScreenState.Input,
    startPurchaseClick: () -> Unit
) {
    val strings = GooglePlaySponsorScreenStrings.getLocalized()

    TextField(
        value = screenState.email.value,
        onValueChange = { screenState.email.value = it },
        label = { Text(text = strings.inputEmailHint) },
        colors = TextFieldDefaults.neutralColors(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        modifier = Modifier.fillMaxWidth()
    )

    TextField(
        value = screenState.message.value,
        onValueChange = { screenState.message.value = it },
        label = { Text(text = strings.inputMessageHint) },
        colors = TextFieldDefaults.neutralColors(),
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
    )

    Text(
        text = strings.inputMultipleQuantityNotice,
        style = MaterialTheme.typography.labelSmall
    )

    Button(
        onClick = startPurchaseClick,
        modifier = Modifier.fillMaxWidth(),
        enabled = screenState.buttonEnabled.value,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.onSurface,
            contentColor = MaterialTheme.colorScheme.surface
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Text(text = strings.inputButton(screenState.formattedPrice))
    }

}

@Composable
private fun ColumnScope.ScreenStateCompleted() {
    val strings = GooglePlaySponsorScreenStrings.getLocalized()
    Spacer(modifier = Modifier.weight(1f))
    Box(
        modifier = Modifier.align(Alignment.CenterHorizontally)
    ) {

        Icon(
            painter = painterResource(id = R.drawable.judo_9219332),
            contentDescription = null,
            modifier = Modifier.size(160.dp),
            tint = Color.Unspecified
        )

    }

    FuriganaText(
        furiganaString = buildFuriganaString {
            append(
                character = "ありがとうございます！",
                annotation = strings.completeThankYouMessage
            )
        },
        textStyle = MaterialTheme.typography.headlineSmall
    )

    Spacer(modifier = Modifier.weight(1f))

    val creditTextColor = MaterialTheme.colorScheme.onSurface
    val creditTextAnimatedColor = remember { Animatable(creditTextColor.copy(0f)) }

    LaunchedEffect(Unit) {
        delay(600)
        creditTextAnimatedColor.animateTo(creditTextColor, tween(600))
    }

    Text(
        text = strings.completeIconCredit,
        color = creditTextAnimatedColor.value,
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier.align(Alignment.CenterHorizontally)
    )

}
