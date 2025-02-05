package com.example.tramapp

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import com.example.tramapp.databinding.ActivityMeasurementDetailBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MeasurementDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMeasurementDetailBinding
    private var measurementId: Long = -1
    private lateinit var database: AppDatabase
    private lateinit var measurementDao: MeasurementDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMeasurementDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getDatabase(this, lifecycleScope)
        measurementDao = database.measurementDao()

        measurementId = intent.getLongExtra("MEASUREMENT_ID", -1)

        if (measurementId == -1L) {
            finish()
            return
        }

        loadMeasurementDetails()

        binding.buttonCancelMeasurementDetail.setOnClickListener {
            finish()
        }

        binding.buttonExportExcel.setOnClickListener { // Modified OnClickListener
            openFilePickerForExcel() // Directly open file picker - no permission check needed
        }
    }

    // Removed onRequestPermissionsResult function

    private fun loadMeasurementDetails() {
        lifecycleScope.launch(Dispatchers.IO) {
            val measurementWithDetails = measurementDao.getMeasurementWithWheelMeasurements(measurementId)
            withContext(Dispatchers.Main) {
                measurementWithDetails?.let { details ->
                    displayMeasurementDetails(details)
                } ?: run {
                    Snackbar.make(binding.root, getString(R.string.measurement_details_load_failed), Snackbar.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun displayMeasurementDetails(measurementWithDetails: MeasurementWithWheelMeasurements) {
        binding.containerMeasurementDetails.removeAllViews()

        val dateFormat = SimpleDateFormat("yyyy.MM.dd.", Locale.getDefault())
        val formattedDate = dateFormat.format(measurementWithDetails.measurement.date)

        val headerTextView = TextView(this).apply {
            text = "${measurementWithDetails.measurement.trackNumber} - $formattedDate"
            textSize = 18f
            gravity = Gravity.CENTER_HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 16)
            }
        }
        binding.containerMeasurementDetails.addView(headerTextView)

        val groupedWheelMeasurements = measurementWithDetails.wheelMeasurements.groupBy { (it.wheelNumber - 1) / 2 + 1 }

        for (axleNumber in 1..(groupedWheelMeasurements.keys.maxOrNull() ?: 0)) {
            val axleLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            val leftWheelMeasurement = groupedWheelMeasurements[axleNumber]?.find { it.wheelNumber % 2 != 0 }
            val rightWheelMeasurement = groupedWheelMeasurements[axleNumber]?.find { it.wheelNumber % 2 == 0 }

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

            val leftWheelInfo = TextView(this).apply {
                text = getString(R.string.wheel, leftWheelMeasurement?.wheelNumber ?: 0) + " : " + (leftWheelMeasurement?.average ?: getString(R.string.no_measurement))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            wheelLayout.addView(leftWheelInfo)

            val axleNumberTextView = TextView(this).apply {
                text = getString(R.string.axle, axleNumber)
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                    setMargins(16, 0, 16, 0)
                }
            }
            wheelLayout.addView(axleNumberTextView)

            val rightWheelInfo = TextView(this).apply {
                text = (rightWheelMeasurement?.average ?: getString(R.string.no_measurement)) + " : " + getString(R.string.wheel, rightWheelMeasurement?.wheelNumber ?: 0)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                gravity = Gravity.END
            }
            wheelLayout.addView(rightWheelInfo)

            axleLayout.addView(wheelLayout)

            val horizontalLine = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    5
                ).apply {
                    setMargins(0, 8, 0, 8)
                }
                setBackgroundColor(android.graphics.Color.GRAY)
            }
            axleLayout.addView(horizontalLine)

            binding.containerMeasurementDetails.addView(axleLayout)
        }
    }

    private fun openFilePickerForExcel() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            putExtra(Intent.EXTRA_TITLE, "TramMeasurement_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis())}.xlsx")
        }
        startActivityForResult(intent, CREATE_EXCEL_FILE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CREATE_EXCEL_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                exportToExcel(uri)
            }
        }
    }

    private fun exportToExcel(uri: Uri) {
        lifecycleScope.launch(Dispatchers.IO) {
            val measurementWithDetails = measurementDao.getMeasurementWithWheelMeasurements(measurementId)
            measurementWithDetails?.let { details ->
                try {
                    val workbook = XSSFWorkbook()
                    val sheet = workbook.createSheet("Mérés")

                    var rowNum = 0

                    // Header Row 1: Train Track Number
                    val trainNumberHeaderRow = sheet.createRow(rowNum++)
                    trainNumberHeaderRow.createCell(0).setCellValue("Tram Track Number:")
                    trainNumberHeaderRow.createCell(1).setCellValue(details.measurement.trackNumber)

                    // Header Row 2: Column Headers
                    val columnHeadersRow = sheet.createRow(rowNum++)
                    columnHeadersRow.createCell(0).setCellValue("Wheel Number")
                    columnHeadersRow.createCell(1).setCellValue("Axis Number")
                    columnHeadersRow.createCell(2).setCellValue("Measured Value")
                    columnHeadersRow.createCell(3).setCellValue("Fixed Value 15.69")
                    columnHeadersRow.createCell(4).setCellValue("Fixed Value 200")
                    columnHeadersRow.createCell(5).setCellValue("Formula Result")


                    details.wheelMeasurements.sortedBy { it.wheelNumber }.forEachIndexed { index, wheelMeasurement ->
                        val dataRow = sheet.createRow(rowNum++)

                        // Wheel Number (Column A)
                        dataRow.createCell(0).setCellValue(wheelMeasurement.wheelNumber.toDouble())

                        // Axis Number (Column B)
                        dataRow.createCell(1).setCellValue(((wheelMeasurement.wheelNumber - 1) / 2 + 1).toDouble())

                        // Measured Value (Column C)
                        val averageValue = wheelMeasurement.average?.replace(",", ".")?.toDoubleOrNull() ?: 0.0
                        dataRow.createCell(2).setCellValue(averageValue)

                        // Fixed Value 15.69 (Column D)
                        dataRow.createCell(3).setCellValue(15.69)

                        // Fixed Value 200 (Column E)
                        dataRow.createCell(4).setCellValue(200.0)

                        // Formula (Column F)
                        val cell = dataRow.createCell(5)
                        val measuredValueCellRef = "C" + (dataRow.rowNum)
                        val fixedValue1CellRef = "D" + (dataRow.rowNum)
                        val fixedValue2CellRef = "E" + (dataRow.rowNum)

                        cell.cellFormula = "(((${fixedValue1CellRef}+${measuredValueCellRef})^2)+(${fixedValue2CellRef}^2)/4)/(${fixedValue1CellRef}+${measuredValueCellRef})"
                    }


                    // Auto-size columns (disable for now to avoid java.awt error)
                    // for (i in 0..5) {
                    //     sheet.autoSizeColumn(i)
                    // }

                    try {
                        contentResolver.openOutputStream(uri)?.use { outputStream ->
                            workbook.write(outputStream)
                        }
                        workbook.close()
                        withContext(Dispatchers.Main) {
                            Snackbar.make(binding.root, getString(R.string.excel_export_successful), Snackbar.LENGTH_LONG).show()
                        }
                    } catch (e: IOException) {
                        Log.e("ExcelExport", "Error writing Excel file", e)
                        withContext(Dispatchers.Main) {
                            Snackbar.make(binding.root, getString(R.string.excel_export_failed, e.localizedMessage), Snackbar.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ExcelExport", "Error creating Excel file", e)
                    withContext(Dispatchers.Main) {
                        Snackbar.make(binding.root, getString(R.string.excel_export_failed, e.localizedMessage), Snackbar.LENGTH_LONG).show()
                    }
                }
            } ?: withContext(Dispatchers.Main) {
                Snackbar.make(binding.root, getString(R.string.measurement_data_not_found), Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val CREATE_EXCEL_FILE_REQUEST_CODE = 1002
    }
}