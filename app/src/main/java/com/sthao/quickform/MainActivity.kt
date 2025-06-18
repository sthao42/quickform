package com.sthao.quickform

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.sthao.quickform.ui.components.DotsIndicator
import com.sthao.quickform.ui.components.FabRow
import com.sthao.quickform.ui.components.TopBanner
import com.sthao.quickform.ui.dropoff.DropoffScreen
import com.sthao.quickform.ui.pickup.PickupScreen
import com.sthao.quickform.ui.saved.SavedFormsScreen
import com.sthao.quickform.ui.theme.QuickFormTheme
import com.sthao.quickform.ui.viewmodel.FormEvent
import com.sthao.quickform.ui.viewmodel.FormViewModel
import com.sthao.quickform.ui.viewmodel.FormViewModelFactory
import kotlinx.coroutines.launch
import kotlin.math.abs

class MainActivity : ComponentActivity() {
    private val formViewModel: FormViewModel by viewModels {
        FormViewModelFactory(application)
    }

    companion object {
        private const val PAGE_COUNT = 3
        private const val PICKUP_PAGE_INDEX = 0
        private const val DROPOFF_PAGE_INDEX = 1
        private const val SAVED_FORMS_PAGE_INDEX = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QuickFormTheme {
                // The granular state collection for performance is kept.
                val pickupState by formViewModel.pickupState.collectAsState()
                val dropoffState by formViewModel.dropoffState.collectAsState()

                val context = LocalContext.current
                val coroutineScope = rememberCoroutineScope()
                val appPagerState = rememberPagerState(pageCount = { PAGE_COUNT })
                val focusManager = LocalFocusManager.current
                val customTextFieldColors = rememberCustomTextFieldColors()

                val nestedScrollConnection = remember {
                    object : NestedScrollConnection {
                        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset = Offset.Zero
                        override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                            if (source == NestedScrollSource.UserInput && abs(available.y) > abs(available.x)) {
                                return Offset(x = available.x, y = 0f)
                            }
                            return super.onPostScroll(consumed, available, source)
                        }
                    }
                }

                Scaffold(
                    topBar = { TopBanner() },
                ) { paddingValues ->
                    val formPageModifier =
                        Modifier
                            .fillMaxSize()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) {
                                focusManager.clearFocus()
                            }
                            .padding(paddingValues)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .padding(top = 5.dp, bottom = 135.dp)

                    Box(modifier = Modifier.fillMaxSize()) {
                        HorizontalPager(
                            state = appPagerState,
                            modifier = Modifier
                                .fillMaxSize()
                                .nestedScroll(nestedScrollConnection),
                        ) { page ->
                            when (page) {
                                PICKUP_PAGE_INDEX ->
                                    Column(modifier = formPageModifier) {
                                        PickupScreen(
                                            state = pickupState,
                                            onEvent = formViewModel::onEvent,
                                            customTextFieldColors = customTextFieldColors,
                                        )
                                    }
                                DROPOFF_PAGE_INDEX ->
                                    Column(modifier = formPageModifier) {
                                        DropoffScreen(
                                            state = dropoffState,
                                            onEvent = formViewModel::onEvent,
                                            customTextFieldColors = customTextFieldColors,
                                        )
                                    }
                                SAVED_FORMS_PAGE_INDEX ->
                                    Box(modifier = Modifier.padding(paddingValues)) {
                                        // FIX: Reverted to the original call signature to match SavedFormsScreen's expected parameters.
                                        SavedFormsScreen(
                                            formViewModel = formViewModel,
                                            onEntryClick = { selectedForm: FormEntryWithImages ->
                                                formViewModel.onEvent(FormEvent.LoadForm(selectedForm))
                                                coroutineScope.launch {
                                                    appPagerState.animateScrollToPage(PICKUP_PAGE_INDEX)
                                                }
                                            },
                                        )
                                    }
                            }
                        }

                        Column(
                            modifier =
                                Modifier
                                    .align(Alignment.BottomCenter)
                                    .navigationBarsPadding()
                                    .padding(bottom = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            FabRow(
                                onNewEntry = { formViewModel.onEvent(FormEvent.ClearForm) },
                                onSaveEntry = { formViewModel.onEvent(FormEvent.SaveOrUpdateForm(context)) },
                                onNavigateToSaved = {
                                    coroutineScope.launch { appPagerState.animateScrollToPage(SAVED_FORMS_PAGE_INDEX) }
                                },
                                modifier = Modifier.padding(bottom = 16.dp),
                            )
                            DotsIndicator(
                                pageCount = appPagerState.pageCount,
                                selectedIndex = appPagerState.currentPage,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun rememberCustomTextFieldColors(): TextFieldColors {
    return TextFieldDefaults.colors(
        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
        unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        cursorColor = MaterialTheme.colorScheme.onBackground,
        focusedLabelColor = MaterialTheme.colorScheme.onBackground,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.7f),
    )
}