package com.example.dpbug

import android.media.AudioTrack
import android.media.audiofx.DynamicsProcessing
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.dpbug.ui.theme.DPBugTheme


class MainActivity : ComponentActivity() {
    var atAndDp: Pair<AudioTrack, DynamicsProcessing>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val ok31 = createAndDestroyDP(this, 31)
        val ok64 = createAndDestroyDP(this, 64)
        val ok128 = createAndDestroyDP(this, 128)
        val ok256 = createAndDestroyDP(this, 256)
        val ok512 = createAndDestroyDP(this, 512)
        val ok1024 = createAndDestroyDP(this, 1024)
        // We can do more on the older platforms, though inefficient params marshaling effectively prevents that
        //val ok2048 = createAndDestroyDP(this, 2048)
        //val ok4096 = createAndDestroyDP(this, 4096)

        val results = "31 bands=$ok31\n" +
                      "64 bands=$ok64\n" +
                      "128 bands=$ok128\n" +
                      "256 bands=$ok256\n" +
                      "512 bands=$ok512\n" +
                      "1024 bands=$ok1024\n"
                      //"2048 bands=$ok2048\n" +
                      //"4096 bands=$ok4096\n"

        // Expected: After this call we expected to see last 2 band gains set to 5dB and 10dB OR some exception or other
        // error accessible by the code

        // Actual: Pixel9ProXL @15 BP1A.250305.020:
        // bands 0-31 - 0dB
        // band 31 - 5dB (the last assigned value for band 127)

        // dumpsys media.audio_flinger - clamped to 32 bands:
        // Engine architecture: EngineArchitecture{resolutionPreference: FAVOR_FREQUENCY_RESOLUTION, preferredProcessingDurationMs: 10.000000,
        //    preEqStage: StageEnablement{inUse: false, bandCount: 0}, postEqStage: StageEnablement{inUse: true, bandCount: 32},
        //    mbcStage: StageEnablement{inUse: false, bandCount: 0}, limiterInUse: false}


        atAndDp = createATAndDPAndSetLastBandsTo5and10dB(this, 128)

        setContent {
            DPBugTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greetings(
                        results = results,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        atAndDp?.second?.release()
        atAndDp?.first?.release()
    }
}

@Composable
fun Greetings(results: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(16.dp)) {
        // Pixel 4 XL @13
        // 31 bands=true
        // 64 bands=true
        // 128 bands=true
        // 256 bands=true
        // 512 bands=true
        // 1024 bands=true

        // Pixel7 @15 BP1A.250305.019
        // 31 bands=true
        // 64 bands=true
        // 128 bands=true
        // 256 bands=true
        // 512 bands=true
        // 1024 bands=true

        // Pixel9ProXL @15 PRIOR BP1A.250305.020
        // 31 bands=true
        // 64 bands=true
        // 128 bands=true
        // 256 bands=true
        // 512 bands=false
        // 1024 bands=false

        // Pixel9ProXL @15 BP1A.250305.020
        // 31 bands=true
        // 64 bands=true (fake)
        // 128 bands=true (fake)
        // 256 bands=true (fake)
        // 512 bands=true (fake)
        // 1024 bands=true (fake)

        Text(
            text = results,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DPBugTheme {
        Greetings("Android")
    }
}