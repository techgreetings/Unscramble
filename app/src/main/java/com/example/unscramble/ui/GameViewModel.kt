package com.example.unscramble.ui

import android.annotation.SuppressLint
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.unscramble.data.allWords
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.unscramble.data.MAX_NO_OF_WORDS
import com.example.unscramble.data.SCORE_INCREASE
import kotlinx.coroutines.flow.update


class GameViewModel: ViewModel() {
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState : StateFlow<GameUiState> = _uiState.asStateFlow()

    var userGuess by mutableStateOf("")
        private set
    init {
        resetGame()
    }

    fun resetGame(){
        usedWords.clear()
        _uiState.value=GameUiState(currentScrambledWord= pickRandomWordAndShuffle())
    }

    fun updateUserGuess(guessedWord:String){
        userGuess = guessedWord
    }

    fun skipWord() {
        updateGameState(_uiState.value.score)
        // Reset user guess
        updateUserGuess("")
    }


    private fun updateGameState(updatedScore:Int) {
        if (usedWords.size == MAX_NO_OF_WORDS) {
            //last round in the game,update isGameOver to true, donot pick a new word
            _uiState.update { currentState ->
                currentState.copy(
                    isGuessedWordWrong = false,
                    score = updatedScore,
                    isGameOver=true
                )
            }
        } else {
            //normal round in the game
            _uiState.update { currentState ->
                currentState.copy(
                    isGuessedWordWrong = false,
                    currentScrambledWord = pickRandomWordAndShuffle(),
                    score = updatedScore,
                    currentWordCount = currentState.currentWordCount.inc(),
                )
            }
        }
    }

    fun checkUserGuess(){
        if (userGuess.equals(currentWord, ignoreCase = true)) {
            // user's guess is correct,increase the score
            // and call updateGameState() to prepare the game for next round
            val updatedScore = _uiState.value.score.plus(SCORE_INCREASE)
            updateGameState(updatedScore)
        }else{
            //  users guess is wrong, show an error
            _uiState.update{ currentState ->
                currentState.copy(isGuessedWordWrong =true)
            }
        }
        //reset user guess
        updateUserGuess(" ")

    }

}
private lateinit var currentWord: String
//set of words used in the game
private var usedWords:MutableSet<String> = mutableSetOf()
@SuppressLint("SuspiciousIndentation")
private fun pickRandomWordAndShuffle():String{
    // Continue picking up a new a random word until you get one that has not been used before
  currentWord= allWords.random()
    if (usedWords.contains(currentWord)){
        return pickRandomWordAndShuffle()
    }else{
        usedWords.add(currentWord)
        return shuffleCurrentWord(currentWord)
    }
}

private fun shuffleCurrentWord(word:String):String{
    val tempWord = word.toCharArray()
//scramble the word
    tempWord.shuffle()
    while (String(tempWord).equals(word)){
        tempWord.shuffle()
    }
    return String(tempWord)
}

