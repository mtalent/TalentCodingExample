package com.talent.sweetp.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.talent.sweetp.api.ApiService
import com.talent.sweetp.model.Joke
import com.talent.sweetp.model.Quote
import com.talent.sweetp.model.TriviaQuestion
import com.talent.sweetp.repository.Repository
import kotlinx.coroutines.launch

class SharedViewModel : ViewModel() {

    // Backing mutable state
    private val _sharedText = mutableStateOf("Initial State")
    private val _username = mutableStateOf("")
    private val _password = mutableStateOf("")

    private val _quote = mutableStateOf<Quote?>(null)
    private val _quoteList = mutableStateOf<List<Quote>>(emptyList())
    private val _selectedQuote = mutableStateOf<Quote?>(null)

    private val _user = mutableStateOf<FirebaseUser?>(null)
    private val _authError = mutableStateOf<String?>(null)

    private val _joke = mutableStateOf<Joke?>(null)

    private val _triviaQuestions = mutableStateOf<List<TriviaQuestion>>(emptyList())
    private val _currentQuestionIndex = mutableStateOf(0)
    private val _selectedAnswer = mutableStateOf<String?>(null)
    private val _isAnswerCorrect = mutableStateOf<Boolean?>(null)

    // Public immutable state
    val sharedText: State<String> = _sharedText
    val username: State<String> = _username
    val password: State<String> = _password

    val quote: State<Quote?> = _quote
    val quoteList: State<List<Quote>> = _quoteList
    val selectedQuote: State<Quote?> = _selectedQuote

    val user: State<FirebaseUser?> = _user
    val authError: State<String?> = _authError

    val joke: State<Joke?> = _joke

    val triviaQuestions: State<List<TriviaQuestion>> = _triviaQuestions
    val currentQuestionIndex: State<Int> = _currentQuestionIndex
    val selectedAnswer: State<String?> = _selectedAnswer
    val isAnswerCorrect: State<Boolean?> = _isAnswerCorrect

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val repository: Repository

    init {
        // Initialize repository with API services
        val quoteApi = ApiService.createQuoteApi()
        val jokeApi = ApiService.createJokeApi()
        val triviaApi = ApiService.createTriviaApi()
        repository = Repository(quoteApi, jokeApi, triviaApi)

        // Fetch initial data
        fetchQuotes(5)
        fetchTriviaQuestions()
    }

    // General purpose updater
    fun updateText(newText: String) {
        _sharedText.value = newText
    }

    // UI event handlers for user credentials
    fun onUsernameChanged(newUsername: String) {
        _username.value = newUsername
    }

    fun onPasswordChanged(newPassword: String) {
        _password.value = newPassword
    }

    // Joke fetching
    fun fetchRandomJoke() {
        viewModelScope.launch {
            val response = repository.getRandomJoke()
            if (response.isSuccessful) {
                _joke.value = response.body()
            }
        }
    }

    // Quote fetching
    fun fetchRandomQuote() {
        viewModelScope.launch {
            val response = repository.getRandomQuote()
            if (response.isSuccessful) {
                _quote.value = response.body()
            }
        }
    }

    fun fetchQuotes(page: Int) {
        viewModelScope.launch {
            val response = repository.getQuotesByPage(page)
            if (response.isSuccessful) {
                _quoteList.value = response.body()?.results ?: emptyList()
            }
        }
    }

    fun fetchQuoteById(id: String) {
        viewModelScope.launch {
            val response = repository.getQuoteById(id)
            if (response.isSuccessful) {
                _selectedQuote.value = response.body()
            }
        }
    }

    fun resetSelectedQuote() {
        _selectedQuote.value = null
    }

    // Authentication
    fun registerUser(onRegistrationSuccess: () -> Unit) {
        viewModelScope.launch {
            if (_username.value.isNotBlank() && _password.value.isNotBlank()) {
                auth.createUserWithEmailAndPassword(_username.value, _password.value)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            _user.value = auth.currentUser
                            onRegistrationSuccess()
                        } else {
                            _authError.value = task.exception?.message
                        }
                    }
            } else {
                _authError.value = "Please enter a valid username and password."
            }
        }
    }

    fun loginUser(onLoginSuccess: () -> Unit) {
        viewModelScope.launch {
            if (_username.value.isNotBlank() && _password.value.isNotBlank()) {
                auth.signInWithEmailAndPassword(_username.value, _password.value)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            _user.value = auth.currentUser
                            onLoginSuccess()
                        } else {
                            _authError.value = task.exception?.message
                        }
                    }
            } else {
                _authError.value = "Please enter a valid username and password."
            }
        }
    }

    fun signOut() {
        auth.signOut()
        _user.value = null
    }

    // Trivia
    fun fetchTriviaQuestions() {
        viewModelScope.launch {
            val response = repository.getTriviaQuestions()
            if (response.isSuccessful) {
                _triviaQuestions.value = response.body()?.results ?: emptyList()
                _currentQuestionIndex.value = 0
                _selectedAnswer.value = null
                _isAnswerCorrect.value = null
            }
        }
    }

    fun checkAnswer(selected: String) {
        // Update selected answer and correctness
        _selectedAnswer.value = selected
        val correct = _triviaQuestions.value.getOrNull(_currentQuestionIndex.value)?.correct_answer
        _isAnswerCorrect.value = (selected == correct)
    }

    fun nextQuestion() {
        if (_currentQuestionIndex.value < _triviaQuestions.value.lastIndex) {
            _currentQuestionIndex.value += 1
            _selectedAnswer.value = null
            _isAnswerCorrect.value = null
        }
    }
}
