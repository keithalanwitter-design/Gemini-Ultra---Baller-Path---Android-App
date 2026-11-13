package com.example.ui.components

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.SportsBasketball
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.SportsBasketball
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.BurntOrange
import com.example.ui.theme.BurntOrangeTint
import com.example.ui.theme.NavyDeep

enum class TopLevelTab(
  val route: String,
  val labelRes: Int,
  val testTag: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
) {
  TODAY(
    route = "today",
    labelRes = R.string.nav_today,
    testTag = "nav_today",
    selectedIcon = Icons.Filled.SportsBasketball,
    unselectedIcon = Icons.Outlined.SportsBasketball,
  ),
  PLAN(
    route = "plan",
    labelRes = R.string.nav_plan,
    testTag = "nav_plan",
    selectedIcon = Icons.Filled.CalendarMonth,
    unselectedIcon = Icons.Outlined.CalendarMonth,
  ),
  WORK_FEEDBACK(
    route = "work_feedback",
    labelRes = R.string.nav_work_feedback,
    testTag = "nav_work_feedback",
    selectedIcon = Icons.Filled.Forum,
    unselectedIcon = Icons.Outlined.Forum,
  ),
  ACCOUNT(
    route = "account",
    labelRes = R.string.nav_account,
    testTag = "nav_account",
    selectedIcon = Icons.Filled.AccountCircle,
    unselectedIcon = Icons.Outlined.AccountCircle,
  ),
}

