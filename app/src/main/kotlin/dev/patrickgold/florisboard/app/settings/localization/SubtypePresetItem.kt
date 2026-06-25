/*
 * Copyright (C) 2021-2025 Tachiwin Tutunakú
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.patrickgold.florisboard.app.settings.localization

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.patrickgold.florisboard.ime.core.SubtypePreset

/**
 * Parse the displayLabel (5 lines: autonym\\nname\\ncode\\nfamily\\nsuperlanguage)
 * into its components.
 */
private fun parseDisplayLabel(label: String?): LabelParts {
    if (label == null) return LabelParts()
    val lines = label.split("\n")
    return LabelParts(
        autonym = lines.getOrNull(0) ?: "",
        name = lines.getOrNull(1) ?: "",
        code = lines.getOrNull(2) ?: "",
        family = lines.getOrNull(3) ?: "",
        superlanguage = lines.getOrNull(4) ?: "",
    )
}

private data class LabelParts(
    val autonym: String = "",
    val name: String = "",
    val code: String = "",
    val family: String = "",
    val superlanguage: String = "",
)

/**
 * Chip colors per linguistic family — distinctive tones.
 */
private val familyColors = mapOf(
    "otomangue" to Color(0xFF2E7D32),      // green
    "yuto-nahua" to Color(0xFF1565C0),      // blue
    "mayense" to Color(0xFF6A1B9A),         // purple
    "totonaco-tepehua" to Color(0xFFE65100), // orange
    "mixe-zoque" to Color(0xFF00838F),      // teal
    "tarasca" to Color(0xFFC62828),         // red
    "algonquino" to Color(0xFF4E342E),      // brown
)

private fun chipColor(label: String): Color {
    val key = label.lowercase().trim()
    return familyColors.entries.firstOrNull { key.contains(it.key) }?.value
        ?: Color(0xFF546E7A) // blue-grey fallback
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SubtypePresetListItem(
    preset: SubtypePreset,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
) {
    val parts = parseDisplayLabel(preset.displayLabel)

    if (onLongClick != null) {
        @OptIn(ExperimentalFoundationApi::class)
        Box(
            modifier = modifier.combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            )
        ) {
            SubtypePresetContent(parts = parts, compact = true)
        }
    } else {
        Box(
            modifier = modifier.clickable(onClick = onClick)
        ) {
            SubtypePresetContent(parts = parts, compact = true)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SubtypePresetHeader(
    preset: SubtypePreset?,
    modifier: Modifier = Modifier,
) {
    val parts = parseDisplayLabel(preset?.displayLabel)
    SubtypePresetContent(parts = parts, compact = false, modifier = modifier)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SubtypePresetContent(
    parts: LabelParts,
    compact: Boolean,
    modifier: Modifier = Modifier,
) {
    val titleSize = if (compact) 16.sp else 22.sp
    val subtitleSize = if (compact) 13.sp else 16.sp
    val chipSize = if (compact) 11.sp else 13.sp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = if (compact) 10.dp else 14.dp),
    ) {
        // Row 1: Autonym
        Text(
            text = parts.autonym.ifBlank { parts.name.ifBlank { parts.code } },
            fontSize = titleSize,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Row 2: Code (outline/gray) + Name
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = parts.code,
                fontSize = subtitleSize,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                fontWeight = FontWeight.Medium,
            )
            if (parts.name.isNotBlank() && parts.name != parts.autonym) {
                Text(
                    text = parts.name,
                    fontSize = subtitleSize,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // Row 3: Chips for superlanguage and family
        val chips = buildList {
            if (parts.superlanguage.isNotBlank()) add(parts.superlanguage)
            if (parts.family.isNotBlank()) add(parts.family)
        }
        if (chips.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                for (chip in chips) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = chipColor(chip).copy(alpha = 0.15f),
                        tonalElevation = 0.dp,
                    ) {
                        Text(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            text = chip,
                            fontSize = chipSize,
                            color = chipColor(chip),
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
        }
    }
}
