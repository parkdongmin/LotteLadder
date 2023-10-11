package com.example.lotteladder

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Random

class LadderView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val paint = Paint()
    private var participants = 0
    private var ladderHeight = 0
    private var ladder: MutableList<MutableList<Boolean>> = mutableListOf()
    private val rungSpacing = 150 // 각 막대 사이의 간격
    private val rungHeight = 50 // 막대의 높이

    private var drawX = 0
    private var drawY = 0
    private var result: MutableList<MutableList<String>> = mutableListOf()
    private var turn = "first"
    private var cnt = 0
    private var ing = false
    private var boomNumber = 0


    fun setParameters(participants: Int) {
        this.participants = participants
        this.ladderHeight = 5 //높이 5로 고정
        generateRandomLadder()
        invalidate() // View를 다시 그리도록 요청
    }

    fun preview(): Int {
        return boomNumber
    }

    private fun generateRandomLadder() {
        ladder.clear()
        val random = Random(System.currentTimeMillis())
        boomNumber = random.nextInt(participants)
        var i = 0
        while (i < ladderHeight) {
            val rung = mutableListOf<Boolean>()
            var lastValue = false
            for (j in 0 until (participants - 1)) {
                val nextValue = if (lastValue) {
                    // 바로 직전 값이 true이면 false로 설정
                    false
                } else {
                    // 랜덤 Boolean 값 생성
                    random.nextBoolean()
                }
                rung.add(nextValue)
                lastValue = nextValue
            }
            i++
            ladder.add(rung)
        }

        var verticalEmpty = true

        for (j in 0 until participants - 1) {
            for (i in 0 until ladder.size) {
                if (ladder[i][j]) {
                    verticalEmpty = false
                    break
                }
            }
            if (verticalEmpty) {
                generateRandomLadder()
            } else {
                verticalEmpty = true
            }
        }

        turn = "first"
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val totalWidth = (participants - 1) * rungSpacing
        val startX = (width - totalWidth) / 2 // 사다리 시작 위치

        var x = startX
        var y = 0

        if(turn == "first"){
            // 맨 처음 보여지는 사다리 그리기
            // 각 참여자 별로 세로 선 그림
            for (i in 0 until participants) {
                canvas.drawLine(x.toFloat(), 0f, x.toFloat(), height.toFloat(), paint)
                canvas.drawCircle(x.toFloat(), 9f,9F, paint)
                x += rungSpacing
            }

            // 가로 선 그리기
            for (i in 0 until ladderHeight) {
                x = startX
                y += rungHeight
                for (j in 0 until participants - 1) {
                    if (ladder[i][j]) {
                        // 사다리가 연결된 경우 그림
                        canvas.drawLine(x.toFloat(), y.toFloat(), (x + rungSpacing).toFloat(), y.toFloat(), paint)
                    }
                    x += rungSpacing
                }
            }
        }else{
            // onTouchEvent 이후 다시 사다리 그리기
            // 각 참여자 별로 세로 선 그림
            for (i in 0 until participants) {
                canvas.drawLine(x.toFloat(), 0f, x.toFloat(), height.toFloat(), paint)
                x += rungSpacing
            }

            // 가로 선 그리기
            for (i in 0 until ladderHeight) {
                x = startX
                y += rungHeight
                for (j in 0 until participants - 1) {
                    if (ladder[i][j]) {
                        // 사다리가 연결된 경우 그림
                        canvas.drawLine(x.toFloat(), y.toFloat(), (x + rungSpacing).toFloat(), y.toFloat(), paint)
                    }
                    x += rungSpacing
                }
            }

            // 결과로 받아온 좌표값 계산을 위해 분할 하기
            val resultString = result[0].toString().replace("[", "").replace("]","")
            val resultReplace = resultString.split(',')

            // Circle 찍힐 좌표값 계산
            var drawX : Float = startX + (resultReplace[cnt].trim().substring(2,3).toFloat() * rungSpacing)
            var drawY : Float = 55 + (resultReplace[cnt].trim().substring(0,1).toFloat() * rungHeight)
            Log.d("drawX","$drawX")
            Log.d("drawY","$drawY")
            canvas.drawCircle(
                drawX,
                drawY - 5,
                9F,
                paint
            )
        }

    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        if(ing)
            return false
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                ing = true
                Log.d("ing","$ing")
                val totalWidth = (participants - 1) * rungSpacing
                val touchX = event.x
                val startX = (width - totalWidth) / 2.5
                val selectedParticipant = ((touchX - startX) / rungSpacing).toInt()
                turn = "second"
                result.clear()

                if (selectedParticipant in 0 until participants) {
                    Log.d("Selected Participant", "참여자 $selectedParticipant 선택됨")
                    var x = 0
                    var t = selectedParticipant
                    val data = mutableListOf<String>()
                    while(x <= 4){
                        if (t != (participants - 1) && ladder[x][t]) {
                            drawX += rungSpacing
                            data.add("$x.$t")
                            t += 1
                        }else if(t != 0 && ladder[x][t-1]){
                            drawX -= rungSpacing
                            data.add("$x.$t")
                            t -= 1
                        }else{
                            drawY += rungHeight
                        }
                        data.add("$x.$t")
                        x ++
                    }
                    data.add("${x+1}.$t")
                    data.add("${x+2}.$t")
                    result.add(data)
                    Log.d("boomNumber","$boomNumber")
                    GlobalScope.launch(Dispatchers.Default) {
                        repeat(data.size){
                            invalidate()
                            delay(250L)
                            cnt += 1
                        }
                        withContext(Dispatchers.Main) {
                            if(t == boomNumber){
                                Toast.makeText(context,"선택 번호 : ${selectedParticipant+1} 당첨입니다",Toast.LENGTH_SHORT).show()
                            }else{
                                Toast.makeText(context,"선택 번호 : ${selectedParticipant+1} 꽝입니다",Toast.LENGTH_SHORT).show()
                            }
                        }
                        ing = false
                        cnt = 0
                    }
                }
            }
        }
        return true
    }
}