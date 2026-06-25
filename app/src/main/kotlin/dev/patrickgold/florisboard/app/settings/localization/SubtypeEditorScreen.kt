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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Observer
import androidx.lifecycle.compose.LocalLifecycleOwner
import dev.patrickgold.florisboard.R
import dev.patrickgold.florisboard.app.FlorisPreferenceStore
import dev.patrickgold.florisboard.app.LocalNavController
import dev.patrickgold.florisboard.app.Routes
import dev.patrickgold.florisboard.ime.core.DisplayLanguageNamesIn
import dev.patrickgold.florisboard.ime.core.Subtype
import dev.patrickgold.florisboard.ime.core.SubtypeJsonConfig
import dev.patrickgold.florisboard.ime.core.SubtypeLayoutMap
import dev.patrickgold.florisboard.ime.core.SubtypeNlpProviderMap
import dev.patrickgold.florisboard.ime.core.SubtypePreset
import dev.patrickgold.florisboard.ime.keyboard.LayoutArrangement
import dev.patrickgold.florisboard.ime.keyboard.LayoutArrangementComponent
import dev.patrickgold.florisboard.ime.keyboard.LayoutType
import dev.patrickgold.florisboard.ime.keyboard.extCorePopupMapping
import dev.patrickgold.florisboard.ime.nlp.han.HanShapeBasedLanguageProvider
import dev.patrickgold.florisboard.ime.nlp.latin.LatinLanguageProvider
import dev.patrickgold.florisboard.keyboardManager
import dev.patrickgold.florisboard.lib.FlorisLocale
import dev.patrickgold.florisboard.lib.compose.FlorisScreen
import dev.patrickgold.florisboard.lib.ext.ExtensionComponentName
import dev.patrickgold.florisboard.lib.io.DefaultJsonConfig
import dev.patrickgold.florisboard.lib.observeAsNonNullState
import dev.patrickgold.florisboard.subtypeManager
import dev.patrickgold.jetpref.datastore.model.observeAsState
import dev.patrickgold.jetpref.material.ui.JetPrefAlertDialog
import dev.patrickgold.jetpref.material.ui.JetPrefDropdown
import dev.patrickgold.jetpref.material.ui.JetPrefDropdownMenuDefaults
import dev.patrickgold.jetpref.material.ui.JetPrefListItem
import org.florisboard.lib.compose.FlorisButtonBar
import org.florisboard.lib.compose.FlorisDropdownLikeButton
import org.florisboard.lib.compose.florisScrollbar
import org.florisboard.lib.compose.stringRes


private val SelectComponentName = ExtensionComponentName("00", "00")
private val SelectNlpProviderId = SelectComponentName.toString()
private val SelectNlpProviders = SubtypeNlpProviderMap(
    spelling = SelectNlpProviderId,
)
private val SelectLayoutMap = SubtypeLayoutMap(
    characters = SelectComponentName,
    symbols = SelectComponentName,
    symbols2 = SelectComponentName,
    numeric = SelectComponentName,
    numericAdvanced = SelectComponentName,
    numericRow = SelectComponentName,
    phone = SelectComponentName,
    phone2 = SelectComponentName,
)
private val SelectLocale = FlorisLocale.from("00", "00")
private val SelectListKeys = listOf(SelectComponentName)

private class SubtypeEditorState(init: Subtype?) {
    companion object {
        val Saver = Saver<SubtypeEditorState, String>(
            save = { editor ->
                val subtype = Subtype(
                    id = editor.id.value,
                    primaryLocale = editor.primaryLocale.value,
                    secondaryLocales = editor.secondaryLocales.value,
                    nlpProviders = editor.nlpProviders.value,
                    composer = editor.composer.value,
                    currencySet = editor.currencySet.value,
                    punctuationRule = editor.punctuationRule.value,
                    popupMapping = editor.popupMapping.value,
                    layoutMap = editor.layoutMap.value,
                )
                SubtypeJsonConfig.encodeToString(subtype)
            },
            restore = { str ->
                val subtype = SubtypeJsonConfig.decodeFromString<Subtype>(str)
                SubtypeEditorState(subtype)
            },
        )
    }

