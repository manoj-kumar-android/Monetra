package com.monetra.presentation.screen.notes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monetra.domain.model.Note
import com.monetra.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditNoteViewModel @Inject constructor(
    private val repository: NoteRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private var noteId: Long? = null

    private val _title = MutableStateFlow("")
    val title = _title.asStateFlow()

    private val _content = MutableStateFlow("")
    val content = _content.asStateFlow()

    private var existingNote: Note? = null

    fun loadNote(id: Long?) {
        if (id == null || id == -1L || id == 0L) {
            this.noteId = null
            _title.value = ""
            _content.value = ""
            existingNote = null
            return
        }

        if (this.noteId == id) return
        this.noteId = id

        viewModelScope.launch {
            repository.getNoteById(id)?.let { note ->
                existingNote = note
                _title.value = note.title
                _content.value = note.content
            } ?: run {
                this@AddEditNoteViewModel.noteId = null
                _title.value = ""
                _content.value = ""
                existingNote = null
            }
        }
    }

    fun onTitleChange(newTitle: String) {
        _title.value = newTitle
    }

    fun onContentChange(newContent: String) {
        _content.value = newContent
    }

    fun saveNote(onSuccess: () -> Unit) {
        if (_title.value.isBlank() && _content.value.isBlank()) {
            onSuccess()
            return
        }

        viewModelScope.launch {
            val note = existingNote?.copy(
                title = _title.value,
                content = _content.value,
                updatedAt = System.currentTimeMillis()
            ) ?: Note(
                title = _title.value,
                content = _content.value
            )
            repository.insertNote(note)
            onSuccess()
        }
    }
}
