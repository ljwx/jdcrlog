package com.jdcr.jdcrlogcommon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.jdcr.jdcrlog.JdcrLog
import com.jdcr.jdcrlogcommon.ui.theme.JdcrLogCommonTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JdcrLogCommonTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
        JdcrLog.enable(true, filePath = cacheDir.absolutePath + "/test/log.txt")
        JdcrLog.i("测试日志1")
//        for (i in 0..10) {
//            JdcrLogUtils.getLatest().onSuccess {
//                it.forEachIndexed { index, string ->
//                    Log.d("jdcr_log_base", "$string")
//                }
//            }
//        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    JdcrLogCommonTheme {
        Greeting("Android")
    }
}