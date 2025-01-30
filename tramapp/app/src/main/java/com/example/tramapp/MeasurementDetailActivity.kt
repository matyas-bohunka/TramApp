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
    private lateinit var database: AppDatabase // Declare database variable
    private lateinit var measurementDao: MeasurementDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMeasurementDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getDatabase(this, lifecycleScope) // Initialize database and pass lifecycleScope
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

        binding.buttonExportExcel.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                openFilePickerForExcel()
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), EXCEL_EXPORT_PERMISSION_CODE)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == EXCEL_EXPORT_PERMISSION_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openFilePickerForExcel()
        } else {
            Snackbar.make(binding.root, getString(R.string.excel_export_permission_required), Snackbar.LENGTH_SHORT).show()
        }
    }

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

        val groupedWheelMeasurements = measurementWithDetails.wheelMeasurements.groupBy { (it.wheelNumber - 1) / 2 + 1 } // Group by axle number

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
                    val dateFormatExcel = SimpleDateFormat("yyyy.MM.dd.", Locale.getDefault())

                    // Headers Row (First Row)
                    val headerRow = sheet.createRow(0)
                    headerRow.createCell(0).setCellValue("Kerék Száma")
                    headerRow.createCell(1).setCellValue("Tengely Száma")
                    headerRow.createCell(2).setCellValue("Mért érték")
                    headerRow.createCell(3).setCellValue("Segéd érték 15.69")
                    headerRow.createCell(4).setCellValue("Segéd érték 200")
                    headerRow.createCell(5).setCellValue("Eredmény")
                    headerRow.createCell(6).setCellValue("Pálya Szám: " +details.measurement.trackNumber)
                    headerRow.createCell(7).setCellValue("Dátum: " +dateFormatExcel.format(Date(details.measurement.date)))




                    details.wheelMeasurements.sortedBy { it.wheelNumber }.forEachIndexed { index, wheelMeasurement ->
                        val rowNum = index + 1 // Start writing data from row 2 onwards, for each wheel

                        val dataRow = sheet.createRow(rowNum)

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
                        val measuredValueCellRef = "C" + (rowNum + 1) // e.g., "C2", "C3", etc.
                        val fixedValue1CellRef = "D" + (rowNum + 1) // e.g., "D2", "D3", etc.
                        val fixedValue2CellRef = "E" + (rowNum + 1) // e.g., "E2", "E3", etc.

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
        private const val EXCEL_EXPORT_PERMISSION_CODE = 1001
        private const val CREATE_EXCEL_FILE_REQUEST_CODE = 1002
    }
}