@Composable
fun BallerPathBottomBar(
  currentTab: TopLevelTab,
  onSelectTab: (TopLevelTab) -> Unit,
  modifier: Modifier = Modifier,
) {
  NavigationBar(
    modifier = modifier.testTag("bottom_navigation_bar"),
    containerColor = MaterialTheme.colorScheme.surface,
    tonalElevation = 3.dp,
    windowInsets = WindowInsets.navigationBars,
  ) {
    TopLevelTab.entries.forEach { tab ->
      val selected = currentTab == tab
      val label = stringResource(tab.labelRes)
      val displayLabel = if (tab == TopLevelTab.WORK_FEEDBACK) "Work +\nFeedback" else label
      NavigationBarItem(
        selected = selected,
        onClick = { onSelectTab(tab) },
        modifier = Modifier
          .defaultMinSize(minHeight = 48.dp)
          .semantics(mergeDescendants = true) {
            contentDescription = "$label tab"
          }
          .testTag(tab.testTag),
        icon = {
          Icon(
            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
            contentDescription = null,
          )
        },
        label = {
          Text(
            text = displayLabel,
            fontSize = 10.sp,
            lineHeight = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 2,
            softWrap = true,
            overflow = TextOverflow.Ellipsis,
          )
        },
        colors = NavigationBarItemDefaults.colors(
          selectedIconColor = BurntOrange,
          selectedTextColor = MaterialTheme.colorScheme.onSurface,
          indicatorColor = BurntOrangeTint,
          unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
          unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
      )
    }
  }
}

@Composable
fun BallerPathNavRail(
  currentTab: TopLevelTab,
  onSelectTab: (TopLevelTab) -> Unit,
  modifier: Modifier = Modifier,
) {
  NavigationRail(
    modifier = modifier.testTag("navigation_rail"),
    containerColor = MaterialTheme.colorScheme.surface,
  ) {
    Spacer(modifier = Modifier.height(12.dp))
    TopLevelTab.entries.forEach { tab ->
      val selected = currentTab == tab
      val label = stringResource(tab.labelRes)
      val displayLabel = if (tab == TopLevelTab.WORK_FEEDBACK) "Work +\nFeedback" else label
      NavigationRailItem(
        selected = selected,
        onClick = { onSelectTab(tab) },
        modifier = Modifier
          .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
          .semantics(mergeDescendants = true) {
            contentDescription = "$label tab"
          }
          .testTag(tab.testTag),
        icon = {
          Icon(
            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
            contentDescription = null,
          )
        },
        label = {
          Text(
            text = displayLabel,
            fontSize = 11.sp,
            lineHeight = 13.sp,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
          )
        },
        colors = NavigationRailItemDefaults.colors(
          selectedIconColor = BurntOrange,
          selectedTextColor = MaterialTheme.colorScheme.onSurface,
          indicatorColor = BurntOrangeTint,
          unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
          unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
      )
    }
  }
}

@Composable
fun FictionalDemoBadge(
  text: String,
  modifier: Modifier = Modifier,
  onDarkBackground: Boolean = false,
) {
  val bg = if (onDarkBackground) Color.White.copy(alpha = 0.14f) else MaterialTheme.colorScheme.secondaryContainer
  val fg = if (onDarkBackground) Color.White else MaterialTheme.colorScheme.onSecondaryContainer
  val border = if (onDarkBackground) Color.White.copy(alpha = 0.24f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(50),
    color = bg,
    border = BorderStroke(1.dp, border),
  ) {
    Text(
      text = text,
      style = MaterialTheme.typography.labelSmall,
      color = fg,
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
    )
  }
}

@Composable
fun CoachKeithAvatar(
  sizeDp: Int = 44,
  modifier: Modifier = Modifier,
) {
  Image(
    painter = painterResource(id = R.drawable.coach_keith),
    contentDescription = stringResource(R.string.cd_coach_keith_avatar),
    contentScale = ContentScale.Crop,
    modifier = modifier
      .size(sizeDp.dp)
      .clip(CircleShape)
      .background(NavyDeep),
  )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlayerHeaderBar(
  playerName: String,
  jerseyAndPosition: String,
  demoBadgeLabel: String,
  modifier: Modifier = Modifier,
) {
  FlowRow(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalArrangement = Arrangement.spacedBy(6.dp),
  ) {
    androidx.compose.foundation.layout.Column(
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color.White,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
      ) {
        Image(
          painter = painterResource(id = R.drawable.logo_baller_path_horizontal),
          contentDescription = stringResource(R.string.cd_baller_path_logo),
          contentScale = ContentScale.Fit,
          modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 3.dp)
            .height(22.dp)
            .testTag("baller_path_horizontal_logo"),
        )
      }
      Text(
        text = "$playerName · $jerseyAndPosition",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground,
      )
    }
    if (demoBadgeLabel.isNotBlank()) {
      FictionalDemoBadge(
        text = demoBadgeLabel,
        modifier = Modifier.align(Alignment.CenterVertically),
      )
    }
  }
}

fun openExternalUrl(context: Context, url: String) {
  val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
    if (context !is Activity) {
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
  }
  runCatching {
    context.startActivity(intent)
  }
}

@Composable
fun PrivacyPolicyLinkCard(
  linkTestTag: String,
  urlTestTag: String,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val privacyLabel = stringResource(R.string.privacy_policy_label)
  val privacyUrl = stringResource(R.string.privacy_policy_url)
  val privacyCd = stringResource(R.string.cd_privacy_policy_link)

  Surface(
    onClick = { openExternalUrl(context, privacyUrl) },
    modifier = modifier
      .fillMaxWidth()
      .defaultMinSize(minHeight = 48.dp)
      .semantics(mergeDescendants = true) {
        contentDescription = privacyCd
        role = Role.Button
      }
      .testTag(linkTestTag),
    shape = MaterialTheme.shapes.small,
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(2.dp),
      ) {
        Text(
          text = privacyLabel,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
          text = privacyUrl,
          style = MaterialTheme.typography.bodySmall.copy(
            textDecoration = TextDecoration.Underline,
            fontWeight = FontWeight.Medium,
          ),
          color = NavyDeep,
          modifier = Modifier.testTag(urlTestTag),
        )
      }
      Icon(
        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
        contentDescription = null,
        tint = BurntOrange,
        modifier = Modifier
          .padding(start = 8.dp)
          .size(18.dp),
      )
    }
  }
}

@Composable
fun HeadlineVisualBanner(
  drawableRes: Int,
  contentDescription: String,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = MaterialTheme.shapes.large,
    color = NavyDeep,
    border = BorderStroke(1.dp, BurntOrange.copy(alpha = 0.4f)),
  ) {
    Image(
      painter = painterResource(id = drawableRes),
      contentDescription = contentDescription,
      contentScale = ContentScale.Crop,
      modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(1f)
        .clip(MaterialTheme.shapes.large),
    )
  }
}
