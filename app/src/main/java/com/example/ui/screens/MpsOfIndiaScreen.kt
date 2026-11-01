package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.HouseMeta
import com.example.data.model.LokSabhaMpRecord
import com.example.data.model.ParliamentHouse
import com.example.data.model.RajyaSabhaMpRecord
import com.example.data.model.SelectedMpDetail
import com.example.ui.viewmodel.MpsUiState

@Composable
private fun ParliamentHouse.localizedDisplayName(): String = when (this) {
    ParliamentHouse.LOK_SABHA -> stringResource(R.string.mps_house_lok_sabha)
    ParliamentHouse.RAJYA_SABHA -> stringResource(R.string.mps_house_rajya_sabha)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MpsOfIndiaScreen(
    uiState: MpsUiState,
    onSelectHouse: (ParliamentHouse) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSelectState: (String) -> Unit,
    onSelectParty: (String) -> Unit,
    onResetFilters: () -> Unit,
    onOpenLokSabhaMp: (LokSabhaMpRecord) -> Unit,
    onOpenRajyaSabhaMp: (RajyaSabhaMpRecord) -> Unit,
    onCloseMpDetail: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedDetail = uiState.selectedMpDetail
    if (selectedDetail != null) {
        MpDetailScreen(
            detail = selectedDetail,
            onBack = onCloseMpDetail,
            modifier = modifier
        )
        return
    }

    var showStatePickerSheet by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("mps_of_india_screen")
    ) {
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("mps_loading_state"),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(24.dp)
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.mps_loading_text),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            MpsDisclaimerFooter()
            return
        }

        if (uiState.errorMessage != null) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.mps_error_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = uiState.errorMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
            MpsDisclaimerFooter()
            return
        }

        val activeListEmpty = when (uiState.selectedHouse) {
            ParliamentHouse.LOK_SABHA -> uiState.filteredLokSabhaMps.isEmpty()
            ParliamentHouse.RAJYA_SABHA -> uiState.groupedRajyaSabhaMps.isEmpty()
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .testTag("mps_list_column"),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // Entry header with two House cards ("Lok Sabha" & "Rajya Sabha"), counts & as_of_date
            item(key = "mps_entry_header") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.mps_title),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            val activeMeta = when (uiState.selectedHouse) {
                                ParliamentHouse.LOK_SABHA -> uiState.lokSabhaMeta
                                ParliamentHouse.RAJYA_SABHA -> uiState.rajyaSabhaMeta
                            }
                            Text(
                                text = stringResource(R.string.mps_data_as_of, activeMeta.formattedAsOfDate),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.testTag("mps_as_of_date_label")
                            )
                        }
                    }

                    // Two House Selection Cards showing sitting and vacant counts from meta
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        HouseSummaryCard(
                            house = ParliamentHouse.LOK_SABHA,
                            meta = uiState.lokSabhaMeta,
                            selected = uiState.selectedHouse == ParliamentHouse.LOK_SABHA,
                            onClick = { onSelectHouse(ParliamentHouse.LOK_SABHA) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("house_card_lok_sabha")
                        )
                        HouseSummaryCard(
                            house = ParliamentHouse.RAJYA_SABHA,
                            meta = uiState.rajyaSabhaMeta,
                            selected = uiState.selectedHouse == ParliamentHouse.RAJYA_SABHA,
                            onClick = { onSelectHouse(ParliamentHouse.RAJYA_SABHA) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("house_card_rajya_sabha")
                        )
                    }

                    // Search input (MP name, constituency, state, party)
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("mps_search_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        placeholder = {
                            val hint = if (uiState.selectedHouse == ParliamentHouse.LOK_SABHA) {
                                stringResource(R.string.mps_search_placeholder_lok)
                            } else {
                                stringResource(R.string.mps_search_placeholder_rajya)
                            }
                            Text(
                                text = hint,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = stringResource(R.string.cd_search_mps),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { onSearchQueryChange("") },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .testTag("mps_search_clear_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = stringResource(R.string.cd_clear_search)
                                    )
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    // State/UT Filter selector + Active count summary
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { showStatePickerSheet = true },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .weight(1f)
                                .testTag("mps_state_filter_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = stringResource(R.string.cd_filter_state_ut),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = uiState.selectedState,
                                style = MaterialTheme.typography.labelLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        val hasActiveFilters = uiState.searchQuery.isNotBlank() ||
                            uiState.selectedState != MpsUiState.ALL_STATES_OPTION ||
                            uiState.selectedParty != MpsUiState.ALL_PARTIES_OPTION

                        if (hasActiveFilters) {
                            TextButton(
                                onClick = onResetFilters,
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("mps_reset_filters_button")
                            ) {
                                Text(stringResource(R.string.action_reset))
                            }
                        }
                    }

                    // Party filter chips (Top parties + "All")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .testTag("mps_party_chips_row"),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        uiState.topPartyOptions.forEach { partyOption ->
                            val isSelected = uiState.selectedParty.equals(partyOption, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSelectParty(partyOption) },
                                label = {
                                    Text(
                                        text = partyOption,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                },
                                leadingIcon = if (isSelected) {
                                    {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                } else {
                                    null
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("party_filter_chip_$partyOption")
                            )
                        }
                    }

                    // Results summary line
                    val resultSummaryText = when (uiState.selectedHouse) {
                        ParliamentHouse.LOK_SABHA ->
                            stringResource(
                                R.string.mps_showing_lok_format,
                                uiState.filteredLokSabhaMps.size,
                                uiState.lokSabhaMeta.totalSeats
                            )
                        ParliamentHouse.RAJYA_SABHA ->
                            stringResource(
                                R.string.mps_showing_rajya_format,
                                uiState.totalFilteredRajyaSabhaCount,
                                uiState.rajyaSabhaMeta.sitting
                            )
                    }
                    Text(
                        text = resultSummaryText,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.testTag("mps_results_count_text")
                    )
                }
            }

            if (activeListEmpty) {
                item(key = "mps_empty_state") {
                    MpsEmptySearchState(
                        onResetFilters = onResetFilters,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 24.dp)
                    )
                }
            } else {
                when (uiState.selectedHouse) {
                    ParliamentHouse.LOK_SABHA -> {
                        items(
                            items = uiState.filteredLokSabhaMps,
                            key = { "lok_${it.id}" }
                        ) { record ->
                            LokSabhaMpRowCard(
                                record = record,
                                onClick = { onOpenLokSabhaMp(record) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 5.dp)
                            )
                        }
                    }

                    ParliamentHouse.RAJYA_SABHA -> {
                        uiState.groupedRajyaSabhaMps.forEach { (sectionName, members) ->
                            stickyHeader(key = "rajya_header_$sectionName") {
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    tonalElevation = 2.dp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("rajya_sticky_header_$sectionName")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val localizedSectionName = if (sectionName.equals("Nominated", ignoreCase = true)) {
                                            stringResource(R.string.mps_nominated)
                                        } else {
                                            sectionName
                                        }
                                        Text(
                                            text = localizedSectionName,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        val countText = if (members.size == 1) {
                                            stringResource(R.string.mps_members_count_one, members.size)
                                        } else {
                                            stringResource(R.string.mps_members_count_other, members.size)
                                        }
                                        Text(
                                            text = countText,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            items(
                                items = members,
                                key = { "rajya_${it.id}" }
                            ) { record ->
                                RajyaSabhaMpRowCard(
                                    record = record,
                                    onClick = { onOpenRajyaSabhaMp(record) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }

            item(key = "mps_list_footer") {
                MpsDisclaimerFooter(
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
    }

    if (showStatePickerSheet) {
        ModalBottomSheet(
            onDismissRequest = { showStatePickerSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .testTag("mps_state_picker_sheet")
            ) {
                Text(
                    text = stringResource(R.string.mps_state_sheet_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                )
                HorizontalDivider()
                LazyColumn(
                    modifier = Modifier.heightIn(max = 420.dp)
                ) {
                    items(
                        items = uiState.availableStates,
                        key = { it }
                    ) { stateOption ->
                        val isSelected = uiState.selectedState.equals(stateOption, ignoreCase = true)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .clickable {
                                    onSelectState(stateOption)
                                    showStatePickerSheet = false
                                }
                                .padding(horizontal = 20.dp, vertical = 12.dp)
                                .testTag("state_option_$stateOption"),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stateOption,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = stringResource(R.string.cd_selected),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HouseSummaryCard(
    house: ParliamentHouse,
    meta: HouseMeta,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val borderStroke = if (selected) {
        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    }
    val houseTitle = house.localizedDisplayName()
    val sittingVacantText = stringResource(R.string.mps_sitting_vacant_format, meta.sitting, meta.vacant)
    val dataAsOfText = stringResource(R.string.mps_data_as_of, meta.formattedAsOfDate)

    Card(
        modifier = modifier
            .heightIn(min = 96.dp)
            .semantics {
                role = Role.Tab
                contentDescription = "$houseTitle: $sittingVacantText, $dataAsOfText"
            }
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = borderStroke,
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 2.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = houseTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    imageVector = Icons.Default.AccountBalance,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = if (selected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            Text(
                text = sittingVacantText,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = contentColor
            )

            Text(
                text = dataAsOfText,
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) {
                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun LokSabhaMpRowCard(
    record: LokSabhaMpRecord,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isVacant = record.isVacant
    val vacantLabel = stringResource(R.string.mps_vacant_seat)
    Card(
        modifier = modifier
            .heightIn(min = 68.dp)
            .clickable(onClick = onClick)
            .testTag("lok_sabha_row_${record.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isVacant) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = if (isVacant) vacantLabel else (record.mpName ?: vacantLabel),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isVacant) FontWeight.Medium else FontWeight.SemiBold,
                    fontStyle = if (isVacant) FontStyle.Italic else FontStyle.Normal,
                    color = if (isVacant) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                Text(
                    text = "${record.constituencyName} · ${record.stateOrUt}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            PartyShortBadge(
                partyShort = if (isVacant) vacantLabel else (record.partyShort ?: "IND"),
                isVacant = isVacant
            )
        }
    }
}

@Composable
private fun RajyaSabhaMpRowCard(
    record: RajyaSabhaMpRecord,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val unknownMpLabel = stringResource(R.string.mps_unknown_mp)
    val termUnavailableLabel = stringResource(R.string.mps_term_unavailable)
    Card(
        modifier = modifier
            .heightIn(min = 68.dp)
            .clickable(onClick = onClick)
            .testTag("rajya_sabha_row_${record.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = record.mpName ?: unknownMpLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                val termLabel = record.termEndYear?.let {
                    stringResource(R.string.mps_term_ends_format, it)
                } ?: termUnavailableLabel
                Text(
                    text = termLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            PartyShortBadge(
                partyShort = record.partyShort ?: "IND",
                isVacant = false
            )
        }
    }
}

@Composable
fun PartyShortBadge(
    partyShort: String,
    isVacant: Boolean = false,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val (bgColor, textColor) = if (isVacant) {
        MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        partyBadgeColors(partyShort = partyShort, isDark = isDark)
    }

    Surface(
        color = bgColor,
        contentColor = textColor,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Text(
            text = partyShort,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun partyBadgeColors(partyShort: String, isDark: Boolean): Pair<Color, Color> {
    return when (partyShort.trim().uppercase()) {
        "BJP" -> if (isDark) {
            Color(0xFF5C2B09) to Color(0xFFFFB68B)
        } else {
            Color(0xFFFFE5D0) to Color(0xFF8C3800)
        }
        "INC" -> if (isDark) {
            Color(0xFF0D3B66) to Color(0xFFA9D6FF)
        } else {
            Color(0xFFD9EEFF) to Color(0xFF08427A)
        }
        "AITC" -> if (isDark) {
            Color(0xFF0E4429) to Color(0xFF9BF0B9)
        } else {
            Color(0xFFD8F8E3) to Color(0xFF0A5C36)
        }
        "SP" -> if (isDark) {
            Color(0xFF5A161B) to Color(0xFFFFB3B8)
        } else {
            Color(0xFFFFE0E3) to Color(0xFF8E1420)
        }
        "DMK" -> if (isDark) {
            Color(0xFF4D1924) to Color(0xFFFFB2C1)
        } else {
            Color(0xFFFFDFE6) to Color(0xFF7A1228)
        }
        "TDP" -> if (isDark) {
            Color(0xFF4F3E06) to Color(0xFFFFE082)
        } else {
            Color(0xFFFFF3C4) to Color(0xFF6B5200)
        }
        "JD(U)" -> if (isDark) {
            Color(0xFF1A4329) to Color(0xFFA8EBC0)
        } else {
            Color(0xFFDDF5E5) to Color(0xFF165B31)
        }
        "SHS" -> if (isDark) {
            Color(0xFF522A0E) to Color(0xFFFFC299)
        } else {
            Color(0xFFFFE8D6) to Color(0xFF7D3C0A)
        }
        else -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
    }
}

@Composable
private fun MpsEmptySearchState(
    onResetFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.testTag("mps_empty_state_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.mps_empty_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.mps_empty_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            OutlinedButton(
                onClick = onResetFilters,
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("mps_empty_reset_button")
            ) {
                Text(stringResource(R.string.mps_empty_reset_btn))
            }
        }
    }
}

@Composable
fun MpDetailScreen(
    detail: SelectedMpDetail,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    val vacantLabel = stringResource(R.string.mps_vacant_seat)
    val vacantSeatDetailLabel = stringResource(R.string.mps_vacant_seat_detail)
    val independentLabel = stringResource(R.string.mps_independent)
    val lokSabhaLabel = stringResource(R.string.mps_house_lok_sabha)
    val rajyaSabhaLabel = stringResource(R.string.mps_house_rajya_sabha)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("mp_detail_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("mp_detail_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back_mps_list)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.mps_detail_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        when (detail) {
            is SelectedMpDetail.LokSabha -> {
                val record = detail.record
                val isVacant = record.isVacant

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = if (isVacant) vacantLabel else (record.mpName ?: vacantLabel),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    fontStyle = if (isVacant) FontStyle.Italic else FontStyle.Normal,
                                    color = if (isVacant) {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                                    modifier = Modifier.testTag("mp_detail_name")
                                )
                                Text(
                                    text = lokSabhaLabel,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            PartyShortBadge(
                                partyShort = if (isVacant) vacantLabel else (record.partyShort ?: "IND"),
                                isVacant = isVacant
                            )
                        }

                        HorizontalDivider()

                        DetailFieldRow(
                            label = stringResource(R.string.mps_detail_field_house),
                            value = lokSabhaLabel
                        )
                        DetailFieldRow(
                            label = stringResource(R.string.mps_detail_field_constituency),
                            value = if (record.constituencyNumber > 0) {
                                stringResource(
                                    R.string.mps_detail_constituency_no_format,
                                    record.constituencyName,
                                    record.constituencyNumber
                                )
                            } else {
                                record.constituencyName
                            }
                        )
                        DetailFieldRow(
                            label = stringResource(R.string.mps_detail_field_state),
                            value = record.stateOrUt
                        )
                        if (!record.reservation.isNullOrBlank()) {
                            DetailFieldRow(
                                label = stringResource(R.string.mps_detail_field_reservation),
                                value = record.reservation
                            )
                        }
                        DetailFieldRow(
                            label = stringResource(R.string.mps_detail_field_party),
                            value = if (isVacant) {
                                vacantSeatDetailLabel
                            } else {
                                record.party ?: record.partyShort ?: independentLabel
                            }
                        )
                        if (!record.gender.isNullOrBlank()) {
                            DetailFieldRow(
                                label = stringResource(R.string.mps_detail_field_gender),
                                value = record.gender
                            )
                        }

                        if (!record.notes.isNullOrBlank()) {
                            HorizontalDivider()
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("mp_detail_notes_box")
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = if (isVacant) {
                                            stringResource(R.string.mps_detail_reason_vacancy)
                                        } else {
                                            stringResource(R.string.mps_detail_notes)
                                        },
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = record.notes,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            is SelectedMpDetail.RajyaSabha -> {
                val record = detail.record
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = record.mpName ?: stringResource(R.string.mps_unknown_mp),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.testTag("mp_detail_name")
                                )
                                Text(
                                    text = rajyaSabhaLabel,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            PartyShortBadge(
                                partyShort = record.partyShort ?: "IND",
                                isVacant = false
                            )
                        }

                        HorizontalDivider()

                        DetailFieldRow(
                            label = stringResource(R.string.mps_detail_field_house),
                            value = rajyaSabhaLabel
                        )
                        DetailFieldRow(
                            label = stringResource(R.string.mps_detail_field_state),
                            value = if (record.nominated) {
                                stringResource(R.string.mps_nominated)
                            } else {
                                record.stateOrUtRepresented
                            }
                        )
                        DetailFieldRow(
                            label = stringResource(R.string.mps_detail_field_party),
                            value = record.party ?: record.partyShort ?: independentLabel
                        )
                        if (record.termEndYear != null) {
                            DetailFieldRow(
                                label = stringResource(R.string.mps_detail_field_term_end),
                                value = record.termEndYear.toString()
                            )
                        }

                        if (!record.notes.isNullOrBlank()) {
                            HorizontalDivider()
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("mp_detail_notes_box")
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.mps_detail_notes),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = record.notes,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        MpsDisclaimerFooter()
    }
}

@Composable
private fun DetailFieldRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(0.6f)
        )
    }
}

@Composable
fun MpsDisclaimerFooter(
    modifier: Modifier = Modifier
) {
    Text(
        text = stringResource(R.string.mps_public_sources_footer),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("mps_public_sources_footer")
    )
}
