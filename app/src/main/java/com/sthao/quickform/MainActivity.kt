package com.sthao.quickform

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Storefront
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
import com.sthao.quickform.util.clearAppCache
import kotlinx.coroutines.launch
import kotlin.math.abs

class MainActivity : ComponentActivity() {
    private val formViewModel: FormViewModel by viewModels {
        FormViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display for a modern Material 3 look.
        enableEdgeToEdge()
        
        // Clear temporary cache on start to ensure no old data lingers.
        clearAppCache(this)

        setContent {
            QuickFormTheme {
                val pickupState by formViewModel.pickupState.collectAsState()
                val dropoffState by formViewModel.dropoffState.collectAsState()
                val stationsState by formViewModel.stationsState.collectAsState()

                val context = LocalContext.current
                val coroutineScope = rememberCoroutineScope()
                val appPagerState = rememberPagerState { 4 }
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
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.LocalShipping, contentDescription = null) },
                                label = { Text("Pickup") },
                                selected = appPagerState.currentPage == 0,
                                onClick = { coroutineScope.launch { appPagerState.animateScrollToPage(0) } },
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.PinDrop, contentDescription = null) },
                                label = { Text("Dropoff") },
                                selected = appPagerState.currentPage == 1,
                                onClick = { coroutineScope.launch { appPagerState.animateScrollToPage(1) } },
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.Storefront, contentDescription = null) },
                                label = { Text("Stations") },
                                selected = appPagerState.currentPage == 2,
                                onClick = { coroutineScope.launch { appPagerState.animateScrollToPage(2) } },
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.Storage, contentDescription = null) },
                                label = { Text("Saved") },
                                selected = appPagerState.currentPage == 3,
                                onClick = { coroutineScope.launch { appPagerState.animateScrollToPage(3) } },
                            )
                        }
                    },
                    floatingActionButton = {
                        if (appPagerState.currentPage < 3) {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                FloatingActionButton(
                                    onClick = { formViewModel.onEvent(FormEvent.ClearForm) },
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "New Form")
                                }
                                FloatingActionButton(
                                    onClick = { formViewModel.onEvent(FormEvent.SaveOrUpdateForm(context)) },
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                ) {
                                    Icon(Icons.Default.Save, contentDescription = "Save Form")
                                }
                            }
                        }
                    },
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

                            when (page) {
                                0 ->
                                    Column(modifier = formPageModifier) {
                                        PickupScreen(
                                            state = pickupState,
                                            onEvent = formViewModel::onEvent,
                                            runNumber = pickupState.run,
                                        ) { newRun ->
                                            formViewModel.onEvent(FormEvent.UpdateField(FormSection.PICKUP, FormFieldType.RUN, newRun))
                                            formViewModel.onEvent(FormEvent.UpdateField(FormSection.DROPOFF, FormFieldType.RUN, newRun))
                                            formViewModel.onEvent(FormEvent.UpdateField(FormSection.STATIONS, FormFieldType.RUN, newRun))
                                        }
                                    }
                                1 ->
                                    Column(modifier = formPageModifier) {
                                        DropoffScreen(
                                            state = dropoffState,
                                            onEvent = formViewModel::onEvent,
                                            runNumber = dropoffState.run,
                                        ) { newRun ->
                                            formViewModel.onEvent(FormEvent.UpdateField(FormSection.PICKUP, FormFieldType.RUN, newRun))
                                            formViewModel.onEvent(FormEvent.UpdateField(FormSection.DROPOFF, FormFieldType.RUN, newRun))
                                            formViewModel.onEvent(FormEvent.UpdateField(FormSection.STATIONS, FormFieldType.RUN, newRun))
                                        }
                                    }
                                2 ->
                                    Column(modifier = formPageModifier) {
                                        val stationsItemSections by formViewModel.stationsItemSections.collectAsState()
                                        StationsScreen(
                                            stationsState = stationsState,
                                            stationsItemSections = stationsItemSections,
                                            onEvent = formViewModel::onEvent,
                                            onUpdateItemSection = { index, section ->
                                                formViewModel.onEvent(FormEvent.UpdateStationsItemSection(index, section))
                                            },
                                            onAddItemSection = {
                                                formViewModel.onEvent(FormEvent.AddStationsItemSection)
                                            },
                                        ) { indices ->
                                            formViewModel.onEvent(FormEvent.RemoveStationsItemSections(indices))
                                        }
                                    }

                                3 ->
                                    Box(modifier = Modifier.padding(paddingValues)) {
                                        SavedFormsScreen(
                                            formViewModel = formViewModel,
                                        ) { selectedForm ->
                                            formViewModel.loadFormWithSections(selectedForm.formEntry.id)
                                            coroutineScope.launch {
                                                appPagerState.animateScrollToPage(0)
                                            }
                                        }
                                    }
                            }
                        }
                    }
                }
            }
        }
    }
}

