package com.virtuslab.pulumikotlin.codegen

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.KotlinCompilation.ExitCode.*
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.cli.common.ExitCode
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertEquals

internal class GenerateTypeWithNiceBuildersTest {

    @Test
    fun `just test something`() {
        val generatedSpec1 = generateTypeWithNiceBuilders(
            "FirstTypeArgs.kt",
            "com.pulumi.kotlin.aws",
            "FirstTypeArgs",
            "FirstTypeArgsBuilder",
            listOf(
                Field(
                    "field1",
                    NormalField(PrimitiveType("String"), { from, to -> CodeBlock.of("val $to = $from") }),
                    true,
                    listOf()
                )
            )
        )
        val generatedSpec2 = generateTypeWithNiceBuilders(
            "SecondTypeArgs.kt",
            "com.pulumi.kotlin.aws",
            "SecondTypeArgs",
            "SecondTypeArgsBuilder",
            listOf(
                Field(
                    "field2",
                    NormalField(
                        ComplexType(
                            TypeMetadata(
                                PulumiName("aws", listOf("aws"), "FirstType"),
                                InputOrOutput.Input,
                                UseCharacteristic.ResourceRoot
                            ),
                            mapOf(
                                "firstType" to PrimitiveType("String")
                            )
                        ),
                        { from, to -> CodeBlock.of("val $to = $from") }
                    ),
                    true,
                    listOf()
                )
            )
        )

        val compileResult =
            KotlinCompilation()
                .apply {
                    sources = listOf(generatedSpec1, generatedSpec2).map { fileSpecToSourceFile(it) }

                    messageOutputStream = System.out
                }
                .compile()


        assertEquals(OK, compileResult.exitCode)
    }

    private fun fileSpecToSourceFile(spec: FileSpec): SourceFile {
        return SourceFile.kotlin(
            spec.name,
            spec.toString()
        )
    }

}