package com.example.jsonapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import android.R.string
import android.text.TextUtils.split
import android.view.View
import android.widget.*
import org.json.JSONArray


class MainActivity : AppCompatActivity() {

    var hashMap:HashMap<String, Double> = HashMap<String, Double>()

    var n = 0.0

    lateinit var dateLabel: TextView
    lateinit var EUROLabel: TextView
    lateinit var convertButton: Button
    lateinit var resultLabel: TextView

    lateinit var spinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dateLabel = findViewById(R.id.date)
        EUROLabel = findViewById(R.id.euro)
        convertButton = findViewById(R.id.convertButton)
        resultLabel = findViewById(R.id.result)
        spinner = findViewById(R.id.spinner)

        updateResult()

        convertButton.setOnClickListener{
            resultLabel.text = (EUROLabel.text.toString().toDouble() * n).toString()
        }
    }

    fun updateResult() {
        var result = ""
        var date = ""

        CoroutineScope(IO).launch {

            var data = async { fetchData() }.await()

            if(!data.isEmpty()) {

                var jsonOfAdvice = JSONObject(data)
                var list = jsonOfAdvice.getJSONObject("eur").toString().split(",").toMutableList()
                list[0] = list[0].replace("{", "")
                list[list.size-1] = list[list.size-1].replace("}", "")

                list.forEach {
                    var item = it
                    item = item.replace("\"", "")
                    var itemList = item.split(":").toMutableList()
                    hashMap.put(itemList[0], itemList[1].toDouble())
                }

                date = jsonOfAdvice.getString("date")

                withContext(Main){
                    resultLabel.text = result
                    dateLabel.text = date

                    setupSpinner()
                }
;
            }
        }
    }

    fun setupSpinner(){
        var options = hashMap.keys.toMutableList()
        spinner.adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, options)

//        hashMap.forEach {
//            Log.d("MAIN", "$it")
//        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, view: View?, position: Int, id: Long) {
                var key = hashMap.keys.elementAt(position)
                var value = hashMap.values.elementAt(position)

                n = value

            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                Log.d("MAIN", "onNothingSelected")
            }

        }
    }

    fun fetchData(): String{

        var response = ""
        try{
            response = URL("https://cdn.jsdelivr.net/gh/fawazahmed0/currency-api@1/latest/currencies/eur.json").readText()
        }catch(e: Exception){
            Log.d("MAIN", "ISSUE: $e")
        }
        // our response is saved as a string and returned
        return response
    }
}