package com.example.tugasto.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tugasto.data.local.dao.TuGastoDao
import com.example.tugasto.data.local.entity.CategoryEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoryEditorState(
    val id: Int = 0,
    val name: String = "",
    val iconName: String = "category",
    val colorHex: String = "#6B7280",
    val isNew: Boolean = true
)

val PRESET_COLORS = listOf(
    "#EF4444", "#F97316", "#EAB308", "#22C55E",
    "#14B8A6", "#3B82F6", "#6366F1", "#8B5CF6",
    "#EC4899", "#6B7280", "#84CC16", "#F59E0B"
)

val PRESET_ICONS = listOf(
    "local_dining", "directions_bus", "electric_bolt", "confirmation_number",
    "health_and_safety", "school", "shopping_bag", "home",
    "work", "category", "shopping_cart", "fitness_center"
)

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val dao: TuGastoDao
) : ViewModel() {

    val categories: StateFlow<List<CategoryEntity>> = dao.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _editorState = MutableStateFlow<CategoryEditorState?>(null)
    val editorState: StateFlow<CategoryEditorState?> = _editorState.asStateFlow()

    fun startCreate() {
        _editorState.value = CategoryEditorState(isNew = true)
    }

    fun startEdit(cat: CategoryEntity) {
        _editorState.value = CategoryEditorState(
            id = cat.id, name = cat.name,
            iconName = cat.iconName, colorHex = cat.colorHex, isNew = false
        )
    }

    fun updateEditorName(name: String) {
        _editorState.value = _editorState.value?.copy(name = name)
    }

    fun updateEditorIcon(icon: String) {
        _editorState.value = _editorState.value?.copy(iconName = icon)
    }

    fun updateEditorColor(color: String) {
        _editorState.value = _editorState.value?.copy(colorHex = color)
    }

    fun dismissEditor() { _editorState.value = null }

    fun saveCategory() {
        val s = _editorState.value ?: return
        if (s.name.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            if (s.isNew) {
                dao.insertCategories(listOf(CategoryEntity(name = s.name.trim(), iconName = s.iconName, colorHex = s.colorHex)))
            } else {
                dao.updateCategory(CategoryEntity(id = s.id, name = s.name.trim(), iconName = s.iconName, colorHex = s.colorHex))
            }
            _editorState.value = null
        }
    }

    fun deleteCategory(id: Int) {
        viewModelScope.launch(Dispatchers.IO) { dao.deleteCategoryById(id) }
    }
}
