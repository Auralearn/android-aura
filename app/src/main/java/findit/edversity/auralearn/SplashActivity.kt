package findit.edversity.auralearn

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import findit.edversity.auralearn.ui.theme.AuralearnTheme
import findit.edversity.auralearn.ui.theme.Black
import findit.edversity.auralearn.ui.theme.CyanTertiary
import findit.edversity.auralearn.ui.theme.PurplePrimary
import findit.edversity.auralearn.ui.theme.PurpleSecondary
import kotlinx.coroutines.delay

@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AuralearnTheme {
                SplashScreen(
                    logoResId = R.drawable.app_logo,
                    onSplashEnd = {
                        // Navigate to MainActivity
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun SplashScreen(
    logoResId: Int,
    onSplashEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val glowAnimation = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        glowAnimation.animateTo(1f, tween(1000))
        glowAnimation.animateTo(0f, tween(1000))
        delay(1500)
        onSplashEnd()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(PurpleSecondary, Black)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Background blurred circles
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithCache {
                    onDrawWithContent {
                        drawContent()
                        val size = size
                        val circleSize = size.minDimension * 0.35f

                        // Purple circle
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(PurplePrimary.copy(alpha = 0.35f), Color.Transparent),
                                center = Offset(0.3f * size.width, 0.7f * size.height),
                                radius = circleSize
                            ),
                            center = Offset(0.3f * size.width, 0.7f * size.height),
                            radius = circleSize,
                            blendMode = BlendMode.Screen
                        )

                        // Cyan circle
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(CyanTertiary.copy(alpha = 0.35f), Color.Transparent),
                                center = Offset(0.7f * size.width, 0.3f * size.height),
                                radius = circleSize
                            ),
                            center = Offset(0.7f * size.width, 0.3f * size.height),
                            radius = circleSize,
                            blendMode = BlendMode.Screen
                        )
                    }
                }
                .blur(radius = 32.dp)
        )

        // Logo with glow
        Box(
            modifier = Modifier
                .size(350.dp)
                .graphicsLayer {
                    scaleX = 1f + glowAnimation.value * 0.1f
                    scaleY = 1f + glowAnimation.value * 0.1f
                    alpha = 0.8f + glowAnimation.value * 0.2f
                }
                .drawWithCache {
                    onDrawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.1f * glowAnimation.value),
                                    Color.Transparent
                                )
                            ),
                            radius = size.minDimension * 0.7f,
                            blendMode = BlendMode.Plus
                        )
                    }
                }
        ) {
            Image(
                painter = painterResource(id = logoResId),
                contentDescription = "App Logo",
                modifier = Modifier.size(250.dp).align(Alignment.Center)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    AuralearnTheme {
        SplashScreen(
            logoResId = R.drawable.app_logo,
            onSplashEnd = {}
        )
    }
}