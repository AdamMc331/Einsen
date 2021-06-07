package dev.spikeysanju.einsen.view.add

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.spikeysanju.einsen.R
import dev.spikeysanju.einsen.components.InputTextField
import dev.spikeysanju.einsen.components.Message
import dev.spikeysanju.einsen.components.PrimaryButton
import dev.spikeysanju.einsen.components.StepSlider
import dev.spikeysanju.einsen.components.TopBarWithBack
import dev.spikeysanju.einsen.model.Task
import dev.spikeysanju.einsen.model.TaskStatus
import dev.spikeysanju.einsen.navigation.MainActions
import dev.spikeysanju.einsen.ui.theme.typography
import dev.spikeysanju.einsen.utils.EmojiViewState
import dev.spikeysanju.einsen.utils.showToast
import dev.spikeysanju.einsen.view.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@ExperimentalFoundationApi
@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@Composable
fun AddTaskScreen(viewModel: MainViewModel, actions: MainActions) {
    val bottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    var emojiState by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    var urgencyState by remember { mutableStateOf(0F) }
    var importanceState by remember { mutableStateOf(0F) }
    val stepCount by remember { mutableStateOf(5) }
    val result = viewModel.emoji.collectAsState().value

    ModalBottomSheetLayout(sheetState = bottomSheetState, sheetContent = {

        LazyVerticalGrid(
            cells = GridCells.Adaptive(minSize = 128.dp)
        ) {
            // get all emoji
            viewModel.getAllEmoji(context)
            // parse emoji into ViewStates
            when (result) {
                EmojiViewState.Empty -> {
                    item {
                        Message(title = "Empty")
                    }
                }
                is EmojiViewState.Error -> {
                    item {
                        Message("Error ${result.exception}")
                    }
                }
                EmojiViewState.Loading -> {
                    item {
                        Message("Loading")
                    }
                }
                is EmojiViewState.Success -> {
                    items(result.emojiItem) { emoji ->
                        EmojiPlaceHolderSmall(emoji = emoji.emoji, onSelect = {
                            scope.launch {
                                emojiState = it
                                bottomSheetState.hide()
                            }
                        })
                    }
                }
            }

        }

    }) {
        Scaffold(topBar = {
            TopBarWithBack(title = stringResource(R.string.text_addTask), actions.upPress)
        }) {

            LazyColumn(state = listState) {

                // Emoji
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    EmojiPlaceHolder(emoji = emojiState, onTap = {
                        scope.launch {
                            bottomSheetState.show()
                        }
                    })
                }

                // Title
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    InputTextField(title = stringResource(R.string.text_title), value = title) {
                        title = it
                    }
                }

                // Description
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    InputTextField(
                        title = stringResource(R.string.text_description),
                        value = description
                    ) {
                        description = it
                    }
                }

                // Category
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    InputTextField(
                        title = stringResource(R.string.text_category),
                        value = category
                    ) {
                        category = it
                    }
                }

                // Urgency
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.text_urgency),
                            style = typography.subtitle1,
                            color = MaterialTheme.colors.onPrimary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        StepSlider(stepCount = stepCount, value = urgencyState) {
                            urgencyState = it
                        }
                    }
                }

                // Importance
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.text_importance),
                            style = typography.subtitle1,
                            color = MaterialTheme.colors.onPrimary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        StepSlider(stepCount = stepCount, value = importanceState) {
                            importanceState = it
                        }
                    }
                }

                // Save Task
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    PrimaryButton(title = stringResource(R.string.text_save_task)) {
                        val task = Task(
                            title = title,
                            description = description,
                            category = category,
                            emoji = "🚀",
                            urgency = urgencyState,
                            importance = importanceState,
                            due = "18/12/2021",
                            status = TaskStatus.TODO
                        )

                        when {
                            title.isEmpty() -> showToast(context, "Title is Empty!")
                            description.isEmpty() -> showToast(context, "Description is Empty!")
                            category.isEmpty() -> showToast(context, "Category is Empty!")
                            else -> viewModel.insertTask(task).run {
                                showToast(context, "Task Added Successfully!")
                            }
                        }
                    }
                }
            }

        }

    }

}

@Composable
fun EmojiPlaceHolder(emoji: String, onTap: () -> Unit) {
    Box(
        modifier = Modifier
            .size(100.dp)
            .clip(CircleShape)
            .clickable { onTap() }
            .background(color = MaterialTheme.colors.onPrimary), contentAlignment = Alignment.Center
    ) {
        Text(
            text = emoji,
            style = typography.h3,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colors.onSecondary
        )
    }
}

@Composable
fun EmojiPlaceHolderSmall(emoji: String, onSelect: (String) -> Unit) {
    Box(
        modifier = Modifier
            .size(50.dp)
            .clip(CircleShape)
            .clickable { onSelect(emoji) }
            .background(color = MaterialTheme.colors.onPrimary), contentAlignment = Alignment.Center
    ) {
        Text(
            text = emoji,
            style = typography.h5,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colors.background
        )
    }
}




