//package com.mkumar.data.validation
//
//import com.mkumar.data.ProductFormData
//
//object ProductFormValidators {
//
//    fun validateFrameForm(data: ProductFormData.FrameData): ValidationResult {
//        val errors = mutableMapOf<String, String>()
//
////        if (data.brand.isBlank()) errors["brand"] = "Brand is required"
////        if (data.size.isBlank()) errors["size"] = "Size is required"
////        if (data.color.isBlank()) errors["color"] = "Color is required"
//
//        return ValidationResult(errors.isEmpty(), errors)
//    }
//
//    fun validateLensForm(data: ProductFormData.GlassData): ValidationResult {
//        val errors = mutableMapOf<String, String>()
//
////        if (data.leftSphere.isBlank()) errors["leftSphere"] = "Left eye Sphere is required"
////        if (data.rightSphere.isBlank()) errors["rightSphere"] = "Right eye Sphere is required"
////        if (data.leftAxis.isBlank()) errors["leftAxis"] = "Left axis is required"
////        if (data.rightAxis.isBlank()) errors["rightAxis"] = "Right axis is required"
//
//        return ValidationResult(errors.isEmpty(), errors)
//    }
//
//    fun validateContactLensForm(data: ProductFormData.LensData): ValidationResult {
//        val errors = mutableMapOf<String, String>()
//
////        if (data.duration.isBlank()) errors["duration"] = "Duration is required"
////        if (data.power.isBlank()) errors["power"] = "Power is required"
//
//        return ValidationResult(errors.isEmpty(), errors)
//    }
//
////    fun validate(formData: ProductFormData): ValidationResult {
////        return when (formData) {
////            is ProductFormData.FrameData -> validateFrameForm(formData)
////            is ProductFormData.LensData -> validateLensForm(formData)
////            is ProductFormData.ContactLensData -> validateContactLensForm(formData)
////        }
////    }
//}