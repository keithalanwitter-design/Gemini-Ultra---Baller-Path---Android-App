package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.components.CoachKeithAvatar
import com.example.ui.theme.BurntOrange
import com.example.ui.theme.BurntOrangeTint
import com.example.ui.theme.NavyDeep

@Composable
fun ParentConsentScreen(
  onConfirmConsent: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var isChecked by rememberSaveable { mutableStateOf(false) }
  var showError by rememberSaveable { mutableStateOf(false) }
  val scrollState = rememberScrollState()

  val checkboxCd = stringResource(R.string.cd_parent_consent_checkbox)
  val continueCd = stringResource(R.string.cd_parent_consent_continue)

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .statusBarsPadding()
      .navigationBarsPadding(),
    contentAlignment = Alignment.TopCenter,
  ) {
    Column(
      modifier = Modifier
        .widthIn(max = 600.dp)
        .fillMaxSize()
        .verticalScroll(scrollState)
        .padding(horizontal = 16.dp, vertical = 20.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = Color.White,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
          contentAlignment = Alignment.Center,
        ) {
          Image(
            painter = painterResource(id = R.drawable.logo_baller_path_stacked),
            contentDescription = stringResource(R.string.cd_baller_path_logo),
            contentScale = ContentScale.Fit,
            modifier = Modifier.height(78.dp),
          )
        }
      }

      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("parent_consent_card"),
        shape = MaterialTheme.shapes.large,
        color = NavyDeep,
      ) {
        Column(
          modifier = Modifier.padding(18.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Outlined.VerifiedUser,
              contentDescription = null,
              tint = BurntOrangeTint,
              modifier = Modifier.size(22.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "BEFORE FIRST USE",
              style = MaterialTheme.typography.labelMedium,
              color = BurntOrangeTint,
            )
          }

          Text(
            text = stringResource(R.string.parent_consent_title),
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
          )

          Text(
            text = stringResource(R.string.parent_consent_audience),
            style = MaterialTheme.typography.titleMedium,
            color = BurntOrangeTint,
            fontWeight = FontWeight.Bold,
          )

          Surface(
            shape = MaterialTheme.shapes.medium,
            color = Color.White.copy(alpha = 0.10f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)),
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              verticalAlignment = Alignment.Top,
            ) {
              CoachKeithAvatar(sizeDp = 38)
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                text = stringResource(R.string.parent_consent_coach_note),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
              )
            }
          }
        }
      }

      Surface(
        onClick = {
          isChecked = !isChecked
          if (isChecked) showError = false
        },
        modifier = Modifier
          .fillMaxWidth()
          .defaultMinSize(minHeight = 56.dp)
          .semantics(mergeDescendants = true) {
            contentDescription = checkboxCd
          }
          .testTag("consent_checkbox_row"),
        shape = MaterialTheme.shapes.medium,
        color = if (isChecked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
          width = if (isChecked) 1.5.dp else 1.dp,
          color = if (isChecked) BurntOrange else MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
        ),
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Checkbox(
            checked = isChecked,
            onCheckedChange = { checked ->
              isChecked = checked
              if (checked) showError = false
            },
            modifier = Modifier
              .size(48.dp)
              .testTag("consent_checkbox"),
            colors = CheckboxDefaults.colors(
              checkedColor = BurntOrange,
              checkmarkColor = Color.White,
            ),
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = stringResource(R.string.parent_consent_checkbox_label),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
          )
        }
      }

      if (showError) {
        Text(
          text = "Please check the parent or guardian confirmation box before continuing.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.error,
          modifier = Modifier.testTag("consent_validation_error"),
        )
      }

      Button(
        onClick = {
          if (isChecked) {
            onConfirmConsent()
          } else {
            showError = true
          }
        },
        modifier = Modifier
          .fillMaxWidth()
          .defaultMinSize(minHeight = 54.dp)
          .semantics { contentDescription = continueCd }
          .testTag("consent_continue_button"),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = BurntOrange,
          contentColor = Color.White,
        ),
      ) {
        Text(
          text = stringResource(R.string.parent_consent_continue),
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowForward,
          contentDescription = null,
          modifier = Modifier.size(18.dp),
        )
      }

      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
      ) {
        Column(
          modifier = Modifier.padding(12.dp),
          verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
          Text(
            text = stringResource(R.string.privacy_policy_label),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Text(
            text = stringResource(R.string.privacy_policy_url),
            style = MaterialTheme.typography.bodySmall,
            color = NavyDeep,
          )
        }
      }
    }
  }
}
