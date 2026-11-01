package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.CaseDossier
import com.example.data.model.CaseStage
import com.example.data.model.CityJurisdiction
import com.example.data.model.MediaAttachmentType
import com.example.data.model.SensitivityLevel
import com.example.data.model.StateJurisdiction
import com.example.data.model.TimelineUpdateItem
import com.example.data.model.VerificationStatus
import com.example.ui.theme.SuccessIndicator
import com.example.ui.theme.WarningIndicator

/**
 * Compact location pill ("All India ▾" or "State ▸ City ▾") for the Home and Track top bars.
 */
@Composable
fun CompactLocationSelectorPill(
    selectedState: String,
    selectedDistrict: String,
    selectedCity: String,
    onClick: () -> Unit,
    onReset: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val allIndiaLabel = stringResource(R.string.jurisdiction_all_india)
    val isFiltered = selectedState != "All India"
    val labelText = when {
        selectedState == "All India" -> allIndiaLabel
        selectedDistrict == "All Districts" -> selectedState
        selectedCity == "All Cities" -> "$selectedState ▸ $selectedDistrict"
        else -> "$selectedCity, $selectedState"
    }

    Surface(
        color = if (isFiltered) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        shape = RoundedCornerShape(24.dp),
        modifier = modifier
            .heightIn(min = 40.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .testTag("compact_location_selector_pill")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = stringResource(R.string.cd_select_location),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = labelText,
                style = MaterialTheme.typography.labelLarge,
                color = if (isFiltered) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            if (isFiltered && onReset != null) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = stringResource(R.string.cd_clear_jurisdiction),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { onReset() }
                        .testTag("reset_location_filter_button")
                )
            }
        }
    }
}

