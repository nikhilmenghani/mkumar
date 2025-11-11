// file: com/mkumar/ui/preview/InvoicePreviewScreen.kt
package com.mkumar.ui.preview

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.mkumar.domain.invoice.InvoiceData
import com.mkumar.domain.invoice.InvoicePdfBuilderImpl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoicePreviewScreen(
    data: InvoiceData,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // 1) build the PDF bytes
    val pdfBytes by remember(data) {
        mutableStateOf(InvoicePdfBuilderImpl().build(data))
    }

    // 2) render to bitmaps (do once per data)
    var pages by remember { mutableStateOf<List<android.graphics.Bitmap>>(emptyList()) }

    LaunchedEffect(pdfBytes) {
        pages = renderPdfToBitmaps(context, pdfBytes)
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Invoice Preview") })
        }
    ) { inner ->
        if (pages.isEmpty()) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(inner),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(inner),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(pages) { bmp ->
                    // Fit page width; height will scale automatically
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "Invoice page",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
