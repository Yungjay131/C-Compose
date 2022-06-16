package com.slyworks.cryptocompose.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.VerticalAlignmentLine
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.slyworks.cryptocompose.R
import com.slyworks.cryptocompose.ui.activities.main.SearchViewModel
import com.slyworks.models.CryptoModel
import com.slyworks.models.Outcome


/**
 *Created by Joshua Sylvanus, 1:29 AM, 13-Jun-22.
 */

@ExperimentalComposeUiApi
@ExperimentalUnitApi
@Composable
fun SearchMain(viewModel: SearchViewModel){
    val state:State<Outcome?> = viewModel.searchStateLiveData.observeAsState()
    val list:State<List<CryptoModel>?> = viewModel.searchDataListLiveData.observeAsState()
    val progressState:MutableState<Boolean> = remember{ mutableStateOf(true) }

    viewModel.search()


        Column {
            SearchViewComposable(
                onSearchTextChanged = {
                    progressState.value = true
                    viewModel.searchObservable.onNext(it)
                },
                onClearClick = {
                    progressState.value = true
                    viewModel.searchObservable.onNext(it)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            if(progressState.value){
                ProgressBar()
            }

            when{
               state.value!!.isSuccess ->{
                   progressState.value = false

                   LazyColumn {
                       itemsIndexed(items = list.value!!) { index, item ->
                           CardListItem(entity = item, viewModel)
                           Spacer(modifier = Modifier.height(4.dp))
                       }
                   }
               }
                state.value!!.isFailure ->{
                    progressState.value = false

                    NoResultFoundComposable()
                }
                state.value!!.isError -> progressState.value = true


            }

        }
}


@ExperimentalComposeUiApi
@Composable
fun SearchViewComposable(onSearchTextChanged:(String) -> Unit,
                         onClearClick:(String) -> Unit,
                         placeHolderText:String = "search for a cryptocurrency"){

    var showClearButton:Boolean by remember{ mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit){
        focusRequester.requestFocus()
    }

    var state:String by remember{ mutableStateOf("") }

    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .onFocusChanged { focusState ->
                showClearButton = focusState.isFocused
            }
            .focusRequester(focusRequester),
        value = state,
        onValueChange ={
          state = it

           onSearchTextChanged(it)
        },
        placeholder = {
            Text(text = placeHolderText)
        },
        colors = TextFieldDefaults.textFieldColors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            backgroundColor = Color.Transparent,
            cursorColor = LocalContentColor.current.copy(alpha = LocalContentAlpha.current)
        ),
        trailingIcon = {
            AnimatedVisibility(
                visible = showClearButton,
                enter = fadeIn(),
                exit = fadeOut()) {

                IconButton(onClick = { onClearClick(state) }) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "" )
                }
            }
        },
        maxLines = 1,
        singleLine = true,
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = {
            keyboardController?.hide()
            onSearchTextChanged(state)
        })
       )
}

/*TODO: add lottie json here ( find how - maybe a .gif)*/
@ExperimentalUnitApi
@Composable
fun NoResultFoundComposable(){
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {

        DisplayLottieAnim(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp))

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            fontSize = TextUnit(20F, TextUnitType.Sp),
            text = "No matches found")
    }
}

@Composable
fun DisplayLottieAnim(modifier: Modifier){
    val animationSpec by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(R.raw.not_found))
    LottieAnimation(
        modifier = modifier,
        composition = animationSpec)
}

@ExperimentalUnitApi
@Composable
fun NetworkUnAvailable(){
    Row {
       Text(
           modifier = Modifier.align(Alignment.CenterVertically),
           fontSize = TextUnit(20F, TextUnitType.Sp),
           text = "no network connectivity available" )
    }
}