/**
 * Bottom sheet for cascading State/UT -> District -> City selection with search, Apply, and Reset.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LocationFilterBottomSheet(
    states: List<StateJurisdiction>,
    initialState: String,
    initialDistrict: String,
    initialCity: String,
    onDismiss: () -> Unit,
    onApply: (state: String, district: String, city: String) -> Unit,
    onReset: () -> Unit
) {
    var draftState by remember(initialState) { mutableStateOf(initialState) }
    var draftDistrict by remember(initialDistrict) { mutableStateOf(initialDistrict) }
    var draftCity by remember(initialCity) { mutableStateOf(initialCity) }
    var locationSearchQuery by remember { mutableStateOf("") }

    val selectedStateObj = remember(states, draftState) {
        states.find { it.stateName.equals(draftState, ignoreCase = true) }
    }
    val availableDistricts = remember(selectedStateObj) {
        selectedStateObj?.districts.orEmpty()
    }
    val selectedDistrictObj = remember(availableDistricts, draftDistrict) {
        availableDistricts.find { it.districtName.equals(draftDistrict, ignoreCase = true) }
    }
    val availableCities = remember(selectedDistrictObj) {
        selectedDistrictObj?.cities.orEmpty()
    }
    val matchedCityRep = remember(availableCities, draftCity) {
        availableCities.find { it.cityName.equals(draftCity, ignoreCase = true) }
    }

    val filteredStates = remember(states, locationSearchQuery) {
        if (locationSearchQuery.isBlank()) {
            states
        } else {
            states.filter { st ->
                st.stateName.contains(locationSearchQuery, ignoreCase = true) ||
                    st.districts.any { d ->
                        d.districtName.contains(locationSearchQuery, ignoreCase = true) ||
                            d.cities.any { c -> c.cityName.contains(locationSearchQuery, ignoreCase = true) }
                    }
            }
        }
    }

    val allIndiaLabel = stringResource(R.string.jurisdiction_all_india)
    val allDistrictsLabel = stringResource(R.string.jurisdiction_all_districts)
    val allCitiesLabel = stringResource(R.string.jurisdiction_all_cities)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("location_filter_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.location_sheet_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.location_sheet_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TextButton(
                    onClick = {
                        draftState = "All India"
                        draftDistrict = "All Districts"
                        draftCity = "All Cities"
                        locationSearchQuery = ""
                        onReset()
                    },
                    modifier = Modifier.testTag("sheet_reset_location_button")
                ) {
                    Text(stringResource(R.string.action_reset))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = locationSearchQuery,
                onValueChange = { locationSearchQuery = it },
                placeholder = { Text(stringResource(R.string.location_sheet_search_placeholder)) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = stringResource(R.string.cd_search_locations)
                    )
                },
                trailingIcon = {
                    if (locationSearchQuery.isNotBlank()) {
                        IconButton(onClick = { locationSearchQuery = "" }) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = stringResource(R.string.cd_clear_search)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("location_sheet_search_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 1. State / UT Selector
            CascadingLocationDropdownField(
                label = stringResource(R.string.location_field_state_ut),
                selectedValue = if (draftState == "All India") allIndiaLabel else draftState,
                options = listOf("All India") + filteredStates.map { it.stateName },
                enabled = true,
                placeholderText = allIndiaLabel,
                testTagName = "dropdown_state_selector",
                onOptionSelected = { newState ->
                    draftState = newState
                    draftDistrict = "All Districts"
                    draftCity = "All Cities"
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 2. District Selector
            CascadingLocationDropdownField(
                label = stringResource(R.string.location_field_district),
                selectedValue = if (draftDistrict == "All Districts") allDistrictsLabel else draftDistrict,
                options = listOf("All Districts") + availableDistricts.map { it.districtName },
                enabled = draftState != "All India" && availableDistricts.isNotEmpty(),
                placeholderText = stringResource(R.string.location_placeholder_select_state_first),
                testTagName = "dropdown_district_selector",
                onOptionSelected = { newDistrict ->
                    draftDistrict = newDistrict
                    draftCity = "All Cities"
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 3. City / Town Selector
            CascadingLocationDropdownField(
                label = stringResource(R.string.location_field_city),
                selectedValue = if (draftCity == "All Cities") allCitiesLabel else draftCity,
                options = listOf("All Cities") + availableCities.map { it.cityName },
                enabled = draftState != "All India" && draftDistrict != "All Districts" && availableCities.isNotEmpty(),
                placeholderText = stringResource(R.string.location_placeholder_select_district_first),
                testTagName = "dropdown_city_selector",
                onOptionSelected = { newCity ->
                    draftCity = newCity
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Quick State Chips for rapid one-tap selection
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = stringResource(R.string.location_quick_select_state),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = draftState == "All India",
                    onClick = {
                        draftState = "All India"
                        draftDistrict = "All Districts"
                        draftCity = "All Cities"
                    },
                    label = { Text(allIndiaLabel) }
                )
                filteredStates.forEach { st ->
                    FilterChip(
                        selected = draftState.equals(st.stateName, ignoreCase = true),
                        onClick = {
                            draftState = st.stateName
                            draftDistrict = "All Districts"
                            draftCity = "All Cities"
                        },
                        label = { Text(st.stateName) }
                    )
                }
            }

            // Optional Compact Constituency Preview when a specific city is selected
            if (matchedCityRep != null) {
                Spacer(modifier = Modifier.height(14.dp))
                CompactRepresentativeCard(
                    cityRep = matchedCityRep,
                    stateName = draftState,
                    districtName = draftDistrict
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        draftState = "All India"
                        draftDistrict = "All Districts"
                        draftCity = "All Cities"
                        onReset()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(stringResource(R.string.action_reset_location_filters), maxLines = 1)
                }
                Button(
                    onClick = {
                        onApply(draftState, draftDistrict, draftCity)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("apply_location_filter_button"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(stringResource(R.string.action_apply_filters), maxLines = 1)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CascadingLocationDropdownField(
    label: String,
    selectedValue: String,
    options: List<String>,
    enabled: Boolean,
    placeholderText: String,
    testTagName: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded && enabled,
        onExpandedChange = {
            if (enabled) {
                expanded = !expanded
            }
        },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = if (enabled) selectedValue else placeholderText,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            singleLine = true,
            label = {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded && enabled)
            },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            shape = RoundedCornerShape(10.dp),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (enabled) FontWeight.SemiBold else FontWeight.Normal
            ),
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
                .testTag(testTagName)
        )

        ExposedDropdownMenu(
            expanded = expanded && enabled,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                val isSelected = option == selectedValue
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    modifier = Modifier.testTag("${testTagName}_option_${option.replace(" ", "_")}")
                )
            }
        }
    }
}

/**
 * Compact Constituency Representative summary card with source attribution and neutral editorial disclaimer.
 */
