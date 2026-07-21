package com.mkumar.ui.previews

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mkumar.data.ProductFormData
import com.mkumar.ui.components.forms.ContactLensForm
import com.mkumar.ui.components.forms.FrameForm
import com.mkumar.ui.theme.NikThemePreview

private val frameSample = ProductFormData.FrameData(
    productOwner = "Aarav Sharma",
    productDescription = "Titanium rimless frame",
    unitPrice = 2_400,
    quantity = 1,
    discountPct = 10,
    total = 2_160
)

private val lensSample = ProductFormData.LensData(
    productOwner = "Meera Patel",
    productDescription = "Monthly contact lenses",
    rightSph = "-1.75",
    rightCyl = "-0.50",
    rightAxis = "175",
    rightAdd = "+1.25",
    leftSph = "-2.00",
    leftCyl = "-0.75",
    leftAxis = "165",
    leftAdd = "+1.25",
    unitPrice = 1_800,
    quantity = 2,
    discountPct = 5,
    total = 3_420
)

@Composable
private fun FormSurface(content: @Composable () -> Unit) {
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        androidx.compose.foundation.layout.Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) { content() }
    }
}

@Preview(name = "Frame form · Light", showBackground = true, widthDp = 420, heightDp = 700)
@Composable
private fun FrameFormLightPreview() = NikThemePreview {
    FormSurface { FrameForm(initialData = frameSample, onChange = {}) }
}

@Preview(name = "Frame form · Dark", showBackground = true, widthDp = 420, heightDp = 700, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FrameFormDarkPreview() = NikThemePreview {
    FormSurface { FrameForm(initialData = frameSample, onChange = {}) }
}

@Preview(name = "Contact lens form · Light", showBackground = true, widthDp = 420, heightDp = 900)
@Composable
private fun ContactLensLightPreview() = NikThemePreview {
    FormSurface { ContactLensForm(initialData = lensSample, onChange = {}) }
}

@Preview(name = "Contact lens form · Dark", showBackground = true, widthDp = 420, heightDp = 900, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ContactLensDarkPreview() = NikThemePreview {
    FormSurface { ContactLensForm(initialData = lensSample, onChange = {}) }
}