    val id: MutableState<Long> = mutableLongStateOf(init?.id ?: -1)
    val primaryLocale: MutableState<FlorisLocale> = mutableStateOf(init?.primaryLocale ?: SelectLocale)
    val secondaryLocales: MutableState<List<FlorisLocale>> = mutableStateOf(init?.secondaryLocales ?: listOf())
    val nlpProviders: MutableState<SubtypeNlpProviderMap> = mutableStateOf(init?.nlpProviders ?: Subtype.DEFAULT.nlpProviders)
    val composer: MutableState<ExtensionComponentName> = mutableStateOf(init?.composer ?: SelectComponentName)
    val currencySet: MutableState<ExtensionComponentName> = mutableStateOf(init?.currencySet ?: SelectComponentName)
    val punctuationRule: MutableState<ExtensionComponentName> = mutableStateOf(init?.punctuationRule ?: Subtype.DEFAULT.punctuationRule)
    val popupMapping: MutableState<ExtensionComponentName> = mutableStateOf(init?.popupMapping ?: SelectComponentName)
    val layoutMap: MutableState<SubtypeLayoutMap> = mutableStateOf(init?.layoutMap ?: SelectLayoutMap)
    val subtypeChanged: MutableState<Boolean> = mutableStateOf(false)

    fun applySubtype(subtype: Subtype) {
        id.value = subtype.id
        primaryLocale.value = subtype.primaryLocale
        secondaryLocales.value = subtype.secondaryLocales
        composer.value = subtype.composer
        nlpProviders.value = subtype.nlpProviders
        currencySet.value = subtype.currencySet
        punctuationRule.value = subtype.punctuationRule
        popupMapping.value = subtype.popupMapping
        layoutMap.value = subtype.layoutMap
        subtypeChanged.value = true
    }

    fun toSubtype() = runCatching<Subtype> {
        check(primaryLocale.value != SelectLocale)
        check(nlpProviders.value.spelling != SelectNlpProviderId)
        check(nlpProviders.value.suggestion != SelectNlpProviderId)
        check(composer.value != SelectComponentName)
        check(currencySet.value != SelectComponentName)
        check(punctuationRule.value != SelectComponentName)
        check(popupMapping.value != SelectComponentName)
        check(layoutMap.value.characters != SelectComponentName)
        check(layoutMap.value.symbols != SelectComponentName)
        check(layoutMap.value.symbols2 != SelectComponentName)
        check(layoutMap.value.numeric != SelectComponentName)
        check(layoutMap.value.numericAdvanced != SelectComponentName)
        check(layoutMap.value.numericRow != SelectComponentName)
        check(layoutMap.value.phone != SelectComponentName)
        check(layoutMap.value.phone2 != SelectComponentName)
        Subtype(
            id.value, primaryLocale.value, secondaryLocales.value, nlpProviders.value, composer.value,
            currencySet.value, punctuationRule.value, popupMapping.value, layoutMap.value,
        )
    }
}

