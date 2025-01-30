package com.example.tramapp

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.children
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.tramapp.databinding.ActivityWheelMeasurementBinding

class WheelMeasurementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWheelMeasurementBinding
    private var tramTypeId: Long = -1
    private var measurementId: Long = -1
    private var numberOfAxles: Int = 0
    private lateinit var database: AppDatabase
    private lateinit var wheelMeasurementDao: WheelMeasurementDao
    private val wheelMeasurementsMap = mutableMapOf<Int, WheelMeasurement>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWheelMeasurementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getDatabase(this, lifecycleScope)
        wheelMeasurementDao = database.wheelMeasurementDao()

        tramTypeId = intent.getLongExtra("TRAM_TYPE_ID", -1)
        measurementId = intent.getLongExtra("MEASUREMENT_ID", -1)
        numberOfAxles = intent.getIntExtra("NUMBER_OF_AXLES", 0)

        if (tramTypeId == -1L || measurementId == -1L || numberOfAxles == 0) {
            // Handle error, should not happen
            finish()
            return
        }

        setupAxlesAndWheels()

        binding.buttonCancelWheelMeasurement.setOnClickListener {
            finish()
        }

        binding.buttonOkWheelMeasurement.setOnClickListener {
            // Check if all checkboxes are checked
            val allChecked = binding.containerAxles.children
                .filterIsInstance<LinearLayout>()
                .all { axleLayout ->
                    axleLayout.children
                        .filterIsInstance<LinearLayout>()
                        .all { wheelLayout ->
                            wheelLayout.children.filterIsInstance<CheckBox>().firstOrNull()?.isChecked == true
                        }
                }

            if (allChecked) {
                lifecycleScope.launch(Dispatchers.IO) {
                    withContext(Dispatchers.Main) {
                        val intent = Intent(this@WheelMeasurementActivity, MainActivity::class.java).apply {
                            putExtra("NEW_MEASUREMENT_SUCCESS", "Sikeres mérés")
                        }
                        startActivity(intent)
                        finish()
                    }
                }
            } else {
                Snackbar.make(binding.root, "Kérjük, végezzen mérést minden keréknél!", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupAxlesAndWheels() {
        for (i in 1..numberOfAxles) {
            val axleLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            val axleNumberTextView = TextView(this).apply {
                text = "Tengely $i"
                gravity = Gravity.CENTER_HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            axleLayout.addView(axleNumberTextView)

            val horizontalLine = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    5 // Height of the line
                ).apply {
                    setMargins(0, 8, 0, 8)
                }
                setBackgroundColor(android.graphics.Color.GRAY)
            }
            axleLayout.addView(horizontalLine)

            val wheelNumberLeft = (i - 1) * 2 + 1
            val wheelNumberRight = (i - 1) * 2 + 2

            // Left Wheel
            val leftWheelLayout = createWheelLayout(wheelNumberLeft)
            axleLayout.addView(leftWheelLayout)

            // Right Wheel
            val rightWheelLayout = createWheelLayout(wheelNumberRight)
            axleLayout.addView(rightWheelLayout)

            binding.containerAxles.addView(axleLayout)
        }
    }

    private fun createWheelLayout(wheelNumber: Int): LinearLayout {
        val wheelLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8, 0, 8)
            }
        }

        val wheelButton = Button(this).apply {
            text = "Kerék $wheelNumber"
            setOnClickListener { showMeasurementDialog(wheelNumber) }
        }
        wheelLayout.addView(wheelButton)

        val checkBox = CheckBox(this).apply {
            isEnabled = false
        }
        wheelLayout.addView(checkBox)

        val measurementTextView = TextView(this).apply {
            text = ""
        }
        wheelLayout.addView(measurementTextView)

        return wheelLayout
    }

    private fun showMeasurementDialog(wheelNumber: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Kerék $wheelNumber mérése")

        val inputLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val value1EditText = EditText(this).apply {
            hint = "1. érték"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        inputLayout.addView(value1EditText)

        val value2EditText = EditText(this).apply {
            hint = "2. érték"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        inputLayout.addView(value2EditText)

        val value3EditText = EditText(this).apply {
            hint = "3. érték"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        inputLayout.addView(value3EditText)

        builder.setView(inputLayout)

        builder.setPositiveButton("Ok") { dialog, _ ->
            val value1Str = value1EditText.text.toString()
            val value2Str = value2EditText.text.toString()
            val value3Str = value3EditText.text.toString()

            if (TextUtils.isEmpty(value1Str) || TextUtils.isEmpty(value2Str) || TextUtils.isEmpty(value3Str)) {
                Snackbar.make(binding.root, "Kérjük, töltse ki az összes mezőt!", Snackbar.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            val value1 = value1Str.replace(',', '.').toDoubleOrNull()
            val value2 = value2Str.replace(',', '.').toDoubleOrNull()
            val value3 = value3Str.replace(',', '.').toDoubleOrNull()

            if (value1 == null || value2 == null || value3 == null) {
                Snackbar.make(binding.root, "Érvénytelen szám formátum!", Snackbar.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            val average = (value1 + value2 + value3) / 3.0

            lifecycleScope.launch(Dispatchers.IO) {
                val wheelMeasurement = wheelMeasurementsMap[wheelNumber] ?: WheelMeasurement(measurementId = measurementId, wheelNumber = wheelNumber, value1 = null, value2 = null, value3 = null, average = null)
                val updatedWheelMeasurement = wheelMeasurement.copy(
                    value1 = value1Str,
                    value2 = value2Str,
                    value3 = value3Str,
                    average = String.format("%.2f", average).replace('.', ',')
                )

                if (wheelMeasurementsMap.containsKey(wheelNumber)) {
                    wheelMeasurementDao.update(updatedWheelMeasurement)
                } else {
                    wheelMeasurementDao.insert(updatedWheelMeasurement)
                }
                wheelMeasurementsMap[wheelNumber] = updatedWheelMeasurement

                withContext(Dispatchers.Main) {
                    updateWheelUI(wheelNumber, updatedWheelMeasurement.average ?: "", true)
                }
            }

            dialog.dismiss()
        }

        builder.setNegativeButton("Mégse") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun updateWheelUI(wheelNumber: Int, averageValue: String, isChecked: Boolean) {
        val axleLayouts = binding.containerAxles.children.filterIsInstance<LinearLayout>()
        for (axleLayout in axleLayouts) {
            val wheelLayouts = axleLayout.children.filterIsInstance<LinearLayout>()
            for (wheelLayout in wheelLayouts) {
                val button = wheelLayout.children.filterIsInstance<Button>().firstOrNull()
                if (button?.text == "Kerék $wheelNumber") {
                    wheelLayout.children.filterIsInstance<CheckBox>().firstOrNull()?.isChecked = isChecked
                    wheelLayout.children.filterIsInstance<TextView>().firstOrNull()?.text = averageValue
                    return
                }
            }
        }
    }
}