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
import androidx.lifecycle.lifecycleScope
import com.jdcr.jdcrlog.JdcrLog
import com.jdcr.jdcrlogcommon.ui.theme.JdcrLogCommonTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
        JdcrLog.enable(true, filePath = cacheDir.absolutePath+"/test/log.txt")
        JdcrLog.enable(true, filePath = cacheDir.absolutePath+"/test/log.txt")
        JdcrLog.enable(true, filePath = cacheDir.absolutePath+"/test/log.txt")
        lifecycleScope.launch(Dispatchers.Main) {
            for (i in 0..40) {
                JdcrLog.v("日志v:$i")
            }
        }
        lifecycleScope.launch(Dispatchers.Main) {
            for (i in 0..40) {
                JdcrLog.d("日志d:$i")
            }
        }
        lifecycleScope.launch(Dispatchers.Main) {
            for (i in 0..40) {
                JdcrLog.i("日志i:$i")
            }
        }
        JdcrLog.d("做滴22")
        JdcrLog.i("做题")
        JdcrLog.iF("ljwx","做题")
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