@Composable
fun SubtypeEditorScreen(id: Long?) = FlorisScreen {
    title = stringRes(if (id == null) {
        R.string.settings__localization__subtype_add_title
    } else {
        R.string.settings__localization__subtype_edit_title
    })

    val selectValue = stringRes(R.string.settings__localization__subtype_select_placeholder)
    val selectListValues = remember(selectValue) { listOf(selectValue) }

    val prefs by FlorisPreferenceStore
    val navController = LocalNavController.current
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val keyboardManager by context.keyboardManager()
    val subtypeManager by context.subtypeManager()

    val displayLanguageNamesIn by prefs.localization.displayLanguageNamesIn.observeAsState()
    val composers by keyboardManager.resources.composers.observeAsNonNullState()
    val currencySets by keyboardManager.resources.currencySets.observeAsNonNullState()
    val layoutExtensions by keyboardManager.resources.layouts.observeAsNonNullState()
    val popupMappings by keyboardManager.resources.popupMappings.observeAsNonNullState()
    val subtypePresets by keyboardManager.resources.subtypePresets.observeAsNonNullState()

    val subtypeEditor = rememberSaveable(saver = SubtypeEditorState.Saver) {
        val subtype = id?.let { subtypeManager.getSubtypeById(id) }
        SubtypeEditorState(subtype)
    }
    var primaryLocale by subtypeEditor.primaryLocale
    //var secondaryLocales by subtypeEditor.secondaryLocales
    var composer by subtypeEditor.composer
    var currencySet by subtypeEditor.currencySet
    var popupMapping by subtypeEditor.popupMapping
    var layoutMap by subtypeEditor.layoutMap
    var nlpProviders by subtypeEditor.nlpProviders
    var subtypeChanged by subtypeEditor.subtypeChanged

    var showSubtypePresetsDialog by rememberSaveable { mutableStateOf(id == null) }
    var showSelectAsError by rememberSaveable { mutableStateOf(false) }
    var errorDialogStrId by rememberSaveable { mutableStateOf<Int?>(null) }

    val selectLocaleScreenResult = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getLiveData<String>(SelectLocaleScreenResultLanguageTag)
    DisposableEffect(selectLocaleScreenResult, lifecycleOwner) {
        val observer = Observer<String> { languageTag ->
            val locale = FlorisLocale.fromTag(languageTag)
            primaryLocale = locale
            val preset = subtypeManager.getSubtypePresetForLocale(locale)
            popupMapping = preset?.popupMapping ?: extCorePopupMapping("default")
        }
        selectLocaleScreenResult?.observe(lifecycleOwner, observer)
        onDispose { selectLocaleScreenResult?.removeObserver(observer) }
    }


    @Composable
    fun SubtypePropertyDropdown(
        title: String,
        layoutType: LayoutType,
    ) {
        SubtypeProperty(title) {
            SubtypeLayoutDropdown(
                layoutType = layoutType,
                layouts = layoutExtensions[layoutType] ?: mapOf(),
                showSelectAsError = showSelectAsError,
                layoutMap = layoutMap,
                onLayoutMapChanged = { layoutMap = it },
                selectListValues = selectListValues,
            )
        }
    }

    actions {
        if (id != null) {
            IconButton(onClick = {
                val subtype = subtypeManager.getSubtypeById(id)
                if (subtype != null) {
                    subtypeManager.removeSubtype(subtype)
                    navController.popBackStack()
                }
            }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                )
            }
        }
    }

    bottomBar {
        FlorisButtonBar {
            ButtonBarSpacer()
            ButtonBarTextButton(
                modifier = Modifier.padding(8.dp),
                text = stringRes(R.string.action__cancel)
            ) {
                navController.popBackStack()
            }
            ButtonBarButton(
                modifier = Modifier.padding(8.dp),
                text = stringRes(R.string.action__save)
            ) {
                subtypeEditor.toSubtype().onSuccess { subtype ->
                    if (id == null) {
                        if (!subtypeManager.addSubtype(subtype)) {
                            errorDialogStrId = R.string.settings__localization__subtype_error_already_exists
                            return@ButtonBarButton
                        }
                    } else {
                        subtypeManager.modifySubtypeWithSameId(subtype)
                    }
                    navController.popBackStack()
                }.onFailure {
                    showSelectAsError = true
                    errorDialogStrId = R.string.settings__localization__subtype_error_fields_no_value
                }
            }
        }
    }

    content {
        val showAdvanced = rememberSaveable { mutableStateOf(false) }
        Column(modifier = Modifier.padding(8.dp)) {
            // ── Suggested presets (only when ADDING, not editing) ──
            if (id == null && !subtypeChanged) {
                Card(modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .fillMaxWidth()
                    ) {
                        /*Text(
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                            text = stringRes(R.string.settings__localization__suggested_subtype_presets),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        val systemLocales = remember {
                            val list = mutableListOf<FlorisLocale>()
                            val localeList = configuration.locales
                            for (n in 0 until localeList.size()) {
                                list.add(FlorisLocale.from(localeList.get(n)))
                            }
                            list
                        }*/
                        /*val suggestedPresets = remember(subtypePresets) {
                            val presets = mutableListOf<SubtypePreset>()
                            for (systemLocale in systemLocales) {
                                subtypePresets.find { it.locale == systemLocale }?.let { presets.add(it) }
                            }
                            presets
                        }*/
                        /*if (suggestedPresets.isNotEmpty()) {
                            for (suggestedPreset in suggestedPresets) {
                                JetPrefListItem(
                                    modifier = Modifier.clickable {
                                        subtypeEditor.applySubtype(suggestedPreset.toSubtype())
                                    },
                                    text = suggestedPreset.displayLabel?.split("\n")?.get(0)
                                        ?: when (displayLanguageNamesIn) {
                                            DisplayLanguageNamesIn.SYSTEM_LOCALE -> suggestedPreset.locale.displayName()
                                            DisplayLanguageNamesIn.NATIVE_LOCALE -> suggestedPreset.locale.displayName(suggestedPreset.locale)
                                        },
                                    secondaryText = if (suggestedPreset.displayLabel != null) {
                                        suggestedPreset.displayLabel.split("\n").drop(1).joinToString(" • ")
                                    } else {
                                        suggestedPreset.preferred.characters.componentId
                                    },
                                    colors = ListItemDefaults.colors(containerColor = CardDefaults.cardColors().containerColor),
                                )
                            }
                        } else {
                            Text(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                                text = stringRes(R.string.settings__localization__suggested_subtype_presets_none_found),
                            )
                        }*/
                        Button(
                            modifier = Modifier
                                .padding(32.dp)
                                .align(Alignment.CenterHorizontally),
                            onClick = { showSubtypePresetsDialog = true },
                        ) {
                            Text(
                                text = stringRes(R.string.settings__localization__subtype_presets_view_all)
                            )
                        }
                    }
                }
            }

            if (id != null || subtypeChanged) {
                val selectedPreset = remember(primaryLocale, subtypePresets) {
                    subtypePresets.find { it.locale == primaryLocale }
                }
                if (selectedPreset != null) {
                    SubtypePresetHeader(preset = selectedPreset)
                }
                KeyboardPreviewCard(
                    layoutMap = layoutMap,
                    layoutExtensions = layoutExtensions,
                    context = context,
                )

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    onClick = { showAdvanced.value = !showAdvanced.value },
                ) {
                    Text(
                        text = if (showAdvanced.value) {
                            "▲ ${stringRes(R.string.settings__localization__subtype_advanced_hide)}"
                        } else {
                            "▼ ${stringRes(R.string.settings__localization__subtype_advanced_show)}"
                        }
                    )
                }

                // ── Advanced configuration fields (hidden by default) ──
                if (showAdvanced.value) {
                    SubtypeProperty(stringRes(R.string.settings__localization__subtype_locale)) {
                        // Show displayLabel for the selected locale if available
                        val selectedPreset = remember(primaryLocale, subtypePresets) {
                            subtypePresets.find { it.locale == primaryLocale }
                        }
                        val localeDisplay = if (primaryLocale == SelectLocale) {
                            selectValue
                        } else {
                            selectedPreset?.displayLabel?.split("\n")?.get(0)
                                ?: when (displayLanguageNamesIn) {
                                    DisplayLanguageNamesIn.SYSTEM_LOCALE -> primaryLocale.displayName()
                                    DisplayLanguageNamesIn.NATIVE_LOCALE -> primaryLocale.displayName(primaryLocale)
                                }
                        }
                        FlorisDropdownLikeButton(
                            item = localeDisplay,
                            isError = showSelectAsError && primaryLocale == SelectLocale,
                            onClick = {
                                navController.navigate(Routes.Settings.SelectLocale)
                            },
                            appearance = JetPrefDropdownMenuDefaults.outlined(shape = ShapeDefaults.Small),
                        )
                    }
                    SubtypeProperty(stringRes(R.string.settings__localization__subtype_popup_mapping)) {
                        val popupMappingIds = remember(popupMappings) {
                            SelectListKeys + popupMappings.keys
                        }
                        val popupMappingLabels = remember(popupMappings) {
                            selectListValues + popupMappings.values.map { it.label }
                        }
                        val expanded = remember { mutableStateOf(false) }
                        val selectedIndex = popupMappingIds.indexOf(popupMapping).coerceAtLeast(0)
                        JetPrefDropdown(
                            options = popupMappingLabels,
                            expanded = expanded,
                            selectedOptionIndex = selectedIndex,
                            isError = showSelectAsError && selectedIndex == 0,
                            onSelectOption = { popupMapping = popupMappingIds[it] },
                            appearance = JetPrefDropdownMenuDefaults.outlined(shape = ShapeDefaults.Small),
                        )
                    }
                    SubtypePropertyDropdown(stringRes(R.string.settings__localization__subtype_characters_layout), LayoutType.CHARACTERS)

                    SubtypeGroupSpacer()

                    SubtypeProperty(stringRes(R.string.settings__localization__subtype_suggestion_provider)) {
                        val nlpProviderMappings = mapOf(
                            LatinLanguageProvider.ProviderId to "Latin",
                            HanShapeBasedLanguageProvider.ProviderId to "Chinese shape-based"
                        )

                        val nlpProviderMappingIds = remember(nlpProviderMappings) {
                            listOf(SelectNlpProviderId) + nlpProviderMappings.keys
                        }
                        val nlpProviderMappingLabels = remember(nlpProviderMappings) {
                            selectListValues + nlpProviderMappings.values.map { it }
                        }
                        val expanded = remember { mutableStateOf(false) }
                        val selectedIndex = nlpProviderMappingIds.indexOf(nlpProviders.suggestion).coerceAtLeast(0)
                        JetPrefDropdown(
                            options = nlpProviderMappingLabels,
                            expanded = expanded,
                            selectedOptionIndex = selectedIndex,
                            isError = showSelectAsError && selectedIndex == 0,
                            onSelectOption = { nlpProviders = SubtypeNlpProviderMap(
                                suggestion = nlpProviderMappingIds[it],
                                spelling = nlpProviderMappingIds[it]
                            ) },
                            appearance = JetPrefDropdownMenuDefaults.outlined(shape = ShapeDefaults.Small),
                        )
                    }

                    SubtypeGroupSpacer()

                    SubtypePropertyDropdown(stringRes(R.string.settings__localization__subtype_symbols_layout), LayoutType.SYMBOLS)
                    SubtypePropertyDropdown(stringRes(R.string.settings__localization__subtype_symbols2_layout), LayoutType.SYMBOLS2)

                    SubtypeProperty(stringRes(R.string.settings__localization__subtype_composer)) {
                        val composerIds = remember(composers) {
                            SelectListKeys + composers.keys
                        }
                        val composerNames = remember(composers) {
                            selectListValues + composers.values.map { it.label }
                        }
                        val expanded = remember { mutableStateOf(false) }
                        JetPrefDropdown(
                            options = composerNames,
                            expanded = expanded,
                            selectedOptionIndex = composerIds.indexOf(composer).coerceAtLeast(0),
                            isError = showSelectAsError && composer == SelectComponentName,
                            onSelectOption = { composer = composerIds[it] },
                            appearance = JetPrefDropdownMenuDefaults.outlined(shape = ShapeDefaults.Small),
                        )
                    }
                    SubtypeProperty(stringRes(R.string.settings__localization__subtype_currency_set)) {
                        val currencySetIds = remember(currencySets) {
                            SelectListKeys + currencySets.keys
                        }
                        val currencySetNames = remember(currencySets) {
                            selectListValues + currencySets.values.map { it.label }
                        }
                        val expanded = remember { mutableStateOf(false) }
                        JetPrefDropdown(
                            options = currencySetNames,
                            expanded = expanded,
                            selectedOptionIndex = currencySetIds.indexOf(currencySet).coerceAtLeast(0),
                            isError = showSelectAsError && currencySet == SelectComponentName,
                            onSelectOption = { currencySet = currencySetIds[it] },
                            appearance = JetPrefDropdownMenuDefaults.outlined(shape = ShapeDefaults.Small),
                        )
                    }

                    SubtypeGroupSpacer()

                    SubtypePropertyDropdown(stringRes(R.string.settings__localization__subtype_numeric_layout), LayoutType.NUMERIC)

                    SubtypePropertyDropdown(stringRes(R.string.settings__localization__subtype_numeric_advanced_layout), LayoutType.NUMERIC_ADVANCED)

                    SubtypePropertyDropdown(stringRes(R.string.settings__localization__subtype_numeric_row_layout), LayoutType.NUMERIC_ROW)

                    SubtypeGroupSpacer()

                    SubtypePropertyDropdown(stringRes(R.string.settings__localization__subtype_phone_layout), LayoutType.PHONE)

                    SubtypePropertyDropdown(stringRes(R.string.settings__localization__subtype_phone2_layout), LayoutType.PHONE2)
                }

                Button(
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .align(Alignment.Start),
                    onClick = { showSubtypePresetsDialog = true },
                ) {
                    Text(
                        text = stringRes(R.string.settings__localization__change_subtype)
                    )
                }
            }
        }

        if (showSubtypePresetsDialog) {
            var searchQuery by rememberSaveable(stateSaver = Saver(
                save = { it.text },
                restore = { TextFieldValue(it) },
            )) { mutableStateOf(TextFieldValue()) }

            val filteredPresets = remember(subtypePresets, searchQuery) {
                val query = searchQuery.text.trim().lowercase()
                if (query.isBlank()) {
                    subtypePresets
                } else {
                    subtypePresets.filter { preset ->
                        val label = preset.displayLabel?.lowercase() ?: ""
                        label.contains(query) ||
                            preset.locale.languageTag().lowercase().contains(query) ||
                            preset.preferred.characters.componentId.lowercase().contains(query)
                    }
                }
            }

            JetPrefAlertDialog(
                title = stringRes(R.string.settings__localization__subtype_presets),
                dismissLabel = stringRes(android.R.string.cancel),
                scrollModifier = Modifier,
                properties = DialogProperties(
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true,
                ),
                contentPadding = PaddingValues(horizontal = 8.dp),
                onDismiss = {
                    showSubtypePresetsDialog = false
                },
            ) {
                Column {
                    TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 4.dp),
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(stringRes(R.string.settings__localization__subtype_search_locale_placeholder))
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                            )
                        },
                        singleLine = true,
                        shape = RectangleShape,
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                        ),
                    )
                    HorizontalDivider()
                    if (filteredPresets.isEmpty()) {
                        Text(
                            modifier = Modifier.padding(16.dp),
                            text = stringRes(R.string.settings__localization__subtype_search_locale_not_found, "search_term" to searchQuery.text),
                            color = LocalContentColor.current.copy(alpha = 0.54f),
                        )
                    }
                    val lazyListState = rememberLazyListState()
                    LazyColumn(
                        modifier = Modifier
                            .florisScrollbar(lazyListState, isVertical = true)
                            .weight(1f),
                        state = lazyListState,
                    ) {
                        items(filteredPresets) { subtypePreset ->
                            SubtypePresetListItem(
                                preset = subtypePreset,
                                modifier = Modifier.clickable {
                                    subtypeEditor.applySubtype(subtypePreset.toSubtype())
                                    showSubtypePresetsDialog = false
                                },
                                onClick = {
                                    subtypeEditor.applySubtype(subtypePreset.toSubtype())
                                    showSubtypePresetsDialog = false
                                },
                            )
                        }
                    }
                    HorizontalDivider()
                }
            }
        }

        errorDialogStrId?.let { strId ->
            JetPrefAlertDialog(
                title = stringRes(R.string.error__title),
                confirmLabel = stringRes(android.R.string.ok),
                onConfirm = {
                    errorDialogStrId = null
                },
                onDismiss = {
                    errorDialogStrId = null
                },
            ) {
                Text(text = stringRes(strId))
            }
        }
    }
}

