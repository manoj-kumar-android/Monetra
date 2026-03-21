package com.monetra.presentation.screen.notes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.monetra.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditNoteScreen(
    noteId: Long? = null,
    onNavigateBack: () -> Unit,
    viewModel: AddEditNoteViewModel = hiltViewModel()
) {
    LaunchedEffect(noteId) {
        viewModel.loadNote(noteId)
    }
    val title by viewModel.title.collectAsStateWithLifecycle()
    val content by viewModel.content.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.saveNote(onNavigateBack) },
                        modifier = Modifier.padding(end = Spacing.sm)
                    ) {
                        Text(
                            "Save",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = Spacing.screenHorizontal)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(Spacing.md))

            // Bordered Title Field
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = com.monetra.ui.theme.ComponentDefaults.textFieldShape,
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                ),
                color = Color.Transparent
            ) {
                TextField(
                    value = title,
                    onValueChange = viewModel::onTitleChange,
                    placeholder = {
                        Text(
                            "Title",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            // Bordered Content Field
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                shape = com.monetra.ui.theme.ComponentDefaults.textFieldShape,
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                ),
                color = Color.Transparent
            ) {
                TextField(
                    value = content,
                    onValueChange = viewModel::onContentChange,
                    placeholder = {
                        Text(
                            "Note content...",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        )
                    },
                    modifier = Modifier.fillMaxSize(),
                    textStyle = MaterialTheme.typography.bodyLarge,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }

            Spacer(modifier = Modifier.height(Spacing.screenVertical))
        }
    }
}
