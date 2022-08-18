package ru.kiruxadance.notesapp.note.presentation.add_edit_note

import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.flow.collectLatest
import ru.kiruxadance.notesapp.note.presentation.add_edit_note.components.ControlsBar
import ru.kiruxadance.notesapp.note.presentation.add_edit_note.components.TransparentHintTextField
import ru.kiruxadance.notesapp.note.presentation.add_edit_note.controllers.rememberDrawController
import ru.kiruxadance.notesapp.note.presentation.add_edit_note.utils.createPath

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AddEditNoteScreen(
    navController: NavController,
    viewModel: AddEditNoteViewModel = hiltViewModel()
) {
    val titleState = viewModel.noteTitle.value
    val contentState = viewModel.noteContent.value
    val editTypeState = viewModel.noteEditType

    val drawController = rememberDrawController()

    val undoVisibility = remember { mutableStateOf(false) }
    val redoVisibility = remember { mutableStateOf(false) }
    val colorBarVisibility = remember { mutableStateOf(false) }
    val sizeBarVisibility = remember { mutableStateOf(false) }
    val currentColor = remember { mutableStateOf(Color.Red) }
    val bg = MaterialTheme.colors.background
    val currentBgColor = remember { mutableStateOf(bg) }
    val currentSize = remember { mutableStateOf(10) }
    val colorIsBg = remember { mutableStateOf(false) }

    val scaffoldState = rememberScaffoldState()

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when(event) {
                is AddEditNoteViewModel.UiEvent.ShowSnackbar -> {
                    scaffoldState.snackbarHostState.showSnackbar(
                        message = event.message
                    )
                }
                is AddEditNoteViewModel.UiEvent.SaveNote -> {
                    navController.navigateUp()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Add or update Note") },
                actions = {
                    IconButton(onClick = {
                        viewModel.onEvent(AddEditNoteEvent.ChangeEditTypeState)
                    }) {
                        Icon(editTypeState.value.appBarImage, contentDescription = "Localized description")
                    }
                    IconButton(onClick = {
                        viewModel.onEvent(AddEditNoteEvent.SaveNote)
                    }) {
                        Icon(imageVector = Icons.Default.Done, contentDescription = "Save note")
                    }
                }
            )
        },
        scaffoldState = scaffoldState
    ) { contentPadding ->
        Box(modifier = Modifier.padding(contentPadding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()){

                Column( modifier = Modifier
                    .fillMaxSize()
                    .weight(1f, fill = false)
                    .pointerInteropFilter {
                        when (it.action) {
                            MotionEvent.ACTION_DOWN -> {
                                drawController.insertNewPath(Offset(it.x, it.y))
                            }
                            MotionEvent.ACTION_MOVE -> {
                                drawController.updateLatestPath(Offset(it.x, it.y))
                            }
                            MotionEvent.ACTION_UP -> {}
                            else -> false
                        }
                        editTypeState.value.isEditTypeDraw
                    }) {

                    Canvas(modifier = Modifier.zIndex(100f)) {
                        drawController.pathList.forEach { pw ->
                            drawPath(
                                createPath(pw.points),
                                color = pw.strokeColor,
                                alpha = pw.alpha,
                                style = Stroke(
                                    width = pw.strokeWidth,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    TransparentHintTextField(
                        text = titleState.text,
                        hint = titleState.hint,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        onValueChange = {
                            viewModel.onEvent(AddEditNoteEvent.EnteredTitle(it))
                        },
                        onFocusChange = {
                            viewModel.onEvent(AddEditNoteEvent.ChangeTitleFocus(it))
                        },
                        isHintVisible = titleState.isHintVisible,
                        singleLine = true,
                        textStyle = MaterialTheme.typography.h5,
                        focusable = !editTypeState.value.isEditTypeDraw
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    TransparentHintTextField(
                        text = contentState.text,
                        hint = contentState.hint,
                        onValueChange = {
                            viewModel.onEvent(AddEditNoteEvent.EnteredContent(it))
                        },
                        onFocusChange = {
                            viewModel.onEvent(AddEditNoteEvent.ChangeContentFocus(it))
                        },
                        isHintVisible = contentState.isHintVisible,
                        textStyle = MaterialTheme.typography.body1,
                        modifier = Modifier
                            .padding(horizontal = 16.dp),
                        focusable = !editTypeState.value.isEditTypeDraw
                    )

                }

                ControlsBar(
                    drawController = drawController,
                    {

                    },
                    {
                        colorBarVisibility.value = when (colorBarVisibility.value) {
                            false -> true
                            colorIsBg.value -> true
                            else -> false
                        }
                        colorIsBg.value = false
                        sizeBarVisibility.value = false
                    },
                    {
                        colorBarVisibility.value = when (colorBarVisibility.value) {
                            false -> true
                            !colorIsBg.value -> true
                            else -> false
                        }
                        colorIsBg.value = true
                        sizeBarVisibility.value = false
                    },
                    {
                        sizeBarVisibility.value = !sizeBarVisibility.value
                        colorBarVisibility.value = false
                    },
                    undoVisibility = undoVisibility,
                    redoVisibility = redoVisibility,
                    colorValue = currentColor,
                    bgColorValue = currentBgColor,
                    sizeValue = currentSize
                )
            }
        }
    }
}