@Composable
private fun SubtypeProperty(text: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp)) {
        Text(
            modifier = Modifier.padding(bottom = 8.dp),
            text = text,
            style = MaterialTheme.typography.titleSmall,
        )
        content()
    }
}

@Composable
private fun SubtypeLayoutDropdown(
    layoutType: LayoutType,
    layouts: Map<ExtensionComponentName, LayoutArrangementComponent>,
    showSelectAsError: Boolean,
    layoutMap: SubtypeLayoutMap,
    onLayoutMapChanged: (SubtypeLayoutMap) -> Unit,
    selectListValues: List<String>,
) {
    val layoutIds = remember(layouts) { SelectListKeys + layouts.keys.toList() }
    val layoutLabels = remember(layouts) { selectListValues + layouts.values.map { it.label } }
    val layoutId = remember(layoutMap) { layoutMap[layoutType] }
    val expanded = remember { mutableStateOf(false) }
    val selectedIndex = layoutIds.indexOf(layoutId).coerceAtLeast(0)
    JetPrefDropdown(
        options = layoutLabels,
        expanded = expanded,
        selectedOptionIndex = selectedIndex,
        isError = showSelectAsError && selectedIndex == 0,
        onSelectOption = { onLayoutMapChanged(layoutMap.copy(layoutType = layoutType, componentName = layoutIds[it])!!) },
        appearance = JetPrefDropdownMenuDefaults.outlined(shape = ShapeDefaults.Small),
    )
}

