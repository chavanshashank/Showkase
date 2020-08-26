package com.airbnb.android.showkase.processor.writer

import com.airbnb.android.showkase.processor.models.ShowkaseMetadata
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import javax.annotation.processing.ProcessingEnvironment

internal class ShowkaseBrowserIntentWriter(
    private val processingEnv: ProcessingEnvironment
) {
    internal fun generateIntentFile(
        rootModulePackageName: String,
        rootModuleClassName: String,
        showkaseMetadata: Set<ShowkaseMetadata>
    ) {
        val intentFile = 
            getFileBuilder(
                rootModulePackageName, 
                "${rootModuleClassName}$SHOWKASE_BROWSER_INTENT_SUFFIX"
            )
        intentFile.addFunction(
            generateIntentFunction(rootModulePackageName, rootModuleClassName, showkaseMetadata)
        )
            .build()
            .writeTo(processingEnv.filer)
    }

    private fun generateIntentFunction(
        rootModulePackageName: String,
        rootModuleClassName: String,
        showkaseMetadata: Set<ShowkaseMetadata>,
    ) = FunSpec.builder(INTENT_FUNCTION_NAME).apply {
        addParameter(
            CONTEXT_PARAMETER_NAME, CONTEXT_CLASS_NAME
        )
        returns(INTENT_CLASS_NAME)
        addCode(
            CodeBlock.Builder()
                .addStatement(
                    "val intent = %T(%L, %T::class.java)",
                    INTENT_CLASS_NAME,
                    CONTEXT_PARAMETER_NAME,
                    SHOWKASE_BROWSER_ACTIVITY_CLASS_NAME
                )
                .addStatement(
                    "intent.putExtra(%S, %S)",
                    SHOWKASE_ROOT_MODULE_KEY,
                    "$rootModulePackageName.$rootModuleClassName"
                )
                .addStatement(
                    "return intent"
                )
                .build()
        )
        showkaseMetadata.forEach { addOriginatingElement(it.element) }
    }
        .build()

    companion object {
        private const val SHOWKASE_BROWSER_INTENT_SUFFIX = "IntentCodegen"
        private const val SHOWKASE_ROOT_MODULE_KEY = "SHOWKASE_ROOT_MODULE"
        private const val INTENT_FUNCTION_NAME = "createShowkaseBrowserIntent"
        private const val CONTEXT_PARAMETER_NAME = "context"
        val CONTEXT_PACKAGE_NAME = "android.content"
        val CONTEXT_CLASS_NAME =
            ClassName(CONTEXT_PACKAGE_NAME, "Context")
        val INTENT_CLASS_NAME =
            ClassName(CONTEXT_PACKAGE_NAME, "Intent")
        val SHOWKASE_BROWSER_ACTIVITY_CLASS_NAME =
            ClassName("com.airbnb.android.showkase.ui", "ShowkaseBrowserActivity")
    }
}
