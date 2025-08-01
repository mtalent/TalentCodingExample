package com.talent.sweetp.views

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.talent.sweetp.viewmodel.SharedViewModel

@Composable
fun ScreenFour(viewModel: SharedViewModel) {
    // Pull state from ViewModel
    val question = viewModel.triviaQuestions.value
        .getOrNull(viewModel.currentQuestionIndex.value)
    val selectedAnswer = viewModel.selectedAnswer.value
    val isAnswerCorrect = viewModel.isAnswerCorrect.value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        question?.let { q ->
            // Question text
            Text(
                text = q.question,
                fontSize = 20.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Shuffle answers once per question
            val shuffledAnswers = remember(q) {
                (q.incorrect_answers + q.correct_answer).shuffled()
            }

            // Answer buttons
            shuffledAnswers.forEach { answer ->
                Button(
                    onClick = { viewModel.checkAnswer(answer) },
                    enabled = selectedAnswer == null || isAnswerCorrect == false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(text = answer)
                }
            }

            // Feedback & Next button
            if (selectedAnswer != null) {
                // Feedback text
                Text(
                    text = if (isAnswerCorrect == true)
                        "Correct! Proceed to the next question."
                    else
                        "Incorrect! Please try again.",
                    fontSize = 18.sp,
                    color = if (isAnswerCorrect == true) Color.Green else Color.Red,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                // Only show “Next” once they answer correctly
                if (isAnswerCorrect == true) {
                    Button(
                        onClick = { viewModel.nextQuestion() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text(text = "Next Question")
                    }
                }
            }
        }
    }
}
