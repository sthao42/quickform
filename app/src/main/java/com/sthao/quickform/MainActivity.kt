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
import com.sthao.quickform.ui.stations.StationsScreen // Added for Stations
import com.sthao.quickform.ui.theme.QuickFormTheme
import com.sthao.quickform.ui.viewmodel.FormEvent
import com.sthao.quickform.ui.viewmodel.FormFieldType
import com.sthao.quickform.ui.viewmodel.FormSection
import com.sthao.quickform.ui.viewmodel.FormViewModel
import com.sthao.quickform.ui.viewmodel.FormViewModelFactory
import kotlinx.coroutines.launch
import kotlin.math.abs

class MainActivity : ComponentActivity() {
    private val formViewModel: FormViewModel by viewModels {
        FormViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QuickFormTheme {
                val pickupState by formViewModel.pickupState.collectAsState()
                val dropoffState by formViewModel.dropoffState.collectAsState()
                val stationsState by formViewModel.stationsState.collectAsState() // Added for Stations

                val context = LocalContext.current
                val coroutineScope = rememberCoroutineScope()
                val appPagerState = rememberPagerState(pageCount = { 4 }) // Updated page count
                val focusManager = LocalFocusManager.current

                val nestedScrollConnection = remember {
                    object : NestedScrollConnection {
                        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                            return Offset.Zero
                        }

                        override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                            if (source == NestedScrollSource.UserInput) {
                                if (abs(available.y) > abs(available.x)) {
                                    return Offset(x = available.x, y = 0f)
                                }
                            }
                            return super.onPostScroll(consumed, available, source)
                        }
                    }
                }

                Scaffold(
                    topBar = { TopBanner() },
                ) { paddingValues ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        HorizontalPager(
                            state = appPagerState,
                            modifier = Modifier
                                .fillMaxSize()
                                .nestedScroll(nestedScrollConnection),
                        ) { page ->
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

                            when (page) {
                                0 ->
                                    Column(modifier = formPageModifier) {
                                        PickupScreen(
                                            state = pickupState,
                                            onEvent = formViewModel::onEvent,
                                            customTextFieldColors = TextFieldDefaults.colors(
                                                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                                                unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                                cursorColor = MaterialTheme.colorScheme.onBackground,
                                                focusedLabelColor = MaterialTheme.colorScheme.onBackground,
                                                unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                            ),
                                            runNumber = pickupState.run,
                                            onRunNumberChange = { newRun ->
                                                formViewModel.onEvent(FormEvent.UpdateField(FormSection.PICKUP, FormFieldType.RUN, newRun))
                                                formViewModel.onEvent(FormEvent.UpdateField(FormSection.DROPOFF, FormFieldType.RUN, newRun))
                                                formViewModel.onEvent(FormEvent.UpdateField(FormSection.STATIONS, FormFieldType.RUN, newRun))
                                            }
                                        )
                                    }
                                1 ->
                                    Column(modifier = formPageModifier) {
                                        DropoffScreen(
                                            state = dropoffState,
                                            onEvent = formViewModel::onEvent,
                                            customTextFieldColors = TextFieldDefaults.colors(
                                                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                                                unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                                cursorColor = MaterialTheme.colorScheme.onBackground,
                                                focusedLabelColor = MaterialTheme.colorScheme.onBackground,
                                                unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                            ),
                                            runNumber = dropoffState.run,
                                            onRunNumberChange = { newRun ->
                                                formViewModel.onEvent(FormEvent.UpdateField(FormSection.PICKUP, FormFieldType.RUN, newRun))
                                                formViewModel.onEvent(FormEvent.UpdateField(FormSection.DROPOFF, FormFieldType.RUN, newRun))
                                                formViewModel.onEvent(FormEvent.UpdateField(FormSection.STATIONS, FormFieldType.RUN, newRun))
                                            }
                                        )
                                    }
                                2 -> // Added for Stations
                                    Column(modifier = formPageModifier) {
                                        val stationsItemSections by formViewModel.stationsItemSections.collectAsState()
                                        StationsScreen(
                                            stationsState = stationsState,
                                            stationsItemSections = stationsItemSections,
                                            onEvent = formViewModel::onEvent,
                                            customTextFieldColors = TextFieldDefaults.colors(
                                                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                                                unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                                cursorColor = MaterialTheme.colorScheme.onBackground,
                                                focusedLabelColor = MaterialTheme.colorScheme.onBackground,
                                                unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                            ),
                                            onUpdateItemSection = { index, section ->
                                                formViewModel.onEvent(FormEvent.UpdateStationsItemSection(index, section))
                                            },
                                            onAddItemSection = {
                                                formViewModel.onEvent(FormEvent.AddStationsItemSection)
                                            },
                                            onRemoveItemSections = { indices ->
                                                formViewModel.onEvent(FormEvent.RemoveStationsItemSections(indices))
                                            }
                                        )
                                    }
                                3 -> // Adjusted index for SavedFormsScreen
                                    Box(modifier = Modifier.padding(paddingValues)) {
                                        SavedFormsScreen(
                                            formViewModel = formViewModel,
                                            onEntryClick = { selectedForm ->
                                                formViewModel.loadFormWithSections(selectedForm.formEntry.id)
                                                coroutineScope.launch {
                                                    appPagerState.animateScrollToPage(0)
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
                                    coroutineScope.launch { appPagerState.animateScrollToPage(3) } // Adjusted index for SavedFormsScreen
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