@Composable
private fun SubtypeGroupSpacer() {
    Spacer(modifier = Modifier
        .fillMaxWidth()
        .height(32.dp))
}

/**
 * Keyboard preview card that loads the actual layout JSON and displays key rows.
 */
@Composable
private fun KeyboardPreviewCard(
    layoutMap: SubtypeLayoutMap,
    layoutExtensions: Map<LayoutType, Map<ExtensionComponentName, LayoutArrangementComponent>>,
    context: android.content.Context,
) {
    val charactersId = layoutMap.characters
    val charactersLayout = remember(layoutMap, layoutExtensions) {
        layoutExtensions[LayoutType.CHARACTERS]?.get(charactersId)
    }

    // Load the layout arrangement from the JSON file
    @Suppress("UNCHECKED_CAST")
    val arrangement: List<List<String>>? = remember(charactersId, charactersLayout) {
        if (charactersId == SelectComponentName || charactersLayout == null) {
            null
        } else {
            try {
                val extId = charactersId.extensionId
                val layoutId = charactersId.componentId
                val assetPath = "ime/keyboard/$extId/layouts/characters/$layoutId.json"
                val jsonStr = context.assets.open(assetPath).bufferedReader().use { it.readText() }
                val rows: LayoutArrangement = DefaultJsonConfig.decodeFromString(jsonStr)
                val result = mutableListOf<MutableList<String>>()
                for (row in rows) {
                    val rowLabels = mutableListOf<String>()
                    for (keyData in row) {
                        try {
                            val label = (keyData as? dev.patrickgold.florisboard.ime.keyboard.KeyData)?.label
                            rowLabels.add(if (!label.isNullOrBlank()) label else "·")
                        } catch (_: Exception) {
                            rowLabels.add("·")
                        }
                    }
                    result.add(rowLabels)
                }
                result
            } catch (_: Exception) {
                null
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringRes(R.string.settings__localization__keyboard_preview_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            if (charactersLayout != null && arrangement != null) {
                Spacer(modifier = Modifier.height(8.dp))
                for ((_, row) in arrangement.withIndex()) {
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly,
                    ) {
                        for (label in row) {
                            Text(
                                text = label.ifBlank { "·" },
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (label.isBlank()) {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${charactersLayout.label} (${charactersId.componentId})",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                )
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringRes(R.string.settings__localization__subtype_select_placeholder),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
