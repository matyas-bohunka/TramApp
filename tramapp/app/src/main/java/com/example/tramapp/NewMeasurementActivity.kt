package com.example.tramapp

import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.tramapp.databinding.ActivityNewMeasurementBinding
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class NewMeasurementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewMeasurementBinding
    private lateinit var database: AppDatabase
    private lateinit var tramTypeDao: TramTypeDao
    private var selectedTramType: TramType? = null
    private var selectedDate: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewMeasurementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getDatabase(this, lifecycleScope) // Pass lifecycleScope
        tramTypeDao = database.tramTypeDao()

        // Populate the Tram Type Spinner
        lifecycleScope.launch {
            tramTypeDao.getAll().collectLatest { tramTypes ->
                val adapter = ArrayAdapter(
                    this@NewMeasurementActivity,
                    android.R.layout.simple_spinner_dropdown_item,
                    tramTypes.map { it.name }
                )
                binding.spinnerTramType.adapter = adapter

                // Set listener for spinner item selection
                binding.spinnerTramType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        selectedTramType = tramTypes[position]
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        selectedTramType = null
                    }
                }
            }
        }

        // Date Picker
        binding.buttonDatePicker.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, monthOfYear, dayOfMonth ->
                    selectedDate.set(year, monthOfYear, dayOfMonth)
                    updateSelectedDateTextView()
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }
        updateSelectedDateTextView() // Initial update

        binding.buttonCancelMeasurement.setOnClickListener {
            finish()
        }

        binding.buttonOkMeasurement.setOnClickListener {
            if (selectedTramType == null) {
                // An item must be selected if the adapter is not empty
                if ((binding.spinnerTramType.adapter?.count ?: 0) > 0) {
                    Snackbar.make(binding.root, "Kérjük, válasszon egy jármű típust!", Snackbar.LENGTH_SHORT).show()
                } else {
                    Snackbar.make(binding.root, "Nincs elérhető jármű típus. Előbb vegyen fel egyet!", Snackbar.LENGTH_SHORT).show()
                }
                return@setOnClickListener
            }

            val trackNumber = binding.editTextTrackNumber.text.toString()

            if (TextUtils.isEmpty(trackNumber)) {
                binding.editTextTrackNumber.error = "Kérjük, adja meg a pályaszámot!"
                return@setOnClickListener
            }

            lifecycleScope.launch(Dispatchers.IO) {
                val measurement = Measurement(
                    tramTypeId = selectedTramType!!.id,
                    trackNumber = trackNumber,
                    date = selectedDate.timeInMillis
                )
                val measurementDao = database.measurementDao()
                val measurementId = measurementDao.insert(measurement)

                withContext(Dispatchers.Main) {
                    val intent = Intent(this@NewMeasurementActivity, WheelMeasurementActivity::class.java).apply {
                        putExtra("TRAM_TYPE_ID", selectedTramType!!.id)
                        putExtra("MEASUREMENT_ID", measurementId)
                        putExtra("NUMBER_OF_AXLES", selectedTramType!!.numAxles)
                    }
                    startActivity(intent)
                }
            }
        }
    }

    private fun updateSelectedDateTextView() {
        val dateFormat = SimpleDateFormat("yyyy.MM.dd.", Locale.getDefault())
        binding.textViewSelectedDate.text = dateFormat.format(selectedDate.time)
    }
}