package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.BallerPathDatabase
import com.example.data.repository.LocalBallerPathRepository
import com.example.ui.BallerPathApp
import com.example.ui.theme.BallerPathTheme
import com.example.ui.viewmodel.BallerPathViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val database = BallerPathDatabase.getInstance(applicationContext)
    val repository = LocalBallerPathRepository(
      reflectionDao = database.reflectionDao(),
      practiceDraftDao = database.practiceDraftDao(),
      assignmentDao = database.assignmentDao(),
      setupStateDao = database.setupStateDao(),
      parentConsentDao = database.parentConsentDao(),
    )

    setContent {
      BallerPathTheme {
        val viewModel: BallerPathViewModel = viewModel(
          factory = BallerPathViewModel.Factory(
            repository = repository,
            owner = this,
          )
        )
        BallerPathApp(viewModel = viewModel)
      }
    }
  }
}
