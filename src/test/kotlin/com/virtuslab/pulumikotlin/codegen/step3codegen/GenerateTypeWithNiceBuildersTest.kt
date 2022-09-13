package com.virtuslab.pulumikotlin.codegen.step3codegen

import com.squareup.kotlinpoet.FileSpec
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.KotlinCompilation.ExitCode.*
import com.tschuchort.compiletesting.SourceFile
import com.virtuslab.pulumikotlin.codegen.maven.ArtifactDownloader
import com.virtuslab.pulumikotlin.codegen.step2intermediate.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

internal class GenerateTypeWithNiceBuildersTest {

    @Test
    fun `generated kotlin files for some handcrafted types should compile`() {
        val firstType = ComplexType(
            TypeMetadata(
                PulumiName("aws", listOf("aws"), "FirstType"),
                InputOrOutput.Input,
                UseCharacteristic.ResourceNested
            ),

            mapOf(
                "field1" to TypeAndOptionality(PrimitiveType("String"), true)
            )
        )
        val secondType = ComplexType(
            TypeMetadata(
                PulumiName("aws", listOf("aws"), "SecondType"),
                InputOrOutput.Input,
                UseCharacteristic.ResourceNested
            ),
            mapOf(
                "field2" to TypeAndOptionality(firstType, true)
            )
        )

        val generationOptions = GenerationOptions(implementToJava = false, implementToKotlin = false)
        val generatedFiles = CodeGenerator.run(GeneratorArguments(
            types = listOf(firstType, secondType),
            options = generationOptions
        ))

        val files = generatedFiles.map {
            it.get()
        }

        files.forEach { (_, contents) ->
            assertNotEquals("", contents.trim())
        }

        val sourceFiles = files.map { (name, contents) ->
            SourceFile.kotlin(name, contents)
        }

        val compileResult =
            KotlinCompilation()
                .apply {
                    sources = sourceFiles

                    classpaths = listOf(
                        artifact("com.pulumi:pulumi:0.5.2"),
                        artifact("com.pulumi:aws:5.11.0-alpha.1658776797+e45bda97"),
                        artifact("com.google.code.findbugs:jsr305:3.0.2"),
                        artifact("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.6.2")
                    )

                    messageOutputStream = System.out
                }
                .compile()


        assertEquals(OK, compileResult.exitCode)
    }

    private fun artifact(coordinate: String) = ArtifactDownloader.download(coordinate).toFile()

    private fun fileSpecToSourceFile(spec: FileSpec): SourceFile {
        return SourceFile.kotlin(
            spec.name,
            spec.toString()
        )
    }

}
