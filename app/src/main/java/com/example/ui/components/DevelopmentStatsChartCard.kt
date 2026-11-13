package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.DailyDevelopmentStat
import com.example.data.model.DevelopmentMetric
import com.example.ui.theme.BurntOrange
import com.example.ui.theme.BurntOrangeDark
import com.example.ui.theme.BurntOrangeTint
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.OutlineWarm
import com.example.ui.theme.SuccessGreen
import kotlin.math.ceil

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DevelopmentStatsChartCard(
  stats: List<DailyDevelopmentStat>,
  playerFirstName: String,
  modifier: Modifier = Modifier,
) {
  if (stats.isEmpty()) return

  var selectedMetric by remember { mutableStateOf(DevelopmentMetric.PRACTICE_MINUTES) }
  val defaultSelectedIndex = remember(stats) {
    val todayIdx = stats.indexOfLast { it.dayOffsetFromToday == 0 }
    if (todayIdx >= 0 && stats[todayIdx].hasLiveSubmission) {
      todayIdx
    } else {
      stats.indexOfLast { it.practiceMinutes > 0 }.takeIf { it >= 0 } ?: stats.lastIndex
    }
  }
  var selectedDayIndex by remember(stats.size) { mutableIntStateOf(defaultSelectedIndex) }
  val safeSelectedIndex = selectedDayIndex.coerceIn(0, stats.lastIndex)
  val selectedDay = stats[safeSelectedIndex]

  val values = remember(stats, selectedMetric) {
    stats.map { it.valueFor(selectedMetric) }
  }
  val totalValue = remember(values) { values.sum() }
  val activeDays = remember(stats) { stats.count { it.practiceMinutes > 0 || it.focusReps > 0 } }
  val bestValue = remember(values) { values.maxOrNull() ?: 0 }
  val chartCeiling = remember(bestValue, selectedMetric) {
    val step = if (selectedMetric == DevelopmentMetric.PRACTICE_MINUTES) 5 else 10
    val rawMax = bestValue.coerceAtLeast(step * 2)
    (ceil(rawMax / step.toFloat()).toInt() * step).coerceAtLeast(step * 2)
  }

  val chartAccessibilitySummary = stringResource(R.string.cd_dev_stats_chart)

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("dev_stats_chart_card"),
    shape = MaterialTheme.shapes.large,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      // Header + Fictional Sample/Local label
      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            Icon(
              imageVector = Icons.Outlined.BarChart,
              contentDescription = null,
              tint = BurntOrange,
              modifier = Modifier.size(18.dp),
            )
            Text(
              text = stringResource(R.string.dev_stats_section_eyebrow),
              style = MaterialTheme.typography.labelSmall,
              color = BurntOrange,
              fontWeight = FontWeight.Bold,
            )
          }
          Text(
            text = stringResource(R.string.dev_stats_card_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = stringResource(R.string.dev_stats_card_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        FictionalDemoBadge(text = "7-Day Log")
      }

      // Metric Selector Toggle Pills (48dp min touch targets)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        MetricSelectorChip(
          label = stringResource(R.string.dev_stats_metric_minutes),
          isSelected = selectedMetric == DevelopmentMetric.PRACTICE_MINUTES,
          testTag = "stats_metric_minutes",
          onClick = { selectedMetric = DevelopmentMetric.PRACTICE_MINUTES },
          modifier = Modifier.weight(1f),
        )
        MetricSelectorChip(
          label = stringResource(R.string.dev_stats_metric_reps),
          isSelected = selectedMetric == DevelopmentMetric.FOCUS_REPS,
          testTag = "stats_metric_reps",
          onClick = { selectedMetric = DevelopmentMetric.FOCUS_REPS },
          modifier = Modifier.weight(1f),
        )
      }

      // 3-KPI Summary Row
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          StatSummaryCell(
            label = stringResource(R.string.dev_stats_total_label),
            value = "$totalValue ${selectedMetric.unit}",
            modifier = Modifier
              .weight(1f)
              .testTag("stats_total_value"),
          )
          StatSummaryCell(
            label = stringResource(R.string.dev_stats_active_days_label),
            value = "$activeDays / 7",
            modifier = Modifier
              .weight(1f)
              .testTag("stats_active_days_value"),
          )
          StatSummaryCell(
            label = stringResource(R.string.dev_stats_best_day_label),
            value = "$bestValue ${selectedMetric.unit}",
            modifier = Modifier
              .weight(1f)
              .testTag("stats_best_day_value"),
          )
        }
      }

      // Native Compose 7-Day Bar Chart Area
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .semantics { contentDescription = chartAccessibilitySummary }
          .testTag("dev_stats_bar_chart"),
        verticalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = "${selectedMetric.label} (${selectedMetric.unit})",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Text(
            text = "Scale: 0 – $chartCeiling ${selectedMetric.unit}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }

        val gridColor = OutlineWarm
        val baselineColor = MaterialTheme.colorScheme.outline

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(176.dp),
        ) {
          // Horizontal dashed guide lines + solid baseline drawn behind bars
          Canvas(
            modifier = Modifier
              .fillMaxSize()
              .padding(top = 24.dp, bottom = 28.dp),
          ) {
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
            val topY = 0f
            val midY = size.height * 0.5f
            val bottomY = size.height

            drawLine(
              color = gridColor.copy(alpha = 0.65f),
              start = Offset(0f, topY),
              end = Offset(size.width, topY),
              strokeWidth = 1.dp.toPx(),
              pathEffect = dashEffect,
            )
            drawLine(
              color = gridColor.copy(alpha = 0.65f),
              start = Offset(0f, midY),
              end = Offset(size.width, midY),
              strokeWidth = 1.dp.toPx(),
              pathEffect = dashEffect,
            )
            drawLine(
              color = baselineColor.copy(alpha = 0.8f),
              start = Offset(0f, bottomY),
              end = Offset(size.width, bottomY),
              strokeWidth = 1.5.dp.toPx(),
            )
          }

          Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.Bottom,
          ) {
            stats.forEachIndexed { index, stat ->
              val rawValue = stat.valueFor(selectedMetric)
              val targetFraction = (rawValue.toFloat() / chartCeiling.toFloat()).coerceIn(0f, 1f)
              val animatedFraction by animateFloatAsState(
                targetValue = targetFraction,
                animationSpec = spring(
                  dampingRatio = Spring.DampingRatioNoBouncy,
                  stiffness = Spring.StiffnessMediumLow,
                ),
                label = "bar_height_$index",
              )
              val isSelected = index == safeSelectedIndex
              val isTodayLive = stat.dayOffsetFromToday == 0 && stat.hasLiveSubmission

              val barBrush = when {
                rawValue == 0 -> Brush.verticalGradient(
                  colors = listOf(OutlineWarm.copy(alpha = 0.65f), OutlineWarm.copy(alpha = 0.45f)),
                )
                isSelected || isTodayLive -> Brush.verticalGradient(
                  colors = listOf(BurntOrange, BurntOrangeDark),
                )
                else -> Brush.verticalGradient(
                  colors = listOf(NavyPrimary, NavyDeep),
                )
              }

              val dayDesc = buildString {
                append("${stat.dayLabel} (${stat.fullDateLabel}): ")
                append("${stat.practiceMinutes} practice minutes, ${stat.focusReps} focus reps. ")
                append("Focus: ${stat.focusTag}.")
                if (isSelected) append(" Selected.")
              }

              Column(
                modifier = Modifier
                  .weight(1f)
                  .fillMaxHeight()
                  .clip(RoundedCornerShape(10.dp))
                  .background(
                    if (isSelected) BurntOrangeTint.copy(alpha = 0.45f) else Color.Transparent,
                  )
                  .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(),
                    onClick = { selectedDayIndex = index },
                  )
                  .semantics(mergeDescendants = true) {
                    contentDescription = dayDesc
                    selected = isSelected
                    role = Role.Button
                  }
                  .testTag("stats_day_bar_$index"),
                horizontalAlignment = Alignment.CenterHorizontally,
              ) {
                // Numeric value label above bar
                Box(
                  modifier = Modifier.height(24.dp),
                  contentAlignment = Alignment.BottomCenter,
                ) {
                  Text(
                    text = if (rawValue > 0) "$rawValue" else "–",
                    style = MaterialTheme.typography.labelSmall,
                    color = when {
                      isSelected -> BurntOrangeDark
                      rawValue > 0 -> MaterialTheme.colorScheme.onSurface
                      else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    fontWeight = if (isSelected || isTodayLive) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                  )
                }

                // Bar track & filled bar
                Box(
                  modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                  contentAlignment = Alignment.BottomCenter,
                ) {
                  val displayFraction = if (rawValue == 0) 0.05f else animatedFraction.coerceAtLeast(0.1f)
                  Box(
                    modifier = Modifier
                      .fillMaxWidth(0.78f)
                      .fillMaxHeight(displayFraction)
                      .clip(RoundedCornerShape(topStart = 7.dp, topEnd = 7.dp, bottomStart = 2.dp, bottomEnd = 2.dp))
                      .background(barBrush),
                  )
                }

                // Day label below baseline
                Box(
                  modifier = Modifier.height(28.dp),
                  contentAlignment = Alignment.Center,
                ) {
                  Text(
                    text = stat.dayLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) BurntOrangeDark else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isSelected || stat.dayOffsetFromToday == 0) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    textAlign = TextAlign.Center,
                  )
                }
              }
            }
          }
        }
      }

      // Selected Day Detail Card (updates on bar tap)
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .semantics(mergeDescendants = true) {}
          .testTag("stats_selected_day_detail"),
        shape = MaterialTheme.shapes.medium,
        color = NavyDeep,
      ) {
        Column(
          modifier = Modifier.padding(12.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            Text(
              text = "${selectedDay.dayLabel.uppercase()} · ${selectedDay.fullDateLabel.uppercase()}",
              style = MaterialTheme.typography.labelSmall,
              color = BurntOrange,
              fontWeight = FontWeight.Bold,
            )
            Surface(
              shape = RoundedCornerShape(50),
              color = if (selectedDay.hasLiveSubmission) {
                SuccessGreen.copy(alpha = 0.28f)
              } else {
                Color.White.copy(alpha = 0.14f)
              },
            ) {
              Text(
                text = when {
                  selectedDay.hasLiveSubmission -> "Logged by $playerFirstName"
                  selectedDay.practiceMinutes == 0 -> "Rest Day"
                  else -> "Sample Week 2 Log"
                },
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
              )
            }
          }
          Text(
            text = selectedDay.focusTag,
            style = MaterialTheme.typography.titleSmall,
            color = Color.White,
          )
          Text(
            text = if (selectedDay.practiceMinutes == 0 && selectedDay.focusReps == 0) {
              if (selectedDay.dayOffsetFromToday == 0) {
                "Complete today's drill and reflection to log minutes and focus reps here."
              } else {
                "Scheduled recovery day with 0 practice minutes logged."
              }
            } else {
              "${selectedDay.practiceMinutes} practice minutes · ${selectedDay.focusReps} focus reps"
            },
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.85f),
          )
        }
      }
    }
  }
}

@Composable
private fun MetricSelectorChip(
  label: String,
  isSelected: Boolean,
  testTag: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    onClick = onClick,
    modifier = modifier
      .defaultMinSize(minHeight = 48.dp)
      .semantics(mergeDescendants = true) {
        contentDescription = "$label chart view${if (isSelected) ", selected" else ""}"
        selected = isSelected
        role = Role.Button
      }
      .testTag(testTag),
    shape = RoundedCornerShape(12.dp),
    color = if (isSelected) NavyDeep else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    border = BorderStroke(
      width = if (isSelected) 1.5.dp else 1.dp,
      color = if (isSelected) BurntOrange else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
    ),
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 10.dp, vertical = 10.dp),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

@Composable
private fun StatSummaryCell(
  label: String,
  value: String,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier
      .semantics(mergeDescendants = true) {}
      .padding(horizontal = 4.dp),
    horizontalAlignment = Alignment.Start,
    verticalArrangement = Arrangement.spacedBy(2.dp),
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
    Text(
      text = value,
      style = MaterialTheme.typography.titleSmall,
      color = MaterialTheme.colorScheme.onSurface,
      fontWeight = FontWeight.Bold,
      maxLines = 1,
    )
  }
}
