package com.monetra.presentation.screen.notes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.monetra.domain.model.Note
import com.monetra.presentation.component.SwipeToDeleteContainer
import com.monetra.ui.theme.Spacing
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    onNavigateBack: () -> Unit,
    onAddNoteClick: () -> Unit,
    onNoteClick: (Long) -> Unit,
    viewModel: NotesViewModel = hiltViewModel()
) {
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                com.monetra.presentation.component.MonetraSnackbar(snackbarData = data)
            }
        },
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            "My Notes",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        scrolledContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
                // Divider line for Cupertino feel
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    thickness = 0.5.dp
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddNoteClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = com.monetra.ui.theme.ComponentDefaults.fabShape,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = com.monetra.ui.theme.Elevation.raisedCard
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Note")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SearchBar(
                query = searchQuery,
                onQueryChange = viewModel::onSearchQueryChange,
                modifier = Modifier.padding(
                    horizontal = Spacing.screenHorizontal,
                    vertical = Spacing.md
                )
            )

            if (notes.isEmpty()) {
                EmptyNotesView(isSearching = searchQuery.isNotEmpty())
            } else {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        horizontal = Spacing.screenHorizontal,
                        vertical = Spacing.md
                    ),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                    verticalItemSpacing = Spacing.md
                ) {
                    items(notes, key = { it.id }) { note ->
                        val index = notes.indexOf(note)
                        StaggeredListItem(index = index) {
                            SwipeToDeleteContainer(
                                onDelete = {
                                    viewModel.deleteNote(note)
                                    scope.launch {
                                        snackbarHostState.currentSnackbarData?.dismiss()
                                        val result = snackbarHostState.showSnackbar(
                                            message = "Note deleted",
                                            actionLabel = "Undo",
                                            duration = SnackbarDuration.Short
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            viewModel.restoreNote(note)
                                        }
                                    }
                                },
                                title = "Delete Note?",
                                message = "Are you sure you want to delete this note?"
                            ) {
                                NoteItem(
                                    note = note,
                                    onClick = { onNoteClick(note.id) }
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
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = {
            Text(
                "Search notes...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        },
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        singleLine = true,
        shape = com.monetra.ui.theme.ComponentDefaults.textFieldShape,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        textStyle = MaterialTheme.typography.bodyLarge
    )
}

@Composable
fun NoteItem(
    note: Note,
    onClick: () -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }
    val formattedDate = dateFormatter.format(Date(note.updatedAt))

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = com.monetra.ui.theme.ComponentDefaults.cardShape,
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = com.monetra.ui.theme.Elevation.card
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(Spacing.cardPadding)
        ) {
            if (note.title.isNotEmpty()) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 10,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(Spacing.md))
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun EmptyNotesView(isSearching: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 100.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
            )
            Spacer(modifier = Modifier.height(Spacing.lg))
            Text(
                text = if (isSearching) "No matches found" else "No notes yet",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(Spacing.xs))
            Text(
                text = if (isSearching) "Try a different search term" else "Tap + to capture your thoughts",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}
