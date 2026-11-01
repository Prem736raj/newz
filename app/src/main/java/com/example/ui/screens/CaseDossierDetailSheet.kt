package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextDecrease
import androidx.compose.material.icons.filled.TextIncrease
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.CaseDossier
import com.example.data.model.CaseStage
import com.example.ui.components.CaseStageProgressStrip
import com.example.ui.components.MachineTranslationBannerRow
import com.example.ui.components.VerificationBadgePill

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CaseDossierDetailScreen(
    dossier: CaseDossier,
    onBack: () -> Unit,
    onToggleSaveForLife: (String) -> Unit,
    onAddFollowUp: (String, CaseStage, String, String) -> Unit,
    onToggleFollow: (() -> Unit)? = null,
    onSubmitCorrection: ((issueType: String, details: String) -> Unit)? = null,
    relatedDossiers: List<CaseDossier> = emptyList(),
    onOpenRelatedDossier: (CaseDossier) -> Unit = {},
    correctionLogs: List<String> = emptyList()
) {
    BackHandler(onBack = onBack)

    val context = LocalContext.current
    var fontScale by remember { mutableFloatStateOf(1.0f) }
    var showAddFollowUpModal by remember { mutableStateOf(false) }
    var showCorrectionModal by remember { mutableStateOf(false) }
    var showMpMlaExpanded by remember { mutableStateOf(false) }
    var showPrivateNoteEditor by remember { mutableStateOf(false) }
    var showOriginalLanguage by remember(dossier.id) { mutableStateOf(false) }
    var personalNoteInput by remember(dossier.id) { mutableStateOf(dossier.personalNotes) }

    val displayTitle = if (showOriginalLanguage && dossier.originalTitle != null) {
        dossier.originalTitle
    } else {
        dossier.title
    }
    val displaySummary = if (showOriginalLanguage && dossier.originalSummary != null) {
        dossier.originalSummary
    } else {
        dossier.summary
    }
    val displayFullReport = if (showOriginalLanguage && dossier.originalFullReport != null) {
        dossier.originalFullReport
    } else {
        dossier.fullReport
    }

    val stageLabel = stringResource(dossier.caseStage.labelRes)
    val shareChooserTitle = stringResource(R.string.dossier_share_chooser_title)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            // Top Editorial Reader Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("dossier_detail_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.cd_close_dossier)
                                )
                            }
                            Column(modifier = Modifier.padding(end = 8.dp)) {
                                Text(
                                    text = dossier.crimeCategory,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${dossier.cityName}, ${dossier.stateName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { fontScale = (fontScale - 0.1f).coerceAtLeast(0.85f) },
                                modifier = Modifier.size(44.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TextDecrease,
                                    contentDescription = stringResource(R.string.cd_decrease_font)
                                )
                            }
                            IconButton(
                                onClick = { fontScale = (fontScale + 0.1f).coerceAtMost(1.6f) },
                                modifier = Modifier.size(44.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TextIncrease,
                                    contentDescription = stringResource(R.string.cd_increase_font)
                                )
                            }
                            IconButton(
                                onClick = {
                                    val shareText = buildString {
                                        appendLine(displayTitle)
                                        appendLine("${dossier.cityName}, ${dossier.districtName}, ${dossier.stateName}")
                                        appendLine("${dossier.sourceName} (${dossier.incidentDateLabel})")
                                        appendLine(stageLabel)
                                        if (dossier.sourceUrl.isNotBlank()) {
                                            appendLine(dossier.sourceUrl)
                                        }
                                        appendLine()
                                        appendLine(displaySummary)
                                    }
                                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, displayTitle)
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, shareChooserTitle))
                                },
                                modifier = Modifier.size(44.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = stringResource(R.string.cd_share_report)
                                )
                            }
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                // 1. Category, Verification Status & Last Verified Date
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
                            text = "${dossier.crimeCategory} • ${dossier.victimCategory}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    VerificationBadgePill(status = dossier.verificationStatus)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 2. Headline
                Text(
                    text = displayTitle,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = (22 * fontScale).sp,
                        lineHeight = (30 * fontScale).sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (dossier.isMachineTranslated) {
                    Spacer(modifier = Modifier.height(8.dp))
                    MachineTranslationBannerRow(
                        showingOriginal = showOriginalLanguage,
                        onToggleOriginal = { showOriginalLanguage = !showOriginalLanguage }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 3. Publisher, Publication Date & 4. Source Link
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.dossier_source_prefix, dossier.sourceName),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(
                                R.string.dossier_published_verified_format,
                                dossier.incidentDateLabel,
                                dossier.lastVerifiedDate
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (dossier.sourceUrl.isNotBlank()) {
                        TextButton(
                            onClick = {
                                runCatching {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(dossier.sourceUrl))
                                    context.startActivity(intent)
                                }
                            },
                            modifier = Modifier.testTag("open_original_source_link")
                        ) {
                            Text(stringResource(R.string.dossier_original_source_btn))
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = stringResource(R.string.cd_open_source),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 9 & 10. Follow Case + Save Article Action Bar (Single-line labels, 48dp height)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (onToggleFollow != null) {
                        OutlinedButton(
                            onClick = onToggleFollow,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("detail_follow_case_button"),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Icon(
                                imageVector = if (dossier.isFollowed) {
                                    Icons.Default.NotificationsActive
                                } else {
                                    Icons.Default.NotificationsNone
                                },
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (dossier.isFollowed) {
                                    stringResource(R.string.dossier_btn_following_case)
                                } else {
                                    stringResource(R.string.dossier_btn_follow_case)
                                },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Button(
                        onClick = { onToggleSaveForLife(personalNoteInput) },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("detail_save_for_life_button"),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Icon(
                            imageVector = if (dossier.isSavedForLife) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (dossier.isSavedForLife) {
                                stringResource(R.string.dossier_btn_saved_archive)
                            } else {
                                stringResource(R.string.dossier_btn_save_article)
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Optional Private Note toggle for Saved Items
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { showPrivateNoteEditor = !showPrivateNoteEditor }
                    ) {
                        Text(
                            text = if (personalNoteInput.isNotBlank()) {
                                stringResource(R.string.dossier_edit_private_note)
                            } else {
                                stringResource(R.string.dossier_add_private_note)
                            },
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    Text(
                        text = stringResource(R.string.dossier_offline_cache_hint),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                AnimatedVisibility(visible = showPrivateNoteEditor) {
                    Column(modifier = Modifier.padding(bottom = 12.dp)) {
                        OutlinedTextField(
                            value = personalNoteInput,
                            onValueChange = { personalNoteInput = it },
                            label = { Text(stringResource(R.string.dossier_private_note_label)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 5. Factual Summary Callout
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.dossier_key_summary_header),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = displaySummary,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = (15.5f * fontScale).sp,
                                lineHeight = (23 * fontScale).sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 6. Main Factual Details
                Text(
                    text = stringResource(R.string.dossier_background_header),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = displayFullReport,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = (16 * fontScale).sp,
                        lineHeight = (25 * fontScale).sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Legal & Privacy Integrity Notice
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.dossier_legal_privacy_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 8. Related Case Timeline (Never Forgotten Tracker)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
                        .testTag("detail_case_timeline_card"),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    var newestFirst by remember { mutableStateOf(false) }
                    val orderedUpdates = remember(dossier.timelineUpdates, newestFirst) {
                        if (newestFirst) dossier.timelineUpdates.reversed() else dossier.timelineUpdates
                    }

                    Column(modifier = Modifier.padding(16.dp)) {
                        CaseStageProgressStrip(
                            currentStage = dossier.caseStage,
                            followUpCount = dossier.timelineUpdates.size
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.dossier_timeline_title),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = stringResource(R.string.dossier_timeline_subtitle_format, dossier.lastVerifiedDate),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            OutlinedButton(
                                onClick = { showAddFollowUpModal = true },
                                modifier = Modifier
                                    .heightIn(min = 44.dp)
                                    .testTag("add_followup_button"),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddCircleOutline,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(stringResource(R.string.dossier_log_update_btn), maxLines = 1)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilterChip(
                                selected = !newestFirst,
                                onClick = { newestFirst = false },
                                label = {
                                    Text(
                                        text = stringResource(R.string.dossier_sort_chronological),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                modifier = Modifier.testTag("timeline_sort_chronological")
                            )
                            FilterChip(
                                selected = newestFirst,
                                onClick = { newestFirst = true },
                                label = {
                                    Text(
                                        text = stringResource(R.string.dossier_sort_latest),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                modifier = Modifier.testTag("timeline_sort_newest")
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        orderedUpdates.forEachIndexed { index, item ->
                            val stepNumber = if (newestFirst) {
                                orderedUpdates.size - index
                            } else {
                                index + 1
                            }
                            val isLatestMilestone = stepNumber == orderedUpdates.size
                            val itemHeadline = if (showOriginalLanguage && item.originalHeadline != null) {
                                item.originalHeadline
                            } else {
                                item.headline
                            }
                            val itemDetails = if (showOriginalLanguage && item.originalDetails != null) {
                                item.originalDetails
                            } else {
                                item.details
                            }
                            val resolvedStageBadge = stringResource(CaseStage.fromCode(item.stageBadge).labelRes)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(IntrinsicSize.Min)
                                    .padding(bottom = 12.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .padding(top = 6.dp, end = 12.dp)
                                        .fillMaxHeight()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(if (isLatestMilestone) 12.dp else 9.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isLatestMilestone) {
                                                    MaterialTheme.colorScheme.primary
                                                } else {
                                                    MaterialTheme.colorScheme.outline
                                                }
                                            )
                                    )
                                    if (index < orderedUpdates.lastIndex) {
                                        Box(
                                            modifier = Modifier
                                                .width(2.dp)
                                                .weight(1f)
                                                .padding(vertical = 2.dp)
                                                .background(MaterialTheme.colorScheme.outlineVariant)
                                        )
                                    }
                                }
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                            RoundedCornerShape(10.dp)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (isLatestMilestone) {
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                                            } else {
                                                MaterialTheme.colorScheme.outlineVariant
                                            },
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .padding(12.dp)
                                ) {
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
                                            modifier = Modifier.weight(1f)
                                        )
                                        VerificationBadgePill(status = item.verificationStatus)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = itemHeadline,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = itemDetails,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = stringResource(
                                                R.string.dossier_timeline_meta_format,
                                                resolvedStageBadge,
                                                item.sourceName,
                                                item.lastCheckedDate
                                            ),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (item.sourceReferenceUrl.isNotBlank()) {
                                            TextButton(
                                                onClick = {
                                                    runCatching {
                                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.sourceReferenceUrl))
                                                        context.startActivity(intent)
                                                    }
                                                },
                                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = stringResource(R.string.dossier_reference_btn),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 9. Compact, Expandable Constituency Representatives (MP & MLA) Reference Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showMpMlaExpanded = !showMpMlaExpanded }
                                .testTag("detail_mp_mla_toggle"),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalance,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = stringResource(R.string.dossier_jurisdiction_title),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${dossier.cityName}, ${dossier.districtName} • ${dossier.stateName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Icon(
                                imageVector = if (showMpMlaExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (showMpMlaExpanded) {
                                    stringResource(R.string.cd_collapse_representatives)
                                } else {
                                    stringResource(R.string.cd_expand_representatives)
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(
                                R.string.dossier_mla_mp_line_format,
                                dossier.mlaName,
                                dossier.mlaParty,
                                dossier.mpName,
                                dossier.mpParty
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        AnimatedVisibility(visible = showMpMlaExpanded) {
                            Column(modifier = Modifier.padding(top = 10.dp)) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = stringResource(R.string.dossier_mla_detail_format, dossier.mlaName),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = stringResource(
                                        R.string.dossier_constituency_party_format,
                                        dossier.mlaConstituency,
                                        dossier.mlaParty
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = stringResource(R.string.dossier_mp_detail_format, dossier.mpName),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = stringResource(
                                        R.string.dossier_constituency_party_format,
                                        dossier.mpConstituency,
                                        dossier.mpParty
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = stringResource(R.string.dossier_police_jurisdiction_format, dossier.policeJurisdiction),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = stringResource(
                                        R.string.dossier_rep_disclaimer_format,
                                        dossier.representativeDataSource,
                                        dossier.lastVerifiedDate
                                    ),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Applicable Statutes
                Text(
                    text = stringResource(R.string.dossier_relevant_laws_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    dossier.relevantLaws.forEach { law ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Gavel,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = law,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // 7. Related Reporting (Cases from same State or Crime Category)
                if (relatedDossiers.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = stringResource(R.string.dossier_related_reporting_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    relatedDossiers.take(3).forEach { related ->
                        val relatedStageLabel = stringResource(related.caseStage.labelRes)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                                .clickable { onOpenRelatedDossier(related) }
                                .testTag("related_dossier_${related.id}"),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "${related.stateName} • ${related.crimeCategory}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = related.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "$relatedStageLabel • ${related.sourceName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(12.dp))

                // Visible Corrections & Verification History Section
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                        .testTag("corrections_history_section"),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.dossier_corrections_title),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = stringResource(
                                        R.string.dossier_corrections_status_format,
                                        dossier.lastVerifiedDate,
                                        stringResource(dossier.verificationStatus.labelRes)
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            OutlinedButton(
                                onClick = { showCorrectionModal = true },
                                modifier = Modifier
                                    .heightIn(min = 44.dp)
                                    .testTag("report_correction_button"),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Flag,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(stringResource(R.string.dossier_report_issue_btn), maxLines = 1)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(
                                R.string.dossier_audit_bullet_1_format,
                                dossier.sourceName,
                                dossier.lastVerifiedDate
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stringResource(R.string.dossier_audit_bullet_2),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (correctionLogs.isEmpty()) {
                            Text(
                                text = stringResource(R.string.dossier_audit_no_corrections),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            correctionLogs.forEach { logEntry ->
                                Text(
                                    text = "• $logEntry",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }

    if (showAddFollowUpModal) {
        AddFollowUpDialog(
            dossier = dossier,
            onDismiss = { showAddFollowUpModal = false },
            onSubmit = { milestoneLabel, stage, headline, details ->
                onAddFollowUp(milestoneLabel, stage, headline, details)
                showAddFollowUpModal = false
            }
        )
    }

    if (showCorrectionModal) {
        ReportCorrectionDialog(
            dossierTitle = dossier.title,
            onDismiss = { showCorrectionModal = false },
            onSubmit = { issueType, details ->
                onSubmitCorrection?.invoke(issueType, details)
                showCorrectionModal = false
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddFollowUpDialog(
    dossier: CaseDossier,
    onDismiss: () -> Unit,
    onSubmit: (String, CaseStage, String, String) -> Unit
) {
    val milestonePresets = listOf(
        stringResource(R.string.dossier_preset_30),
        stringResource(R.string.dossier_preset_60),
        stringResource(R.string.dossier_preset_90),
        stringResource(R.string.dossier_preset_180),
        stringResource(R.string.dossier_preset_court)
    )
    val defaultDateLabel = stringResource(R.string.dossier_followup_day_update_format, dossier.daysSinceIncident)
    var dateLabel by remember {
        mutableStateOf(defaultDateLabel)
    }
    var selectedStage by remember { mutableStateOf(dossier.caseStage) }
    var headline by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.dossier_followup_dialog_title),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = stringResource(R.string.dossier_followup_dialog_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.dossier_followup_presets_label),
                    style = MaterialTheme.typography.labelMedium
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    milestonePresets.forEach { preset ->
                        FilterChip(
                            selected = dateLabel == preset,
                            onClick = { dateLabel = preset },
                            label = { Text(preset, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
                OutlinedTextField(
                    value = dateLabel,
                    onValueChange = { dateLabel = it },
                    label = { Text(stringResource(R.string.dossier_followup_date_label)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("timeline_update_date_input"),
                    singleLine = true
                )
                Text(
                    text = stringResource(R.string.dossier_followup_stage_label),
                    style = MaterialTheme.typography.labelMedium
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CaseStage.entries.forEach { stage ->
                        FilterChip(
                            selected = selectedStage == stage,
                            onClick = { selectedStage = stage },
                            label = { Text(stringResource(stage.labelRes), style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
                OutlinedTextField(
                    value = headline,
                    onValueChange = { headline = it },
                    label = { Text(stringResource(R.string.dossier_followup_headline_label)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("timeline_update_headline_input")
                )
                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    label = { Text(stringResource(R.string.dossier_followup_details_label)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("timeline_update_details_input"),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(dateLabel, selectedStage, headline, details) },
                enabled = headline.trim().length >= 3 && details.trim().length >= 3,
                modifier = Modifier.testTag("timeline_update_submit_button")
            ) {
                Text(stringResource(R.string.dossier_followup_submit_btn))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReportCorrectionDialog(
    dossierTitle: String,
    onDismiss: () -> Unit,
    onSubmit: (issueType: String, details: String) -> Unit
) {
    val issueTypes = listOf(
        stringResource(R.string.dossier_issue_factual),
        stringResource(R.string.dossier_issue_privacy),
        stringResource(R.string.dossier_issue_court),
        stringResource(R.string.dossier_issue_rep)
    )
    var selectedIssue by remember { mutableStateOf(issueTypes.first()) }
    var details by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.dossier_correction_dialog_title),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = stringResource(R.string.dossier_correction_dialog_prefix, dossierTitle.take(70)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    issueTypes.forEach { type ->
                        FilterChip(
                            selected = selectedIssue == type,
                            onClick = { selectedIssue = type },
                            label = { Text(type, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    label = { Text(stringResource(R.string.dossier_correction_field_label)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("correction_details_input"),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(selectedIssue, details) },
                enabled = details.trim().length >= 5,
                modifier = Modifier.testTag("submit_correction_confirm_button")
            ) {
                Text(stringResource(R.string.dossier_correction_submit_btn))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}