@Composable
fun CompactRepresentativeCard(
    cityRep: CityJurisdiction,
    stateName: String,
    districtName: String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("constituency_accountability_banner")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = stringResource(R.string.cd_constituency_info),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Column {
                        Text(
                            text = stringResource(R.string.rep_card_title_format, cityRep.cityName),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(
                                R.string.rep_card_mla_mp_compact,
                                cityRep.mlaName,
                                cityRep.mlaParty,
                                cityRep.mpName,
                                cityRep.mpParty
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) {
                        stringResource(R.string.cd_collapse_details)
                    } else {
                        stringResource(R.string.cd_expand_details)
                    },
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = stringResource(
                            R.string.rep_card_assembly_format,
                            cityRep.mlaName,
                            cityRep.mlaConstituency,
                            cityRep.mlaParty
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(
                            R.string.rep_card_loksabha_format,
                            cityRep.mpName,
                            cityRep.mpConstituency,
                            cityRep.mpParty
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(
                            R.string.rep_card_police_format,
                            cityRep.policeRange,
                            districtName,
                            stateName
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.rep_card_source_disclaimer),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Clean, modern Editorial News Card for the Home Screen.
 * Supports "Translated automatically" badge and "View original" toggle when machine-translated.
 */
@Composable
fun EditorialNewsCard(
    dossier: CaseDossier,
    isBlurred: Boolean,
    onRevealBlur: () -> Unit,
    onOpenDetail: () -> Unit,
    onToggleSave: () -> Unit,
    onToggleFollow: () -> Unit,
    isFeatured: Boolean = false,
    modifier: Modifier = Modifier
) {
    val latestUpdate = dossier.timelineUpdates.lastOrNull()
    val hasVerifiedFollowUp = dossier.timelineUpdates.size > 1
    var showOriginalText by remember(dossier.id) { mutableStateOf(false) }

    val displayTitle = if (showOriginalText && dossier.originalTitle.isNotBlank()) {
        dossier.originalTitle
    } else {
        dossier.title
    }
    val displaySummary = if (showOriginalText && dossier.originalSummary.isNotBlank()) {
        dossier.originalSummary
    } else {
        dossier.summary
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .border(
                width = 1.dp,
                color = if (isFeatured) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                } else {
                    MaterialTheme.colorScheme.outlineVariant
                },
                shape = RoundedCornerShape(14.dp)
            )
            .clip(RoundedCornerShape(14.dp))
            .clickable { onOpenDetail() }
            .testTag("dossier_card_${dossier.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 1. Category Label + Verification Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    if (isFeatured) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.card_featured_badge),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = dossier.crimeCategory,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                VerificationBadgePill(status = dossier.verificationStatus)
            }

            if (dossier.isMachineTranslated) {
                Spacer(modifier = Modifier.height(6.dp))
                MachineTranslationBannerRow(
                    showingOriginal = showOriginalText,
                    onToggleOriginal = { showOriginalText = !showOriginalText },
                    testTagPrefix = "news_card_${dossier.id}"
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Headline & 3. Factual Summary (with optional sensitivity advisory blur)
            if (isBlurred && dossier.sensitivityLevel != SensitivityLevel.STANDARD) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = stringResource(R.string.cd_content_advisory),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(dossier.sensitivityLevel.labelRes),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = displayTitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .blur(8.dp)
                                .alpha(0.4f),
                            maxLines = 2
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = onRevealBlur,
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("reveal_blur_${dossier.id}"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = stringResource(R.string.cd_show_report),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.card_show_report), maxLines = 1)
                        }
                    }
                }
            } else {
                if (dossier.imageUrl.isNotBlank() || dossier.videoUrl.isNotBlank() || dossier.mediaAttachments.isNotEmpty()) {
                    IncidentMediaPreviewBanner(
                        dossier = dossier,
                        isFeatured = isFeatured
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
                Text(
                    text = displayTitle,
                    style = if (isFeatured) {
                        MaterialTheme.typography.headlineMedium
                    } else {
                        MaterialTheme.typography.titleLarge
                    },
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = displaySummary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = if (isFeatured) 4 else 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // 8. Indication when a tracked case has a verified update
            if (hasVerifiedFollowUp && latestUpdate != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timeline,
                            contentDescription = stringResource(R.string.cd_case_update_available),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(
                                    R.string.card_latest_update_format,
                                    latestUpdate.dateLabel,
                                    stringResource(dossier.caseStage.labelRes)
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = latestUpdate.headline,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(8.dp))

            // 4. Location, 5. Publisher/Source, 6. Publication Date + 7. Bookmark & Follow Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${dossier.cityName}, ${dossier.stateName}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${dossier.sourceName} • ${dossier.incidentDateLabel}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onToggleFollow,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("follow_case_btn_${dossier.id}")
                    ) {
                        Icon(
                            imageVector = if (dossier.isFollowed) {
                                Icons.Default.NotificationsActive
                            } else {
                                Icons.Default.NotificationsNone
                            },
                            contentDescription = if (dossier.isFollowed) {
                                stringResource(R.string.cd_unfollow_case)
                            } else {
                                stringResource(R.string.cd_follow_case)
                            },
                            tint = if (dossier.isFollowed) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }

                    IconButton(
                        onClick = onToggleSave,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("save_for_life_btn_${dossier.id}")
                    ) {
                        Icon(
                            imageVector = if (dossier.isSavedForLife) {
                                Icons.Default.Bookmark
                            } else {
                                Icons.Default.BookmarkBorder
                            },
                            contentDescription = if (dossier.isSavedForLife) {
                                stringResource(R.string.cd_remove_saved)
                            } else {
                                stringResource(R.string.cd_save_for_offline)
                            },
                            tint = if (dossier.isSavedForLife) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Reusable badge + toggle for machine-translated items ("Translated automatically" + "View original").
 */
@Composable
fun MachineTranslationBannerRow(
    showingOriginal: Boolean,
    onToggleOriginal: () -> Unit,
    testTagPrefix: String = "dossier",
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag("${testTagPrefix}_translation_banner"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Translate,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = stringResource(R.string.translation_badge_auto),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        TextButton(
            onClick = onToggleOriginal,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
            modifier = Modifier.testTag("${testTagPrefix}_toggle_original")
        ) {
            Text(
                text = if (showingOriginal) {
                    stringResource(R.string.translation_view_translated)
                } else {
                    stringResource(R.string.translation_view_original)
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Visual banner displaying the incident photo or video thumbnail on News & Tracker cards.
 */
@Composable
fun IncidentMediaPreviewBanner(
    dossier: CaseDossier,
    isFeatured: Boolean = false
) {
    val hasVideo = dossier.videoUrl.isNotBlank() ||
        dossier.mediaAttachments.any { it.type == MediaAttachmentType.VIDEO }
    val previewImageUrl = dossier.imageUrl.ifBlank {
        dossier.mediaAttachments.firstOrNull { it.thumbnailUrl.isNotBlank() }?.thumbnailUrl
            ?: dossier.mediaAttachments.firstOrNull { it.type == MediaAttachmentType.IMAGE }?.url
            ?: ""
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (isFeatured) 108.dp else 92.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .testTag("incident_media_banner_${dossier.id}")
    ) {
        if (previewImageUrl.isNotBlank()) {
            AsyncImage(
                model = previewImageUrl,
                contentDescription = stringResource(R.string.cd_incident_photo),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (hasVideo) Icons.Default.PlayCircleFilled else Icons.Default.PhotoLibrary,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(44.dp)
                )
            }
        }

        // Top-left Photo / Video Report Badge
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.78f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (hasVideo) Icons.Default.PlayCircleFilled else Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.inverseOnSurface,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (hasVideo) {
                            stringResource(R.string.media_badge_video)
                        } else {
                            stringResource(R.string.media_badge_photo)
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.inverseOnSurface,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (dossier.mediaAttachments.size > 1) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "+${dossier.mediaAttachments.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Bottom caption strip
        if (dossier.mediaCaption.isNotBlank()) {
            Surface(
                color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.72f),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                Text(
                    text = dossier.mediaCaption,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.inverseOnSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

/**
 * Dedicated Case Tracker card for the "Never Forgotten" (Track) & Saved screens.
 */
@Composable
fun CaseDossierCard(
    dossier: CaseDossier,
    isBlurred: Boolean,
    onRevealBlur: () -> Unit,
    onOpenDetail: () -> Unit,
    onToggleSaveForLife: () -> Unit,
    onQuickSelectJurisdiction: (String, String, String) -> Unit,
    initiallyExpandedTimeline: Boolean = false,
    onAddTimelineUpdateClick: (() -> Unit)? = null,
    onToggleFollow: (() -> Unit)? = null
) {
    var timelineExpanded by remember(dossier.id, initiallyExpandedTimeline) {
        mutableStateOf(initiallyExpandedTimeline)
    }
    var showOriginalText by remember(dossier.id) { mutableStateOf(false) }

    val displayTitle = if (showOriginalText && dossier.originalTitle.isNotBlank()) {
        dossier.originalTitle
    } else {
        dossier.title
    }
    val displaySummary = if (showOriginalText && dossier.originalSummary.isNotBlank()) {
        dossier.originalSummary
    } else {
        dossier.summary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(14.dp)
            )
            .clip(RoundedCornerShape(14.dp))
            .clickable { onOpenDetail() }
            .testTag("dossier_card_${dossier.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Status Row: Current Verified Stage + Verification Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = stringResource(dossier.caseStage.labelRes),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                VerificationBadgePill(status = dossier.verificationStatus)
            }

            if (dossier.isMachineTranslated) {
                Spacer(modifier = Modifier.height(6.dp))
                MachineTranslationBannerRow(
                    showingOriginal = showOriginalText,
                    onToggleOriginal = { showOriginalText = !showOriginalText },
                    testTagPrefix = "dossier_card_${dossier.id}"
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Location + Original Incident Date
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable {
                        onQuickSelectJurisdiction(dossier.stateName, dossier.districtName, dossier.cityName)
                    }
                    .padding(vertical = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = stringResource(R.string.cd_case_location),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${dossier.cityName}, ${dossier.districtName} • ${dossier.stateName}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Title & Summary
            if (isBlurred && dossier.sensitivityLevel != SensitivityLevel.STANDARD) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = stringResource(dossier.sensitivityLevel.labelRes),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedButton(
                            onClick = onRevealBlur,
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("reveal_blur_${dossier.id}")
                        ) {
                            Text(stringResource(R.string.card_show_case_summary), maxLines = 1)
                        }
                    }
                }
            } else {
                if (dossier.imageUrl.isNotBlank() || dossier.videoUrl.isNotBlank() || dossier.mediaAttachments.isNotEmpty()) {
                    IncidentMediaPreviewBanner(
                        dossier = dossier,
                        isFeatured = false
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Text(
                    text = displayTitle,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = displaySummary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(
                    R.string.card_original_report_format,
                    dossier.incidentDateLabel,
                    dossier.daysSinceIncident,
                    dossier.lastVerifiedDate
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Expandable Case Timeline Section
            CaseTimelineCardSection(
                caseId = dossier.id,
                timelineUpdates = dossier.timelineUpdates,
                isSavedForLife = dossier.isSavedForLife,
                expanded = timelineExpanded,
                onToggleExpand = { timelineExpanded = !timelineExpanded },
                onAddTimelineUpdateClick = onAddTimelineUpdateClick
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(10.dp))

            // Accessible Action Row (Minimum 48dp touch targets, single-line non-wrapping labels)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onToggleFollow != null) {
                    OutlinedButton(
                        onClick = onToggleFollow,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("follow_btn_${dossier.id}"),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = if (dossier.isFollowed) {
                                Icons.Default.NotificationsActive
                            } else {
                                Icons.Default.NotificationsNone
                            },
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (dossier.isFollowed) {
                                stringResource(R.string.card_action_following)
                            } else {
                                stringResource(R.string.card_action_follow_case)
                            },
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                OutlinedButton(
                    onClick = onToggleSaveForLife,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("save_for_life_btn_${dossier.id}"),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = if (dossier.isSavedForLife) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (dossier.isSavedForLife) {
                            stringResource(R.string.card_action_saved)
                        } else {
                            stringResource(R.string.card_action_save)
                        },
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Button(
                    onClick = onOpenDetail,
                    modifier = Modifier
                        .weight(1.1f)
                        .heightIn(min = 48.dp)
                        .testTag("open_dossier_btn_${dossier.id}"),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.card_action_open_timeline),
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun VerificationBadgePill(status: VerificationStatus) {
    val (icon, tintColor, label) = when (status) {
        VerificationStatus.CONFIRMED -> Triple(
            Icons.Default.Verified,
            SuccessIndicator,
            stringResource(R.string.verification_pill_confirmed)
        )
        VerificationStatus.REPORTED_UNCONFIRMED -> Triple(
            Icons.Default.Schedule,
            WarningIndicator,
            stringResource(R.string.verification_pill_reported)
        )
        VerificationStatus.AWAITING_VERIFICATION -> Triple(
            Icons.Default.Info,
            WarningIndicator,
            stringResource(R.string.verification_pill_unverified)
        )
        VerificationStatus.CORRECTED -> Triple(
            Icons.Default.CheckCircle,
            SuccessIndicator,
            stringResource(R.string.verification_pill_corrected)
        )
    }
    val statusFullLabel = stringResource(status.labelRes)
    val semanticsDesc = stringResource(R.string.verification_semantics_prefix, statusFullLabel)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.semantics {
            contentDescription = semanticsDesc
        }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tintColor,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun CaseTimelineCardSection(
    caseId: String,
    timelineUpdates: List<TimelineUpdateItem>,
    isSavedForLife: Boolean,
    expanded: Boolean,
    onToggleExpand: () -> Unit,
    onAddTimelineUpdateClick: (() -> Unit)? = null
) {
    val latestUpdate = timelineUpdates.lastOrNull()

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("case_timeline_section_$caseId")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 36.dp)
                    .clickable { onToggleExpand() }
                    .testTag("toggle_case_timeline_$caseId"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Timeline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = stringResource(
                                R.string.card_timeline_milestones_format,
                                timelineUpdates.size
                            ),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (!expanded && latestUpdate != null) {
                            Text(
                                text = "${latestUpdate.dateLabel}: ${latestUpdate.headline}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (expanded) {
                            stringResource(R.string.card_action_hide)
                        } else {
                            stringResource(R.string.card_action_view)
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) {
                            stringResource(R.string.cd_collapse_timeline)
                        } else {
                            stringResource(R.string.cd_expand_timeline)
                        },
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(10.dp))

                    CaseChronologicalTimelineList(
                        timelineUpdates = timelineUpdates,
                        caseId = caseId
                    )

                    if (onAddTimelineUpdateClick != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedButton(
                            onClick = onAddTimelineUpdateClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("card_add_timeline_update_$caseId"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddCircleOutline,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.card_submit_timeline_update),
                                style = MaterialTheme.typography.labelLarge,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CaseChronologicalTimelineList(
    timelineUpdates: List<TimelineUpdateItem>,
    caseId: String = ""
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        timelineUpdates.forEachIndexed { index, item ->
            val isLatest = index == timelineUpdates.lastIndex

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .testTag("timeline_node_${caseId}_$index")
            ) {
                // Vertical Timeline Spine & Milestone Dot
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .fillMaxHeight()
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .size(if (isLatest) 12.dp else 9.dp)
                            .clip(CircleShape)
                            .background(
                                if (isLatest) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outline
                                }
                            )
                    )
                    if (index < timelineUpdates.lastIndex) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .weight(1f)
                                .padding(vertical = 2.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant)
                        )
                    }
                }

                // Chronological Update Card
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = 10.dp)
                        .border(
                            width = 1.dp,
                            color = if (isLatest) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                            } else {
                                MaterialTheme.colorScheme.outlineVariant
                            },
                            shape = RoundedCornerShape(10.dp)
                        )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.dateLabel,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            VerificationBadgePill(status = item.verificationStatus)
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.headline,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.details,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = stringResource(
                                R.string.card_timeline_source_format,
                                item.sourceName,
                                item.lastCheckedDate
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CaseStageProgressStrip(
    currentStage: CaseStage,
    followUpCount: Int
) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = stringResource(
                        R.string.card_confirmed_status_format,
                        stringResource(currentStage.labelRes)
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = stringResource(R.string.card_milestones_count_format, followUpCount),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
