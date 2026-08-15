package com.example.engine

import com.example.model.PartitionEntry
import java.util.Locale

object ScatterMathParser {

    /**
     * Parses MTK Android Scatter File format (.txt)
     */
    fun parseScatterContent(content: String): List<PartitionEntry> {
        val partitions = mutableListOf<PartitionEntry>()
        val lines = content.lines()

        var currentName = ""
        var currentStart = ""
        var currentLength = ""
        var currentSize: Long = 0
        var currentIndex = 0
        var isProtected = false

        for (rawLine in lines) {
            val line = rawLine.trim()
            if (line.startsWith("- partition_index:") || line.startsWith("partition_index:")) {
                if (currentName.isNotEmpty()) {
                    val size = parseHexSize(currentLength)
                    partitions.add(
                        PartitionEntry(
                            index = currentIndex,
                            name = currentName,
                            linearStartHex = currentStart.ifEmpty { "0x0" },
                            boundaryLengthHex = currentLength.ifEmpty { "0x0" },
                            sizeBytes = if (size > 0) size else currentSize,
                            isProtected = isProtected || isSensitivePartition(currentName)
                        )
                    )
                    currentIndex++
                    currentName = ""
                    currentStart = ""
                    currentLength = ""
                    currentSize = 0
                    isProtected = false
                }
            } else if (line.contains("partition_name:")) {
                currentName = line.substringAfter("partition_name:").trim().replace("\"", "").replace("'", "")
            } else if (line.contains("linear_start_addr:")) {
                currentStart = line.substringAfter("linear_start_addr:").trim()
            } else if (line.contains("physical_start_addr:") && currentStart.isEmpty()) {
                currentStart = line.substringAfter("physical_start_addr:").trim()
            } else if (line.contains("partition_size:")) {
                currentLength = line.substringAfter("partition_size:").trim()
            } else if (line.contains("boundary_check:") && line.contains("true", ignoreCase = true)) {
                isProtected = true
            }
        }

        if (currentName.isNotEmpty()) {
            val size = parseHexSize(currentLength)
            partitions.add(
                PartitionEntry(
                    index = currentIndex,
                    name = currentName,
                    linearStartHex = currentStart.ifEmpty { "0x0" },
                    boundaryLengthHex = currentLength.ifEmpty { "0x0" },
                    sizeBytes = if (size > 0) size else currentSize,
                    isProtected = isProtected || isSensitivePartition(currentName)
                )
            )
        }

        return partitions
    }

    private fun isSensitivePartition(name: String): Boolean {
        val lower = name.lowercase()
        return lower in listOf("nvram", "nvdata", "nvcfg", "protect1", "protect2", "proinfo", "seccfg", "preloader", "pgpt")
    }

    fun parseHexSize(hexStr: String): Long {
        if (hexStr.isEmpty()) return 0
        return try {
            val clean = hexStr.removePrefix("0x").removePrefix("0X").trim()
            clean.toLong(16)
        } catch (_: Exception) {
            0L
        }
    }

    fun calculateHexOffset(startHex: String, lengthHex: String): String {
        return try {
            val start = parseHexSize(startHex)
            val length = parseHexSize(lengthHex)
            val end = start + length
            "0x" + end.toString(16).uppercase(Locale.US)
        } catch (_: Exception) {
            "0x0"
        }
    }
}
